package com.ilimi.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.slugs.Slug;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.neo4j.driver.v1.exceptions.ClientException;
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
import com.ilimi.framework.mgr.ICategoryInstanceManager;
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
 * This is the entry point for all CRUD operations related to category Instance API.
 * 
 * @author Rashmi
 *
 */
@Component
public class CategoryInstanceManagerImpl extends BaseManager implements ICategoryInstanceManager {

	private static final String CATEGORY_INSTANCE_OBJECT_TYPE = "CategoryInstance";

	private static final String GRAPH_ID = "domain";
	
	@Override
	public Response createCategoryInstance(String identifier, Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get("code") || StringUtils.isBlank((String)request.get("code")))
			return ERROR("ERR_CATEGORY_INSTANCE_CODE_REQUIRED", "Unique code is mandatory for categoryInstance", ResponseCode.CLIENT_ERROR);
		Response responseNode = getDataNode(GRAPH_ID, identifier);
		Node dataNode = (Node) responseNode.get(GraphDACParams.node.name());
		String objectType = dataNode.getObjectType();
		request = setMetadata(objectType, identifier, request);
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_INSTANCE_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(CATEGORY_INSTANCE_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			Response response = createDataNode(node);
			if (checkError(response))
				return response;
			else
				return response;
		} catch(Exception e){
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR);
		}
	}

	@Override
	public Response readCategoryInstance(String identifier, String categoryInstanceId) {
		Response responseNode = getDataNode(GRAPH_ID, categoryInstanceId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CATEGORY_INSTANCE_NOT_FOUND.name(),
					"Content not found with id: " + categoryInstanceId);
		Response response = new Response();
		Node category = (Node) responseNode.get(GraphDACParams.node.name());
		Boolean isValid = validateScopeNode(identifier, category);
		if(!isValid) {
			throw new ClientException(ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/" + ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(), "Given channel/framework is not related to given category");
		}
		else {
			DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_INSTANCE_OBJECT_TYPE);
			Map<String, Object> categoryMap = ConvertGraphNode.convertGraphNode(category, GRAPH_ID, definition, null);
			PlatformLogger.log("Got Node: ", category);
			response.put(CategoryEnum.categoryInstance.name(), categoryMap);
			response.setParams(getSucessStatus());
			return response;
		}
	}

	@Override
	public Response updateCategoryInstance(String identifier, String categoryInstanceId, Map<String, Object> map) {
		boolean checkError = false;
		if (null == map)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_INSTANCE_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, categoryInstanceId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Boolean isValid = validateScopeNode(identifier, graphNode);
		if(!isValid) {
			throw new ClientException(ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/" + ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(), "Given channel/framework is not related to given category");
		}
		else {
			Node domainObj;
			try {
				domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
				domainObj.setGraphId(GRAPH_ID);
				domainObj.setIdentifier(categoryInstanceId);
				domainObj.setObjectType(CATEGORY_INSTANCE_OBJECT_TYPE);
				Response updateResponse = updateDataNode(domainObj);
				checkError = checkError(updateResponse);
				if (checkError)
					return updateResponse;
				else
					return updateResponse;
			} catch (Exception e) {
				return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response searchCategoryInstance(String identifier, Map<String, Object> map) {
		try {
			DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_INSTANCE_OBJECT_TYPE);
			SearchCriteria criteria = new SearchCriteria();
			criteria.setGraphId(GRAPH_ID);
			criteria.setObjectType(CATEGORY_INSTANCE_OBJECT_TYPE);
			criteria.setNodeType("DATA_NODE");
			criteria.addMetadata(getMetadata(identifier, map));
			 Response response = searchNodes(GRAPH_ID, criteria);
			 List<Object> categoryList = new ArrayList<Object>();
			 List<Node> categoryNodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			 for(Node category : categoryNodes){
				Map<String, Object> categoryMap = ConvertGraphNode.convertGraphNode(category, GRAPH_ID, definition, null);
					categoryList.add(categoryMap);
			 }
			 Response resp = new Response();
			 resp.put("count", categoryList.size());
			 resp.put("categoryInstances", categoryList);
			 if(checkError(resp))
				return resp;
			 else
				return resp;
		 } catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}
	
	@Override
	public Response retireCategoryInstance(String identifier, String categoryInstanceId) {
		boolean checkError = false;
		DefinitionDTO definition = getDefinition(GRAPH_ID, CATEGORY_INSTANCE_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, categoryInstanceId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Node domainObj;
		try {
			Boolean isValid = validateScopeNode(identifier, graphNode);
			if(!isValid) {
				throw new ClientException(ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/" + ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(), "Given channel/framework is not related to given category");
			}
			else {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("status", "Retired");
				domainObj = ConvertToGraphNode.convertToGraphNode(map, definition, graphNode);
				domainObj.setGraphId(GRAPH_ID);
				domainObj.setIdentifier(categoryInstanceId);
				domainObj.setObjectType(CATEGORY_INSTANCE_OBJECT_TYPE);
				Response createResponse = updateDataNode(domainObj);
				checkError = checkError(createResponse);
				if (checkError)
					return createResponse;
				else
					return createResponse;
			}
		} catch (Exception e) {
			return ERROR("ERR_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}
	
	public Boolean validateScopeNode(String identifier, Node node) {
		if(null != node) {
			List<Relation> inRelations = node.getInRelations();
			for(Relation rel : inRelations) {
				if(StringUtils.equalsIgnoreCase(identifier, rel.getStartNodeId()))
					return true;
				else {
					return false;
				}
				
			}
		}
		return false;
	}
	
	private Map<String, Object> setMetadata(String objectType, String identifier, Map<String, Object> request) {
		List<Map<String,Object>> relationList = new ArrayList<Map<String,Object>>();
		Map<String,Object> relationMap = new HashMap<String,Object>();
		relationMap.put("identifier", identifier);
		relationMap.put("relation", "hasSequenceMember");
		if(StringUtils.equalsIgnoreCase(objectType, "Channel")) {
			request = setRequestData(objectType, "channel", identifier, request, relationMap,relationList);
		}
		if(StringUtils.equalsIgnoreCase(objectType, "Framework")) {
			request = setRequestData(objectType, "framework", identifier, request, relationMap,relationList);
		}
		return request;
	}

	private Map<String,Object> setRequestData(String objectType, String value, String identifier, Map<String,Object> request, Map<String, Object> relationMap, List<Map<String, Object>> relationList){
		relationList.add(relationMap);
		request.put(value, relationList);
		String code = (String) request.get("code");
		request.put("identifier", Slug.makeSlug(identifier + "_" + code));
		return request;
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
	
	/**
	 * @param categoryId
	 * @param map
	 * @return
	 */
	private MetadataCriterion getMetadata(String identifier, Map<String, Object> map) {
		List<Filter> filters = new ArrayList<Filter>();
		Filter filter = new Filter(CategoryEnum.status.name(), SearchConditions.OP_IN, CategoryEnum.Live.name());
		filters.add(filter);

		if ((null != map) && !map.isEmpty()) {
			for (String key : map.keySet()) {
				if (StringUtils.isNotBlank((String) map.get(key))) {
					filter = new Filter(key, SearchConditions.OP_IN, map.get(key));
					filters.add(filter);
				}
			}
		}

		for (String id : getChildren(identifier)) {
			filter = new Filter(CategoryEnum.identifier.name(), SearchConditions.OP_IN, id);
			filters.add(filter);
		}

		return MetadataCriterion.create(filters);

	}

	/**
	 * @param categoryId
	 * @return
	 */
	private List<String> getChildren(String identifier) {
		Response getNodeResponse = getDataNode(GRAPH_ID, identifier);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		List<String> identifiers = new ArrayList<String>();
		for (Relation relation : graphNode.getOutRelations()) {
			if (StringUtils.equalsIgnoreCase(CATEGORY_INSTANCE_OBJECT_TYPE, relation.getEndNodeObjectType()))
				identifiers.add(relation.getEndNodeId());
		}

		return identifiers;
	}

	private Response updateDataNode(Node node) {
		PlatformLogger.log("[updateNode] | Node: ", node);
		Response response = new Response();
		if (null != node) {
			String categoryId = node.getIdentifier();
			PlatformLogger.log("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
			PlatformLogger.log("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq);

			response.put(CategoryEnum.node_id.name(), categoryId);
			PlatformLogger.log("Returning Node Update Response.");
		}
		return response;
	}
	
	public boolean validateScopeId(String identifier) {
		if (StringUtils.isNotBlank(identifier)) {
			Response response = getDataNode(GRAPH_ID, identifier);
			if (checkError(response)) {
				return false;
			} else {
				Node node = (Node) response.get(GraphDACParams.node.name());
				if (StringUtils.equalsIgnoreCase(identifier, node.getIdentifier())) {
					return true;
				}
			}
		} else {
			throw new ClientException("ERR_INVALID_CHANNEL_ID/ERR_INVALID_FRAMEWORK_ID", "Required fields missing...");
		}
		return false;
	}
}