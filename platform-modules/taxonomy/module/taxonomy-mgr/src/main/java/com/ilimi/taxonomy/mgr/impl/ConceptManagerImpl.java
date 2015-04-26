package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.BooleanValue;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.FilterDTO;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.dto.ConceptDTO;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;
import com.ilimi.taxonomy.mgr.IConceptManager;

@Component
public class ConceptManagerImpl extends BaseManager implements IConceptManager {

    private static Logger LOGGER = LogManager.getLogger(IConceptManager.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String taxonomyId, String[] cfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        LOGGER.info("Find All Concepts : " + taxonomyId + " | cfields: " + cfields);
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType",
                GraphDACParams.OBJECT_TYPE.name(), new StringValue("Concept"));
        request.put(GraphDACParams.GET_TAGS.name(), new BooleanValue(true));
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        BaseValueObjectList<Node> nodes = (BaseValueObjectList<Node>) findRes.get(GraphDACParams.NODE_LIST.name());
        if (null != nodes && null != nodes.getValueObjectList() && !nodes.getValueObjectList().isEmpty()) {
            List<ConceptDTO> concepts = new ArrayList<ConceptDTO>();
            for (Node node : nodes.getValueObjectList()) {
                ConceptDTO dto = new ConceptDTO(node, cfields);
                concepts.add(dto);
            }
            response.put(TaxonomyAPIParams.CONCEPTS.name(), new BaseValueObjectList<ConceptDTO>(concepts));
        }
        return response;
    }

    @Override
    public Response find(String id, String taxonomyId, String[] cfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT_ID.name(), "Concept Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.NODE_ID.name(),
                new StringValue(id));
        request.put(GraphDACParams.GET_TAGS.name(), new BooleanValue(true));
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.NODE.name());
        if (null != node) {
            ConceptDTO dto = new ConceptDTO(node, cfields);
            response.put(TaxonomyAPIParams.CONCEPT.name(), dto);
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response create(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node concept = (Node) request.get(TaxonomyAPIParams.CONCEPT.name());
        if (null == concept)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT.name(), "Concept Object is blank");
        Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
        createReq.put(GraphDACParams.NODE.name(), concept);
        Response createRes = getResponse(createReq, LOGGER);
        if (checkError(createRes)) {
            return createRes;
        } else {
            BaseValueObjectList<MetadataDefinition> newDefinitions = (BaseValueObjectList<MetadataDefinition>) request
                    .get(TaxonomyAPIParams.METADATA_DEFINITIONS.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.OBJECT_TYPE.name(), new StringValue(concept.getObjectType()));
                defRequest.put(GraphDACParams.METADATA_DEFINITIONS.name(), newDefinitions);
                Response defResponse = getResponse(defRequest, LOGGER);
                if (checkError(defResponse)) {
                    return defResponse;
                }
            }
        }
        return createRes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response update(String id, String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT_ID.name(), "Concept Id is blank");
        Node concept = (Node) request.get(TaxonomyAPIParams.CONCEPT.name());
        if (null == concept)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT.name(), "Concept Object is blank");
        Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.NODE.name(), concept);
        updateReq.put(GraphDACParams.NODE_ID.name(), new StringValue(concept.getIdentifier()));
        Response updateRes = getResponse(updateReq, LOGGER);
        if (checkError(updateRes)) {
            return updateRes;
        } else {
            BaseValueObjectList<MetadataDefinition> newDefinitions = (BaseValueObjectList<MetadataDefinition>) request
                    .get(TaxonomyAPIParams.METADATA_DEFINITIONS.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.OBJECT_TYPE.name(), new StringValue(concept.getObjectType()));
                defRequest.put(GraphDACParams.METADATA_DEFINITIONS.name(), newDefinitions);
                Response defResponse = getResponse(defRequest, LOGGER);
                if (checkError(defResponse)) {
                    return defResponse;
                }
            }
        }
        return updateRes;
    }

    @Override
    public Response delete(String id, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT_ID.name(), "Concept Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.NODE_ID.name(),
                new StringValue(id));
        return getResponse(request, LOGGER);
    }

    @Override
    public Response deleteRelation(String startConceptId, String relationType, String endConceptId, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(startConceptId) || StringUtils.isBlank(relationType) || StringUtils.isBlank(endConceptId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_UPDATE_CONCEPT.name(),
                    "Start Concept Id, Relation Type and End Concept Id are required to delete relation");
        Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER, "removeRelation");
        request.put(GraphDACParams.START_NODE_ID.name(), new StringValue(startConceptId));
        request.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(relationType));
        request.put(GraphDACParams.END_NODE_ID.name(), new StringValue(endConceptId));
        return getResponse(request, LOGGER);
    }

    @Override
    public Response getConcepts(String id, String relationType, int depth, String taxonomyId) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT_ID.name(), "Concept Id is blank");
        if (StringUtils.isBlank(relationType))
            relationType = RelationTypes.HIERARCHY.relationName();
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getSubGraph");
        request.put(GraphDACParams.START_NODE_ID.name(), new StringValue(id));
        request.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(relationType));
        if (depth > 0)
            request.put(GraphDACParams.DEPTH.name(), new Identifier(depth));
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        Graph graph = (Graph) findRes.get(GraphDACParams.SUB_GRAPH.name());
        if (null != graph && null != graph.getNodes() && !graph.getNodes().isEmpty()) {
            List<ConceptDTO> concepts = new ArrayList<ConceptDTO>();
            for (Node node : graph.getNodes()) {
                ConceptDTO dto = new ConceptDTO(node);
                concepts.add(dto);
            }
            response.put(TaxonomyAPIParams.CONCEPTS.name(), new BaseValueObjectList<ConceptDTO>(concepts));
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    public Response getConceptsGames(String taxonomyId, String[] cfields, String[] gfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchRelations");
        request.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(RelationTypes.ASSOCIATED_TO.relationName()));
        request.put(GraphDACParams.DIRECTION.name(), new Identifier(RelationTraversal.DIRECTION_IN));
        List<FilterDTO> nodeFilters = new ArrayList<FilterDTO>();
        nodeFilters.add(new FilterDTO(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()));
        nodeFilters.add(new FilterDTO(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Concept"));
        request.put(GraphDACParams.START_NODE_FILTER.name(), new BaseValueObjectList<FilterDTO>(nodeFilters));

        List<FilterDTO> relFilters = new ArrayList<FilterDTO>();
        relFilters.add(new FilterDTO(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()));
        relFilters.add(new FilterDTO(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Game"));
        request.put(GraphDACParams.RELATED_NODE_FILTER.name(), new BaseValueObjectList<FilterDTO>(relFilters));

        List<StringValue> nodeFields = new ArrayList<StringValue>();
        if (null != cfields && cfields.length > 0) {
            for (String field : cfields)
                nodeFields.add(new StringValue(field));
        } else {
            nodeFields.add(new StringValue("name"));
        }
        request.put(GraphDACParams.START_NODE_FIELDS.name(), new BaseValueObjectList<StringValue>(nodeFields));

        List<StringValue> relFields = new ArrayList<StringValue>();
        if (null != gfields && gfields.length > 0) {
            for (String field : gfields)
                relFields.add(new StringValue(field));
        } else {
            relFields.add(new StringValue("name"));
            relFields.add(new StringValue("identifier"));
            relFields.add(new StringValue("purpose"));
        }
        request.put(GraphDACParams.RELATED_NODE_FIELDS.name(), new BaseValueObjectList<StringValue>(relFields));

        Response findRes = getResponse(request, LOGGER);
        if (checkError(findRes))
            return findRes;
        Response response = copyResponse(findRes);
        BaseValueObjectList<BaseValueObjectMap<Object>> result = (BaseValueObjectList<BaseValueObjectMap<Object>>) findRes
                .get(GraphDACParams.RESULTS.name());
        if (validateRequired(result)) {
            for (BaseValueObjectMap<Object> resultMap : result.getValueObjectList()) {
                if (validateRequired(resultMap)) {
                    Map<String, Object> map = resultMap.getBaseValueMap();
                    if (null != map && !map.isEmpty()) {
                        List<Map<String, Object>> relList = (List<Map<String, Object>>) map.get(RelationTypes.ASSOCIATED_TO.relationName());
                        map.put("games", relList);
                        map.remove(RelationTypes.ASSOCIATED_TO.relationName());
                    }
                }
            }
        }
        response.put(GraphDACParams.RESULTS.name(), result);
        return response;
    }

}
