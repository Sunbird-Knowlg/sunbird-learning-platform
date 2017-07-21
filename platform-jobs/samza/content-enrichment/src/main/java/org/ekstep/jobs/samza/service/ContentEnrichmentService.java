package org.ekstep.jobs.samza.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.ContentEnrichmentParams;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.enums.CollectionTypes;

public class ContentEnrichmentService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(ContentEnrichmentService.class);

	private Config config = null;

	private static final int AWS_UPLOAD_RESULT_URL_INDEX = 1;

	private static final String s3Content = "s3.content.folder";

	private static final String s3Artifact = "s3.artifact.folder";

	private static ObjectMapper mapper = new ObjectMapper();

	private ControllerUtil util = new ControllerUtil();

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		S3PropertyReader.loadProperties(props);
		Configuration.loadProperties(props);
		PropertiesUtil.loadProperties(props);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Actors initialized");
		JedisFactory.initialize(props);
		LOGGER.info("Redis connection factory initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {

		Map<String, Object> eks = getStateChangeData(message);
		if (null == eks) {
			metrics.incSkippedCounter();
			return;
		}
		try {
			Node node = getNode(eks);
			if ((null != node) && (node.getObjectType().equalsIgnoreCase(ContentEnrichmentParams.content.name()))){
				if (node.getMetadata().get(ContentEnrichmentParams.contentType.name()).equals(ContentEnrichmentParams.Collection.name())) {
					processCollection(node);
				} else {
					// processData(node);
				}
				if (node.getMetadata().get(ContentEnrichmentParams.mimeType.name()).equals("application/vnd.ekstep.content-collection")) {
					processCollectionForTOC(node);
				}
			} else {
				metrics.incSkippedCounter();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to process message. Content enrichment failed", message, e);
			metrics.incFailedCounter();
		}
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> getStateChangeData(Map<String, Object> message) throws Exception {

		String eid = (String) message.get("eid");
		if (null == eid || !StringUtils.equalsIgnoreCase(eid, ContentEnrichmentParams.BE_OBJECT_LIFECYCLE.name())) {
			return null;
		}

		Map<String, Object> edata = (Map<String, Object>) message.get("edata");
		if (null == edata) {
			return null;
		}

		Map<String, Object> eks = (Map<String, Object>) edata.get("eks");
		if (null == eks) {
			return null;
		}

		if (StringUtils.equalsIgnoreCase("Live", (String) eks.get("state"))) {
			return eks;
		}

		return null;
	}

	private Node getNode(Map<String, Object> eks) throws Exception {
		if (null != eks.get("id")) {
			return util.getNode("domain", eks.get("id").toString());
		}
		return null;
	}

	/**
	 * This method holds logic to fetch conceptIds and conceptGrades from the out relations
	 * 
	 * @param node The content node
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private void processData(Node node) {

		Set<String> conceptIds = new HashSet<String>();
		Set<String> conceptGrades = new HashSet<String>();
		Map<String, Object> result = new HashMap<String, Object>();

		LOGGER.info("getting graphId and contentId from node");
		String graphId = node.getGraphId();
		String contentId = node.getIdentifier();

		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			List<Relation> outRelations = node.getOutRelations();
			result = getOutRelationsMap(outRelations);
		}

		if (null != result.get(ContentEnrichmentParams.conceptIds.name())) {
			List list = (List) result.get(ContentEnrichmentParams.conceptIds.name());
			if (null != list && !list.isEmpty())
				conceptIds.addAll(list);
		}

		if (null != result.get(ContentEnrichmentParams.conceptGrades.name())) {
			List list = (List) result.get(ContentEnrichmentParams.conceptGrades.name());
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

		List<String> items = getItemsMap(node, graphId, contentId);
		if (null != items && !items.isEmpty()) {
			getConceptsFromItems(graphId, contentId, items, node, conceptIds, conceptGrades);

		} else if (null != conceptGrades && !conceptGrades.isEmpty()) {
			Node content_node = processGrades(node, null, conceptGrades);
			Node contentNode = processAgeGroup(content_node);
			util.updateNode(contentNode);
		}
	}

	@SuppressWarnings("unchecked")
	private void processCollection(Node node) throws Exception {

		String graphId = node.getGraphId();
		String contentId = node.getIdentifier();
		LOGGER.info("Enrich collection node - " + contentId);
		Map<String, Object> dataMap = new HashMap<>();
		dataMap = processChildren(node, graphId, dataMap);
		LOGGER.info("Children nodes process for collection - " + contentId);
		if(null != dataMap){
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				if ("concepts".equalsIgnoreCase(entry.getKey()) || "keywords".equalsIgnoreCase(entry.getKey())) {
					continue;
				} else if ("subject".equalsIgnoreCase(entry.getKey())) {
					Set<Object> subject = (HashSet<Object>) entry.getValue();
					if (null != subject.iterator().next()) {
						node.getMetadata().put(entry.getKey(), (String) subject.iterator().next());
					}
				} else if ("medium".equalsIgnoreCase(entry.getKey())) {
					Set<Object> medium = (HashSet<Object>) entry.getValue();
					if (null != medium.iterator().next()) {
						node.getMetadata().put(entry.getKey(), (String) medium.iterator().next());
					}
				} else {
					Set<String> valueSet = (HashSet<String>) entry.getValue();
					String[] value = valueSet.toArray(new String[valueSet.size()]);
					node.getMetadata().put(entry.getKey(), value);
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
			List<String> concepts = new ArrayList<>();
			if(null != dataMap.get("concepts")){
				concepts.addAll((Collection<? extends String>) dataMap.get("concepts"));
				if (null != concepts && !concepts.isEmpty()) {
					util.addOutRelations(graphId, contentId, concepts, RelationTypes.ASSOCIATED_TO.relationName());
				}
			}
		}
	}

	private Map<String, Object> processChildren(Node node, String graphId, Map<String, Object> dataMap) throws Exception {
		List<String> children;
		children = getChildren(node);
		if(!children.isEmpty()){
			for (String child : children) {
				Node childNode = util.getNode(graphId, child);
				dataMap = mergeMap(dataMap, processChild(childNode));
				processChildren(childNode, graphId, dataMap);
			}
		}
		return dataMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mergeMap(Map<String, Object> dataMap, Map<String, Object> childDataMap) throws Exception {
		if (dataMap.isEmpty()) {
			dataMap.putAll(childDataMap);
		} else {
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				Set<Object> value = new HashSet<Object>();
				if (childDataMap.containsKey(entry.getKey())) {
					value.addAll((Collection<? extends Object>) childDataMap.get(entry.getKey()));
				}
				value.addAll((Collection<? extends Object>) entry.getValue());
				dataMap.replace(entry.getKey(), value);
			}
			if (!dataMap.keySet().containsAll(childDataMap.keySet())) {
				for (Map.Entry<String, Object> entry : childDataMap.entrySet()) {
					if (!dataMap.containsKey(entry.getKey())) {
						dataMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return dataMap;
	}

	private List<String> getChildren(Node node) throws Exception {
		List<String> children = new ArrayList<>();
		if(null != node.getOutRelations()){
			for (Relation rel : node.getOutRelations()) {
				if (ContentEnrichmentParams.Content.name().equalsIgnoreCase(rel.getEndNodeObjectType())) {
					children.add(rel.getEndNodeId());
				}
			}
		}
		return children;
	}

	private Map<String, Object> processChild(Node node) throws Exception {

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
		if (null != node.getMetadata().get(ContentEnrichmentParams.domain.name())) {
			String[] domainData = (String[]) node.getMetadata().get(ContentEnrichmentParams.domain.name());
			domain = new HashSet<Object>(Arrays.asList(domainData));
			result.put("domain", domain);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name())) {
			String[] gradeData = (String[]) node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name());
			grade = new HashSet<Object>(Arrays.asList(gradeData));
			result.put("gradeLevel", grade);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.ageGroup.name())) {
			String[] ageData = (String[]) node.getMetadata().get(ContentEnrichmentParams.ageGroup.name());
			age = new HashSet<Object>(Arrays.asList(ageData));
			result.put(ContentEnrichmentParams.ageGroup.name(), age);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.medium.name())) {
			String mediumData = (String) node.getMetadata().get(ContentEnrichmentParams.medium.name());
			medium = new HashSet<Object>(Arrays.asList(mediumData));
			result.put(ContentEnrichmentParams.medium.name(), medium);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.subject.name())) {
			String subjectData = (String) node.getMetadata().get(ContentEnrichmentParams.subject.name());
			subject = new HashSet<Object>(Arrays.asList(subjectData));
			result.put(ContentEnrichmentParams.subject.name(), subject);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.genre.name())) {
			String[] genreData = (String[]) node.getMetadata().get(ContentEnrichmentParams.genre.name());
			genre = new HashSet<Object>(Arrays.asList(genreData));
			result.put(ContentEnrichmentParams.genre.name(), genre);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.theme.name())) {
			String[] themeData = (String[]) node.getMetadata().get(ContentEnrichmentParams.theme.name());
			theme = new HashSet<Object>(Arrays.asList(themeData));
			result.put(ContentEnrichmentParams.theme.name(), theme);
		}
		if (null != node.getMetadata().get(ContentEnrichmentParams.keywords.name())) {
			String[] keyData = (String[]) node.getMetadata().get(ContentEnrichmentParams.keywords.name());
			keywords = new HashSet<Object>(Arrays.asList(keyData));
			result.put(ContentEnrichmentParams.keywords.name(), keywords);
		}
		for (Relation rel : node.getOutRelations()) {
			if ("Concept".equalsIgnoreCase(rel.getEndNodeObjectType())) {
				LOGGER.info("EndNodeId as Concept ->" + rel.getEndNodeId());
				concepts.add(rel.getEndNodeId());
			}
		}
		if (null != concepts && !concepts.isEmpty()) {
			result.put(ContentEnrichmentParams.concepts.name(), concepts);
		}
		return result;
	}

	/**
	 * This method gets the list of itemsets associated with content node and items which are members of item sets used
	 * in the content.
	 * 
	 * @param content The Content node
	 * 
	 * @param existingConceptGrades The conceptGrades from content node
	 * 
	 * @param existingConceptIds The existingConceptIds from Content node
	 * 
	 */
	private List<String> getItemsMap(Node node, String graphId, String contentId) {

		Set<String> itemSets = new HashSet<String>();
		List<String> items = new ArrayList<String>();

		try {
			if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
				List<Relation> outRelations = node.getOutRelations();

				if (null != outRelations && !outRelations.isEmpty()) {
					for (Relation rel : outRelations) {

						if (StringUtils.equalsIgnoreCase("ItemSet", rel.getEndNodeObjectType()) && !itemSets.contains(rel.getEndNodeId()))
							itemSets.add(rel.getEndNodeId());
					}
				}
			}
			if (null != itemSets && !itemSets.isEmpty()) {
				Set<String> itemIds = new HashSet<String>();
				for (String itemSet : itemSets) {
					List<String> members = getItemSetMembers(graphId, itemSet);
					if (null != members && !members.isEmpty())
						itemIds.addAll(members);
				}
				if (!itemIds.isEmpty()) {
					items = new ArrayList<String>(itemIds);
				}
			}
		} catch (Exception e) {
			LOGGER.info("exception occured while getting item and itemsets" + e);
		}
		return items;
	}

	/**
	 * This methods holds logic to get members of givens item set, returns the list of identifiers of the items that are
	 * members of the given item set.
	 * 
	 * @param graphId identifier of the domain graph
	 * 
	 * @param itemSetId identifier of the item set
	 * 
	 * @return list of identifiers of member items
	 */
	@SuppressWarnings("unchecked")
	private List<String> getItemSetMembers(String graphId, String itemSetId) {

		List<String> members = new ArrayList<String>();
		Response response = util.getCollectionMembers(graphId, itemSetId, CollectionTypes.SET.name());
		if (null != response) {
			members = (List<String>) response.get(GraphDACParams.members.name());
		}
		return members;
	}

	/**
	 * This method holds logic to map Concepts from the Items, get their gradeLevel and age Group and add it as a part
	 * of node metadata.
	 * 
	 * @param graphId The identifier of the domain graph
	 * 
	 * @param items The list of assessment item identifiers
	 * 
	 * @param content The Content node
	 * 
	 * @param existingConceptIds The existingConceptIds from content node
	 * 
	 * @param existingConceptGrades grades from concepts associated with content node
	 * 
	 * @return updated node with all metadata
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getConceptsFromItems(String graphId, String contentId, List<String> items, Node content, Set<String> existingConceptIds,
			Set<String> existingConceptGrades) {
		Response response = null;
		Set<String> itemGrades = new HashSet<String>();
		if (null != items && !items.isEmpty()) {
			response = util.getDataNodes(graphId, items);
		}
		if (null != response) {

			List<Node> item_nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			if (null != item_nodes && !item_nodes.isEmpty()) {
				for (Node node : item_nodes) {
					if (null != node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name())) {
						String[] grade_array = (String[]) node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name());
						for (String grade : grade_array) {
							itemGrades.add(grade);
						}
					}

					List<Relation> outRelations = node.getOutRelations();
					Map<String, Object> result = getOutRelationsMap(outRelations);
					if (null != result.get(ContentEnrichmentParams.conceptIds.name())) {
						List list = (List) result.get(ContentEnrichmentParams.conceptIds.name());
						if (null != list && !list.isEmpty())
							existingConceptIds.addAll(list);
					}
				}
			}
		}
		List<String> totalConceptIds = new ArrayList<String>();
		if (null != existingConceptIds && !existingConceptIds.isEmpty()) {
			totalConceptIds.addAll(existingConceptIds);
		}
		Node node = processGrades(content, itemGrades, existingConceptGrades);
		Node content_node = processAgeGroup(node);
		node.setOutRelations(null);
		node.setInRelations(null);
		util.updateNode(content_node);

		if (null != totalConceptIds && !totalConceptIds.isEmpty()) {
			util.addOutRelations(graphId, contentId, totalConceptIds, RelationTypes.ASSOCIATED_TO.relationName());
		}
	}

	/**
	 * This method mainly holds logic to map the content node with concept metadata like gradeLevel and ageGroup
	 * 
	 * @param content The content node
	 * 
	 * @param itemGrades The itemGrades
	 * 
	 * @param conceptGrades The conceptGrades
	 * 
	 * @param existingConceptGrades The concept grades from content
	 * 
	 * @return updated node with required metadata
	 */
	private Node processGrades(Node node, Set<String> itemGrades, Set<String> existingConceptGrades) {
		Node content_node = null;
		try {
			if (null != existingConceptGrades && !existingConceptGrades.isEmpty()) {
				content_node = setGradeLevels(existingConceptGrades, node);
			} else {
				if (null != itemGrades && !itemGrades.isEmpty()) {
					content_node = setGradeLevels(itemGrades, node);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Exception occured while setting age group from grade level" + e.getMessage(), e);
		}
		return content_node;
	}

	/**
	 * This method holds logic to getGrades levels either for itemGrades or conceptGrades and add it to node metadata
	 * 
	 * @param grades The grades
	 * 
	 * @param node The content node
	 * 
	 * @return The updated content node
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Node setGradeLevels(Set<String> grades, Node node) {

		if (null == node.getMetadata().get("gradeLevel")) {
			List<String> gradeLevel = new ArrayList(grades);
			node.getMetadata().put(ContentEnrichmentParams.gradeLevel.name(), gradeLevel);

		} else {
			String[] grade_array = (String[]) node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name());
			if (null != grade_array) {
				for (String grade : grade_array) {
					grades.add(grade);
					List gradeLevel = new ArrayList(grades);
					node.getMetadata().put(ContentEnrichmentParams.gradeLevel.name(), gradeLevel);
				}
			}
		}
		return node;
	}

	/**
	 * This method holds logic to map ageGroup from gradeMap
	 * 
	 * @param grades The gradeMap
	 * 
	 * @param existingAgeGroup The age group from content
	 * 
	 * @return The ageMap mapped from gradeLevel
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node processAgeGroup(Node node) {
		Node data = null;
		Set<String> ageSet = new HashSet<String>();

		if (null != node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name())) {
			List<String> grades = (List) node.getMetadata().get(ContentEnrichmentParams.gradeLevel.name());
			if (null != grades) {

				for (String grade : grades) {
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
				data = setAgeGroup(node, ageSet);
			}
		}
		return data;
	}

	/**
	 * This method holds logic to set ageGroup based on the grades
	 * 
	 * @param node The node
	 * 
	 * @param ageSet The ageSet
	 * 
	 * @return The node
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Node setAgeGroup(Node node, Set<String> ageSet) {
		if (null == node.getMetadata().get(ContentEnrichmentParams.ageGroup.name())) {
			if (null != ageSet) {
				List<String> ageGroup = new ArrayList(ageSet);
				node.getMetadata().put(ContentEnrichmentParams.ageGroup.name(), ageGroup);
			}

		} else {
			String[] age_array = (String[]) node.getMetadata().get("ageGroup");
			if (null != ageSet) {
				if (null != age_array) {
					for (String age : age_array) {
						ageSet.add(age);
					}
					List<String> ageGroup = new ArrayList(ageSet);
					node.getMetadata().put(ContentEnrichmentParams.ageGroup.name(), ageGroup);
				}
			}
		}
		return node;
	}

	@SuppressWarnings("unchecked")
	public void processCollectionForTOC(Node node) throws Exception {

		String contentId = node.getIdentifier();
		LOGGER.info("Processing Collection Content :" + contentId);
		Response response = util.getHirerachy(contentId);
		if (null != response && null != response.getResult()) {
			Map<String, Object> content = (Map<String, Object>) response.getResult().get("content");
			Map<String, Object> mimeTypeMap = new HashMap<>();
			Map<String, Object> contentTypeMap = new HashMap<>();
			int leafCount = 0;
			getTypeCount(content, "mimeType", mimeTypeMap);
			getTypeCount(content, "contentType", contentTypeMap);
			content.put(ContentAPIParams.mimeTypesCount.name(), mimeTypeMap);
			content.put(ContentAPIParams.contentTypesCount.name(), contentTypeMap);
			leafCount = getLeafNodeCount(content, leafCount);
			content.put(ContentAPIParams.leafNodesCount.name(), leafCount);
			LOGGER.info("Write hirerachy to JSON File :" + contentId);
			String data = mapper.writeValueAsString(content);
			File file = new File(getBasePath(contentId) + "TOC.json");
			try {
				FileUtils.writeStringToFile(file, data);
				if (file.exists()) {
					LOGGER.info("Upload File to S3 :" + file.getName());
					String[] uploadedFileUrl = AWSUploader.uploadFile(getAWSPath(contentId), file);
					if (null != uploadedFileUrl && uploadedFileUrl.length > 1) {
						String url = uploadedFileUrl[AWS_UPLOAD_RESULT_URL_INDEX];
						LOGGER.info("Update S3 url to node" + url);
						node.getMetadata().put(ContentAPIParams.toc_url.name(), url);
					}
					FileUtils.deleteDirectory(file.getParentFile());
					LOGGER.info("Deleting Uploaded files");
				}
			} catch (Exception e) {
				LOGGER.error("Error while uploading file ", e);
			}
			node.getMetadata().put(ContentAPIParams.mimeTypesCount.name(), mimeTypeMap);
			node.getMetadata().put(ContentAPIParams.contentTypesCount.name(), contentTypeMap);
			node.getMetadata().put(ContentAPIParams.leafNodesCount.name(), leafCount);
			util.updateNode(node);
		}
	}

	@SuppressWarnings("unchecked")
	private void getTypeCount(Map<String, Object> data, String type, Map<String, Object> typeMap) {
		List<Object> children = (List<Object>) data.get("children");
		if (null != children && !children.isEmpty()) {
			for (Object child : children) {
				Map<String, Object> childMap = (Map<String, Object>) child;
				String typeValue = childMap.get(type).toString();
				if (typeMap.containsKey(typeValue)) {
					int count = (int) typeMap.get(typeValue);
					count++;
					typeMap.put(typeValue, count);
				} else {
					typeMap.put(typeValue, 1);
				}
				if (childMap.containsKey("children")) {
					getTypeCount(childMap, type, typeMap);
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private Integer getLeafNodeCount(Map<String, Object> data, int leafCount) {
		List<Object> children = (List<Object>) data.get("children");
		if (null != children && !children.isEmpty()) {
			for (Object child : children) {
				Map<String, Object> childMap = (Map<String, Object>) child;
				int lc = 0;
				lc = getLeafNodeCount(childMap, lc);
				leafCount = leafCount + lc;
			}
		} else {
			leafCount++;
		}
		return leafCount;
	}

	private String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = this.config.get("lp.tempfile.location") + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name()
					+ File.separator + contentId;
		return path;
	}

	private String getAWSPath(String identifier) {
		String folderName = S3PropertyReader.getProperty(s3Content);
		if (!StringUtils.isBlank(folderName)) {
			folderName = folderName + File.separator + Slug.makeSlug(identifier, true) + File.separator
					+ S3PropertyReader.getProperty(s3Artifact);
		}
		return folderName;
	}

	protected Map<String, Object> getOutRelationsMap(List<Relation> outRelations) {
		List<String> nodeIds = new ArrayList<String>();
		List<String> conceptGrades = new ArrayList<String>();
		String domain = null;
		Map<String, Object> result_map = new HashMap<String, Object>();
		if (null != outRelations && !outRelations.isEmpty()) {
			for (Relation rel : outRelations) {

				if (null != rel.getEndNodeObjectType() && StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
					String status = null;
					if (null != rel.getEndNodeMetadata().get(ContentEnrichmentParams.status.name())) {
						status = (String) rel.getEndNodeMetadata().get(ContentAPIParams.status.name());
					}

					if (StringUtils.isBlank(domain) && null != rel.getEndNodeMetadata().get(ContentEnrichmentParams.subject.name())) {
						domain = (String) rel.getEndNodeMetadata().get(ContentEnrichmentParams.subject.name());
					}

					if (StringUtils.isNotBlank(status) && StringUtils.equalsIgnoreCase(ContentAPIParams.Live.name(), status)) {
						nodeIds.add(rel.getEndNodeId());
					}

					if (null != rel.getEndNodeMetadata().get(ContentEnrichmentParams.gradeLevel.name())) {
						String[] grade_array = (String[]) (rel.getEndNodeMetadata().get(ContentEnrichmentParams.gradeLevel.name()));
						for (String garde : grade_array) {
							conceptGrades.add(garde);
						}
					}
				}
			}

			if (null != nodeIds && !nodeIds.isEmpty()) {
				result_map.put("conceptIds", nodeIds);
			}

			if (null != conceptGrades && !conceptGrades.isEmpty()) {
				result_map.put("conceptGrades", conceptGrades);
			}

			if (StringUtils.isNotBlank(domain)) {
				result_map.put("domain", domain);
			}
		}
		return result_map;
	}
}
