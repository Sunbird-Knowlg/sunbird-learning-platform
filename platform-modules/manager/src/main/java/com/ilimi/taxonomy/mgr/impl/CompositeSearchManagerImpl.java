package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.searchindex.util.LogAsyncGraphEvent;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.CompositeSearchErrorCodes;
import com.ilimi.taxonomy.enums.CompositeSearchParams;
import com.ilimi.taxonomy.mgr.ICompositeSearchManager;

@Component
public class CompositeSearchManagerImpl extends BaseManager implements ICompositeSearchManager {

	private static Logger LOGGER = LogManager.getLogger(ICompositeSearchManager.class.getName());
	private static final int SYNC_BATCH_SIZE = 1000;
	
	@Override
	public Response sync(String graphId, String objectType, Integer start, Integer total) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		LOGGER.info("Composite index sync : " + graphId);
		Response response = OK();
		if (StringUtils.isNotBlank(objectType)) {
		    Map<String, Object> result = getDefinition(graphId, objectType).getResult();
		    DefinitionDTO dto = (DefinitionDTO) result.get(GraphDACParams.definition_node.name());
		    response = genCompositeSearchMessage(graphId, dto, start, total);
		} else {
		    Map<String, Object> result = getAllDefinitions(graphId).getResult();
	        List<DefinitionDTO> lstDefDTO = getDefinitionDTOList(result);
	        response = genCompositeSearchMessages(graphId, lstDefDTO, start, total);
		}
		return response;
	}
	
	@Override
	public Response syncObject(String graphId, String identifier) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		if (StringUtils.isBlank(graphId))
			throw new ClientException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_BLANK_IDENTIFIER.name(),
					"Identifier is blank.");
		LOGGER.info("Composite index sync : " + graphId + " | Identifier: " + identifier);
		Node node = getNode(graphId, identifier);
		if (null != node) {
			List<Map<String, Object>> lstMessages = new ArrayList<Map<String, Object>>();
			lstMessages.add(getKafkaMessage(node));
			return pushMessageToKafka(lstMessages);
		} else {
			throw new ResourceNotFoundException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND.name(),
					"Object not found: " + identifier);
		}
	}
	
	private Response getDefinition(String graphId, String objectType) {
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition");
        request.put(GraphDACParams.object_type.name(), objectType);
        return getResponse(request, LOGGER);
    }
	
	private Response getAllDefinitions(String graphId) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		return getResponse(request, LOGGER);
	}
	
	@SuppressWarnings("unchecked")
	private List<DefinitionDTO> getDefinitionDTOList(Map<String, Object> result) {
		List<DefinitionDTO> lstDefDTO = new ArrayList<DefinitionDTO>();
		for (Entry<String, Object> def: result.entrySet()) {
			lstDefDTO.addAll((List<DefinitionDTO>) def.getValue());
		}
		return lstDefDTO;
	}
	
	private Response genCompositeSearchMessages(String graphId, List<DefinitionDTO> lstDefDTO, Integer startPosition, Integer total) {
	    Response response = OK();
		for(DefinitionDTO def: lstDefDTO) {
			response = genCompositeSearchMessage(graphId, def, startPosition, total);
		}
		return response;
	}
	
    private Response genCompositeSearchMessage(String graphId, DefinitionDTO def, Integer startPosition, Integer total) {
        Response response = OK();
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
	
	private Map<String, Object> getKafkaMessage(Node node) {
	    Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> transactionData = new HashMap<String, Object>();
        if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
                Map<String, Object> valueMap=new HashMap<String, Object>();
                valueMap.put("ov", null); // old value
                valueMap.put("nv", entry.getValue()); // new value
                propertyMap.put((String) entry.getKey(), valueMap);
            }
            transactionData.put(CompositeSearchParams.properties.name(), propertyMap);
        } else
            transactionData.put(CompositeSearchParams.properties.name(), new HashMap<String, Object>());
        transactionData.put(CompositeSearchParams.addedTags.name(), null == node.getTags() ? new ArrayList<String>() : node.getTags());
        transactionData.put(CompositeSearchParams.removedTags.name(), new ArrayList<String>());
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
	
	private String getLabel(Map<String, Object> metadata){
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
		//KafkaMessageProducer.sendMessage(messages);
		LogAsyncGraphEvent.pushMessageToLogger(messages);
		response.put(CompositeSearchParams.graphSyncStatus.name(), "Graph Sync Started Successfully!");
		response.setResponseCode(ResponseCode.OK);
		response.setParams(params);
		params.setStatus(CompositeSearchParams.success.name());
		response.setParams(params);
		return response;
	}
	
	@SuppressWarnings("unchecked")
    private List<Node> getNodes(String graphId, String objectType, int startPosition, int batchSize) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.setResultSize(batchSize);
        sc.setStartPosition(startPosition);
        Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        req.put(GraphDACParams.get_tags.name(), true);
        Response listRes = getResponse(req, LOGGER);
        if (checkError(listRes))
            throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: " + graphId);
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            return nodes;
        }
    }
	
    private Node getNode(String graphId, String identifier) {
        Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), identifier);
        Response listRes = getResponse(req, LOGGER);
        if (checkError(listRes))
            throw new ResourceNotFoundException(CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND.name(),
					"Object not found: " + identifier);
        else {
            Node node = (Node) listRes.get(GraphDACParams.node.name());
            return node;
        }
    }

}
