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
				if (null != members && !members.isEmpty())
					itemIds.addAll(members);
			}
			LOGGER.info("Total number of items: " + itemIds.size());
			if (!itemIds.isEmpty()) {
				List<String> items = new ArrayList<String>(itemIds);
				LOGGER.info("getting concepts associated with items" + items);
				Node node = MapConceptsFromItems(graphId, items, content, ExistingConcepts, contentId);
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
			LOGGER.info("List of nodes rerieved from response" + nodes.size());
			
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
							if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())
									&& !conceptIds.contains(rel.getEndNodeId())) {
								String status = (String) rel.getEndNodeMetadata()
										.get(ContentWorkflowPipelineParams.status.name());
								if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status))
									conceptIds.add(rel.getEndNodeId());
								if(null != rel.getEndNodeMetadata().get("gradeLevel")){
									conceptGrades.addAll((List)rel.getEndNodeMetadata().get("gradeLevel"));
								}
							}
						}
					}
				}
			}
		}
		conceptIds.addAll(ExistingConcepts);
		if (null != conceptIds && !conceptIds.isEmpty()) {
			LOGGER.info("Number of concepts: " + conceptIds.size());
			Response resp = util.addOutRelations(graphId, contentId, conceptIds,
					RelationTypes.ASSOCIATED_TO.relationName());
			if (null != resp) {
				LOGGER.info("New concepts tagged successfully: " + contentId);
			}
		}	
		Node node = getConceptMetadata(graphId, conceptIds, content, itemGrades, conceptGrades);
		return node;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node getConceptMetadata(String graphId, List<String> conceptIds, Node content, List itemsGrades, List<String> conceptGrades) {
		if(!conceptGrades.isEmpty() && null != conceptGrades){
			List<String> ageGroup = mapGradeWithAge(conceptGrades);
			content.getMetadata().put("gradeLevel", conceptGrades);
			content.getMetadata().put("ageGroup", ageGroup);
		}
		else{
			List<String> ageGroup = mapGradeWithAge(itemsGrades);
			content.getMetadata().put("gradeLevel", itemsGrades);
			content.getMetadata().put("ageGroup", ageGroup);
		}
		return content;
	}

	private List<String> mapGradeWithAge(List<String> grades) {
		List<String> age = new ArrayList<String>();
		if (!grades.isEmpty()) {
			for (String grade : grades) {
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
		return age;
	}
}
