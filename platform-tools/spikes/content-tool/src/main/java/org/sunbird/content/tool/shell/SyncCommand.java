package org.sunbird.content.tool.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.content.tool.service.ISyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SyncCommand extends BaseCommand implements CommandMarker {

    @Autowired
    @Qualifier("contentSyncService")
    ISyncService syncService;



    @CliCommand(value = "content-tool sync", help = "Content Sync")
    public void sync(@CliOption(key = {
            "filter"}, mandatory = false, help = "filters to search for ") final String filter,
                     @CliOption(key = {
                             "ids"}, mandatory = false, help = "identifiers to be synced") final String[] ids,
                     @CliOption(key = {
                             "objectType"}, mandatory = false, help = "object Type") final String objectType,
                     @CliOption(key = {
                             "createdBy"}, mandatory = false, help = "createdBy / user id") final String createdBy,
                     @CliOption(key = {
                             "lastUpdatedOn"}, mandatory = false, help = "last updated on date") final String lastUpdatedOn,
                     @CliOption(key = {
                             "dry-run"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "dry run ") String dryRun,
                     @CliOption(key = {
                             "force"}, mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "force update") String forceUpdate,
                     @CliOption(key = {
                             "limit"}, mandatory = false, help = "limit on sync count") String limit,
                     @CliOption(key = {
                             "offset"}, mandatory = false, help = "offset from which the sync should continue") String offset)
            throws Exception

    {

        System.out.println("-----------------------------------------");
        Map<String, Object> filters = prepareFilters(objectType, filter, ids, createdBy, lastUpdatedOn,  limit, offset, true, null);
        syncService.sync(filters, dryRun, forceUpdate);
        System.out.println("-----------------------------------------");
    }

}
