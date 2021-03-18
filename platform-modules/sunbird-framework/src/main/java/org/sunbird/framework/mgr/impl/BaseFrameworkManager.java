/**
 * 
 */
package org.sunbird.framework.mgr.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import akka.pattern.Patterns;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.Slug;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.common.mgr.ConvertGraphNode;
import org.sunbird.common.mgr.ConvertToGraphNode;
import org.sunbird.common.router.RequestRouterPool;
import org.sunbird.framework.enums.FrameworkEnum;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.common.enums.LearningActorNames;
import org.sunbird.learning.framework.FrameworkHierarchyOperations;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * @author pradyumna
 *
 */
public class BaseFrameworkManager extends BaseManager {

	protected static final String GRAPH_ID = (Platform.config.hasPath("graphId")) ? Platform.config.getString("graphId")
			: "domain";
	private static final List<String> LANGUAGE_CODES = (Platform.config.hasPath("language.graph_ids"))
			? Platform.config.getStringList("language.graph_ids")
			: null;
	protected ObjectMapper mapper = new ObjectMapper();


	protected Response create(Map<String, Object> request, String objectType) {
		if (request.containsKey("translations"))
			validateTranslation(request);
		
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
		ConvertGraphNode.filterNodeRelationships(responseMap, definition);
		response.put(responseObject, responseMap);
		response.setParams(getSucessStatus());
		return response;
	}
	
	protected Response update(String identifier, String objectType, Map<String, Object> map) {
		if (map.containsKey("translations"))
			validateTranslation(map);

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
			resp.setParams(getSucessStatus());
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
	 *
	 * @param objectId
	 * @return boolean
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
			if(request.containsKey("index")){
				relationMap.put("index",request.get("index"));
			}
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
			default: break;
			}
		} catch (Exception e) {
			throw new ServerException("SERVER_ERROR", "Something went wrong while setting inRelations", e);
		}
	}

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
	protected Map<String, Object> getHierarchy(String id, int index, boolean includeMetadata, boolean includeRelations)
			throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		Response responseNode = getDataNode(GRAPH_ID, id);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + id,
					ResponseCode.RESOURCE_NOT_FOUND);
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
			if (includeRelations) {
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

						boolean getChildren = true;
						// TODO: This condition value should get from definition node.
						if ("associations".equalsIgnoreCase(title)) {
							getChildren = false;
						}
						Map<String, Object> childData = getHierarchy(relation.getEndNodeId(), seqIndex, true,
								getChildren);
						if (!childData.isEmpty())
							relData.add(childData);
					}
				}
				for (String key : sortKeys) {
					List<Map<String, Object>> prop = (List<Map<String, Object>>) data.get(key);
					getSorted(prop);
				}
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
		Request request = new Request();
		request.setManagerName(LearningActorNames.FRAMEWORK_HIERARCHY_ACTOR.name());
		request.setOperation(FrameworkHierarchyOperations.generateFrameworkHierarchy.name());
		request.put("identifier", objectId);
		makeLearningRequest(request);

	}

	private void makeLearningRequest(Request request) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			// TODO: Since we dont have actor implementation here, sending noSender as
			// AcoterRef to tell call. Need to improve this
			router.tell(request, ActorRef.noSender());

		} catch (Exception e) {
			TelemetryManager.error("Error! Something went wrong: " + e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void validateTranslation(Map<String, Object> request) {
		Map<String, Object> translations = (Map<String, Object>) request.get("translations");
		if (null == translations || translations.isEmpty())
			request.remove("translations");
		else {
			Set<String> codes = translations.keySet();
			List<String> result = codes.stream().filter(code -> !LANGUAGE_CODES.contains(code))
					.collect(Collectors.toList());
			if (!result.isEmpty())
				throw new ClientException("ERR_INVALID_LANGUAGE_CODE",
						"Please Provide Valid Language Code For translations. Valid Language Codes are : "
								+ LANGUAGE_CODES);
		}
	}

	protected Response getFrameworkHierarchy(String identifier) throws Exception {
		Request request = new Request();
		request.setManagerName(LearningActorNames.FRAMEWORK_HIERARCHY_ACTOR.name());
		request.setOperation(FrameworkHierarchyOperations.getFrameworkHierarchy.name());
		request.put("identifier", identifier);
		return makeActorRequest(request);
	}

	private Response makeActorRequest(Request request) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response response = (Response) obj;
				TelemetryManager.log("Response Params: " + response.getParams() + " | Code: "
						+ response.getResponseCode() + " | Result: " + response.getResult().keySet());
				return response;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Something went wrong while making actor call for get framework hierarchy : " + e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Something Went Wrong While Processing Your Request", e);
		}
	}

	/**
	 * @param responseMap
	 * @param returnCategories
	 */
	@SuppressWarnings("unchecked")
	protected void removeAssociations(Map<String, Object> responseMap, List<String> returnCategories) {
		((List<Map<String, Object>>) responseMap.get("categories")).forEach(category -> {
			removeTermAssociations((List<Map<String, Object>>) category.get("terms"), returnCategories);
		});
	}

	@SuppressWarnings("unchecked")
	protected void removeTermAssociations(List<Map<String, Object>> terms, List<String> returnCategories) {
		if (!CollectionUtils.isEmpty(terms)) {
			terms.forEach(term -> {
				if (!CollectionUtils.isEmpty((List<Map<String, Object>>) term.get("associations"))) {
					term.put("associations",
							((List<Map<String, Object>>) term.get("associations")).stream().filter(s -> s != null)
									.filter(p -> returnCategories.contains(p.get("category")))
									.collect(Collectors.toList()));
					if (CollectionUtils.isEmpty((List<Map<String, Object>>) term.get("associations")))
						term.remove("associations");

					removeTermAssociations((List<Map<String, Object>>) term.get("children"), returnCategories);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	protected Response copyHierarchy(String existingObjectId, String clonedObjectId, String existingFrameworkId,
									 String clonedFrameworkId, Map<String, Object> requestMap) throws Exception {
		Response responseNode = getDataNode(GRAPH_ID, existingObjectId);
		if (checkError(responseNode)) {
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + existingObjectId, ResponseCode.RESOURCE_NOT_FOUND);
		}
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		String objectType = node.getObjectType();
		DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);

		Map<String, Object> request = new HashMap<>();
		String[] fields = getFields(definition);
		if (fields != null) {
			for (String field : fields) {
				request.put(field, node.getMetadata().get(field));
			}
		} else {
			request.putAll(node.getMetadata());
		}
		if(StringUtils.equalsIgnoreCase(objectType, "Framework")) {
			Set<String> propertySet = requestMap.keySet();
			for(String property : propertySet) {
				if(request.containsKey(property))
					request.put(property, requestMap.get(property));
			}
		}
		request.put("identifier", clonedObjectId);


		Response getNodeResponse = getDataNode(GRAPH_ID, clonedObjectId);
		if (checkError(getNodeResponse))
			create(request, objectType);
		else
			update(clonedObjectId, objectType, request);

		Map<String, String> inRelDefMap = new HashMap<>();
		Map<String, String> outRelDefMap = new HashMap<>();
		ConvertGraphNode.getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
		List<Relation> outRelations = node.getOutRelations();
		if (null != outRelations && !outRelations.isEmpty()) {
			for (Relation relation : outRelations) {
				String endNodeObjectType = relation.getEndNodeObjectType();
				if(StringUtils.equals(endNodeObjectType, "Framework") ||
						StringUtils.equals(endNodeObjectType, "CategoryInstance") ||
						StringUtils.equals(endNodeObjectType, "Term")) {
					String title = outRelDefMap.get(relation.getRelationType() + relation.getEndNodeObjectType());
					String endNodeId = relation.getEndNodeId();
					endNodeId = endNodeId.replaceFirst(existingFrameworkId, clonedFrameworkId);
					Response res = copyHierarchy(relation.getEndNodeId(), endNodeId, existingFrameworkId, clonedFrameworkId, null);

					Map<String, Object> childObjectMap = new HashMap<>();
					childObjectMap.put("identifier", res.get("node_id"));
					childObjectMap.put("index", relation.getMetadata().get("IL_SEQUENCE_INDEX"));

					if(request.containsKey(title)) {
						List<Map<String, Object>> relationshipList = (List<Map<String, Object>>)request.get(title);
						relationshipList.add(childObjectMap);
					}else {
						List<Map<String, Object>> relationshipList = new ArrayList<>();
						relationshipList.add(childObjectMap);
						request.put(title, relationshipList);
					}
				}
				update(clonedObjectId, objectType, request);
			}
		}
		Response response = new Response();
		response.put("node_id", clonedObjectId);
		return response;
	}


	protected void filterFrameworkCategories(Map<String, Object> framework, List<String> categoryNames) throws JsonProcessingException {
		List<Map<String, Object>> categories = (List<Map<String, Object>>) framework.get("categories");
		if (CollectionUtils.isNotEmpty(categories) && CollectionUtils.isNotEmpty(categoryNames)) {
			framework.put("categories",
					categories.stream().filter(p -> categoryNames.contains(p.get("code")))
							.collect(Collectors.toList()));
			removeAssociations(framework, categoryNames);
		}
	}



}
