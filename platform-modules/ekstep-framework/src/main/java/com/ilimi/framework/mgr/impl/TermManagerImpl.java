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
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.mgr.ConvertToGraphNode;
import com.ilimi.framework.mgr.ITermManager;
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

		String id = getTermIdentifier(categoryId, (String) request.get("label"), frameworkId);
		if (null != id)
			request.put("identifier", id);
		else
			throw new ServerException("ERR_SERVER_ERROR", "Unable to create TermId", ResponseCode.SERVER_ERROR);

		Relation inRelation = new Relation(categoryId, "hasSequenceMember", null);
		List<Relation> inRelations = new ArrayList<Relation>();
		inRelations.add(inRelation);

		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(TERM_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			node.setInRelations(inRelations);
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

		String id = getTermIdentifier(categoryId, (String) request.get("label"), null);
		if (null != id)
			request.put("identifier", id);
		else
			throw new ServerException("ERR_SERVER_ERROR", "Unable to create TermId", ResponseCode.SERVER_ERROR);

		Relation inRelation = new Relation(categoryId, "hasSequenceMember", null);
		List<Relation> inRelations = new ArrayList<Relation>();
		inRelations.add(inRelation);

		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		try {
			Node node = ConvertToGraphNode.convertToGraphNode(request, definition, null);
			node.setObjectType(TERM_OBJECT_TYPE);
			node.setGraphId(GRAPH_ID);
			node.setInRelations(inRelations);
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
		if (map.containsKey("label")) {
			return ERROR("ERR_SERVER_ERROR", "Term Label cannot be updated", ResponseCode.SERVER_ERROR);
		}

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
	public Response searchTerms(String categoryId, Map<String, Object> map) {
		try {
			DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
			List<Filter> filters = new ArrayList<Filter>();
			Filter filter = new Filter("status", SearchConditions.OP_IN, "Live");
			filters.add(filter);

			if ((null != map) && !map.isEmpty()) {
				for (String key : map.keySet()) {
					if (StringUtils.isNotBlank((String) map.get(key))) {
						filter = new Filter(key, SearchConditions.OP_IN, map.get(key));
						filters.add(filter);
					}
				}
			}
			MetadataCriterion metadata = MetadataCriterion.create(filters);
			SearchCriteria criteria = new SearchCriteria();
			criteria.setGraphId(GRAPH_ID);
			criteria.setObjectType(TERM_OBJECT_TYPE);
			criteria.setNodeType("DATA_NODE");
			criteria.addMetadata(metadata);
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

	/* (non-Javadoc)
	 * @see com.ilimi.framework.mgr.ITermManager#retireTerm(java.lang.String, java.lang.String)
	 */
	@Override
	public Response retireTerm(String categoryId, String termId) {
		Response createResponse = null;
		boolean checkError = false;

		DefinitionDTO definition = getDefinition(GRAPH_ID, TERM_OBJECT_TYPE);
		Response getNodeResponse = getDataNode(GRAPH_ID, termId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		Node domainObj;
		try {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("status", "Retire");
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

	private Response getDataNode(String graphId, String id) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		Response getNodeRes = getResponse(request);
		return getNodeRes;
	}

	private Response searchNodes(String graphId, SearchCriteria criteria) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.node_id.name(), graphId);
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

	/**
	 * @param categoryId
	 * @param object
	 * @param object2
	 * @return
	 */
	private String getTermIdentifier(String categoryId, String label, String frameworkId) {
		String id = null;
		if (StringUtils.isNotBlank(categoryId) && StringUtils.isNotBlank(label)) {
			if (null != frameworkId)
				id = Slug.makeSlug(frameworkId + "_" + categoryId + "_" + label);
			else
				id = Slug.makeSlug(categoryId + "_" + label);
		}
		return id;
	}

	public Boolean validateRequest(String scope, String categoryId) {
		if (StringUtils.isNotBlank(scope) && StringUtils.isNotBlank(categoryId)) {
			Response categoryResp = getDataNode(GRAPH_ID, categoryId);
			if (checkError(categoryResp)) {
				return false;
			} else {
				Node node = (Node) categoryResp.get(GraphDACParams.node.name());
				if (StringUtils.equalsIgnoreCase(categoryId, node.getIdentifier())) {
					List<Relation> inRelation = node.getInRelations();
					if (!inRelation.isEmpty()) {
						for (Relation relation : inRelation) {
							if (StringUtils.equalsIgnoreCase(scope, relation.getStartNodeId()))
								return true;
						}
					}
				}
			}
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Required fields missing...");
		}
		return false;
	}

	public Boolean validateCategoryId(String categoryId) {
		if (StringUtils.isNotBlank(categoryId)) {
			Response categoryResp = getDataNode(GRAPH_ID, categoryId);
			if (checkError(categoryResp)) {
				return false;
			} else {
				Node node = (Node) categoryResp.get(GraphDACParams.node.name());
				if (StringUtils.equalsIgnoreCase(categoryId, node.getIdentifier())) {
					return true;
				}
			}
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Required fields missing...");
		}
		return false;
	}

}
