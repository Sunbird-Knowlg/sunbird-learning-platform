package org.ekstep.learning.util;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.contentstore.ContentStoreOperations;
import org.ekstep.learning.contentstore.ContentStoreParams;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.text.MessageFormat;
import java.util.*;


/**
 * The Class ControllerUtil, provides controller utility functionality for all
 * learning actors.
 *
 * @author karthik
 */
public class ControllerUtil extends BaseLearningManager {

	/** The logger. */

	private static ObjectMapper mapper = new ObjectMapper();
	private static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

	/**
	 * Gets the node.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param contentId
	 *            the content id
	 * @return the node
	 */
	public Node getNode(String taxonomyId, String contentId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), contentId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return null;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

	/**
	 * Update node.
	 *
	 * @param node
	 *            the node
	 * @return the response
	 */
	public Response updateNode(Node node) {
		Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq);
		return updateRes;
	}

	/**
	 * Gets the definition.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param objectType
	 *            the object type
	 * @return the definition
	 */
	public DefinitionDTO getDefinition(String taxonomyId, String objectType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	/**
	 * Gets all the definitions
	 *
	 * @param taxonomyId
	 * 				the taxonomy id
	 * @return list of defintions
	 */
	public List<DefinitionDTO> getAllDefinitions(String taxonomyId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
		Response response = getResponse(request);
		if (!checkError(response))
			return (List<DefinitionDTO>) response.getResult().get(GraphDACParams.definition_nodes.name());
		return new ArrayList<>();
	}

	/**
	 * Adds out relations.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * 
	 * @param startNodeId
	 *            the startNodeId
	 * 
	 * @param endNodeIds
	 *            the list of endNodeIds
	 * 
	 * @param relationType
	 *            the relationType
	 * 
	 * @return the response
	 */
	public Response addOutRelations(String taxonomyId, String startNodeId, List<String> endNodeIds,
			String relationType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER, "addOutRelations",
				GraphDACParams.start_node_id.name(), startNodeId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), endNodeIds);
		Response response = getResponse(request);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	/**
	 * Gets the collection members.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param collectionId
	 *            the collectionId
	 * @param collectionType
	 *            the collectionType
	 * 
	 * @return the response
	 */
	public Response getCollectionMembers(String taxonomyId, String collectionId, String collectionType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getCollectionMembers",
				GraphDACParams.collection_id.name(), collectionId);
		request.put(GraphDACParams.collection_type.name(), collectionType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	public Response getSet(String taxonomyId, String collectionId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getSet",
				GraphDACParams.collection_id.name(), collectionId);
		request.put(GraphDACParams.collection_type.name(), taxonomyId);
		Response response = getResponse(request);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	public Response copyResponse(Response res) {
        Response response = new Response();
        response.setResponseCode(res.getResponseCode());
        response.setParams(res.getParams());
        return response;
    }
	
	public String getContentBody(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = getResponse(request);
		String body = (String) response.get(ContentStoreParams.body.name());
		return body;
	}

	public Response getContentProperties(String contentId, List<String> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = getResponse(request);
		return response;
	}

	public Response updateContentProperties(String contentId, Map<String, Object> properties) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.updateContentProperties.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		request.put(ContentStoreParams.properties.name(), properties);
		Response response = getResponse(request);
		return response;
	}

	 public Response copyResponse(Response to, Response from) {
	        to.setResponseCode(from.getResponseCode());
	        to.setParams(from.getParams());
	        return to;
	    }
	 
	/**
	 * Gets Data nodes.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param node
	 *            the list of nodes
	 * 
	 * @return the response
	 */
	public Response getDataNodes(String taxonomyId, List<String> nodes) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
				GraphDACParams.node_ids.name(), nodes);
		Response response = getResponse(request);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	public Response getHirerachy(String identifier) {
		String url = Platform.config.getString("platform-api-url") + "/content/v3/hierarchy/" + identifier;
		Response hirerachyRes = null;
		try {
			String result = HTTPUtil.makeGetRequest(url);
			hirerachyRes = mapper.readValue(result, Response.class);
		} catch (Exception e) {
			TelemetryManager.error(" Error while getting the Hirerachy of the node: " + e.getMessage(), e);
		}
		return hirerachyRes;
	}
	
	public Response createDataNode(Node node){
		Request request = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "createDataNode");
		request.put(GraphDACParams.node.name(), node);
		Response response = getResponse(request);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<NodeDTO> getNodesForPublish(Node node) {
		List<NodeDTO> nodes = new ArrayList<NodeDTO>();
		String nodeId = null;
		String imageNodeId = null;
		if(StringUtils.endsWith(node.getIdentifier(), ".img")) {
			imageNodeId = node.getIdentifier();
			nodeId = node.getIdentifier().replace(".img", "");
		} else {
			nodeId = node.getIdentifier();
			imageNodeId = nodeId + ".img";
		}
		Request request = getRequest(node.getGraphId(), GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
		String queryString = "MATCH p=(n:domain'{'IL_UNIQUE_ID:\"{0}\"'}')-[r:hasSequenceMember*0..10]->(s:domain) RETURN s.IL_UNIQUE_ID as identifier, s.name as name, length(p) as depth, s.status as status, s.mimeType as mimeType, s.visibility as visibility, s.compatibilityLevel as compatibilityLevel";
		String query = MessageFormat.format(queryString, nodeId) + " UNION " + MessageFormat.format(queryString, imageNodeId) + " ORDER BY depth DESC;";
        request.put(GraphDACParams.query.name(), query);
        List<String> props = new ArrayList<String>();
        props.add("identifier");
        props.add("name");
        props.add("depth");
        props.add("status");
        props.add("mimeType");
        props.add("compatibilityLevel");
        props.add("visibility");
        request.put(GraphDACParams.property_keys.name(), props);
        Response response = getResponse(request);
		if (!checkError(response)) {
			Map<String, Object> result = response.getResult();
			List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("properties");
			if (null != list && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> properties = list.get(i);
					NodeDTO obj = new NodeDTO((String)properties.get("identifier"), (String) properties.get("name"), null);
					obj.setDepth(((Long)properties.get("depth")).intValue());
					obj.setStatus((String) properties.get("status"));
					obj.setMimeType((String) properties.get("mimeType"));
					obj.setVisibility((String) properties.get("visibility"));
					Integer compatibilityLevel = 1;
					if(null != properties.get("compatibilityLevel"))
						compatibilityLevel = ((Number) properties.get("compatibilityLevel")).intValue();
					obj.setCompatibilityLevel(compatibilityLevel);
					nodes.add(obj);
				}
			}
		}
		TelemetryManager.info("Node children count:"+ nodes.size());
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> getNodes(String graphId, String objectType, int startPosition, int batchSize) {
		SearchCriteria sc = new SearchCriteria();
		//sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		sc.setResultSize(batchSize);
		sc.setStartPosition(startPosition);
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: " + graphId);
		else {
			List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
			return nodes;
		}
	}
	
	public List<String> getNodesWithInDateRange(String graphId, String objectType, String startDate, String endDate) {

		List<String> nodeIds = new ArrayList<>();
		String objectTypeQuery = "";
		if(StringUtils.isNotBlank(objectType))
			objectTypeQuery="{IL_FUNC_OBJECT_TYPE:'"+objectType+"'}";			
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
		String queryString ="MATCH (n:{0}{1})  WITH split(left(n.lastUpdatedOn, 10), {2}) AS dd, n where {4}>=toInt(dd[0]+dd[1]+dd[2])>={3} and NOT n.IL_SYS_NODE_TYPE in [\"TAG\", \"DEFINITION_NODE\", \"ROOT_NODE\"] return n.IL_UNIQUE_ID as identifier";
		String query = MessageFormat.format(queryString, graphId, objectTypeQuery, "'-'", startDate, endDate);
        request.put(GraphDACParams.query.name(), query);
        List<String> props = new ArrayList<String>();
        props.add("identifier");
        request.put(GraphDACParams.property_keys.name(), props);
        Response response = getResponse(request);
        if (!checkError(response)) {
			Map<String, Object> result = response.getResult();
			List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("properties");
			if (null != list && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> properties = list.get(i);
					nodeIds.add((String)properties.get("identifier"));
				}
			}
		}
		return nodeIds;
	}
	
	public Map<String, Long> getCountByObjectType(String graphId) {
		Map<String, Long> counts = new HashMap<String, Long>();
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
		request.put(GraphDACParams.query.name(), MessageFormat.format("MATCH (n:{0}) WHERE EXISTS(n.IL_FUNC_OBJECT_TYPE) RETURN n.IL_FUNC_OBJECT_TYPE AS objectType, COUNT(n) AS count;", graphId));
		List<String> props = new ArrayList<String>();
        props.add("objectType");
        props.add("count");
        request.put(GraphDACParams.property_keys.name(), props);
        Response response = getResponse(request);
        if (!checkError(response)) {
			Map<String, Object> result = response.getResult();
			List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("properties");
			if (null != list && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> properties = list.get(i);
					counts.put((String) properties.get("objectType"), (Long) properties.get("count"));
				}
			}
			
        }
		return counts;
	}


	/**
	 *
	 * @param graphId
	 * @param node
	 * @param definition
	 * @param mode
	 * @return
	 */
	public Map<String, Object> getContentHierarchyRecursive(String graphId, Node node, DefinitionDTO definition,
															 String mode, boolean fetchAll) {
		Map<String, Object> contentMap = ConvertGraphNode.convertGraphNode(node, graphId, definition, null);
		List<NodeDTO> children = (List<NodeDTO>) contentMap.get("children");

		// Collections sort method is used to sort the child content list on the
		// basis of index.
		if (null != children && !children.isEmpty()) {
			Collections.sort(children, new Comparator<NodeDTO>() {
				@Override
				public int compare(NodeDTO o1, NodeDTO o2) {
					return o1.getIndex() - o2.getIndex();
				}
			});
		}

		if (null != children && !children.isEmpty()) {
			List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
			for (NodeDTO dto : children) {
				Node childNode = getContentNode(graphId, dto.getIdentifier(), mode);
				String nodeStatus = (String)childNode.getMetadata().get("status");
				if((!org.apache.commons.lang3.StringUtils.equalsIgnoreCase(nodeStatus, "Retired")) &&
						(fetchAll || (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(nodeStatus, "Live") || org.apache.commons.lang3.StringUtils.equalsIgnoreCase(nodeStatus, "Unlisted")))) {
					Map<String, Object> childMap = getContentHierarchyRecursive(graphId, childNode, definition, mode, fetchAll);
					childMap.put("index", dto.getIndex());
					Map<String, Object> childData = contentCleanUp(childMap);
					childList.add(childData);
				}
			}
			contentMap.put("children", childList);
		}
		// TODO: Not the best Solution, need to optimize
		contentMap.remove("collections");
		contentMap.remove("usedByContent");
		contentMap.remove("item_sets");
		contentMap.remove("methods");
		contentMap.remove("libraries");
		return contentMap;
	}

	private Map<String, Object> contentCleanUp(Map<String, Object> map) {
		if (map.containsKey("identifier")) {
			String identifier = (String) map.get("identifier");
			if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
				String newIdentifier = identifier.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
				map.replace("identifier", identifier, newIdentifier);
			}
		}
		return map;
	}

	private Node getContentNode(String graphId, String contentId, String mode) {

		if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase("edit", mode)) {
			String contentImageId = getImageId(contentId);
			Response responseNode = getDataNode(graphId, contentImageId);
			if (!checkError(responseNode)) {
				Node content = (Node) responseNode.get(GraphDACParams.node.name());
				return content;
			}
		}
		Response responseNode = getDataNode(graphId, contentId);
		if (checkError(responseNode))
			throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.name(),
					"Content not found with id: " + contentId);

		Node content = (Node) responseNode.get(GraphDACParams.node.name());
		return content;
	}

	protected String getImageId(String identifier) {
		String imageId = "";
		if (org.apache.commons.lang3.StringUtils.isNotBlank(identifier))
			imageId = identifier + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return imageId;
	}


	public List<String> getPublishedCollections(String graphId) {
		List<String> identifiers = new ArrayList<>();
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
		request.put(GraphDACParams.query.name(), MessageFormat.format("MATCH (n:{0}) WHERE n.IL_FUNC_OBJECT_TYPE=\"Content\" AND n.mimeType=\"application/vnd.ekstep.content-collection\" AND n.status IN [\"Live\", \"Unlisted\", \"Flagged\"] RETURN n.IL_UNIQUE_ID as identifier;", graphId));
		List<String> props = new ArrayList<String>();
		props.add("identifier");
		request.put(GraphDACParams.property_keys.name(), props);
		Response response = getResponse(request);
		if (!checkError(response)) {
			Map<String, Object> result = response.getResult();
			List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("properties");
			if (null != list && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Map<String, Object> properties = list.get(i);
					identifiers.add((String) properties.get("identifier"));
				}
			}

		}
		return identifiers;
	}


}

