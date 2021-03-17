package org.sunbird.common.mgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.enums.CompositeSearchErrorCodes;
import org.sunbird.common.enums.CompositeSearchParams;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.sunbird.telemetry.util.LogAsyncGraphEvent;

/**
 * The Class CompositeIndexSyncManager consists of different operations to
 * support sync operations between the Graph DB and an ElasticSearch index .
 * 
 * @author Rayulu
 * 
 */
public abstract class CompositeIndexSyncManager extends BaseManager {

	/** The logger. */
	

	/** The Constant SYNC_BATCH_SIZE. */
	private static final int SYNC_BATCH_SIZE = 1000;

	/** The Constant MAX_LIMIT. */
	private static final int MAX_LIMIT = 5000;

	/**
	 * Syncs all objects of a given object type or all object types in a given
	 * graph ID with ES.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param start
	 *            the start
	 * @param total
	 *            the total
	 * @return the response
	 */
	protected Response syncDefinition(String graphId, String objectType, Integer start, Integer total) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		TelemetryManager.log("Composite index sync : " + graphId);
		Response response = OK();

		// if object type is given, sync only objects of the given type
		if (StringUtils.isNotBlank(objectType)) {
			Map<String, Object> result = getDefinition(graphId, objectType).getResult();
			DefinitionDTO dto = (DefinitionDTO) result.get(GraphDACParams.definition_node.name());
			response = genCompositeSearchMessage(graphId, dto, start, total);
		}

		// if object type is not given, sync all objects from a given graph
		else {
			Map<String, Object> result = getAllDefinitions(graphId).getResult();
			List<DefinitionDTO> lstDefDTO = getDefinitionDTOList(result);
			response = genCompositeSearchMessages(graphId, lstDefDTO, start, total);
		}
		return response;
	}

	/**
	 * Sync a list of nodes based on the identifiers provided.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifiers
	 *            the identifiers
	 * @return the response
	 */
	protected Response syncNode(String graphId, String[] identifiers) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_IDENTIFIER.name(),
					"Identifier is blank.");
		TelemetryManager.log("Composite index sync : " + graphId + " | Identifier: " + identifiers);
		List<Map<String, Object>> lstMessages = new ArrayList<Map<String, Object>>();
		if (null != identifiers && identifiers.length > 0) {
			for (String identifier : identifiers) {
				Node node = getNode(graphId, identifier);
				if (null != node) {
					lstMessages.add(getKafkaMessage(node));
				} else {
					throw new ResourceNotFoundException(
							CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND.name(),
							"Object not found: " + identifier);
				}
			}
			TelemetryManager.log("Sync messages count : " + lstMessages.size());
		}
		return pushMessageToKafka(lstMessages);
	}

	/**
	 * Gets the object definition.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @return the definition
	 */
	private Response getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition");
		request.put(GraphDACParams.object_type.name(), objectType);
		return getResponse(request);
	}

	/**
	 * Gets all object definitions from the graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @return the all definitions
	 */
	private Response getAllDefinitions(String graphId) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request);
	}

	/**
	 * Gets the definition DTO as a list from the result.
	 *
	 * @param result
	 *            the result
	 * @return the definition DTO list
	 */
	@SuppressWarnings("unchecked")
	private List<DefinitionDTO> getDefinitionDTOList(Map<String, Object> result) {
		List<DefinitionDTO> lstDefDTO = new ArrayList<DefinitionDTO>();
		for (Entry<String, Object> def : result.entrySet()) {
			lstDefDTO.addAll((List<DefinitionDTO>) def.getValue());
		}
		return lstDefDTO;
	}

	/**
	 * Generates composite search messages for the given definitions.
	 *
	 * @param graphId
	 *            the graph id
	 * @param lstDefDTO
	 *            the lst def DTO
	 * @param startPosition
	 *            the start position
	 * @param total
	 *            the total
	 * @return the response
	 */
	private Response genCompositeSearchMessages(String graphId, List<DefinitionDTO> lstDefDTO, Integer startPosition,
			Integer total) {
		Response response = OK();
		for (DefinitionDTO def : lstDefDTO) {
			response = genCompositeSearchMessage(graphId, def, startPosition, total);
		}
		return response;
	}

	/**
	 * Generates batches of composite search messages for the given definition.
	 *
	 * @param graphId
	 *            the graph id
	 * @param def
	 *            the def
	 * @param startPosition
	 *            the start position
	 * @param total
	 *            the total
	 * @return the response
	 */
	private Response genCompositeSearchMessage(String graphId, DefinitionDTO def, Integer startPosition,
			Integer total) {
		Response response = OK();
		if (null == total || total > MAX_LIMIT)
			total = MAX_LIMIT;
		int start = 0;
		if (null != startPosition && startPosition.intValue() > 0)
			start = startPosition.intValue();
		int batch = SYNC_BATCH_SIZE;
		if (null != total && total < batch)
			batch = total;
		int fetchCount = 0;
		boolean found = true;
		while (found) {
			List<Node> nodes = getNodes(graphId, def.getObjectType(), start, batch);
			fetchCount += batch;
			if (null != nodes && !nodes.isEmpty()) {
				List<Map<String, Object>> lstMessages = new ArrayList<Map<String, Object>>();
				for (Node node : nodes) {
					lstMessages.add(getKafkaMessage(node));
				}
				response = pushMessageToKafka(lstMessages);
				System.out.println("sent " + start + " + " + batch + " -- " + def.getObjectType() + " objects");
				start += batch;
				if (null != total && fetchCount >= total) {
					found = false;
					break;
				}
			} else {
				found = false;
				break;
			}
		}
		return response;
	}

	/**
	 * Gets the kafka message DTO for a given node.
	 *
	 * @param node
	 *            the node
	 * @return the kafka message
	 */
	private Map<String, Object> getKafkaMessage(Node node) {
	    Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();
        if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
            	String key = entry.getKey();
            	if (StringUtils.isNotBlank(key)) {
            		Map<String, Object> valueMap=new HashMap<String, Object>();
                    valueMap.put("ov", null); // old value
                    valueMap.put("nv", entry.getValue()); // new value
                    // temporary check to not sync body and editorState
                    if (!StringUtils.equalsIgnoreCase("body", key) && !StringUtils.equalsIgnoreCase("editorState", key))
                    	propertyMap.put(entry.getKey(), valueMap);
            	}
            }
            transactionData.put(CompositeSearchParams.properties.name(), propertyMap);
        } else
            transactionData.put(CompositeSearchParams.properties.name(), new HashMap<String, Object>());
        
        
        // add IN relations
        List<Map<String, Object>> relations = new ArrayList<Map<String, Object>>();
        if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
            for (Relation rel : node.getInRelations()) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("rel", rel.getRelationType());
                relMap.put("id", rel.getStartNodeId());
                relMap.put("dir", "IN");
                relMap.put("type", rel.getStartNodeObjectType());
                relMap.put("label", getLabel(rel.getStartNodeMetadata()));
                relations.add(relMap);
            }
        }
        
        // add OUT relations
        if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
            for (Relation rel : node.getOutRelations()) {
                Map<String, Object> relMap = new HashMap<>();
                relMap.put("rel", rel.getRelationType());
                relMap.put("id", rel.getEndNodeId());
                relMap.put("dir", "OUT");
                relMap.put("type", rel.getEndNodeObjectType());
                relMap.put("label", getLabel(rel.getEndNodeMetadata()));
                relations.add(relMap);
            }
        }
        transactionData.put(CompositeSearchParams.addedRelations.name(), relations);
        map.put(CompositeSearchParams.operationType.name(), GraphDACParams.UPDATE.name());
        map.put(CompositeSearchParams.graphId.name(), node.getGraphId());
        map.put(CompositeSearchParams.nodeGraphId.name(), node.getId());
        map.put(CompositeSearchParams.nodeUniqueId.name(), node.getIdentifier());
        map.put(CompositeSearchParams.objectType.name(), node.getObjectType());
        map.put(CompositeSearchParams.nodeType.name(), SystemNodeTypes.DATA_NODE.name());
        map.put(CompositeSearchParams.transactionData.name(), transactionData);
        map.put(CompositeSearchParams.syncMessage.name(), true);
        return map;
	}

	/**
	 * Gets the label of an object from the metadata.
	 *
	 * @param metadata
	 *            the metadata
	 * @return the label
	 */
	private String getLabel(Map<String, Object> metadata) {
		if (null != metadata && !metadata.isEmpty()) {
			if (StringUtils.isNotBlank((String) metadata.get("name")))
				return (String) metadata.get("name");
			else if (StringUtils.isNotBlank((String) metadata.get("lemma")))
				return (String) metadata.get("lemma");
			else if (StringUtils.isNotBlank((String) metadata.get("title")))
				return (String) metadata.get("title");
			else if (StringUtils.isNotBlank((String) metadata.get("gloss")))
				return (String) metadata.get("gloss");
		}
		return "";
	}

	/**
	 * Pushes message to kafka via Logstash.
	 *
	 * @param messages
	 *            the messages
	 * @return the response
	 */
	private Response pushMessageToKafka(List<Map<String, Object>> messages) {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		if (null == messages || messages.size() <= 0) {
			response.put(CompositeSearchParams.graphSyncStatus.name(), "No Graph Objects to Sync!");
			response.setResponseCode(ResponseCode.CLIENT_ERROR);
			params.setStatus(CompositeSearchParams.success.name());
			response.setParams(params);
			return response;
		}
		System.out.println("Sending to KAFKA.... ");
		// KafkaMessageProducer.sendMessage(messages);
		LogAsyncGraphEvent.pushMessageToLogger(messages);
		response.put(CompositeSearchParams.graphSyncStatus.name(), "Graph Sync Started Successfully!");
		response.setResponseCode(ResponseCode.OK);
		response.setParams(params);
		params.setStatus(CompositeSearchParams.success.name());
		response.setParams(params);
		return response;
	}

	/**
	 * Gets the nodes of a given object type from the Graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param startPosition
	 *            the start position
	 * @param batchSize
	 *            the batch size
	 * @return the nodes
	 */
	@SuppressWarnings("unchecked")
	private List<Node> getNodes(String graphId, String objectType, int startPosition, int batchSize) {
		SearchCriteria sc = new SearchCriteria();
		//sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		sc.setResultSize(batchSize);
		sc.setStartPosition(startPosition);
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: " + graphId);
		else {
			List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
			return nodes;
		}
	}

	/**
	 * Gets the node from the graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifier
	 *            the identifier
	 * @return the node
	 */
	private Node getNode(String graphId, String identifier) {
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), identifier);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException(
					CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND.name(),
					"Object not found: " + identifier);
		else {
			Node node = (Node) listRes.get(GraphDACParams.node.name());
			return node;
		}
	}
}
