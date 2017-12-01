package com.ilimi.framework.mgr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.framework.enums.FrameworkEnum;
import com.ilimi.framework.mgr.IFrameworkManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
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

	/*
	 * create framework
	 * 
	 * @param Map request
	 * 
	 */
	@Override
	public Response createFramework(Map<String, Object> request) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_FRMAEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		DefinitionDTO definition = getDefinition(GRAPH_ID, FRAMEWORK_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(FRAMEWORK_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (!checkError(response))
				return response;
			else
				return response;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal Server Error (Create Framework API)", ResponseCode.SERVER_ERROR);
		}
	}

	/*
	 * Read framework by Id
	 * 
	 * @param graphId
	 * 
	 * @param frameworkId
	 * 
	 */
	@Override
	public Response readFramework(String graphId, String frameworkId) throws Exception {
		Response responseNode = getDataNode(graphId, frameworkId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : "+frameworkId);
		Response response = new Response();
		Node framework = (Node) responseNode.get(GraphDACParams.node.name());
		DefinitionDTO definition = getDefinition(GRAPH_ID, FRAMEWORK_OBJECT_TYPE);
		Map<String, Object> frameworkMap = ConvertGraphNode.convertGraphNode(framework, GRAPH_ID, definition, null);
		PlatformLogger.log("Framework Node Found : ", framework);
		response.put(FrameworkEnum.framework.name(), frameworkMap);
		response.setParams(getSucessStatus());
		return response;
	}

	/*
	 * Update Framework Details
	 * 
	 * @param frameworkId
	 * 
	 * @param Map<String,Object> map
	 * 
	 */
	@Override
	public Response updateFramework(String frameworkId, String channelId, Map<String, Object> map) throws Exception {
		Response updateResponse = null;
		boolean checkError = false;
		DefinitionDTO definition = getDefinition(GRAPH_ID, FRAMEWORK_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, frameworkId);
		if (checkError(getNodeResponse))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : "+frameworkId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());

		String owner = (String) graphNode.getMetadata().get("owner");

		if (!(channelId.equalsIgnoreCase(owner))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Owner Information Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}

		Node nodeObj;
		try {
			nodeObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
			nodeObj.setGraphId(GRAPH_ID);
			nodeObj.setIdentifier(frameworkId);
			nodeObj.setObjectType(FRAMEWORK_OBJECT_TYPE);
			updateResponse = updateDataNode(nodeObj);
			checkError = checkError(updateResponse);
			if (checkError)
				return updateResponse;
			else
				return updateResponse;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Internal Server Error (Update Framework API)",
					ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	/*
	 * Read list of all framework based on criteria
	 * 
	 * @param Map<String,Object> map
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Response listFramework(Map<String, Object> map) throws Exception {
		if (map == null)
			return ERROR("ERR_INVALID_SEARCH_REQUEST", "Invalid Search Request", ResponseCode.CLIENT_ERROR);

		try {
			DefinitionDTO definition = getDefinition(GRAPH_ID, FRAMEWORK_OBJECT_TYPE);
			SearchCriteria criteria = new SearchCriteria();
			criteria.setGraphId(GRAPH_ID);
			criteria.setObjectType(FRAMEWORK_OBJECT_TYPE);
			criteria.setNodeType("DATA_NODE");

			List<Filter> filters = new ArrayList<Filter>();
			Filter filter;

			if (!(map.containsKey(FrameworkEnum.status.name()))
					|| ((String) map.get(FrameworkEnum.status.name())).isEmpty()) {
				filter = new Filter("status", SearchConditions.OP_IN, FrameworkEnum.Live.name());
				filters.add(filter);
			}
			if (!map.isEmpty()) {
				for (String critKey : map.keySet()) {
					String critVal = (String) map.get(critKey);
					if(!StringUtils.isBlank(critVal)){
						filter = new Filter(critKey, SearchConditions.OP_IN, critVal);
						filters.add(filter);
					}
				}
			}

			MetadataCriterion metadata = MetadataCriterion.create(filters);
			List<MetadataCriterion> metadataList = new ArrayList<MetadataCriterion>();
			metadataList.add(metadata);
			criteria.setMetadata(metadataList);

			Response response = searchNodes(GRAPH_ID, criteria);
			List<Object> frameworkList = new ArrayList<Object>();
			List<Node> frameworkNodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			for (Node framework : frameworkNodes) {
				Map<String, Object> frameworkMap = ConvertGraphNode.convertGraphNode(framework, GRAPH_ID, definition,
						null);
				frameworkList.add(frameworkMap);
			}
			Response resp = new Response();
			resp.put("count", frameworkList.size());
			resp.put("frameworks", frameworkList);
			if (checkError(resp))
				return resp;
			else
				return resp;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal Server Error (ListFramework API)", ResponseCode.SERVER_ERROR,
					e.getMessage(), null);
		}
	}

	/*
	 * Retire Framework - will update the status From "Live" to "Retire"
	 * 
	 * @param frameworkId
	 * 
	 */

	@Override
	public Response retireFramework(String frameworkId, String channelId) throws Exception {
		Response response = null;
		boolean checkError = false;
		Response getNodeResponse = getDataNode(GRAPH_ID, frameworkId);
		if (checkError(getNodeResponse))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : "+frameworkId);
		Node frameworkNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		
		String owner = (String) frameworkNode.getMetadata().get("owner");

		if (!(channelId.equalsIgnoreCase(owner))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Owner Information Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}
		
		Node frameworkObj;
		try {
			frameworkObj = new Node(frameworkNode.getIdentifier(), frameworkNode.getNodeType(),
					frameworkNode.getObjectType());

			frameworkObj.setMetadata(frameworkNode.getMetadata());
			frameworkObj.setGraphId(frameworkNode.getGraphId());
			frameworkObj.getMetadata().put(FrameworkEnum.status.name(), FrameworkEnum.Retire.name());
			response = updateDataNode(frameworkObj);
			checkError = checkError(response);
			if (checkError)
				return response;
			else
				return response;
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal Server Error (Retire Framework API)", ResponseCode.SERVER_ERROR,
					e.getMessage(), null);
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
	private Response getDataNode(String graphId, String id) {
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

}