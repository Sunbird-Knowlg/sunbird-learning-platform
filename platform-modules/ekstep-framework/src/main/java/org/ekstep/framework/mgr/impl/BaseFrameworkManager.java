/**
 * 
 */
package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.common.slugs.Slug;
import org.ekstep.framework.enums.FrameworkEnum;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.cache.CategoryCache;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;

/**
 * @author pradyumna
 *
 */
public class BaseFrameworkManager extends BaseManager {

	protected static final String GRAPH_ID = (Platform.config.hasPath("graphId")) ? Platform.config.getString("graphId")
			: "domain";

	private ObjectMapper mapper = new ObjectMapper();
	private ElasticSearchUtil esUtil = new ElasticSearchUtil();

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
	protected DefinitionDTO getDefinition(String graphId, String objectType) {
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
		Response response = new Response();
		if (null != node) {
			Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
			request.put(GraphDACParams.node.name(), node);

			TelemetryManager.log("Creating the Node ID: " + node.getIdentifier());
			response = getResponse(request);
		}
		return response;
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
		Response response = new Response();
		if (null != node) {
			String channelId = node.getIdentifier();

			TelemetryManager.log("Getting Update Node Request For Node ID: " + node.getIdentifier());
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			TelemetryManager.log("Updating the Node ID: " + node.getIdentifier());
			response = getResponse(updateReq);

			response.put(FrameworkEnum.node_id.name(), channelId);
			TelemetryManager.log("Returning Node Update Response.");
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
			if (null != inRelations && !inRelations.isEmpty()) {
				for (Relation rel : inRelations) {
					if (StringUtils.equalsIgnoreCase(identifier, rel.getStartNodeId()))
						return true;
				}
			}
		}
		return false;
	}

	private MetadataCriterion getMetadata(String scopeId, Map<String, Object> map, String objectType) {
		List<Filter> filters = new ArrayList<Filter>();
		List<MetadataCriterion> metadataCriterion = new ArrayList<>();
		Filter filter = null;
		boolean defaultSearch = true;

		if ((null != map) && !map.isEmpty()) {
			for (String key : map.keySet()) {
				if (StringUtils.isNotBlank((String) map.get(key))) {
					if (StringUtils.equalsIgnoreCase(key, FrameworkEnum.status.name()))
						defaultSearch = false;
					filter = new Filter(key.toLowerCase(), SearchConditions.OP_IN, map.get(key));
					filters.add(filter);
				}
			}
		}

		if (defaultSearch) {
			filter = new Filter(FrameworkEnum.status.name(), SearchConditions.OP_IN, FrameworkEnum.Live.name());
			filters.add(filter);
		}

		if (StringUtils.isNotBlank(scopeId)) {
			List<Filter> identifierFilter = new ArrayList<>();
			for (String identifier : getChildren(scopeId, objectType)) {
				filter = new Filter(FrameworkEnum.identifier.name(), SearchConditions.OP_EQUAL, identifier);
				identifierFilter.add(filter);
			}

			metadataCriterion.add(MetadataCriterion.create(identifierFilter, SearchConditions.LOGICAL_OR));
		}
		return MetadataCriterion.create(filters, metadataCriterion, SearchConditions.LOGICAL_AND);
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
	 */

	protected boolean validateObject(String objectId) {
		boolean isValidObject = false;

		Response responseNode = getDataNode(GRAPH_ID, objectId);
		if (!checkError(responseNode)) {
			isValidObject = true;
		}
		return isValidObject;
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
			/**
			 * TODO: Get the relationTitle from definition or from the calling method. For
			 * now it is hardcoded as objectType suffixed with "s"
			 */
			switch (objectType.toLowerCase()) {
			case "framework":
				request.put("frameworks", relationList);
				break;
			case "category":
				request.put("categories", relationList);
				break;
			case "categoryinstance":
				request.put("categories", relationList);
				break;
			case "channel":
				request.put("channels", relationList);
				break;
			case "term":
				request.put("terms", relationList);
				break;
			}
		} catch (Exception e) {
			throw new ServerException("SERVER_ERROR", "Something went wrong while setting inRelations", e);
		}
	}
	
	/*public void setRelationsCopy(String parnetObjectType, String childObjectType, String scopeId, Map<String, Object> request) {
		DefinitionDTO parentDefinition =  getDefinition(GRAPH_ID, parnetObjectType);
		DefinitionDTO childDefinition = getDefinition(GRAPH_ID, childObjectType);
		if(null != parentDefinition && null != childDefinition) {
			List<RelationDefinition> parentOutRelations = parentDefinition.getOutRelations();
			List<RelationDefinition> childInRelations = childDefinition.getInRelations();
			if(null != parentOutRelations && !parentOutRelations.isEmpty() && 
					null != childInRelations && !childInRelations.isEmpty()) {
				for(RelationDefinition parentOutRelation : parentOutRelations) {
					for(RelationDefinition childInRelation : childInRelations) {
						if(StringUtils.equalsIgnoreCase(childInRelation.getRelationName(), parentOutRelation.getRelationName()) &&
								childInRelation.getObjectTypes().contains(parnetObjectType) &&
								parentOutRelation.getObjectTypes().contains(childObjectType)) {
							List<Map<String, Object>> relationList = new ArrayList<Map<String, Object>>();
							Map<String, Object> relationMap = new HashMap<String, Object>();
							relationMap.put("identifier", scopeId);
							relationList.add(relationMap);
							
							Response responseNode = getDataNode(GRAPH_ID, scopeId);
							Node dataNode = (Node) responseNode.get(GraphDACParams.node.name());
							if(StringUtils.equalsIgnoreCase(dataNode.getObjectType(), parnetObjectType))
								request.put(childInRelation.getTitle(), relationList);
							else if (StringUtils.equalsIgnoreCase(dataNode.getObjectType(), childObjectType))
								request.put(parentOutRelation.getTitle(), relationList);
							
							return;
						}
					}
					
				}
			}
		}
		
	}*/

	/**
	 * This is the method to get the full hierarchy of a tree. It assumes that
	 * status is present in all the methods and return only Live nodes.
	 * 
	 * @param id
	 * @param index
	 * @param includeMetadata
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getHierarchy(String id, int index, boolean includeMetadata) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		Response responseNode = getDataNode(GRAPH_ID, id);
	    if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + id, ResponseCode.RESOURCE_NOT_FOUND);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		
		Map<String, Object> metadata = node.getMetadata();
		String status = (String) metadata.get("status");
		if (StringUtils.equalsIgnoreCase("Live", status)) {
			String objectType = node.getObjectType();
			DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
			if (includeMetadata) {
				String[] fields = getFields(definition);
				if (fields != null) {
					for (String field : fields) {
						data.put(field, metadata.get(field));
					}
				} else {
					data.putAll(node.getMetadata());
				}
				data.put("identifier", node.getIdentifier());
				if (index > 0)
					data.put("index", index);
			}
			Map<String, String> inRelDefMap = new HashMap<>();
			Map<String, String> outRelDefMap = new HashMap<>();
			List<String> sortKeys = new ArrayList<String>();
			ConvertGraphNode.getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
			List<Relation> outRelations = node.getOutRelations();
			if (null != outRelations && !outRelations.isEmpty()) {
				for (Relation relation : outRelations) {
					String type = relation.getRelationType();
					String key = type + relation.getEndNodeObjectType();
					String title = outRelDefMap.get(key);
					List<Map<String, Object>> relData = (List<Map<String, Object>>) data.get(title);
					if (relData == null) {
						relData = new ArrayList<Map<String, Object>>();
						data.put(title, relData);
						if ("hasSequenceMember".equalsIgnoreCase(type))
							sortKeys.add(title);
					}
					Map<String, Object> relMeta = relation.getMetadata();
					int seqIndex = 0;
					if (relMeta != null) {
						Object indexObj = relMeta.get("IL_SEQUENCE_INDEX");
						if (indexObj != null)
							seqIndex = ((Long) indexObj).intValue();
					}
					Map<String, Object> childData = getHierarchy(relation.getEndNodeId(), seqIndex, true);
					if (!childData.isEmpty())
						relData.add(childData);
				}
			}
			for (String key : sortKeys) {
				List<Map<String, Object>> prop = (List<Map<String, Object>>) data.get(key);
				getSorted(prop);
			}
		}

		return data;
	}

	protected String[] getFields(DefinitionDTO definition) {
		Map<String, Object> meta = definition.getMetadata();
		return (String[]) meta.get("fields");
	}

	private void getSorted(List<Map<String, Object>> relObjects) {
		Collections.sort(relObjects, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				int o1Index = (int) o1.get("index");
				int o2Index = (int) o2.get("index");
				return o1Index - o2Index;
			}
		});
	}

	protected void generateFrameworkHierarchy(String objectId) throws Exception {
		Response responseNode = getDataNode(GRAPH_ID, objectId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + objectId);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		if (StringUtils.equalsIgnoreCase(node.getObjectType(), "Framework")) {
			pushFrameworkEvent(node);
		} else if (StringUtils.equalsIgnoreCase(node.getObjectType(), "CategoryInstance")) {
			List<Relation> inRelations = node.getInRelations();
			if (null != inRelations && !inRelations.isEmpty()) {
				for (Relation rel : inRelations) {
					if (StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), "Framework")
							&& StringUtils.equalsIgnoreCase(rel.getRelationType(), "hasSequenceMember")) {
						generateFrameworkHierarchy(rel.getStartNodeId());
					}
				}
			}
		} else if (StringUtils.equalsIgnoreCase(node.getObjectType(), "Term")) {
			List<Relation> inRelations = node.getInRelations();
			if (null != inRelations && !inRelations.isEmpty()) {
				for (Relation rel : inRelations) {
					if ((StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), "CategoryInstance") || StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), "Term"))
							&& StringUtils.equalsIgnoreCase(rel.getRelationType(), "hasSequenceMember")) {
						generateFrameworkHierarchy(rel.getStartNodeId());
					}
				}
			}
		}
	}

	protected void pushFrameworkEvent(Node node) throws Exception {
		Map<String, Object> frameworkDocument = new HashMap<>();
		Map<String, Object> frameworkHierarchy = getHierarchy(node.getIdentifier(), 0, false);
		CategoryCache.setFramework(node.getIdentifier(), frameworkHierarchy);

		frameworkDocument.put("fw_hierarchy", mapper.writeValueAsString(frameworkHierarchy));
		frameworkDocument.put("graph_id", GRAPH_ID);
		frameworkDocument.put("node_id", (int) node.getId());
		frameworkDocument.put("identifier", node.getIdentifier());
		frameworkDocument.put("objectType", node.getObjectType());
		frameworkDocument.put("nodeType", node.getNodeType());
		esUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, node.getIdentifier(),
				mapper.writeValueAsString(frameworkDocument));
	}

}
