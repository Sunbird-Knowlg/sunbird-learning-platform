package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.learning.util.ControllerUtil;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.enums.CollectionTypes;

/**
 * The Class ContentEnrichmentMessageProcessor is a kafka consumer which
 * provides implementations of the core Content feature extraction operations
 * defined in the IMessageProcessor along with the methods to implement content
 * enrichment with additional metadata
 * 
 * @author Rashmi
 * 
 * @see IMessageProcessor
 */
public class ContentEnrichmentMessageProcessor extends BaseProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentEnrichmentMessageProcessor.class.getName());

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The Controller Utility */
	private ControllerUtil util = new ControllerUtil();

	/** The constructor */
	public ContentEnrichmentMessageProcessor() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(String messageData) {
		try {
			LOGGER.info("Reading from kafka consumer" + messageData);
			Map<String, Object> message = new HashMap<String, Object>();

			if (StringUtils.isNotBlank(messageData)) {
				LOGGER.debug("checking if kafka message is blank or not" + messageData);
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}

			if (null != message) {
				String eid = (String) message.get("eid");
				if (StringUtils.isNotBlank(eid) && StringUtils.equals("BE_CONTENT_LIFECYCLE", eid))
					processMessage(message);
			}
		} catch (Exception e) {
			LOGGER.error("Error while processing kafka message", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {

		LOGGER.info("filtering out the kafka message" + message);
		Node node = filterMessage(message);

		if (null != node) {
			if (node.getMetadata().get(ContentWorkflowPipelineParams.contentType.name())
					.equals(ContentWorkflowPipelineParams.Collection.name())) {
				LOGGER.info("Processing Collection :" + node.getIdentifier());
				processCollection(node);
			} else {
				LOGGER.info("calling processData to fetch node metadata" + node);
				processData(node);
			}
		}
	}

	/**
	 * This method holds logic to fetch conceptIds and conceptGrades from the
	 * out relations
	 * 
	 * @param node
	 *            The content node
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processData(Node node) {

		Set<String> conceptIds = new HashSet<String>();
		Set<String> conceptGrades = new HashSet<String>();
		Map<String, Object> result = new HashMap<String, Object>();

		LOGGER.info("getting graphId and contentId from node");
		String graphId = node.getGraphId();
		String contentId = node.getIdentifier();

		LOGGER.info("checking if node contains outRelations");
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			List<Relation> outRelations = node.getOutRelations();
			result = getOutRelationsMap(outRelations);
		}

		LOGGER.info("fetching conceptIds from result" + result.containsKey("conceptIds"));
		if (null != result.get("conceptIds")) {
			List list = (List) result.get("conceptIds");
			if (null != list && !list.isEmpty())
				conceptIds.addAll(list);
		}

		LOGGER.info("fetching conceptGrades from result" + result.containsKey("conceptGrades"));
		if (null != result.get("conceptGrades")) {
			List list = (List) result.get("conceptGrades");
			if (null != list && !list.isEmpty())
				conceptGrades.addAll(list);
		}

		String language = null;
		if (null != node.getMetadata().get("language")) {
			String[] languageArr = (String[]) node.getMetadata().get("language");
			if (null != languageArr && languageArr.length > 0)
				language = languageArr[0];
		}
		String medium = (String) node.getMetadata().get("medium");
		// setting language as medium if medium is not already set
		if (StringUtils.isBlank(medium) && StringUtils.isNotBlank(language))
			node.getMetadata().put("medium", language);

		String subject = (String) node.getMetadata().get("subject");
		if (StringUtils.isBlank(subject)) {
			// if subject is not set for the content, set the subject using the
			// associated domain
			String domain = (String) result.get("domain");
			if (StringUtils.isNotBlank(domain)) {
				if (StringUtils.equalsIgnoreCase("numeracy", domain))
					subject = "MATHS";
				else if (StringUtils.equalsIgnoreCase("science", domain))
					subject = "Science";
				else if (StringUtils.equalsIgnoreCase("literacy", domain))
					subject = language;
				node.getMetadata().put("subject", subject);
			}
		}

		LOGGER.info("calling getItemsMap method to get items from item sets");
		List<String> items = getItemsMap(node, graphId, contentId);

		LOGGER.info("null and empty check for items" + items.isEmpty());
		if (null != items && !items.isEmpty()) {
			LOGGER.info("calling getConceptsFromItems method to get concepts from items" + items);
			getConceptsFromItems(graphId, contentId, items, node, conceptIds, conceptGrades);

		} else if (null != conceptGrades && !conceptGrades.isEmpty()) {

			LOGGER.info("calling process grades method to fetch and update grades");
			Node content_node = processGrades(node, null, conceptGrades);

			LOGGER.info("calling processAgeGroup method to process ageGroups from gradeLevels");
			Node contentNode = processAgeGroup(content_node);

			LOGGER.info("updating node with extracted features" + contentNode);
			util.updateNode(contentNode);
		}
	}

	@SuppressWarnings("unchecked")
	private void processCollection(Node node) {
		String graphId = node.getGraphId();
		String contentId = node.getIdentifier();
		try {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap = processChildren(node, graphId, dataMap);
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				if ("concepts".equalsIgnoreCase(entry.getKey()) || "keywords".equalsIgnoreCase(entry.getKey())) {
					continue;
				} else if ("subject".equalsIgnoreCase(entry.getKey())) {
					Set<Object> subject = (HashSet<Object>) entry.getValue();
					if (null != subject.iterator().next()) {
						node.getMetadata().put(entry.getKey(), subject.iterator().next());
					}
				} else if ("medium".equalsIgnoreCase(entry.getKey())) {
					Set<Object> medium = (HashSet<Object>) entry.getValue();
					if (null != medium.iterator().next()) {
						node.getMetadata().put(entry.getKey(), medium.iterator().next());
					}
				} else {
					Set<String> valueSet = (HashSet<String>) entry.getValue();
					String[] value = valueSet.toArray(new String[valueSet.size()]);
					node.getMetadata().put(entry.getKey(), value);
					LOGGER.info("Updating property" + entry.getKey() + ":" + value);
				}
			}
			Set<String> keywords = (HashSet<String>) dataMap.get("keywords");
			if (null != keywords && !keywords.isEmpty()) {
				if (null != node.getMetadata().get("keywords")) {
					Object object = node.getMetadata().get("keywords");
					if (object instanceof String[]) {
						String[] stringArray = (String[]) node.getMetadata().get("keywords");
						keywords.addAll(Arrays.asList(stringArray));
					} else if (object instanceof String) {
						String keyword = (String) node.getMetadata().get("keywords");
						keywords.add(keyword);
					}
				}
				List<String> keywordsList = new ArrayList<>();
				keywordsList.addAll(keywords);
				node.getMetadata().put("keywords", keywordsList);
			}
			util.updateNode(node);
			LOGGER.info("Keywords ->" + node.getMetadata().get("keywords"));
			List<String> concepts = new ArrayList<>();
			LOGGER.info("Concepts from Map:" + dataMap.get(ContentWorkflowPipelineParams.concepts.name()));
			concepts.addAll((Collection<? extends String>) dataMap.get(ContentWorkflowPipelineParams.concepts.name()));
			LOGGER.info("Updating concepts ->" + concepts);
			if (null != concepts && !concepts.isEmpty()) {
				util.addOutRelations(graphId, contentId, concepts, RelationTypes.ASSOCIATED_TO.relationName());
			}
			LOGGER.info("Updated Concepts ->" + concepts);
			LOGGER.info(node.getMetadata());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	private Map<String, Object> processChildren(Node node, String graphId, Map<String, Object> dataMap) {
		List<String> children;
		children = getChildren(node);
		for (String child : children) {
			Node childNode = util.getNode(graphId, child);
			dataMap = mergeMap(dataMap, processChild(childNode));
			processChildren(childNode, graphId, dataMap);
		}
		return dataMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mergeMap(Map<String, Object> dataMap, Map<String, Object> childDataMap) {
		if (dataMap.isEmpty()) {
			dataMap.putAll(childDataMap);
		} else {
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				Set<Object> value = new HashSet<Object>();
				value.addAll((Collection<? extends Object>) childDataMap.get(entry.getKey()));
				value.addAll((Collection<? extends Object>) entry.getValue());
				dataMap.replace(entry.getKey(), value);
			}
		}
		return dataMap;
	}

	private List<String> getChildren(Node node) {
		List<String> children = new ArrayList<>();
		for (Relation rel : node.getOutRelations()) {
			if (ContentWorkflowPipelineParams.Content.name().equalsIgnoreCase(rel.getEndNodeObjectType())) {
				children.add(rel.getEndNodeId());
			}
		}
		return children;
	}

	private Map<String, Object> processChild(Node node) {
		Map<String, Object> result = new HashMap<>();
		Set<Object> language = new HashSet<Object>();
		Set<Object> concepts = new HashSet<Object>();
		Set<Object> domain = new HashSet<Object>();
		Set<Object> grade = new HashSet<Object>();
		Set<Object> age = new HashSet<Object>();
		Set<Object> medium = new HashSet<Object>();
		Set<Object> subject = new HashSet<Object>();
		Set<Object> genre = new HashSet<Object>();
		Set<Object> theme = new HashSet<Object>();
		Set<Object> keywords = new HashSet<Object>();
		if (null != node.getMetadata().get("language")) {
			String[] langData = (String[]) node.getMetadata().get("language");
			language = new HashSet<Object>(Arrays.asList(langData));
			result.put("language", language);
		}
		if (null != node.getMetadata().get("domain")) {
			String[] domainData = (String[]) node.getMetadata().get("domain");
			domain = new HashSet<Object>(Arrays.asList(domainData));
			result.put("domain", domain);
		}
		if (null != node.getMetadata().get("gradeLevel")) {
			String[] gradeData = (String[]) node.getMetadata().get("gradeLevel");
			grade = new HashSet<Object>(Arrays.asList(gradeData));
			result.put("gradeLevel", grade);
		}
		if (null != node.getMetadata().get("ageGroup")) {
			String[] ageData = (String[]) node.getMetadata().get("ageGroup");
			age = new HashSet<Object>(Arrays.asList(ageData));
			result.put("ageGroup", age);
		}
		if (null != node.getMetadata().get("medium")) {
			String mediumData = (String) node.getMetadata().get("medium");
			medium = new HashSet<Object>(Arrays.asList(mediumData));
			result.put("medium", medium);
		}
		if (null != node.getMetadata().get("subject")) {
			String subjectData = (String) node.getMetadata().get("subject");
			subject = new HashSet<Object>(Arrays.asList(subjectData));
			result.put("subject", subject);
		}
		if (null != node.getMetadata().get("genre")) {
			String[] genreData = (String[]) node.getMetadata().get("genre");
			genre = new HashSet<Object>(Arrays.asList(genreData));
			result.put("genre", genre);
		}
		if (null != node.getMetadata().get("theme")) {
			String[] themeData = (String[]) node.getMetadata().get("theme");
			theme = new HashSet<Object>(Arrays.asList(themeData));
			result.put("theme", theme);
		}
		if (null != node.getMetadata().get("keywords")) {
			String[] keyData = (String[]) node.getMetadata().get("keywords");
			keywords = new HashSet<Object>(Arrays.asList(keyData));
			result.put("keywords", keywords);
		}
		for (Relation rel : node.getOutRelations()) {
			if (ContentWorkflowPipelineParams.concepts.name().equalsIgnoreCase(rel.getEndNodeObjectType())) {
				concepts.add(rel.getEndNodeId());
			}
		}
		if (null != concepts && !concepts.isEmpty()) {
			result.put("concepts", concepts);
		}
		return result;
	}

	/**
	 * This method gets the list of itemsets associated with content node and
	 * items which are members of item sets used in the content.
	 * 
	 * @param content
	 *            The Content node
	 * 
	 * @param existingConceptGrades
	 *            The conceptGrades from content node
	 * 
	 * @param existingConceptIds
	 *            The existingConceptIds from Content node
	 * 
	 */
	private List<String> getItemsMap(Node node, String graphId, String contentId) {

		Set<String> itemSets = new HashSet<String>();
		List<String> items = new ArrayList<String>();

		try {
			if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
				List<Relation> outRelations = node.getOutRelations();

				LOGGER.info("outRelations fetched from each item" + outRelations);
				if (null != outRelations && !outRelations.isEmpty()) {

					LOGGER.info("Iterating through relations");
					for (Relation rel : outRelations) {

						LOGGER.info("Get item sets associated with the content: " + contentId);
						if (StringUtils.equalsIgnoreCase("ItemSet", rel.getEndNodeObjectType())
								&& !itemSets.contains(rel.getEndNodeId()))
							itemSets.add(rel.getEndNodeId());
					}
				}
			}
			LOGGER.info("checking if itemSets are empty" + itemSets);
			if (null != itemSets && !itemSets.isEmpty()) {

				LOGGER.info("Number of item sets: " + itemSets.size());
				Set<String> itemIds = new HashSet<String>();

				LOGGER.info("Iterating through itemSet map" + itemSets);
				for (String itemSet : itemSets) {

					LOGGER.info("calling getItemSetMembers methods to get items from itemSets");
					List<String> members = getItemSetMembers(graphId, itemSet);

					LOGGER.info("getting item memebers from item set" + members);
					if (null != members && !members.isEmpty())
						itemIds.addAll(members);
				}
				LOGGER.info("Total number of items: " + itemIds.size());
				if (!itemIds.isEmpty()) {
					items = new ArrayList<String>(itemIds);
					LOGGER.info("getting items associated with itemsets" + items);

				}
			}
		} catch (Exception e) {
			LOGGER.info("exception occured while getting item and itemsets", e);
		}
		return items;
	}

	/**
	 * This methods holds logic to get members of givens item set, returns the
	 * list of identifiers of the items that are members of the given item set.
	 * 
	 * @param graphId
	 *            identifier of the domain graph
	 * 
	 * @param itemSetId
	 *            identifier of the item set
	 * 
	 * @return list of identifiers of member items
	 */
	@SuppressWarnings("unchecked")
	private List<String> getItemSetMembers(String graphId, String itemSetId) {

		List<String> members = new ArrayList<String>();
		LOGGER.info("Get members of items set: " + itemSetId);
		Response response = util.getCollectionMembers(graphId, itemSetId, CollectionTypes.SET.name());

		LOGGER.info("checking if response is null" + response);
		if (null != response) {
			LOGGER.info("getting members from response");
			members = (List<String>) response.get(GraphDACParams.members.name());
		}
		LOGGER.info("item members fetched from itemSets" + members);
		return members;
	}

	/**
	 * This method holds logic to map Concepts from the Items, get their
	 * gradeLevel and age Group and add it as a part of node metadata.
	 * 
	 * @param graphId
	 *            The identifier of the domain graph
	 * 
	 * @param items
	 *            The list of assessment item identifiers
	 * 
	 * @param content
	 *            The Content node
	 * 
	 * @param existingConceptIds
	 *            The existingConceptIds from content node
	 * 
	 * @param existingConceptGrades
	 *            grades from concepts associated with content node
	 * 
	 * @return updated node with all metadata
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getConceptsFromItems(String graphId, String contentId, List<String> items, Node content,
			Set<String> existingConceptIds, Set<String> existingConceptGrades) {
		Response response = null;
		Set<String> itemGrades = new HashSet<String>();

		LOGGER.info("checking if itemsList is empty" + items);
		if (null != items && !items.isEmpty()) {
			LOGGER.debug("getting all items Data from itemIds" + items);

			response = util.getDataNodes(graphId, items);
			LOGGER.info("response from getDataNodes" + response);
		}

		LOGGER.info("checking if response is null" + response);
		if (null != response) {

			List<Node> item_nodes = (List<Node>) response.get(GraphDACParams.node_list.name());

			LOGGER.info("List of nodes retrieved from response" + item_nodes.size());
			if (null != item_nodes && !item_nodes.isEmpty()) {

				LOGGER.info("Iterating through item_nodes");
				for (Node node : item_nodes) {

					LOGGER.info("Checking if item node contains gradeLevel");
					if (null != node.getMetadata().get("gradeLevel")) {
						String[] grade_array = (String[]) node.getMetadata().get("gradeLevel");
						for (String grade : grade_array) {
							LOGGER.info("adding item grades" + grade);
							itemGrades.add(grade);
						}
					}

					List<Relation> outRelations = node.getOutRelations();
					LOGGER.info("calling getOutRelationsMap" + outRelations);
					Map<String, Object> result = getOutRelationsMap(outRelations);

					LOGGER.info("fetching conceptIds from result" + result);
					if (null != result.get("conceptIds")) {
						List list = (List) result.get("conceptIds");
						if (null != list && !list.isEmpty())
							existingConceptIds.addAll(list);
					}
				}
			}
		}
		List<String> totalConceptIds = new ArrayList<String>();
		LOGGER.info("adding conceptId from content node to list");
		if (null != existingConceptIds && !existingConceptIds.isEmpty()) {
			totalConceptIds.addAll(existingConceptIds);
		}

		LOGGER.info("calling process grades method to fetch and update grades");
		Node node = processGrades(content, itemGrades, existingConceptGrades);

		LOGGER.info("calling processAgeGroup method to process ageGroups from gradeLevels");
		Node content_node = processAgeGroup(node);

		LOGGER.info("updating node with extracted features" + content_node);
		node.setOutRelations(null);
		node.setInRelations(null);
		util.updateNode(content_node);

		if (null != totalConceptIds && !totalConceptIds.isEmpty()) {
			LOGGER.info("result node after adding required metadata" + node);
			util.addOutRelations(graphId, contentId, totalConceptIds, RelationTypes.ASSOCIATED_TO.relationName());
		}
	}

	/**
	 * This method mainly holds logic to map the content node with concept
	 * metadata like gradeLevel and ageGroup
	 * 
	 * @param content
	 *            The content node
	 * 
	 * @param itemGrades
	 *            The itemGrades
	 * 
	 * @param conceptGrades
	 *            The conceptGrades
	 * 
	 * @param existingConceptGrades
	 *            The concept grades from content
	 * 
	 * @return updated node with required metadata
	 */
	private Node processGrades(Node node, Set<String> itemGrades, Set<String> existingConceptGrades) {
		Node content_node = null;
		try {

			LOGGER.info("checking if concept grades exist" + existingConceptGrades);
			if (null != existingConceptGrades && !existingConceptGrades.isEmpty()) {
				content_node = setGradeLevels(existingConceptGrades, node);
			} else {
				LOGGER.info("checking if item grades exist" + itemGrades);
				if (null != itemGrades && !itemGrades.isEmpty()) {
					content_node = setGradeLevels(itemGrades, node);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception occured while setting age group from grade level", e);
		}
		return content_node;
	}

	/**
	 * This method holds logic to getGrades levels either for itemGrades or
	 * conceptGrades and add it to node metadata
	 * 
	 * @param grades
	 *            The grades
	 * 
	 * @param node
	 *            The content node
	 * 
	 * @return The updated content node
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Node setGradeLevels(Set<String> grades, Node node) {

		LOGGER.info("checking if node contains gradeLevel");
		if (null == node.getMetadata().get("gradeLevel")) {
			List<String> gradeLevel = new ArrayList(grades);
			node.getMetadata().put("gradeLevel", gradeLevel);

		} else {
			LOGGER.info("fetching grade levels from node");
			String[] grade_array = (String[]) node.getMetadata().get("gradeLevel");

			LOGGER.info("checking if grade levels obtained are empty ");
			if (null != grade_array) {

				LOGGER.info("adding grades which doesnt exist in node" + grades);
				for (String grade : grade_array) {

					LOGGER.info("checking if grade already exists" + grade);
					grades.add(grade);
					List gradeLevel = new ArrayList(grades);
					node.getMetadata().put("gradeLevel", gradeLevel);
					LOGGER.info("updating node metadata with additional grades" + node);
				}
			}
		}
		return node;
	}

	/**
	 * This method holds logic to map ageGroup from gradeMap
	 * 
	 * @param grades
	 *            The gradeMap
	 * 
	 * @param existingAgeGroup
	 *            The age group from content
	 * 
	 * @return The ageMap mapped from gradeLevel
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node processAgeGroup(Node node) {
		Node data = null;
		Set<String> ageSet = new HashSet<String>();

		if (null != node.getMetadata().get("gradeLevel")) {
			LOGGER.info("fetching gradeLevel from node metadata" + node);
			List<String> grades = (List) node.getMetadata().get("gradeLevel");
			if (null != grades) {

				for (String grade : grades) {
					LOGGER.info("mapping age group based on grades");
					if ("Kindergarten".equalsIgnoreCase(grade)) {
						ageSet.add("<5");
					} else if ("Grade 1".equalsIgnoreCase(grade)) {
						ageSet.add("5-6");
					} else if ("Grade 2".equalsIgnoreCase(grade)) {
						ageSet.add("6-7");
					} else if ("Grade 3".equalsIgnoreCase(grade)) {
						ageSet.add("7-8");
					} else if ("Grade 4".equalsIgnoreCase(grade)) {
						ageSet.add("8-10");
					} else if ("Grade 5".equalsIgnoreCase(grade)) {
						ageSet.add(">10");
					} else if ("Other".equalsIgnoreCase(grade)) {
						ageSet.add("Other");
					}
				}
				LOGGER.info("Calling set ageGroup method to set ageGroups" + ageSet);
				data = setAgeGroup(node, ageSet);
			}
		}
		return data;
	}

	/**
	 * This method holds logic to set ageGroup based on the grades
	 * 
	 * @param node
	 *            The node
	 * 
	 * @param ageSet
	 *            The ageSet
	 * 
	 * @return The node
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Node setAgeGroup(Node node, Set<String> ageSet) {

		LOGGER.info("Checking if node contains ageGroup in it");
		if (null == node.getMetadata().get("ageGroup")) {

			LOGGER.info("adding ageSet to node if it doesnt have ageGroup in it");
			if (null != ageSet) {
				LOGGER.info("adding age metadata to node" + ageSet);
				List<String> ageGroup = new ArrayList(ageSet);
				node.getMetadata().put("ageGroup", ageGroup);
			}

		} else {

			LOGGER.info("fetching ageGroup from node");
			String[] age_array = (String[]) node.getMetadata().get("ageGroup");
			if (null != ageSet) {
				if (null != age_array) {
					for (String age : age_array) {
						ageSet.add(age);
					}
					LOGGER.info("adding age metadata to node" + ageSet);
					List<String> ageGroup = new ArrayList(ageSet);
					node.getMetadata().put("ageGroup", ageGroup);
				}
			}
		}
		return node;
	}
}