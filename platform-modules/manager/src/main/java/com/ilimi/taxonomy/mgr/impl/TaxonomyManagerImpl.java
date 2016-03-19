package com.ilimi.taxonomy.mgr.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Component
public class TaxonomyManagerImpl extends BaseManager implements ITaxonomyManager {

    private static Logger LOGGER = LogManager.getLogger(ITaxonomyManager.class.getName());

    public static String[] taxonomyIds = { "numeracy", "literacy", "literacy_v2" };

    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String[] tfields) {
        LOGGER.info("Find All Taxonomy");
        List<Request> requests = new ArrayList<Request>();
        for (String id : taxonomyIds) {
            Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                    GraphDACParams.node_id.name(), id);
            request.put(GraphDACParams.get_tags.name(), true);
            requests.add(request);
        }
        Response response = getResponse(requests, LOGGER, GraphDACParams.node.name(),
                TaxonomyAPIParams.taxonomy_list.name());
        if (null != tfields && tfields.length > 0) {
            List<Node> list = (List<Node>) response.get(TaxonomyAPIParams.taxonomy_list.name());
            if (validateRequired(list)) {
                for (Node node : list) {
                    setMetadataFields(node, tfields);
                }
            }
        }
        return response;
    }

    @Override
    public Response find(String id, boolean subgraph, String[] tfields, String[] cfields) {
        if (StringUtils.isBlank(id)) {
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        }
        LOGGER.info("Find Taxonomy : " + id);
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), id);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        } else {
            Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
            if (null != node) {
                setMetadataFields(node, tfields);
                response.put(TaxonomyAPIParams.taxonomy.name(), node);
            }
            if (subgraph) {
                Request req = new Request(request);
                req.put(GraphDACParams.start_node_id.name(), id);
                req.put(GraphDACParams.relation_type.name(), RelationTypes.HIERARCHY.relationName());
                req.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
                req.setOperation("getSubGraph");
                Response subGraphRes = getResponse(req, LOGGER);
                if (checkError(subGraphRes)) {
                    response = copyResponse(response, subGraphRes);
                } else {
                    Graph subGraph = (Graph) subGraphRes.get(GraphDACParams.sub_graph.name());
                    if (null != subGraph) {
                        if (null != cfields && cfields.length > 0) {
                            if (null != subGraph.getNodes() && !subGraph.getNodes().isEmpty()) {
                                for (Node concept : subGraph.getNodes())
                                    setMetadataFields(concept, cfields);
                            }
                        }
                        response.put(TaxonomyAPIParams.subgraph.name(), subGraph);
                    }
                }
            }
            return response;
        }
    }

    @Override
    public Response getSubGraph(String graphId, String id, Integer depth, List<String> relations) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Domain Id is blank");
        LOGGER.info("Find Taxonomy : " + id);
        Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "traverseSubGraph",
                GraphDACParams.start_node_id.name(), id);
        request.put(GraphDACParams.relations.name(), relations);
        if (null != depth && depth.intValue() > 0)
            request.put(GraphDACParams.depth.name(), depth);
        Response subGraphRes = getResponse(request, LOGGER);
        return subGraphRes;
    }

    @Override
    public Response create(String id, InputStream stream) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == stream)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INPUT_STREAM.name(),
                    "Taxonomy object is emtpy");
        LOGGER.info("Create Taxonomy : " + stream);
        Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
        request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(stream));
        Response createRes = getResponse(request, LOGGER);
        if (checkError(createRes)) {
            return createRes;
        } else {
            Response response = copyResponse(createRes);
            OutputStreamValue os = (OutputStreamValue) createRes.get(GraphEngineParams.output_stream.name());
            if (null != os && null != os.getOutputStream() && null != os.getOutputStream().toString()) {
                ByteArrayOutputStream bos = (ByteArrayOutputStream) os.getOutputStream();
                String csv = new String(bos.toByteArray());
                response.put(TaxonomyAPIParams.taxonomy.name(), csv);
            }
            return response;
        }
    }

	@Override
	public Response export(String id, Map<String, Object> reqMap) {
		if (StringUtils.isBlank(id))
			throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(),
					"Taxonomy Id is blank");
		String format = (String) reqMap.get(GraphEngineParams.format.name());
		LOGGER.info("Export Taxonomy : " + id + " | Format: " + format);
		Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "exportGraph");
		request.put(GraphEngineParams.format.name(), format);
		Response exportRes = getResponse(request, LOGGER);
		return exportRes;
	}

    @Override
    public Response delete(String id) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        LOGGER.info("Delete Taxonomy : " + id);
        Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "deleteGraph");
        return getResponse(request, LOGGER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response search(String id, Request request) {
        SearchCriteria sc = (SearchCriteria) request.get(TaxonomyAPIParams.search_criteria.name());
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == sc)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_SEARCH_CRITERIA.name(),
                    "Empty Search Criteria");
        LOGGER.info("Search Taxonomy : " + sc);
        sc.setObjectType("Concept");
        request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
        request.put(TaxonomyAPIParams.search_criteria.name(), sc);
        request.setOperation("searchNodes");
        request.getContext().put(GraphHeaderParams.graph_id.name(), id);
        Response searchRes = getResponse(request, LOGGER);
        Response response = copyResponse(searchRes);
        if (!checkError(response)) {
            List<Node> nodeList = (List<Node>) searchRes.get(GraphDACParams.node_list.name());
            if (null != nodeList && !nodeList.isEmpty()) {
                response.put(TaxonomyAPIParams.concepts.name(), nodeList);
            }
        }
        return response;
    }

    @Override
    public Response updateDefinition(String id, String json) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(json))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(),
                    "Definition nodes JSON is empty");
        LOGGER.info("Update Definition : " + id);
        Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "importDefinitions");
        request.put(GraphEngineParams.input_stream.name(), json);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response findAllDefinitions(String id) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        LOGGER.info("Get All Definitions : " + id);
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getAllDefinitions");
        return getResponse(request, LOGGER);
    }

    @Override
    public Response findDefinition(String id, String objectType) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
        LOGGER.info("Get Definition : " + id + " : Object Type : " + objectType);
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
                GraphDACParams.object_type.name(), objectType);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response deleteDefinition(String id, String objectType) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
        LOGGER.info("Delete Definition : " + id + " : Object Type : " + objectType);
        Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "deleteDefinition",
                GraphDACParams.object_type.name(), objectType);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response createIndex(String id, List<String> keys, Boolean unique) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == keys || keys.isEmpty())
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INDEX_KEYS.name(),
                    "Property keys are empty");
        LOGGER.info("Create Index : " + id + " : Keys : " + keys);
        Request request = null;
        if (null != unique && unique)
            request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "createUniqueConstraint",
                    GraphDACParams.property_keys.name(), keys);
        else
            request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "createIndex",
                    GraphDACParams.property_keys.name(), keys);
        return getResponse(request, LOGGER);
    }
    
    @Override
    public Response compositeSearch() {
        String graphId = "domain";
        Response res = new Response();
        getObjectList(graphId, res, "Domain", "domains");
        getObjectList(graphId, res, "Dimension", "dimensions");
        getObjectList(graphId, res, "Content", "content");
        return res;
    }
    
    private void getObjectList(String graphId, Response res, String objectType, String key) {
        DefinitionDTO domainDef = getDefinition(graphId, objectType);
        if (null != domainDef) {
            List<Map<String, Object>> domains = searchObjectType(graphId, objectType, domainDef);
            if (null != domains && !domains.isEmpty()) {
                res.put(key, domains);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchObjectType(String graphId, String objectType, DefinitionDTO definition) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
        sc.setResultSize(2);
        
        List<String> statusList = new ArrayList<String>();
        statusList.add("Live");
        MetadataCriterion mc = MetadataCriterion
                .create(Arrays.asList(new Filter(PARAM_STATUS, SearchConditions.OP_IN, statusList)));
        sc.addMetadata(mc);
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        Response res = getResponse(req, LOGGER);
        if (!checkError(res)) {
            List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    Map<String, Object> map = ConvertGraphNode.convertGraphNode(node, graphId, definition, null);
                    maps.add(map);
                }
            }
        }
        return maps;
    }
    
    private DefinitionDTO getDefinition(String taxonomyId, String objectType) {
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
                GraphDACParams.object_type.name(), objectType);
        Response response = getResponse(request, LOGGER);
        if (!checkError(response)) {
            DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
            return definition;
        }
        return null;
    }

}
