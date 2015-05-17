package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.enums.LearningObjectAPIParams;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;
import com.ilimi.taxonomy.mgr.IGameManager;

@Component
public class GameManagerImpl extends BaseManager implements IGameManager {

    private static Logger LOGGER = LogManager.getLogger(IGameManager.class.getName());
    private static final String PARAM_SUBJECT = "subject";
    private static final String PARAM_FIELDS = "fields";
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_UID = "uid";
    private static final List<String> DEFAULT_FIELDS = new ArrayList<String>();
    private static final int DEFAULT_LIMIT = 50;

    static {
        DEFAULT_FIELDS.add("identifier");
        DEFAULT_FIELDS.add("name");
        DEFAULT_FIELDS.add("description");
        DEFAULT_FIELDS.add("posterImage");
        DEFAULT_FIELDS.add("appIcon");
        DEFAULT_FIELDS.add("purpose");
        DEFAULT_FIELDS.add("status");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response listGames(Request request) {
        String taxonomyId = (String) request.get(PARAM_SUBJECT);
        if (StringUtils.isBlank(taxonomyId))
            throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_BLANK_TAXONOMY_ID.name(), "Taxonomy Id is blank");
        LOGGER.info("List Games : " + taxonomyId);
        String objectType = LearningObjectManagerImpl.OBJECT_TYPE;
        SearchCriteria sc = new SearchCriteria();
        sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()));
        sc.add(SearchConditions.eq(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), objectType));
        sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
        Integer limit = DEFAULT_LIMIT;
        try {
            limit = (Integer) request.get(PARAM_LIMIT);
            if (null == limit || limit.intValue() <= 0)
                limit = DEFAULT_LIMIT;
        } catch (Exception e) {
        }
        ObjectMapper mapper = new ObjectMapper();

        // Return only active or live games.
        List<String> statusList = new ArrayList<String>();
        statusList.add("Active");
        statusList.add("Live");
        sc.add(SearchConditions.in("status", statusList));
        // Temp: commented out for first version. Search criteria is not used.
        /*for (Entry<String, Object> entry : request.getRequest().entrySet()) {
            if (!StringUtils.equalsIgnoreCase(PARAM_SUBJECT, entry.getKey()) && !StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_LIMIT, entry.getKey())
                    && !StringUtils.equalsIgnoreCase(PARAM_UID, entry.getKey())) {
                List<String> list = getList(mapper, entry.getValue());
                if (null != list && !list.isEmpty()) {
                    sc.add(SearchConditions.in(entry.getKey(), list));
                }
            }
        }*/
        Object objFields = request.get(PARAM_FIELDS);
        List<String> fields = getList(mapper, objFields);
        if (null == fields || fields.isEmpty())
            fields = DEFAULT_FIELDS;
        sc.returnFields(fields);

        Request req = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes", GraphDACParams.search_criteria.name(), sc);
        Response response = getResponse(req, LOGGER);
        Response listRes = copyResponse(response);
        if (checkError(response))
            return response;
        else {
            List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
            List<Map<String, Object>> games = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    games.add(node.getMetadata());
                }
            }
            listRes.put(LearningObjectAPIParams.games.name(), games);
            return listRes;
        }
    }

    @SuppressWarnings("rawtypes")
    private List getList(ObjectMapper mapper, Object object) {
        if (null != object) {
            try {
                String strObject = mapper.writeValueAsString(object);
                List list = mapper.readValue(strObject.toString(), List.class);
                return list;
            } catch (Exception e) {
            }
        }
        return null;
    }
}
