package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
 * The Class ContentExtractionMessageProcessor is a kafka consumer which 
 * provides implementations of the core Content
 * feature extraction operations defined in the IMessageProcessor along with the methods to
 * implement content enrichment with additional metadata
 * 
 * @author Rashmi
 * 
 * @see IMessageProcessor
 */
public class ContentExtractionMessageProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentExtractionMessageProcessor.class.getName());

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The constructor */
	public ContentExtractionMessageProcessor() {
		super();
	}
	
	/** The Controller Utility */
	ControllerUtil util = new ControllerUtil();

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
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		List<String> ExistingConcepts = new ArrayList<String>();
		
		LOGGER.info("processing kafka message" + message);
		if (null != message.get("edata")) {
			LOGGER.info("checking if kafka message contains edata or not" + message.get("edata"));
			edata = (Map) message.get("edata");
			if (null != edata.get("eks")) {
				LOGGER.info("checking if edata has eks present in it" + eks);
				eks = (Map) edata.get("eks");
				if(null != eks.get("concepts")){
					LOGGER.info("getting conceptsMap from kafka message" + eks.get("concepts"));
					List<String> conceptList = (List)eks.get("concepts");
					for(Object data : conceptList){
						Map<String,String> map = (Map)data;
						for (Entry<String, String> entry : map.entrySet()){
							if(entry.getKey().equalsIgnoreCase("identifier")){
								ExistingConcepts.add(entry.getValue());
							}
						} 
					}
				}
				LOGGER.info("conceptId retrieved from kafka message" + ExistingConcepts);
				if (null != eks.get("cid")) {
					Node node = util.getNode("domain", eks.get("cid").toString());
					LOGGER.info("Calling tagNewConcepts method to extract content data" + node);
					tagNewConcepts(node, ExistingConcepts);
				}
			}
		}
	}

	/**
	 * This method gets the list of concepts associated with items that are
	 * members of item sets used in the content. These concepts are then
	 * associated with the input content object.
	 * 
	 * @param content
	 * 			the content node 
	 * @param concepts 
	 * 			the list of existing concepts
	 */
	private void tagNewConcepts(Node content, List<String> ExistingConcepts) {

		List<String> itemSets = new ArrayList<String>();
		List<Relation> outRelations = content.getOutRelations();
		String contentId = content.getIdentifier();
		String graphId = "domain";

		LOGGER.info("Get item sets associated with the content: " + contentId);
		if (null != outRelations && !outRelations.isEmpty()) {
			for (Relation rel : outRelations) {
				if (StringUtils.equalsIgnoreCase("ItemSet", rel.getEndNodeObjectType())
						&& !itemSets.contains(rel.getEndNodeId()))
					itemSets.add(rel.getEndNodeId());
				LOGGER.info("checking if endNodeType is ItemSet and fetching the itemSets" + itemSets);
			}
		}
		if (null != itemSets && !itemSets.isEmpty()) {
			LOGGER.info("Number of item sets: " + itemSets.size());
			Set<String> itemIds = new HashSet<String>();
			for (String itemSet : itemSets) {
				List<String> members = getItemSetMembers(graphId, itemSet);
				LOGGER.info("getting item memebers from item set" + members);
				if (null != members && !members.isEmpty())
					itemIds.addAll(members);
			}
			LOGGER.info("Total number of items: " + itemIds.size());
			if (!itemIds.isEmpty()) {
				List<String> items = new ArrayList<String>(itemIds);
				LOGGER.info("getting concepts associated with items" + items);
				Node node = MapConceptsFromItems(graphId, items, content, ExistingConcepts, contentId);
				LOGGER.info("calling content update operation after adding required metadata" + node);
				util.updateNode(node);
			}
		}
	}

	/**
	 * Returns the list of identifiers of the items that are members of the
	 * given item set.
	 * 
	 * @param graphId
	 *            identifier of the domain graph
	 * @param itemSetId
	 *            identifier of the item set
	 * @return list of identifiers of member items
	 */
	@SuppressWarnings("unchecked")
	private List<String> getItemSetMembers(String graphId, String itemSetId) {
		List<String> members = new ArrayList<String>();
		LOGGER.info("Get members of items set: " + itemSetId);
		Response response = util.getCollectionMembers(graphId, itemSetId, CollectionTypes.SET.name());
		if (null != response) {
			members = (List<String>) response.get(GraphDACParams.members.name());
		}
		LOGGER.info("item members fetched from itemSets" + members);
		return members;
	}

	/**
	 * Returns the list of identifiers of concepts associated with the given
	 * list of assessment items.
	 * 
	 * @param graphId
	 *            identifier of the domain graph
	 * @param items
	 *            list of assessment item identifiers
	 * @return list of concept identifiers associated with the input assessment
	 *         items
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node MapConceptsFromItems(String graphId, List<String> items, Node content, List<String> ExistingConcepts, String contentId) {
		
		LOGGER.info("getting all itemsData from itemIds" + items);
		
		Response response = util.getDataNodes(graphId, items);
		LOGGER.info("response from getDataNodes" + response);
		
		List<String> conceptIds = new ArrayList<String>();
		List<String> itemGrades = new ArrayList<String>();
		List<String> conceptGrades = new ArrayList<String>();
		if (null != response) {
			
			List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			LOGGER.info("List of nodes retrieved from response" + nodes.size());
			
			if (null != nodes && !nodes.isEmpty()) {
				for (Node node : nodes) {
					LOGGER.info("getting gradeLevel from assessment items");
					if(null != node.getMetadata().get("gradeLevel")){
						itemGrades.addAll((List)node.getMetadata().get("gradeLevel"));
						LOGGER.info("gradeLevels retrieved from items" + itemGrades);
					}

					List<Relation> outRelations = node.getOutRelations();
					LOGGER.info("outRelations fetched from each item");
					
					if (null != outRelations && !outRelations.isEmpty()) {
						for (Relation rel : outRelations) {
							LOGGER.info("checking if endNodeType is Concept");
							if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())
									&& !conceptIds.contains(rel.getEndNodeId())) {
								LOGGER.info("getting status from node");
								String status = (String) rel.getEndNodeMetadata()
										.get(ContentWorkflowPipelineParams.status.name());
								LOGGER.info("checking if status is LIVE and fetching conceptIds from it" + status);
								if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status))
									conceptIds.add(rel.getEndNodeId());
								LOGGER.info("concepts fetched form LIVE items" + conceptIds);
								if(null != rel.getEndNodeMetadata().get("gradeLevel")){
									conceptGrades.addAll((List)rel.getEndNodeMetadata().get("gradeLevel"));
									LOGGER.info("Adding gradeLevel from concepts" + conceptGrades);
								}
							}
						}
					}
				}
			}
		}
		LOGGER.info("getting nodes of existing conceptIds");
		if(null != ExistingConcepts && !ExistingConcepts.isEmpty()){
			Response result = util.getDataNodes(graphId,ExistingConcepts);
			if(null != result){
				List<Node> nodes = (List) result.get(GraphDACParams.node_list.name());
				for(Node node : nodes){
					LOGGER.info("Checking if concept has gradeLevel associated with it");
					if(null != node.getMetadata().get("gradeLevel")){
						LOGGER.info("adding gradeLevel from existing concepts" + node.getMetadata().get("gradeLevel"));
						conceptGrades.addAll((List)node.getMetadata().get("gradeLevel"));
					}
				}
			}
		}
		LOGGER.info("Adding concepts from content node to concepts extracted from items");
		conceptIds.addAll(ExistingConcepts);
		if (null != conceptIds && !conceptIds.isEmpty()) {
			LOGGER.info("Number of concepts: " + conceptIds.size());
			Response resp = util.addOutRelations(graphId, contentId, conceptIds,
					RelationTypes.ASSOCIATED_TO.relationName());
			if (null != resp) {
				LOGGER.info("New concepts tagged successfully: " + contentId);
			}
		}	
		Node node = getConceptMetadata(content, itemGrades, conceptGrades);
		LOGGER.info("result node after adding required metadata" + node);
		return node;
	}
	
	
	/**
	 * This methods mainly holds logic to map the content node
	 * with concept metaData like gradeLevel and ageGroup
	 *  
	 * @param content
	 * 			The content node
	 * 
	 * @param itemsGrades
	 * 			The itemGrades
	 * 
	 * @param conceptGrades
	 * 			The conceptGrades
	 * 
	 * @return Node
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node getConceptMetadata(Node content, List itemsGrades, List<String> conceptGrades) {
		LOGGER.info("checking if conceptGrades are empty" + conceptGrades.size());
		if(!conceptGrades.isEmpty() && null != conceptGrades){
			LOGGER.info("Mapping grades with ageGroup" + conceptGrades);
			List<String> ageGroup = mapGradeWithAge(conceptGrades);
			content.getMetadata().put("gradeLevel", conceptGrades);
			content.getMetadata().put("ageGroup", ageGroup);
			LOGGER.info("ageGroup and conceptGrades added to content successfully" + content);
		}
		else{
			List<String> ageGroup = mapGradeWithAge(itemsGrades);
			content.getMetadata().put("gradeLevel", itemsGrades);
			content.getMetadata().put("ageGroup", ageGroup);
			LOGGER.info("ageGroup and itemGrades added to content successfully" + content);
		}
		return content;
	}
	
	/**
	 * This method holds logic to map ageGroup from gradeMap
	 * 
	 * @param grades
	 * 			The gradeMap
	 * @return
	 * 			The ageMap
	 */
	private List<String> mapGradeWithAge(List<String> grades) {
		List<String> age = new ArrayList<String>();
		if (!grades.isEmpty()) {
			for (String grade : grades) {
				LOGGER.info("mapping age group based on grades");
				if ("Kindergarten".equalsIgnoreCase(grade)) {
					age.add("<5");
				} else if ("Grade 1".equalsIgnoreCase(grade)) {
					age.add("5-6");
				} else if ("Grade 2".equalsIgnoreCase(grade)) {
					age.add("6-7");
				} else if ("Grade 3".equalsIgnoreCase(grade)) {
					age.add("7-8");
				} else if ("Grade 4".equalsIgnoreCase(grade)) {
					age.add("8-10");
				} else if ("Grade 5".equalsIgnoreCase(grade)) {
					age.add(">10");
				} else if ("other".equalsIgnoreCase(grade)) {
					age.add("other");
				}
			}
		}
		LOGGER.info("age map mapped from grade" + age);
		return age;
	}
}
