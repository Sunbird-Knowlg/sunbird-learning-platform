package org.sunbird.learning.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.neo4j.driver.v1.Values;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.NodeDTO;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.ConvertGraphNode;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.engine.mgr.impl.NodeManager;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.sunbird.learning.common.enums.LearningActorNames;
import org.sunbird.learning.contentstore.ContentStoreOperations;
import org.sunbird.learning.contentstore.ContentStoreParams;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.searchindex.util.HTTPUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The Class ControllerUtil, provides controller utility functionality for all
 * learning actors.
 *
 * @author karthik
 */
public class ControllerUtil extends BaseLearningManager {

    /**
     * The logger.
     */

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

    /**
     * Gets the node.
     *
     * @param taxonomyId the taxonomy id
     * @param contentId  the content id
     * @return the node
     */
    public Node getNode(String taxonomyId, String contentId) {
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), contentId);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            if (StringUtils.endsWith(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
                return null;
            else {
                switch (response.getResponseCode()) {
                    case RESOURCE_NOT_FOUND:
                        throw new ResourceNotFoundException(response.getParams().getErr(), response.getParams().getErrmsg());
                    case CLIENT_ERROR:
                        throw new ClientException(response.getParams().getErr(), response.getParams().getErrmsg());
                    default:
                        throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
                }
            }
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        return node;
    }

    public Response deleteNode(String taxonomyId, String id) {
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode",
                GraphDACParams.node_id.name(), id);
        return getResponse(request);
    }

    /**
     * Update node.
     *
     * @param node the node
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
     * Update node.
     *
     * @param nodes
     * @param metadata
     * @return the response
     */
    public Response updateNodes(List<Node> nodes, Map<String, Object> metadata) {
        List<String> nodeIds = new ArrayList<>();
        nodes.forEach(node -> nodeIds.add(node.getIdentifier()));
        Request updateReq = getRequest(nodes.get(0).getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNodes");
        updateReq.put(GraphDACParams.node_ids.name(), nodeIds);
        updateReq.put(GraphDACParams.metadata.name(), metadata);
        Response updateRes = getResponse(updateReq);
        return updateRes;
    }

    /**
     * Update node without validation.
     *
     * @param node the node
     * @return the response
     */
    public Response updateNodeWithoutValidation(Node node) {
        Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), node);
        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
        updateReq.put(GraphDACParams.skip_validations.name(), true);
        Response updateRes = getResponse(updateReq);
        return updateRes;
    }

    /**
     * Gets the definition.
     *
     * @param taxonomyId the taxonomy id
     * @param objectType the object type
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
    
    public DefinitionDTO getDefinition(String taxonomyId, String objectType, boolean disableAkka) {
    	DefinitionDTO definition = null;
    	if(disableAkka) {
    		try {
    			Request request = new Request();
    	        request.getContext().put(GraphHeaderParams.graph_id.name(), TAXONOMY_ID);
    			request.put(GraphDACParams.object_type.name(), objectType);
    			NodeManager nodeManager = new NodeManager();
    			definition = nodeManager.getNodeDefinition(request);
    		}catch (Exception e) {
    			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage() + ". Please Try Again After Sometime!");
    		}
    	}else {
    		definition = getDefinition(taxonomyId, objectType);
    	}
		return definition;
		
	}

    /**
     * Gets all the definitions
     *
     * @param taxonomyId the taxonomy id
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
     * @param taxonomyId   the taxonomy id
     * @param startNodeId  the startNodeId
     * @param endNodeIds   the list of endNodeIds
     * @param relationType the relationType
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
     * @param taxonomyId     the taxonomy id
     * @param collectionId   the collectionId
     * @param collectionType the collectionType
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
        Response response = getResponse(request, LearningRequestRouterPool.getRequestRouter());
        String body = (String) response.get(ContentStoreParams.body.name());
        return body;
    }

    public Response updateContentBody(String contentId, String ecmlBody) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.updateContentBody.name());
        request.put(ContentStoreParams.content_id.name(), contentId);
        request.put(ContentStoreParams.body.name(), ecmlBody);
        Response response = getResponse(request, LearningRequestRouterPool.getRequestRouter());
        return response;
    }

    public Response updateContentOldBody(String contentId, String ecmlBody) {
        Request request = new Request();
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
        request.setOperation(ContentStoreOperations.updateContentOldBody.name());
        request.put(ContentStoreParams.content_id.name(), contentId);
        request.put(ContentStoreParams.body.name(), ecmlBody);
        Response response = getResponse(request, LearningRequestRouterPool.getRequestRouter());
        return response;
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
     * @param taxonomyId the taxonomy id
     * @param nodes      the list of nodes
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
        String url = Platform.config.getString("platform-api-url") + "/content/v3/hierarchy/" + identifier + "?mode=edit";
        Response hirerachyRes = null;
        try {
            String result = HTTPUtil.makeGetRequest(url);
            hirerachyRes = mapper.readValue(result, Response.class);
        } catch (Exception e) {
            TelemetryManager.error(" Error while getting the Hirerachy of the node: " + e.getMessage(), e);
        }
        return hirerachyRes;
    }

    public Response createDataNode(Node node) {
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
        if (StringUtils.endsWith(node.getIdentifier(), ".img")) {
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
                    NodeDTO obj = new NodeDTO((String) properties.get("identifier"), (String) properties.get("name"), null);
                    obj.setDepth(((Long) properties.get("depth")).intValue());
                    obj.setStatus((String) properties.get("status"));
                    obj.setMimeType((String) properties.get("mimeType"));
                    obj.setVisibility((String) properties.get("visibility"));
                    Integer compatibilityLevel = 1;
                    if (null != properties.get("compatibilityLevel"))
                        compatibilityLevel = ((Number) properties.get("compatibilityLevel")).intValue();
                    obj.setCompatibilityLevel(compatibilityLevel);
                    nodes.add(obj);
                }
            }
        }
        TelemetryManager.info("Node children count:" + nodes.size());
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
            return null;
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            return nodes;
        }
    }

    public List<Node> getNodes(String graphId, String objectType, List<String> mimeTypes, List<String> status, List<String> contentIdsList, double migrationVersion, int startPosition, int batchSize) {
        List<Filter> filters = new ArrayList<Filter>();
        if(!mimeTypes.isEmpty())
            filters.add(new Filter("mimeType", SearchConditions.OP_IN, mimeTypes));
        if(!status.isEmpty())
            filters.add(new Filter("status", SearchConditions.OP_IN, status));
        if(!contentIdsList.isEmpty())
            filters.add(new Filter("IL_UNIQUE_ID", SearchConditions.OP_IN, contentIdsList));
        if(migrationVersion == 0) filters.add(new Filter("migrationVersion", SearchConditions.OP_IS, Values.NULL));
        else filters.add(new Filter("migrationVersion", SearchConditions.OP_EQUAL, migrationVersion));

        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.setResultSize(batchSize);
        sc.setStartPosition(startPosition);
       if(!filters.isEmpty() && filters.size()>0)
            sc.addMetadata(MetadataCriterion.create(filters));
        Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        req.put(GraphDACParams.get_tags.name(), true);
        Response listRes = getResponse(req);
        if (checkError(listRes))
            return null;
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            return nodes;
        }
    }

    public List<String> getNodesWithInDateRange(String graphId, String objectType, String startDate, String endDate) {

        List<String> nodeIds = new ArrayList<>();
        String objectTypeQuery = "";
        if (StringUtils.isNotBlank(objectType))
            objectTypeQuery = "{IL_FUNC_OBJECT_TYPE:'" + objectType + "'}";
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
        String queryString = "MATCH (n:{0}{1})  WITH split(left(n.lastUpdatedOn, 10), {2}) AS dd, n where {4}>=toInt(dd[0]+dd[1]+dd[2])>={3} and NOT n.IL_SYS_NODE_TYPE in [\"TAG\", \"DEFINITION_NODE\", \"ROOT_NODE\"] return n.IL_UNIQUE_ID as identifier";
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
                    nodeIds.add((String) properties.get("identifier"));
                }
            }
        }
        return nodeIds;
    }

    public Map<String, Long> getCountByObjectType(String graphId) {
        Map<String, Long> counts = new HashMap<String, Long>();
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
        request.put(GraphDACParams.query.name(), MessageFormat.format("MATCH (n:{0}) WHERE EXISTS(n.IL_FUNC_OBJECT_TYPE) AND n.IL_SYS_NODE_TYPE=\"DATA_NODE\" RETURN n.IL_FUNC_OBJECT_TYPE AS objectType, COUNT(n) AS count;", graphId));
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
                String nodeStatus = (String) childNode.getMetadata().get("status");
                if ((!org.apache.commons.lang3.StringUtils.equalsIgnoreCase(nodeStatus, "Retired")) &&
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
                map.replace("objectType", "ContentImage", "Content");
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

    public Map<String, Object> constructHierarchy(List<Map<String, Object>> list) {

        Map<String, Object> hierarchy = list.stream().filter(e -> ((Number) e.get("depth")).intValue() == 0).findFirst().get();
        if (MapUtils.isNotEmpty(hierarchy)) {
            int max = list.stream().map(e -> ((Number) e.get("depth")).intValue()).max(Comparator.naturalOrder()).orElse(1);

            for (int i = 0; i <= max; i++) {
                final int depth = i;
                Map<String, List<Map<String, Object>>> currentLevelNodes = new HashMap<>();
                list.stream().filter(e -> ((Number) e.get("depth")).intValue() == depth)
                        .collect(Collectors.toList()).forEach(e -> {
                    String id = (String) e.get("identifier");
                    List<Map<String, Object>> nodes = currentLevelNodes.get(id);
                    if (CollectionUtils.isEmpty(nodes)) {
                        nodes = new ArrayList<>();
                        currentLevelNodes.put((String) e.get("identifier"), nodes);
                    }
                    nodes.add(e);

                });

                List<Map<String, Object>> nextLevelNodes = list.stream().filter(e -> ((Number) e.get("depth")).intValue() == depth + 1)
                        .collect(Collectors.toList());
                if (MapUtils.isNotEmpty(currentLevelNodes) && CollectionUtils.isNotEmpty(nextLevelNodes)) {
                    nextLevelNodes.forEach(e -> {
                                String parentId = (String) e.get("parent");
                                List<Map<String, Object>> parents = currentLevelNodes.get(parentId);
                                if (CollectionUtils.isNotEmpty(parents)) {
                                    for (Map<String, Object> parent : parents) {
                                        List<Object> children = (List<Object>) parent.get("children");
                                        if (CollectionUtils.isEmpty(children)) {
                                            children = new ArrayList<>();
                                            parent.put("children", children);
                                        }
                                        //contentCleanUp(e);
                                        children.add(e);
                                    }
                                }
                            }
                    );
                }
            }
        }
        return hierarchy;
    }

    public List<Map<String, Object>> getContentHierarchy(String graphId, String contentId, String mode) {
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
        String query = "MATCH p=(n:{0})-[r:hasSequenceMember*0..10]->(s:{0}) WHERE n.IL_UNIQUE_ID=\"{1}\" RETURN s.IL_UNIQUE_ID as identifier, s.IL_FUNC_OBJECT_TYPE as objectType, s.visibility as visibility, s.status as status, length(p) as depth, (nodes(p)[length(p)-1]).IL_UNIQUE_ID as parent, (rels(p)[length(p)-1]).IL_SEQUENCE_INDEX as index order by depth,index;";
        request.put(GraphDACParams.query.name(), MessageFormat.format(query, graphId, contentId));
        List<String> props = Arrays.asList("identifier", "objectType", "visibility", "status", "depth", "parent", "index");
        request.put(GraphDACParams.property_keys.name(), props);
        Response response = getResponse(request);
        if (!checkError(response)) {
            Map<String, Object> result = response.getResult();
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("properties");
            if (CollectionUtils.isEmpty(list)) {
                throw new ResourceNotFoundException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "No data find for the given identifier: " + contentId.replace(".img", ""));
            }
            List<String> invalidStatus = Arrays.asList("Flagged", "Retired");
            list = list.stream().filter(e -> !invalidStatus.contains(e.get("status"))).distinct().collect
                    (Collectors.toList());

            // Get leaf nodes(image) from the hierarchy (graph) and remove them.
            List<String> resourceImgIds = list.stream()
                    .filter(e -> StringUtils.equals((String) e.get("objectType"), "ContentImage") && StringUtils.equalsIgnoreCase((String) e.get("visibility"), "default") && ((Number) e.get("depth")).intValue() > 0)
                    .map(e -> (String) e.get("identifier"))
                    .map(id -> id.endsWith(".img") ? id : id + ".img")
                    .distinct().collect(Collectors.toList());
//			Stream<Map<String, Object>> listStream = list.stream().filter(e -> !resourceImgIds.contains((String) e.get("identifier")));

            // Get image nodes from the hierarchy (graph) other than root.
//			Stream<Map<String,Object>> imgList = list.stream().filter(e -> !resourceImgIds.contains((String) e.get("identifier")))
//					.filter(e -> StringUtils.equals((String) e.get("objectType"), "ContentImage") && ((Number) e.get("depth")).intValue() > 0);

            if (StringUtils.equalsIgnoreCase("edit", mode)) {
                // mode=edit - remove the Content which have Image Nodes.
                List<String> removeIds = list.stream().filter(e -> !resourceImgIds.contains((String) e.get("identifier")))
                        .filter(e -> StringUtils.equals((String) e.get("objectType"), "ContentImage") && ((Number) e.get("depth")).intValue() > 0).map(e -> ((String) e.get("identifier")).replace(".img", "")).collect(Collectors.toList());
                List<Map<String, Object>> contentList = list.stream().filter(e -> !resourceImgIds.contains((String) e.get("identifier")))
                        .filter(e -> !removeIds.contains(e.get("identifier")))
                        .collect(Collectors.toList());

                return contentList;
            } else {
                List<String> publicStatus = Arrays.asList("Live", "Unlisted");
                Map<String, Object> root = list.stream().filter(e -> ((Number) e.get("depth")).intValue() == 0).findFirst().get();
                if (MapUtils.isEmpty(root) || !publicStatus.contains(root.get("status"))) {
                    throw new ResourceNotFoundException(ContentErrorCodes.ERR_INVALID_INPUT.name(), "No data find for the given identifier: " + contentId.replace(".img", ""));
                }
                // mode!=edit - remove Image Nodes.
                List<String> removeIds = list.stream().filter(e -> !resourceImgIds.contains((String) e.get("identifier")))
                        .filter(e -> (!publicStatus.contains(e.get("status")) || StringUtils.equals((String) e.get("objectType"), "ContentImage")) && ((Number) e.get("depth")).intValue() > 0).map(e -> ((String) e.get("identifier"))).collect(Collectors.toList());
                List<Map<String, Object>> contentList = list.stream().filter(e -> !resourceImgIds.contains((String) e.get("identifier")))
                        .filter(e -> !removeIds.contains(e.get("identifier"))).collect(Collectors.toList());
                return contentList;
            }

        } else {
            if (response.getResponseCode() == ResponseCode.CLIENT_ERROR) {
                throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), response.getParams().getErrmsg());
            } else {
                throw new ServerException(ContentAPIParams.SERVER_ERROR.name(), response.getParams().getErrmsg());
            }
        }
    }


    public Map<String, Object> getHierarchyMap(String graphId, String contentId, DefinitionDTO
            definition, String mode, List<String> fields) {

//		long startTime = System.currentTimeMillis();
        List<Map<String, Object>> contentList = getContentHierarchy(graphId, contentId, mode);
//		System.out.println("Time to call cypher and get hierarchy list: " + (System.currentTimeMillis() - startTime));

        List<String> ids = contentList.stream().map(content -> (String) content.get("identifier")).collect(Collectors
                .toList());
        Map<String, Object> collectionHierarchy = new HashMap<>();
//		startTime = System.currentTimeMillis();
        Response getList = getDataNodes(graphId, ids);
//		System.out.println("Time to get required data nodes: " + (System.currentTimeMillis() - startTime));
		if (null != getList && !checkError(getList)) {
			List<Node> nodeList = (List<Node>) getList.get("node_list");
			Map<String, Map<String, Object>> contentsWithMetadata = nodeList.stream().map(n -> ConvertGraphNode.convertGraphNode
					(n, graphId, definition, fields)).map(contentMap -> {
				contentMap.remove("collections");
				contentMap.remove("children");
				contentMap.remove("usedByContent");
				contentMap.remove("item_sets");
				contentMap.remove("methods");
				contentMap.remove("libraries");
				contentMap.remove("editorState");
				return contentMap;
			}).collect(Collectors.toMap(e -> (String) e.get("identifier"), e -> e));

            contentList = contentList.stream().map(n -> {
                n.putAll(contentsWithMetadata.get(n.get("identifier")));
                return n;
            }).collect(Collectors.toList());
//			startTime = System.currentTimeMillis();
            collectionHierarchy = contentCleanUp(constructHierarchy(contentList));
//			System.out.println("Time to construct hierarchy: " + (System.currentTimeMillis() - startTime));
		} else {
			if (null != getList && getList.getResponseCode() == ResponseCode.CLIENT_ERROR) {
				throw new ClientException(ContentErrorCodes.ERR_INVALID_INPUT.name(), getList.getParams().getErrmsg());
			} else {
				throw new ServerException(ContentAPIParams.SERVER_ERROR.name(), getList.getParams().getErrmsg());
			}
		}
		hierarchyCleanUp(collectionHierarchy);
		return collectionHierarchy;
	}


    public List<String> getPublishedCollections(String graphId, int offset, int limit) {
        List<String> identifiers = new ArrayList<>();
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
        String query = "MATCH (n:{0}) WHERE n.IL_FUNC_OBJECT_TYPE=\"Content\" AND n.mimeType=\"application/vnd.ekstep.content-collection\" AND n.status IN [\"Live\", \"Unlisted\", \"Flagged\"] RETURN n.IL_UNIQUE_ID as identifier";
        if (offset > 0) {
            query += " SKIP " + offset;
        }
        if (limit > 0) {
            query += " LIMIT " + limit;
        }
        request.put(GraphDACParams.query.name(), MessageFormat.format(query, graphId));
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

    public Response updateDefinitionCache(String graphId, String objectType) {
        Request request = getRequest(graphId, GraphEngineManagers.GRAPH_MANAGER, "updateDefinitionCache");
        request.put(GraphDACParams.graphId.name(), graphId);
        request.put(GraphDACParams.objectType.name(), objectType);
        Response response = getResponse(request);
        if (!checkError(response)) {
            return response;
        }
        return null;
    }

    public void hierarchyCleanUp(Map<String, Object> map) {
        if (map.containsKey("identifier")) {
            String identifier = (String) map.get("identifier");
            String parentId = (String) map.get("parent");
            if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
                String newIdentifier = identifier.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
                map.replace("identifier", identifier, newIdentifier);
                map.replace("objectType", "ContentImage", "Content");
            }

            if (StringUtils.isNotBlank(parentId)) {
                String newParentId = parentId.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, "");
                map.replace("parent", parentId, newParentId);
            }
        }
        List<Map<String, Object>> children = (List<Map<String, Object>>) map.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Map<String, Object> child : children)
                hierarchyCleanUp(child);
        }
    }

    public Map<String, Long> getCSPMigrationObjectCount(String graphId, List<String> objectTypes, List<String> mimeTypeList, List<String> statusList, List<String> contentIdsList, double migrationVersion) {
        Map<String, Long> counts = new HashMap<String, Long>();
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "executeQueryForProps");
        StringBuilder queryString = new StringBuilder();
        queryString.append("MATCH (n:{0}) WHERE EXISTS(n.IL_FUNC_OBJECT_TYPE) AND n.IL_SYS_NODE_TYPE=\"DATA_NODE\" AND n.IL_FUNC_OBJECT_TYPE IN {1} ");

        if(mimeTypeList!=null && !mimeTypeList.isEmpty())
            queryString.append(" AND n.mimeType IN {2} ");

        if(statusList!=null && !statusList.isEmpty())
            queryString.append(" AND n.status IN {3} ");

        if(contentIdsList!=null && !contentIdsList.isEmpty())
            queryString.append(" AND n.IL_UNIQUE_ID IN {5} ");

        if(migrationVersion == 0) queryString.append(" AND NOT EXISTS(n.migrationVersion) ");
        else queryString.append(" AND n.migrationVersion={4} ");

        queryString.append("RETURN n.IL_FUNC_OBJECT_TYPE AS objectType, COUNT(n) AS count;");

        System.out.println("Count queryString:: " + MessageFormat.format(queryString.toString(), graphId, new JSONArray(objectTypes), new JSONArray(mimeTypeList), new JSONArray(statusList), migrationVersion, new JSONArray(contentIdsList)));

        request.put(GraphDACParams.query.name(), MessageFormat.format(queryString.toString(), graphId, new JSONArray(objectTypes), new JSONArray(mimeTypeList), new JSONArray(statusList), migrationVersion, new JSONArray(contentIdsList)));

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

}
