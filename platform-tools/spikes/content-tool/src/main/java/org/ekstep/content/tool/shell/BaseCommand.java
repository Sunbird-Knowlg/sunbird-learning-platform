package org.ekstep.content.tool.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BaseCommand {

    private ObjectMapper mapper = new ObjectMapper();

    protected String prepareFilters(String objectType, String filter, String[] ids, String createdBy, String lastUpdatedOn, String limit, String offset, boolean removeStatus) throws Exception {
        Map<String, Object> filters = new HashMap<>();

        if(StringUtils.isNoneBlank(filter))
            filters = mapper.readValue(filter, Map.class);

        if(null != ids && ids.length>0)
            filters.put("identifier", Arrays.asList(ids));

        if(StringUtils.isNotBlank(createdBy))
            filters.put("createdBy", createdBy);

        if(StringUtils.isNotBlank(lastUpdatedOn))
            filters.put("lastUpdatedOn", mapper.readValue(lastUpdatedOn, Object.class));

        if(StringUtils.isNotBlank(limit) && !StringUtils.equalsIgnoreCase("0", limit))
            filters.put("limit", Integer.parseInt(limit));

        if(StringUtils.isNotBlank(limit) && !StringUtils.equalsIgnoreCase("0", offset))
            filters.put("offset", Integer.parseInt(offset));

        filters.put("objectType", objectType);

        if(removeStatus)
            filters.remove("status");

        return mapper.writeValueAsString(filters);
    }

}
