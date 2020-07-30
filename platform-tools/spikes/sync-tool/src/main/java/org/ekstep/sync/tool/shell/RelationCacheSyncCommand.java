package org.ekstep.sync.tool.shell;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.sync.tool.mgr.RelationCacheSyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class RelationCacheSyncCommand implements CommandMarker {

    @Autowired
    @Qualifier("relationCacheSyncManager")
    RelationCacheSyncManager relationCacheSyncManager;

    @CliCommand(value = "sync-collection-rel-cache", help = "Generate the events to push Kafka events for Collection Relation Cache")
    public void syncRelationCache(
            @CliOption(key = {
                    "ids"}, mandatory = false, help = "ids") final String[] ids,
            @CliOption(key = {
        "all"}, mandatory = false, unspecifiedDefaultValue="false", specifiedDefaultValue="true",  help = "Generate events for all the Collections") final String forAll,
            @CliOption(key = {"limit"}, mandatory = false, unspecifiedDefaultValue="0", help = "ignored identifiers to sync") final int limit,
            @CliOption(key = {"offset"}, mandatory = false, unspecifiedDefaultValue="0", help = "ignored identifiers to sync") final int offset,
            @CliOption(key = {
                    "verbose"}, mandatory = false, unspecifiedDefaultValue="false", specifiedDefaultValue="true",  help = "Print more data for debug.") final String verbose
    ) throws Exception  {
        if (StringUtils.equalsIgnoreCase("true", forAll)) {
            System.out.println("Generating relations sync events for all collections.");
            int totalCollections = relationCacheSyncManager.getCollectionCount();
            int finalLimit = (limit > 0) ? limit : totalCollections;
            System.out.println("Total Collections: " + totalCollections + " and processing " + finalLimit + " collections.");
            boolean verboseBool = (StringUtils.equalsIgnoreCase("true", verbose)) ? true : false;
            if (totalCollections > 0) {
                relationCacheSyncManager.syncAllCollections(totalCollections, finalLimit, offset, verboseBool);
                System.out.println("Completed processing " + totalCollections + " collections.");
            }
        } else if (null != ids && CollectionUtils.isEmpty(Arrays.asList(ids))) {
            System.out.println("Generating relations sync events for collections with id: " + ids);
            // TODO: pending.
        } else {
            System.out.println("Required options are not available. Please provide 'ids' or 'all'");
        }
    }
}
