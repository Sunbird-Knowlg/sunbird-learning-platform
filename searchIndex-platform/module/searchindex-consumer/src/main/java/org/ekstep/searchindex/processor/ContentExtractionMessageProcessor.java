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
	private ControllerUtil util = new ControllerUtil();

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
		Set<String> existingConceptIds = new HashSet<String>();
		Set<String> existingConceptGrades = new HashSet<String>();
		try{
			LOGGER.info("processing kafka message" + message);
			if (null != message.get("edata")) {
				LOGGER.info("checking if kafka message contains edata or not" + message.get("edata"));
				edata = (Map) message.get("edata");
				if (null != edata.get("eks")) {
					LOGGER.info("checking if edata has eks present in it" + eks);
					eks = (Map) edata.get("eks");
					if (null != eks.get("cid")) {
						Node node = util.getNode("domain", eks.get("cid").toString());
						List<Relation> outRelations = (List) node.getOutRelations();
						if (null != outRelations && !outRelations.isEmpty()) {
							for (Relation rel : outRelations) {
								if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())){
										LOGGER.info("getting status from node");
									String status = (String) rel.getEndNodeMetadata()
											.get(ContentWorkflowPipelineParams.status.name());
									LOGGER.info("checking if status is LIVE and fetching conceptIds from it" + status);
									if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status))
										existingConceptIds.add(rel.getEndNodeId());
									LOGGER.info("concepts fetched form LIVE items" + existingConceptIds);
									if(null != rel.getEndNodeMetadata().get("gradeLevel")){
										List<String> list = (List) (rel.getEndNodeMetadata().get("gradeLevel"));
										if(null != list && !list.isEmpty())
											existingConceptGrades.addAll(list);
										LOGGER.info("Adding gradeLevel from concepts" + existingConceptGrades);
									}
								}
							}
						}
						LOGGER.info("Calling tagNewConcepts method to extract content data" + node);
						tagNewConcepts(node, existingConceptIds, existingConceptGrades);
					}
				}
			}
		}catch(Exception e){
			LOGGER.info("exception occured while processing kafka message and node operations", e);
			e.printStackTrace();
		}
	}

	/**
	 * This method gets the list of itemsets associated with content node and
	 * items which are members of item sets used in the content.
	 * 
	 * @param content
	 * 			The Content node 
	 * 
	 * @param existingConceptGrades 
	 * 			The conceptGrades from content node
	 * 
	 * @param existingConceptIds
	 * 			The existingConceptIds from Content node
	 * 
	 * @param concepts 
	 * 			The list of existing concepts from content node
	 */
	private void tagNewConcepts(Node content, Set<String> existingConceptIds, Set<String> existingConceptGrades) {

		List<String> itemSets = new ArrayList<String>();
		List<Relation> outRelations = content.getOutRelations();
		String contentId = content.getIdentifier();
		String graphId = "domain";
		try{
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
					Node node = mapConceptsFromItems(graphId, items, content, existingConceptIds,  existingConceptGrades, contentId);
					LOGGER.info("calling content update operation after adding required metadata" + node);
					util.updateNode(node);
				}
			}
		} catch(Exception e){
			LOGGER.info("exception occured while getting item and itemsets", e);
			e.printStackTrace();
		}
	}

	/**
	 * This methods holds logic to get members of givens item set,
	 * returns the list of identifiers of the items that are members of the
	 * given item set.
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
		if (null != response) {
			members = (List<String>) response.get(GraphDACParams.members.name());
		}
		LOGGER.info("item members fetched from itemSets" + members);
		return members;
	}

	/**
	 * This method holds logic to map Concepts from the Items, 
	 * get their gradeLevel and age Group and add it as a part of
	 * node metadata.
	 * 
	 * @param graphId
	 *           The identifier of the domain graph
	 *            
	 * @param items
	 *          The list of assessment item identifiers
	 *      
	 * @param content
	 * 			The Content node
	 * 
	 * @param existingConceptIds
	 * 			The existingConceptIds from content node
	 * 
	 * @param existingConceptGrades 
	 * 			grades from concepts associated with content node
	 * 
	 * @return updated node with all metadata 
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node mapConceptsFromItems(String graphId, List<String> items, Node content, Set<String> existingConceptIds, Set<String> existingConceptGrades, String contentId) {
		
		LOGGER.info("getting all items Data from itemIds" + items);
		
		Response response = util.getDataNodes(graphId, items);
		LOGGER.info("response from getDataNodes" + response);
		
		Set<String> conceptIds = new HashSet<String>();
		Set<String> itemGrades = new HashSet<String>();
		Set<String> conceptGrades = new HashSet<String>();
		try{
			if (null != response) {
				
				List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
				LOGGER.info("List of nodes retrieved from response" + nodes.size());
				
				if (null != nodes && !nodes.isEmpty()) {
					for (Node node : nodes) {
						LOGGER.info("getting gradeLevel from assessment items");
						if(null != node.getMetadata().get("gradeLevel")){
							List<String> list = (List)node.getMetadata().get("gradeLevel");
							if(null != list && !list.isEmpty())
								itemGrades.addAll(list);
							LOGGER.info("gradeLevels retrieved from items" + itemGrades);
						}
	
						List<Relation> outRelations = node.getOutRelations();
						LOGGER.info("outRelations fetched from each item");
						
						if (null != outRelations && !outRelations.isEmpty()) {
							for (Relation rel : outRelations) {
								LOGGER.info("checking if endNodeType is Concept");
								if (StringUtils.equalsIgnoreCase("Concept", rel.getEndNodeObjectType())) {
									LOGGER.info("getting status from node");
									String status = (String) rel.getEndNodeMetadata()
											.get(ContentWorkflowPipelineParams.status.name());
									LOGGER.info("checking if status is LIVE and fetching conceptIds from it" + status);
									if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.Live.name(), status))
										conceptIds.add(rel.getEndNodeId());
									LOGGER.info("concepts fetched form LIVE items" + conceptIds);
									if(null != rel.getEndNodeMetadata().get("gradeLevel")){
										List<String> grades = (List)rel.getEndNodeMetadata().get("gradeLevel");
										if(null != grades && !grades.isEmpty())
											conceptGrades.addAll(grades);
										LOGGER.info("Adding gradeLevel from concepts" + conceptGrades);
									}
								}
							}
						}
					}
				}
			}
		} catch(Exception e){
			LOGGER.error("exception occured while getting concept maps", e);
			e.printStackTrace();
		}
		LOGGER.info("Adding concepts from content node to concepts extracted from items");
		List<String> totalConceptIds = new ArrayList<String>();
			
			if(null != existingConceptIds && !existingConceptIds.isEmpty())
				totalConceptIds.addAll(existingConceptIds);
			
			if(null != conceptIds && !conceptIds.isEmpty()){
				for(String concept : conceptIds){
					if(!totalConceptIds.contains(concept)){
						totalConceptIds.add(concept);
					}
				}
			}
			
			Node node = getConceptMetadata(content, itemGrades, conceptGrades, existingConceptGrades);
			List<Relation> outRelations = new ArrayList<Relation>();
			if(null != totalConceptIds && !totalConceptIds.isEmpty()){
				for(String conceptId : totalConceptIds){
					Relation relation = new Relation(contentId, RelationTypes.ASSOCIATED_TO.relationName(), conceptId);
					outRelations.add(relation);
				}
				node.setOutRelations(outRelations);
			}
		LOGGER.info("result node after adding required metadata" + node);
		return node;
	}
	
	/**
	 * This method mainly holds logic to map the content node
	 * with concept metadata like gradeLevel and ageGroup
	 *  
	 * @param content
	 * 			The content node
	 * 
	 * @param itemGrades
	 * 			The itemGrades
	 * 
	 * @param conceptGrades
	 * 			The conceptGrades
	 * 
	 * @param existingConceptGrades 
	 * 			The concept grades from content
	 * 
	 * @return updated node with required metadata
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node getConceptMetadata(Node content, Set<String> itemGrades, Set<String> conceptGrades, Set<String> existingConceptGrades) {
		try{
			List<String> totalGrades = new ArrayList<String>();
			
			LOGGER.info("Adding grades associated with content node" + existingConceptGrades);
			if(null != existingConceptGrades && !existingConceptGrades.isEmpty())
				totalGrades.addAll(existingConceptGrades);
			
			if(null != content.getMetadata().get("ageGroup"));
				List<String> existingAgeGroup = (List)content.getMetadata().get("ageGroup");
			LOGGER.info("Adding age group associated with content node" + existingAgeGroup);
				
			LOGGER.info("checking if conceptGrades are empty" + conceptGrades.size());
			if(null != conceptGrades && !conceptGrades.isEmpty()){
				for(String grade : conceptGrades){
					if(!totalGrades.contains(grade)){
						totalGrades.add(grade);
					}
				}
				LOGGER.info("Mapping totalGrades with ageGroup from concept"+  totalGrades);
				List<String> ageGroup = mapGradeWithAge(totalGrades, existingAgeGroup);
				content.getMetadata().put("gradeLevel", totalGrades);
				content.getMetadata().put("ageGroup", ageGroup);
				LOGGER.info("ageGroup and conceptGrades added to content successfully" + content);
			}
			else if(null != itemGrades && !itemGrades.isEmpty()){
				for(String grade : itemGrades){
					if(!totalGrades.contains(grade)){
						totalGrades.add(grade);
					}
				}
				LOGGER.info("Mapping totalGrades with ageGroup from items" + totalGrades);
				List<String> ageGroup = mapGradeWithAge(totalGrades, existingAgeGroup);
				content.getMetadata().put("gradeLevel", totalGrades);
				content.getMetadata().put("ageGroup", ageGroup);
				LOGGER.info("ageGroup and itemGrades added to content successfully" + content);
			}
			
		}catch(Exception e){
			LOGGER.error("Exception occured while setting age group from grade level", e);
			e.printStackTrace();
		}
		return content;
	}
	
	/**
	 * This method holds logic to map ageGroup from gradeMap
	 * 
	 * @param grades
	 * 			The gradeMap
	 * 
	 * @param existingAgeGroup 
	 * 			The age group from content
	 * 
	 * @return
	 * 		The ageMap mapped from gradeLevel
	 */
	private List<String> mapGradeWithAge(List<String> grades, List<String> existingAgeGroup) {
		try{
			List<String> ageList = new ArrayList<String>();
			if (!grades.isEmpty()) {
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
			}
			for(String str : ageList){
				if(!existingAgeGroup.contains(str)){
					LOGGER.info("adding new age group if its not there in content age group" + str);
					existingAgeGroup.add(str);
				}
			}
			LOGGER.info("age map mapped from grade" + existingAgeGroup);
		} catch(Exception e){
			LOGGER.info("exception occured while getting age group", e);
			e.printStackTrace();
		}
		return existingAgeGroup;
	}
}
