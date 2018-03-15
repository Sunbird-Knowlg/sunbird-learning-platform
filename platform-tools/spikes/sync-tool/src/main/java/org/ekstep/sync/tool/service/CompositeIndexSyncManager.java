package org.ekstep.sync.tool.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.CompositeSearchParams;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class CompositeIndexSyncManager extends BaseManager{

	public void syncNode(String graphId, String identifier){
		System.out.println("identifier*: " + identifier);
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID",
					"Graph Id is blank.");
		if (StringUtils.isBlank(identifier))
			throw new ClientException("BLANK_IDENTIFIER",
					"Identifier is blank.");
		//TelemetryManager.log("Composite index sync : " + graphId + " | Identifier: " + identifier);
		System.out.println("identifier**: " + identifier);
		Node node = getNode(graphId, identifier);
		System.out.println("identifier***: " + identifier);
		if (null != node) {
			System.out.println("identifier****: " + identifier + "   node: " + node.getMetadata());
			Map<String, Object> kafkaMessage = getKafkaMessage(node);
			
			System.out.println("kafkaMessage: " + kafkaMessage);
		} else {
			throw new ResourceNotFoundException("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND", "Object not found: " + identifier);
		}
	}
	
	private Node getNode(String graphId, String identifier) {
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), identifier);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException("OBJECT_NOT_FOUND", "Object not found: " + identifier);
		else {
			Node node = (Node) listRes.get(GraphDACParams.node.name());
			return node;
		}
	}
	public Response getResponse(Request request) {
		ActorRef router = RequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}
	
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
                Map<String, Object> relMap = new HashMap<String, Object>();
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
                Map<String, Object> relMap = new HashMap<String, Object>();
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
	
	
	
}
