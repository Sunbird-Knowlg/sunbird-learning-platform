/**
 * 
 */
package com.ilimi.framework.mgr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.framework.mgr.ITermManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

/**
 * @author pradyumna
 *
 */
@Component
public class TermManagerImpl extends BaseManager implements ITermManager {

	private static final String TERM_OBJECT_TYPE = "Term";

	private static final String GRAPH_ID = "domain";

	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.ITermManager#createTerm(java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public Response createTerm(String frameworkId, String categoryId, Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(TERM_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (checkError(response))
				return response;
			else
				return response;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR);
		}
	}

	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.ITermManager#createTerm(java.lang.String, java.util.Map)
	 */
	@Override
	public Response createTerm(String categoryId, Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(TERM_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (checkError(response))
				return response;
			else
				return response;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR);
		}
	}

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#readTerm(java.lang.String, java.lang.String)
	 */
	@Override
	public Response readTerm(String graphId, String termId) {
		Response responseNode = getDataNode(graphId, termId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_TERM_NOT_FOUND", "Content not found with id: " + termId);
		Response response = new Response();
		Node termNode = (Node) responseNode.get(GraphDACParams.node.name());
		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		Map<String, Object> termMap = ConvertGraphNode.convertGraphNode(termNode, graphId, definition, null);
		PlatformLogger.log("Got Node: ", termNode);
		response.put("term", termMap);
		response.setParams(getSucessStatus());
		return response;
	}

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#updateCategory(java.lang.String, java.util.Map)
	 */
	@Override
	public Response updateTerm(String categoryId, String termId, Map<String, Object> map) {
		Response createResponse = null;
		boolean checkError = false;
		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, termId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Node domainObj;
		try {
			domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
			domainObj.setGraphId(GRAPH_ID);
			domainObj.setIdentifier(termId);
			domainObj.setObjectType(TERM_OBJECT_TYPE);
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

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#searchTerms(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response searchTerms(String categoryId) {
		try {
			DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
			SearchCriteria criteria = new SearchCriteria();
			criteria.setGraphId(GRAPH_ID);
			criteria.setObjectType(TERM_OBJECT_TYPE);
			criteria.setNodeType("DATA_NODE");
			Response response = searchNodes(GRAPH_ID, criteria);
			List<Object> termList = new ArrayList<Object>();
			List<Node> terms = (List<Node>) response.get(GraphDACParams.node_list.name());
			for (Node term : terms) {
				Map<String, Object> channelMap = ConvertGraphNode.convertGraphNode(term, GRAPH_ID, definition, null);
				termList.add(channelMap);
			}
			Response resp = new Response();
			resp.put("count", termList.size());
			resp.put("terms", termList);
			if (checkError(resp))
				return resp;
			else
				return resp;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
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

			response.put("node_id", channelId);
			PlatformLogger.log("Returning Node Update Response.");
		}
		return response;
	}

}
