/**
 * 
 */
package com.ilimi.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.slugs.Slug;

import com.ilimi.common.Platform;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.framework.enums.FrameworkEnum;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

/**
 * @author pradyumna
 *
 */
public class BaseFrameworkManager extends BaseManager {

	protected static final String GRAPH_ID = (Platform.config.hasPath("graphId")) ? Platform.config.getString("graphId")
			: "domain";

	protected Response create(Map<String, Object> request, String objectType) {
		DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(objectType);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (!checkError(response))
				return response;
			else
				return response;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal Server Error", ResponseCode.SERVER_ERROR);
		}
	}

	protected Response read(String identifier, String objectType, String responseObject) {
		Response responseNode = getDataNode(GRAPH_ID, identifier);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + identifier);
		Response response = new Response();
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
		Map<String, Object> responseMap = ConvertGraphNode.convertGraphNode(node, GRAPH_ID, definition, null);
		response.put(responseObject, responseMap);
		response.setParams(getSucessStatus());
		return response;
	}

	protected Response update(String identifier, String objectType, Map<String, Object> map) {
		DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
		Response getNodeResponse = getDataNode(GRAPH_ID, identifier);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		try {
			Node domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
			domainObj.setGraphId(GRAPH_ID);
			domainObj.setIdentifier(identifier);
			domainObj.setObjectType(objectType);
			Response updateResponse = updateDataNode(domainObj);
			return updateResponse;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	protected Response search(Map<String, Object> map, String objectType, String responseObject, String identifier) {
		try {
			DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);

			SearchCriteria criteria = new SearchCriteria();
			criteria.setGraphId(GRAPH_ID);
			criteria.setObjectType(objectType);
			criteria.setNodeType("DATA_NODE");
			criteria.addMetadata(getMetadata(identifier, map, objectType));
			Response response = searchNodes(GRAPH_ID, criteria);
			List<Object> nodeList = new ArrayList<Object>();
			List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			for (Node node : nodes) {
				Map<String, Object> responseMap = ConvertGraphNode.convertGraphNode(node, GRAPH_ID, definition, null);
				nodeList.add(responseMap);
			}
			Response resp = new Response();
			resp.put("count", nodeList.size());
			resp.put(responseObject, nodeList);
			if (checkError(resp))
				return resp;
			else
				return resp;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	protected Response retire(String identifier, String objectType) {
		DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
		Response getNodeResponse = getDataNode(GRAPH_ID, identifier);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Node domainObj;
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("status", "Retired");
			domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
			domainObj.setGraphId(GRAPH_ID);
			domainObj.setIdentifier(identifier);
			domainObj.setObjectType(objectType);
			Response createResponse = updateDataNode(domainObj);
			return createResponse;

		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	/*
	 * Read Framework Definition from Neo4j
	 * 
	 * @param graphId
	 * 
	 * @param objectType
	 * 
	 */
	private DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	/*
	 * Create Data Node
	 * 
	 * @param Node node
	 * 
	 */
	private Response createDataNode(Node node) {
		PlatformLogger.log("Node :", node);
		Response response = new Response();
		if (null != node) {
			Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
			request.put(GraphDACParams.node.name(), node);

			PlatformLogger.log("Creating the Node ID: " + node.getIdentifier());
			response = getResponse(request);
		}
		return response;
	}

	/*
	 * Read Data Node
	 * 
	 * @param graphId
	 * 
	 * @param frameworkId
	 * 
	 */
	protected Response getDataNode(String graphId, String id) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		Response getNodeRes = getResponse(request);
		return getNodeRes;
	}

	/*
	 * 
	 * Search Data Node based on criteria.
	 * 
	 * @param grpahId
	 * 
	 * @param criteria
	 * 
	 */
	private Response searchNodes(String graphId, SearchCriteria criteria) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.node_id.name(), graphId);
		request.put(FrameworkEnum.search_criteria.name(), criteria);
		Response getNodeRes = getResponse(request);
		return getNodeRes;
	}

	/*
	 * Update Data Node
	 * 
	 * @param Node node
	 * 
	 */
	private Response updateDataNode(Node node) {
		PlatformLogger.log("[updateNode] | Node: ", node);
		Response response = new Response();
		if (null != node) {
			String channelId = node.getIdentifier();

			PlatformLogger.log("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			PlatformLogger.log("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq);

			response.put(FrameworkEnum.node_id.name(), channelId);
			PlatformLogger.log("Returning Node Update Response.");
		}
		return response;
	}



	public String generateIdentifier(String scopeId, String code) {
		String id = null;
		if (StringUtils.isNotBlank(scopeId)) {
			id = Slug.makeSlug(scopeId + "_" + code);
		}
		return id;
	}

	public Boolean validateScopeNode(String scopeId, String identifier) {
		Response responseNode = getDataNode(GRAPH_ID, scopeId);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		if (null != node) {
			List<Relation> inRelations = node.getInRelations();
			for (Relation rel : inRelations) {
				if (StringUtils.equalsIgnoreCase(identifier, rel.getStartNodeId()))
					return true;
				else {
					return false;
				}

			}
		}
		return false;
	}

	private MetadataCriterion getMetadata(String scopeId, Map<String, Object> map, String objectType) {
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = null;
		boolean defaultSearch = true;

		if ((null != map) && !map.isEmpty()) {
			for (String key : map.keySet()) {
				if (StringUtils.equalsIgnoreCase(key, FrameworkEnum.status.name())
						&& StringUtils.isNotBlank((String) map.get(key))) {
					defaultSearch = false;
					filter = new Filter(FrameworkEnum.status.name(), SearchConditions.OP_IN, map.get(key));
					filters.add(filter);
				}
				if (StringUtils.isNotBlank((String) map.get(key))) {
					filter = new Filter(key, SearchConditions.OP_IN, map.get(key));
					filters.add(filter);
				}
			}
		}

		if (defaultSearch) {
			filter = new Filter(FrameworkEnum.status.name(), SearchConditions.OP_IN, FrameworkEnum.Live.name());
			filters.add(filter);
		}

		if (StringUtils.isNotBlank(scopeId)) {
			for (String identifier : getChildren(scopeId, objectType)) {
				filter = new Filter(FrameworkEnum.identifier.name(), SearchConditions.OP_IN, identifier);
				filters.add(filter);
			}
		}
		return MetadataCriterion.create(filters);

	}

	/**
	 * @param scopeId
	 * @return
	 */
	private List<String> getChildren(String scopeId, String objectType) {
		Response getNodeResponse = getDataNode(GRAPH_ID, scopeId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		List<String> identifiers = new ArrayList<String>();
		for (Relation relation : graphNode.getOutRelations()) {
			if (StringUtils.equalsIgnoreCase(objectType, relation.getEndNodeObjectType()))
				identifiers.add(relation.getEndNodeId());
		}

		return identifiers;
	}
	
	/**
	 * validate channel Node
	 * 
	 * @param channelId
	 * 
	 * @author gauraw
	 * 
	 * */
	
	protected boolean validateChannel(String channelId){
		boolean isValidChannel=false;
		
		Response responseNode = getDataNode(GRAPH_ID, channelId);
		if (!checkError(responseNode)){
			isValidChannel=true;
		}
		return isValidChannel;
	}

	public void setRelations(String scopeId, Map<String, Object> request) {
		try {
			Response responseNode = getDataNode(GRAPH_ID, scopeId);
			Node dataNode = (Node) responseNode.get(GraphDACParams.node.name());
			String objectType = dataNode.getObjectType();
			List<Map<String, Object>> relationList = new ArrayList<Map<String, Object>>();
			Map<String, Object> relationMap = new HashMap<String, Object>();
			relationMap.put("identifier", scopeId);
			relationMap.put("relation", "hasSequenceMember");
			relationList.add(relationMap);
			request.put(StringUtils.lowerCase(objectType), relationList);
		} catch (Exception e) {
			throw new ServerException("SERVER_ERROR", "Something went wrong while setting inRelations", e);
		}
	}
}
