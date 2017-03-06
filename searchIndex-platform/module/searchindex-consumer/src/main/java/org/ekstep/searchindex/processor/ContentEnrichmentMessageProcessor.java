package org.ekstep.searchindex.processor;

import java.util.ArrayList;
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
import org.ekstep.learning.util.ControllerUtil;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.enums.CollectionTypes;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

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
public class ContentEnrichmentMessageProcessor implements IMessageProcessor {

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
				LOGGER.info("checking if kafka message is null" + message);
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
		if(null != node){
			LOGGER.info("calling processData to process out relations" + node);
			processData(node);
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

		LOGGER.info("calling getItemsMap method to get items from item sets");
		List<String> items = getItemsMap(node, graphId, contentId);

		if(null != items && !items.isEmpty()){
			LOGGER.info("calling getConceptsFromItems method to get concepts from items" + items);
			getConceptsFromItems(graphId, contentId, items, node, conceptIds, conceptGrades);
		}

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
			e.printStackTrace();
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

		LOGGER.info("getting all items Data from itemIds" + items);

		Response response = util.getDataNodes(graphId, items);
		LOGGER.info("response from getDataNodes" + response);

		Set<String> conceptIds = new HashSet<String>();
		Set<String> itemGrades = new HashSet<String>();

		if (null != response) {

			List<Node> item_nodes = (List<Node>) response.get(GraphDACParams.node_list.name());

			LOGGER.info("List of nodes retrieved from response" + item_nodes.size());
			if (null != item_nodes && !item_nodes.isEmpty()) {

				LOGGER.info("Iterating through item_nodes");
				for (Node node : item_nodes) {

					LOGGER.info("Checking if item node contains gradeLevel");
					if (null != node.getMetadata().get("gradeLevel")) {
						String[] grade_array = (String[]) node.getMetadata().get("gradeLevel");
						for(String grade : grade_array){
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
							conceptIds.addAll(list);
					}
				}
			}
		}
		LOGGER.info("Adding concepts from content node to concepts extracted from items");
		List<String> totalConceptIds = new ArrayList<String>();

		LOGGER.info("adding conceptId from content node to list");
		if (null != existingConceptIds && !existingConceptIds.isEmpty()) {
			totalConceptIds.addAll(existingConceptIds);
		}

		LOGGER.info("Adding conceptIds from items to list");
		if (null != conceptIds && !conceptIds.isEmpty()) {
			for (String concept : conceptIds) {
				if (!totalConceptIds.contains(concept)) {
					totalConceptIds.add(concept);
				}
			}
		}
		LOGGER.info("calling process grades method to fetch and update grades");
		Node node = processGrades(content, itemGrades, existingConceptGrades);

		LOGGER.info("calling processAgeGroup method to process ageGroups from gradeLevels");
		Node content_node = processAgeGroup(node);

		LOGGER.info("updating node with extracted features" + content_node);
		node.setOutRelations(null);
		node.setInRelations(null);
		util.updateNode(content_node);

		LOGGER.info("result node after adding required metadata" + node);
		util.addOutRelations(graphId, contentId, totalConceptIds, RelationTypes.ASSOCIATED_TO.relationName());
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
					content_node = setGradeLevels(existingConceptGrades, node);
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
	private Node setGradeLevels(Set<String> grades, Node node) {

		LOGGER.info("checking if node contains gradeLevel");
		if (null == node.getMetadata().get("gradeLevel")) {
			node.getMetadata().put("gradeLevel", grades);

		} else {
			LOGGER.info("fetching grade levels from node");
			String[] grade_array = (String[]) node.getMetadata().get("gradeLevel");

			LOGGER.info("checking if grade levels obtained are empty ");
			if (null != grade_array) {

				LOGGER.info("adding grades which doesnt exist in node" + grades);
				for (String grade : grade_array) {
					
					LOGGER.info("checking if grade already exists" + grade);
					if (!grades.contains(grade)) {
						grades.add(grade);
						node.getMetadata().put("gradeLevel", grades);
						LOGGER.info("updating node metadata with additional grades" + node);
					}
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
	private Node processAgeGroup(Node node) {
		Node data = null;
		List<String> ageList = new ArrayList<String>();

		if (null != node.getMetadata().get("gradeLevel")) {
			String[] grades = (String[]) node.getMetadata().get("gradeLevel");

			if (null != grades) {

				for (String grade : grades) {
					LOGGER.info("mapping age group based on grades");
					if ("Kindergarten".equalsIgnoreCase(grade)) {
						ageList.add("<5");
					} else if ("Grade 1".equalsIgnoreCase(grade)) {
						ageList.add("5-6");
					} else if ("Grade 2".equalsIgnoreCase(grade)) {
						ageList.add("6-7");
					} else if ("Grade 3".equalsIgnoreCase(grade)) {
						ageList.add("7-8");
					} else if ("Grade 4".equalsIgnoreCase(grade)) {
						ageList.add("8-10");
					} else if ("Grade 5".equalsIgnoreCase(grade)) {
						ageList.add(">10");
					} else if ("Other".equalsIgnoreCase(grade)) {
						ageList.add("Other");
					}
				}
				data = setAgeGroup(node, ageList);
			}
		}

		return data;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node setAgeGroup(Node node, List<String> ageList) {

		if (null == node.getMetadata().get("ageGroup")) {
			if (!ageList.isEmpty()) {
				node.getMetadata().put("ageGroup", ageList);
			}
		} else {
			List ageGroup = (List) node.getMetadata().get("ageGroup");
			if (null != ageList) {
				for (String age : ageList) {
					if (!ageGroup.contains(age)) {
						ageGroup.add(age);
						node.getMetadata().put("ageGroup", ageGroup);
					}
				}
			}
		}
		return node;
	}
	/**
	 * This method holds logic to filter the kafka message and return the nodeId
	 * 
	 * @param message
	 *            The kafka message
	 * 
	 * @return The content node
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Node filterMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		Node node = null;

		LOGGER.info("checking if kafka message contains edata");
		if (null != message.get("edata")) {
			LOGGER.debug("checking if kafka message contains edata" + edata);
			edata = (Map) message.get("edata");

			LOGGER.info("checking if edata contains eks");
			if (null != edata.get("eks")) {
				LOGGER.debug("checking if edata contains eks" + eks);
				eks = (Map) edata.get("eks");

				LOGGER.info("checking if the content is a live content");
				if (null != eks.get("state") && StringUtils.equalsIgnoreCase("Live", eks.get("state").toString())) {

					LOGGER.info("checking if eks contains cid/nodeId");
					if (null != eks.get("cid")) {

						LOGGER.debug("checking if eks contains cid" + eks);
						node = util.getNode("domain", eks.get("cid").toString());
						LOGGER.info("node data fetched from cid" + node);
						return node;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * This method holds logic to process outRelations and fetch concept nodes
	 * grades from outRelations
	 * 
	 * @param outRelations
	 *            outRelationsMap
	 * @return result map
	 */
	protected Map<String, Object> getOutRelationsMap(List<Relation> outRelations) {

		Set<String> nodeIds = new HashSet<String>();
		List<String> conceptGrades = new ArrayList<String>();
		Map<String, Object> result_map = new HashMap<String, Object>();

		LOGGER.info("outRelations fetched from each item" + outRelations);
		if (null != outRelations && !outRelations.isEmpty()) {

			LOGGER.info("Iterating through outrelations");
			for (Relation rel : outRelations) {

				LOGGER.info("checking if endNodeType is objectType");
				if (null != rel.getEndNodeObjectType()
						&& StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {

					String status = null;
					LOGGER.info("checking relation contains status");
					if (null != rel.getEndNodeMetadata().get("status")) {
						LOGGER.info("getting status from node");
						status = (String) rel.getEndNodeMetadata().get(ContentWorkflowPipelineParams.status.name());
					}

					LOGGER.info("checking if status is LIVE and fetching nodeIds from it" + status);
					if (StringUtils.isNotBlank(status)
							&& StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status)) {
						LOGGER.info("nodeIds fetched form LIVE nodes" + nodeIds);
						nodeIds.add(rel.getEndNodeId());
					}

					LOGGER.info("checking if concept contains gradeLevel");
					if (null != rel.getEndNodeMetadata().get("gradeLevel")) {
						String[] grade_array =  (String[])(rel.getEndNodeMetadata().get("gradeLevel"));
						for(String garde : grade_array){
							conceptGrades.add(garde);
						}
					}
				}
			}

			LOGGER.info("Adding nodeIds to map" + nodeIds);
			if (null != nodeIds && !nodeIds.isEmpty()) {
				result_map.put("conceptIds", nodeIds);
			}

			LOGGER.info("Adding conceptGrades to map" + conceptGrades);
			if (null != conceptGrades && !conceptGrades.isEmpty()) {
				result_map.put("conceptGrades", conceptGrades);
			}
		}
		LOGGER.info("Map of conceptGrades and nodeIds" + result_map);
		return result_map;
	}
}
