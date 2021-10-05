package org.sunbird.content.tool.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BaseCommand {

    private ObjectMapper mapper = new ObjectMapper();

    protected  Map<String, Object> prepareFilters(String objectType, String filter, String[] ids, String createdBy, String lastUpdatedOn, String limit, String offset, boolean removeStatus, String status) throws Exception {
        Map<String, Object> filters = new HashMap<>();

        if(StringUtils.isNoneBlank(filter))
            filters = mapper.readValue(filter, Map.class);

        if(null != ids && ids.length>0) {
            filters.put("identifier", Arrays.asList(ids));
        } else {
            if(StringUtils.isNotBlank(createdBy))
                filters.put("createdBy", createdBy);

            if(StringUtils.isNotBlank(lastUpdatedOn))
                filters.put("lastUpdatedOn", mapper.readValue(lastUpdatedOn, Object.class));
        }

        if(StringUtils.isNotBlank(limit) && !StringUtils.equalsIgnoreCase("0", limit))
            filters.put("limit", Integer.parseInt(limit));

        if(StringUtils.isNotBlank(offset) && !StringUtils.equalsIgnoreCase("0", offset))
            filters.put("offset", Integer.parseInt(offset));

        if(StringUtils.isNotBlank(objectType))
            filters.put("objectType", objectType);

        if(null != status) {
            if(StringUtils.isBlank(status)){
                filters.put("status", new ArrayList<>());
            }else {
                filters.put("status", Arrays.asList(status.split(",")));
            }
        }

        if(removeStatus)
            filters.remove("status");

        Map<String, Object>  removeAsset = new HashMap<>();
        removeAsset.put("ne", Arrays.asList("Asset", "Plugin"));
        filters.put("contentType", removeAsset);

        return filters;
    }

}
