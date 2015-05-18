package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
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
                GraphDACParams.object_type.name(), "Concept");
        request.put(GraphDACParams.get_tags.name(), true);
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
        if (null != nodes && !nodes.isEmpty()) {
            List<ConceptDTO> concepts = new ArrayList<ConceptDTO>();
            for (Node node : nodes) {
                ConceptDTO dto = new ConceptDTO(node, cfields);
                concepts.add(dto);
            }
            response.put(TaxonomyAPIParams.concepts.name(), concepts);
        }
        return response;
    }

    @Override
    public Response find(String id, String taxonomyId, String[] cfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        if (StringUtils.isBlank(id))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT_ID.name(), "Concept Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        Response response = copyResponse(getNodeRes);
        if (checkError(response)) {
            return response;
        }
        Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
        if (null != node) {
            ConceptDTO dto = new ConceptDTO(node, cfields);
            response.put(TaxonomyAPIParams.concept.name(), dto);
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response create(String taxonomyId, Request request) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Node concept = (Node) request.get(TaxonomyAPIParams.concept.name());
        if (null == concept)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT.name(), "Concept Object is blank");
        Request createReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
        createReq.put(GraphDACParams.node.name(), concept);
        Response createRes = getResponse(createReq, LOGGER);
        if (checkError(createRes)) {
            return createRes;
        } else {
            List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(TaxonomyAPIParams.metadata_definitions.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.object_type.name(), concept.getObjectType());
                defRequest.put(GraphDACParams.metadata_definitions.name(), newDefinitions);
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
        Node concept = (Node) request.get(TaxonomyAPIParams.concept.name());
        if (null == concept)
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_CONCEPT.name(), "Concept Object is blank");
        Request updateReq = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), concept);
        updateReq.put(GraphDACParams.node_id.name(), concept.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        if (checkError(updateRes)) {
            return updateRes;
        } else {
            List<MetadataDefinition> newDefinitions = (List<MetadataDefinition>) request.get(TaxonomyAPIParams.metadata_definitions.name());
            if (validateRequired(newDefinitions)) {
                Request defRequest = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "updateDefinition");
                defRequest.put(GraphDACParams.object_type.name(), concept.getObjectType());
                defRequest.put(GraphDACParams.metadata_definitions.name(), newDefinitions);
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
        Request request = getRequest(taxonomyId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode", GraphDACParams.node_id.name(), id);
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
        request.put(GraphDACParams.start_node_id.name(), startConceptId);
        request.put(GraphDACParams.relation_type.name(), relationType);
        request.put(GraphDACParams.end_node_id.name(), endConceptId);
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
        request.put(GraphDACParams.start_node_id.name(), id);
        request.put(GraphDACParams.relation_type.name(), relationType);
        if (depth > 0)
            request.put(GraphDACParams.depth.name(), depth);
        Response findRes = getResponse(request, LOGGER);
        Response response = copyResponse(findRes);
        if (checkError(response))
            return response;
        Graph graph = (Graph) findRes.get(GraphDACParams.sub_graph.name());
        if (null != graph && null != graph.getNodes() && !graph.getNodes().isEmpty()) {
            List<ConceptDTO> concepts = new ArrayList<ConceptDTO>();
            for (Node node : graph.getNodes()) {
                ConceptDTO dto = new ConceptDTO(node);
                concepts.add(dto);
            }
            response.put(TaxonomyAPIParams.concepts.name(), concepts);
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    public Response getConceptsGames(String taxonomyId, String[] cfields, String[] gfields) {
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchRelations");
        request.put(GraphDACParams.relation_type.name(), RelationTypes.ASSOCIATED_TO.relationName());
        request.put(GraphDACParams.direction.name(), RelationTraversal.DIRECTION_IN);
        List<FilterDTO> nodeFilters = new ArrayList<FilterDTO>();
        nodeFilters.add(new FilterDTO(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()));
        nodeFilters.add(new FilterDTO(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Concept"));
        request.put(GraphDACParams.start_node_filter.name(), nodeFilters);

        List<FilterDTO> relFilters = new ArrayList<FilterDTO>();
        relFilters.add(new FilterDTO(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()));
        relFilters.add(new FilterDTO(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), "Game"));
        request.put(GraphDACParams.related_node_filter.name(), relFilters);

        List<String> nodeFields = new ArrayList<String>();
        if (null != cfields && cfields.length > 0) {
            for (String field : cfields)
                nodeFields.add(field);
        } else {
            nodeFields.add("name");
        }
        request.put(GraphDACParams.start_node_fields.name(), nodeFields);

        List<String> relFields = new ArrayList<String>();
        if (null != gfields && gfields.length > 0) {
            for (String field : gfields)
                relFields.add(field);
        } else {
            relFields.add("name");
            relFields.add("identifier");
            relFields.add("purpose");
        }
        request.put(GraphDACParams.related_node_fields.name(), relFields);

        Response findRes = getResponse(request, LOGGER);
        if (checkError(findRes))
            return findRes;
        Response response = copyResponse(findRes);
        List<Map<String, Object>> result = (List<Map<String, Object>>) findRes.get(GraphDACParams.results.name());
        if (validateRequired(result)) {
            for (Map<String, Object> resultMap : result) {
                if (validateRequired(resultMap)) {
                    Map<String, Object> map = resultMap;
                    if (null != map && !map.isEmpty()) {
                        List<Map<String, Object>> relList = (List<Map<String, Object>>) map.get(RelationTypes.ASSOCIATED_TO.relationName());
                        map.put("games", relList);
                        map.remove(RelationTypes.ASSOCIATED_TO.relationName());
                    }
                }
            }
        }
        response.put(GraphDACParams.results.name(), result);
        return response;
    }

}
