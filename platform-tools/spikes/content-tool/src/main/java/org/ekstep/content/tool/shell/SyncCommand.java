package org.ekstep.content.tool.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.tool.service.ISyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SyncCommand implements CommandMarker {


    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    @Qualifier("contentSyncService")
    ISyncService syncService;



    @CliCommand(value = "content-tool sync", help = "Content Sync")
    public void sync(@CliOption(key = {
            "filter"}, mandatory = false, help = "filters to search for ") final String filter,
                     @CliOption(key = {
                             "ids"}, mandatory = false, help = "identifiers to be synced") final String[] ids,
                     @CliOption(key = {
                             "objectType"}, mandatory = false, unspecifiedDefaultValue = "Content", help = "object Type - Content by default") final String objectType,
                     @CliOption(key = {
                             "createdBy"}, mandatory = false, help = "createdBy / user id") final String createdBy,
                     @CliOption(key = {
                             "lastUpdatedOn"}, mandatory = false, help = "last updated on date") final String lastUpdatedOn,
                     @CliOption(key = {
                             "dry-run"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "dry run ") String dryRun,
                     @CliOption(key = {
                             "force"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "force update") String forceUpdate)
            throws Exception

    {

        System.out.println("-----------------------------------------");
        String filters = prepareFilters(objectType, filter, ids, createdBy, lastUpdatedOn);
        syncService.sync(filters, dryRun, forceUpdate);
        System.out.println("-----------------------------------------");
    }



    private String prepareFilters(String objectType, String filter, String[] ids, String createdBy, String lastUpdatedOn) throws Exception {
        Map<String, Object> filters = new HashMap<>();

        if(StringUtils.isNoneBlank(filter)){
            filters = mapper.readValue(filter, Map.class);
        }

        if(null != ids && ids.length>0){
            List<String> identifiers = Arrays.asList(ids);
            filters.put("identifier", identifiers);
        }
        if(StringUtils.isNotBlank(createdBy)){
            filters.put("createdBy", createdBy);
        }
        if(StringUtils.isNotBlank(lastUpdatedOn)){
            filters.put("lastUpdatedOn", mapper.readValue(lastUpdatedOn, Object.class));
        }
        filters.put("objectType", objectType);
        filters.remove("status");
        return mapper.writeValueAsString(filters);
    }
}
