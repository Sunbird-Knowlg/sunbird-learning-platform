package com.ilimi.taxonomy.mgr.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SearchDTO;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Component
public class TaxonomyManagerImpl extends BaseManager implements ITaxonomyManager {

    private static Logger LOGGER = LogManager.getLogger(ITaxonomyManager.class.getName());

    protected static String[] taxonomyIds = { "numeracy", "literacy" };

    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String[] tfields) {
        LOGGER.info("Find All Taxonomy");
        List<Request> requests = new ArrayList<Request>();
        for (String id : taxonomyIds) {
            Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
            request.put(GraphDACParams.get_tags.name(), true);
            requests.add(request);
        }
        Response response = getResponse(requests, LOGGER, GraphDACParams.node.name(), TaxonomyAPIParams.taxonomy_list.name());
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
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
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
    public Response create(String id, InputStream stream) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == stream)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INPUT_STREAM.name(), "Taxonomy object is emtpy");
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
        SearchDTO dto = (SearchDTO) request.get(TaxonomyAPIParams.search_criteria.name());
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == dto)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_SEARCH_CRITERIA.name(), "Empty Search Criteria");
        LOGGER.info("Search Taxonomy : " + dto);
        SearchCriteria sc = dto.searchCriteria();
        sc.add(SearchConditions.eq(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Concept"));
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
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Definition nodes JSON is empty");
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
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.object_type.name(),
                objectType);
        return getResponse(request, LOGGER);
    }

    @Override
    public Response deleteDefinition(String id, String objectType) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
        LOGGER.info("Delete Definition : " + id + " : Object Type : " + objectType);
        Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "deleteDefinition", GraphDACParams.object_type.name(),
                objectType);
        return getResponse(request, LOGGER);
    }

}
