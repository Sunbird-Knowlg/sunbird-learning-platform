package com.ilimi.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.framework.enums.CategoryEnum;
import com.ilimi.framework.mgr.ICategoryManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

/**
 * This is the entry point for all CRUD operations related to category API.
 * 
 * @author Rashmi
 *
 */
@Component
public class CategoryManagerImpl extends BaseManager implements ICategoryManager {

	private static final String CATEGORY_OBJECT_TYPE = "Category";

	private static final String GRAPH_ID = "domain";
	
	@Override
	public Response createCategory(Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(CATEGORY_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (checkError(response))
				return response;
			else
				return response;
		}catch(Exception e){
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR);
		}
	}

	@Override
	public Response readCategory(String channelId) {
		Response responseNode = getDataNode(GRAPH_ID, channelId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CATEGORY_NOT_FOUND.name(),
					"Content not found with id: " + channelId);
		Response response = new Response();
		Node channel = (Node) responseNode.get(GraphDACParams.node.name());
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_OBJECT_TYPE);
		Map<String, Object> channelMap = ConvertGraphNode.convertGraphNode(channel, GRAPH_ID, definition, null);
		PlatformLogger.log("Got Node: ", channel);
		response.put(CategoryEnum.channel.name(), channelMap);
		response.setParams(getSucessStatus());
		return response;
	}

	@Override
	public Response updateCategory(String categoryId, Map<String, Object> map) {
		Response createResponse = null;
		boolean checkError = false;
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, categoryId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Node domainObj;
		try {
			domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
			domainObj.setGraphId(GRAPH_ID);
			domainObj.setIdentifier(categoryId);
			domainObj.setObjectType(CATEGORY_OBJECT_TYPE);
			createResponse = updateDataNode(domainObj);
			checkError = checkError(createResponse);
			if (checkError)
				return createResponse;
			else
				return createResponse;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response searchCategory(Map<String, Object> map) {
		try {
			DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_OBJECT_TYPE);
			SearchCriteria criteria = new SearchCriteria();
			criteria.setGraphId(GRAPH_ID);
			criteria.setObjectType(CATEGORY_OBJECT_TYPE);
			criteria.setNodeType("DATA_NODE");
			Response response = searchNodes(GRAPH_ID, criteria);
			List<Object> categoryList = new ArrayList<Object>();
			List<Node> category = (List<Node>) response.get(GraphDACParams.node_list.name());
			for(Node channel : category){
				Map<String, Object> channelMap = ConvertGraphNode.convertGraphNode(channel, GRAPH_ID, definition, null);
					categoryList.add(channelMap);
			}
			Response resp = new Response();
			resp.put("count", categoryList.size());
			resp.put("categorys", categoryList);
			if(checkError(resp))
				return resp;
			else
				return resp;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}
	
	@Override
	public Response retireCategory(String categoryId) {
		Response createResponse = null;
		boolean checkError = false;
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, categoryId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Node domainObj;
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("status", "retire");
			domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
			updateDataNode(domainObj);
			checkError = checkError(createResponse);
			if (checkError)
				return createResponse;
			else
				return createResponse;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}
	
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
	
	private Response getDataNode(String taxonomyId, String id) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		Response getNodeRes = getResponse(request);
		return getNodeRes;
	}
	
	private Response searchNodes(String taxonomyId, SearchCriteria criteria) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.node_id.name(), taxonomyId);
		request.put("search_criteria", criteria);
		Response getNodeRes = getResponse(request);
		return getNodeRes;
	}

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

			response.put(CategoryEnum.node_id.name(), channelId);
			PlatformLogger.log("Returning Node Update Response.");
		}
		return response;
	}

}