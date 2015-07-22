package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.LearningObjectAPIParams;
import com.ilimi.taxonomy.enums.LearningObjectErrorCodes;
import com.ilimi.taxonomy.mgr.IGameManager;

@Component
public class GameManagerImpl extends BaseManager implements IGameManager {

    private static Logger LOGGER = LogManager.getLogger(IGameManager.class.getName());

    private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();
    private static final int DEFAULT_TTL = 24;

    private static final int DEFAULT_LIMIT = 50;

    static {
        DEFAULT_FIELDS.add("identifier");
        DEFAULT_FIELDS.add("name");
        DEFAULT_FIELDS.add("description");
        DEFAULT_FIELDS.add("appIcon");
        DEFAULT_FIELDS.add("url");

        DEFAULT_STATUS.add("Live");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response listGames(Request request) {
        String taxonomyId = (String) request.get(PARAM_SUBJECT);
        LOGGER.info("List Games : " + taxonomyId);
        DefinitionDTO definition = null;
        String objectType = LearningObjectManagerImpl.OBJECT_TYPE;
        List<Request> requests = new ArrayList<Request>();
        if (StringUtils.isNotBlank(taxonomyId)) {
            definition = getDefinition(taxonomyId);
            Request req = getGamesListRequest(request, taxonomyId, objectType, definition);
            requests.add(req);
        } else {
            definition = getDefinition(TaxonomyManagerImpl.taxonomyIds[0]);
            for (String id : TaxonomyManagerImpl.taxonomyIds) {
                Request req = getGamesListRequest(request, id, objectType, definition);
                requests.add(req);
            }
        }
        Response response = getResponse(requests, LOGGER, GraphDACParams.node_list.name(), LearningObjectAPIParams.games.name());
        Response listRes = copyResponse(response);
        if (checkError(response))
            return response;
        else {
            List<List<Node>> nodes = (List<List<Node>>) response.get(LearningObjectAPIParams.games.name());
            List<Map<String, Object>> games = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                for (List<Node> list : nodes) {
                    if (null != list && !list.isEmpty()) {
                        for (Node node : list) {
                            games.add(node.getMetadata());
                        }
                    }
                }
            }
            listRes.put(LearningObjectAPIParams.games.name(), games);
            Integer ttl = null;
            if (null != definition && null != definition.getMetadata())
                ttl = (Integer) definition.getMetadata().get(PARAM_TTL);
            if (null == ttl || ttl.intValue() <= 0)
                ttl = DEFAULT_TTL;
            listRes.put(PARAM_TTL, ttl);
            return listRes;
        }
    }

    @SuppressWarnings("unchecked")
    private Request getGamesListRequest(Request request, String taxonomyId, String objectType, DefinitionDTO definition) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
        setLimit(request, sc, definition);

        ObjectMapper mapper = new ObjectMapper();
        // status filter
        List<String> statusList = new ArrayList<String>();
        Object statusParam = request.get(PARAM_STATUS);
        if (null != statusParam)
            statusList = getList(mapper, statusParam, PARAM_STATUS);
        if (null == statusList || statusList.isEmpty()) {
            if (null != definition && null != definition.getMetadata()) {
                String[] arr = (String[]) definition.getMetadata().get(PARAM_STATUS);
                if (null != arr && arr.length > 0) {
                    statusList = Arrays.asList(arr);
                }
            }
        }
        if (null == statusList || statusList.isEmpty())
            statusList = DEFAULT_STATUS;
        MetadataCriterion mc = MetadataCriterion.create(Arrays.asList(new Filter(PARAM_STATUS, SearchConditions.OP_IN, statusList)));

        // set metadata filter params
        for (Entry<String, Object> entry : request.getRequest().entrySet()) {
            if (!StringUtils.equalsIgnoreCase(PARAM_SUBJECT, entry.getKey()) && !StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_LIMIT, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_UID, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_STATUS, entry.getKey())) {
                List<String> list = getList(mapper, entry.getValue(), entry.getKey());
                if (null != list && !list.isEmpty()) {
                    mc.addFilter(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                }
            }
        }
        sc.addMetadata(mc);
        Object objFields = request.get(PARAM_FIELDS);
        List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
        if (null == fields || fields.isEmpty()) {
            if (null != definition && null != definition.getMetadata()) {
                String[] arr = (String[]) definition.getMetadata().get(PARAM_FIELDS);
                if (null != arr && arr.length > 0) {
                    fields = Arrays.asList(arr);
                }
            }
        }
        if (null == fields || fields.isEmpty())
            fields = DEFAULT_FIELDS;
        sc.setFields(fields);

        Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes", GraphDACParams.search_criteria.name(), sc);
        return req;
    }

    private void setLimit(Request request, SearchCriteria sc, DefinitionDTO definition) {
        Integer defaultLimit = null;
        if (null != definition && null != definition.getMetadata())
            defaultLimit = (Integer) definition.getMetadata().get(PARAM_LIMIT);
        if (null == defaultLimit || defaultLimit.intValue() <= 0)
            defaultLimit = DEFAULT_LIMIT;
        Integer limit = null;
        try {
            Object obj = request.get(PARAM_LIMIT);
            if (obj instanceof String)
                limit = Integer.parseInt((String) obj);
            else
                limit = (Integer) request.get(PARAM_LIMIT);
            if (null == limit || limit.intValue() <= 0)
                limit = defaultLimit;
        } catch (Exception e) {
        }
        sc.setResultSize(limit);
    }

    private DefinitionDTO getDefinition(String taxonomyId) {
        Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
                GraphDACParams.object_type.name(), LearningObjectManagerImpl.OBJECT_TYPE);
        Response response = getResponse(request, LOGGER);
        if (!checkError(response)) {
            DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
            return definition;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private List getList(ObjectMapper mapper, Object object, String propName) {
        if (null != object) {
            try {
                String strObject = mapper.writeValueAsString(object);
                List list = mapper.readValue(strObject.toString(), List.class);
                return list;
            } catch (Exception e) {
                throw new ClientException(LearningObjectErrorCodes.ERR_GAME_INVALID_PARAM.name(), "Request Parameter '" + propName
                        + "' should be a list");
            }
        }
        return null;
    }
}
