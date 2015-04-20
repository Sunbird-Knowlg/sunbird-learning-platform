package com.ilimi.taxonomy.mgr.impl;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.exception.ClientException;
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
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Component
public class TaxonomyManagerImpl extends BaseManager implements ITaxonomyManager {

    private static Logger LOGGER = LogManager.getLogger(ITaxonomyManager.class.getName());

    @Override
    public Response findAll() {
        return null;
    }

    @Override
    public Response find(String id, boolean subgraph) {
        if (StringUtils.isBlank(id)) {
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        }
        LOGGER.info("Find Taxonomy : " + id);
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.NODE_ID.name(), new StringValue(
                id));
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        } else {
            Node node = (Node) getNodeRes.get(GraphDACParams.NODE.name());
            if (null != node)
                response.put(TaxonomyAPIParams.TAXONOMY.name(), node);
            if (subgraph) {
                Request req = new Request(request);
                req.put(GraphDACParams.START_NODE_ID.name(), new StringValue(id));
                req.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(RelationTypes.HIERARCHY.relationName()));
                req.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
                req.setOperation("getSubGraph");
                Response subGraphRes = getResponse(req, LOGGER);
                if (checkError(subGraphRes)) {
                    response = copyResponse(response, subGraphRes);
                } else {
                    Graph subGraph = (Graph) subGraphRes.get(GraphDACParams.SUB_GRAPH.name());
                    if (null != subGraph) {
                        response.put(TaxonomyAPIParams.SUBGRAPH.name(), subGraph);
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
        request.put(GraphEngineParams.FORMAT.name(), new StringValue(ImportType.CSV.name()));
        request.put(GraphEngineParams.INPUT_STREAM.name(), new InputStreamValue(stream));
        return getResponse(request, LOGGER);
    }

    @Override
    public Response update(String id, InputStream stream) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == stream)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_INPUT_STREAM.name(), "Taxonomy object is emtpy");
        LOGGER.info("Update Taxonomy : " + stream);
        Request request = getRequest(id, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
        request.put(GraphEngineParams.FORMAT.name(), new StringValue(ImportType.CSV.name()));
        request.put(GraphEngineParams.INPUT_STREAM.name(), new InputStreamValue(stream));
        return getResponse(request, LOGGER);
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
        SearchDTO dto = (SearchDTO) request.get(TaxonomyAPIParams.SEARCH_CRITERIA.name());
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == dto)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_EMPTY_SEARCH_CRITERIA.name(), "Empty Search Criteria");
        LOGGER.info("Search Taxonomy : " + dto);
        SearchCriteria sc = dto.searchCriteria();
        sc.add(SearchConditions.eq(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Concept"));
        request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
        request.setOperation("searchNodes");
        Response searchRes = getResponse(request, LOGGER);
        Response response = copyResponse(searchRes);
        if (!checkError(response)) {
            BaseValueObjectList<Node> nodeList = (BaseValueObjectList<Node>) searchRes.get(GraphDACParams.NODE_LIST.name());
            if (null != nodeList && null != nodeList.getValueObjectList() && !nodeList.getValueObjectList().isEmpty()) {
                response.put(TaxonomyAPIParams.CONCEPTS.name(), nodeList);
            }
        }
        return response;
    }

    @Override
    public Response createDefinition(String id, Request request) {
        DefinitionDTO dto = (DefinitionDTO) request.get(TaxonomyAPIParams.DEFINITION_NODE.name());
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (null == dto || StringUtils.isBlank(dto.getObjectType()))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Input Definition dto is null");
        LOGGER.info("Create Definition : " + dto);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("saveDefinitionNode");
        return getResponse(request, LOGGER);
    }

    @Override
    public Response updateDefinition(String id, String objectType, Request request) {
        DefinitionDTO dto = (DefinitionDTO) request.get(TaxonomyAPIParams.DEFINITION_NODE.name());
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
        if (null == dto)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Input Definition dto is null");
        LOGGER.info("Update Definition : " + dto);
        dto.setObjectType(objectType);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("saveDefinitionNode");
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
        Request request = getRequest(id, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.OBJECT_TYPE.name(),
                new StringValue(objectType));
        return getResponse(request, LOGGER);
    }

    @Override
    public Response deleteDefinition(String id, String objectType) {
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_NULL_DEFINITION.name(), "Object Type is empty");
        LOGGER.info("Delete Definition : " + id + " : Object Type : " + objectType);
        Request request = getRequest(id, GraphEngineManagers.NODE_MANAGER, "deleteDefinition", GraphDACParams.OBJECT_TYPE.name(),
                new StringValue(objectType));
        return getResponse(request, LOGGER);
    }

}
