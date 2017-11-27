package com.ilimi.framework.mgr.impl;

import java.util.Map;

import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.framework.enums.CategoryEnum;
import com.ilimi.framework.mgr.IFrameworkManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

/**
 * The Class <code>FrameworkManagerImpl</code> is the implementation of
 * <code>IFrameworkManager</code> for all the operation including CRUD operation
 * and High Level Operations.
 * 
 * 
 * @author gauraw
 *
 */
@Component
public class FrameworkManagerImpl extends BaseManager implements IFrameworkManager {

	private static final String FRAMEWORK_OBJECT_TYPE = "Framework";

	private static final String GRAPH_ID = "domain";
	
	@Override
	public Response createFramework(Map<String, Object> request) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_FRMAEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return getStubbedRespone();
		
		/*DefinitionDTO definition = getDefinition(GRAPH_ID, FRAMEWORK_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(FRAMEWORK_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (!checkError(response))
				return response;
			else
				// check
				return response;
		}catch(Exception e){
			return ERROR("ERR_SERVER_ERROR", "Internal Server Error (Create Framework API)", ResponseCode.SERVER_ERROR);
		}*/
	}

	@Override
	public Response readFramework(String graphId, String frameworkId) throws Exception {
		Response responseNode = getDataNode(graphId, frameworkId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_FRAEWORK_NOT_FOUND",
					"Framework not found with id : " + frameworkId);
		Response response = new Response();
		return getStubbedRespone();
	}

	@Override
	public Response updateFramework(String frameworkId, Map<String, Object> map) throws Exception {
		return getStubbedRespone();
	}

	@Override
	public Response listFramework(Map<String, Object> map) throws Exception {
		return getStubbedRespone();
	}
	
	@Override
	public Response retireFramework(Map<String, Object> map) throws Exception {
		return getStubbedRespone();
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

	// Method to provide dummy response.
	private Response getStubbedRespone() {
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.setResponseCode(ResponseCode.OK);
		return response;
	}

	

}