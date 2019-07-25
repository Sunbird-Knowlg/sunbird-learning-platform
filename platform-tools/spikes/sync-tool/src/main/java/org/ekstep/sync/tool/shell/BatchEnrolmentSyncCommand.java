package org.ekstep.sync.tool.shell;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ClientException;
import org.ekstep.sync.tool.mgr.BatchEnrolmentSyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class BatchEnrolmentSyncCommand implements CommandMarker {

    @Autowired
    @Qualifier("batchEnrolmentSyncManager")
    BatchEnrolmentSyncManager batchEnrolmentSyncManager;


    @CliCommand(value = "syncbatch", help = "Sync data from lms cassandra to elasticsearch")
    public void syncByIds(@CliOption(key = {
                                  "objectType"}, mandatory = false, help = "course-batch and/or user-courses") final String objectType,
                          @CliOption(key = {
                                  "offset"}, mandatory = false, help = "offset") final String offset,
                          @CliOption(key = {
                                  "limit"}, mandatory = false, help = "limit") final String limit,
                          @CliOption(key = {
                                  "reset-progress"}, mandatory = false , help = "ignored identifiers to sync") final Boolean resetProgress)
            throws Exception {
        System.out.println("Fetching data from cassandra for: " + objectType + ".");
        System.out.println("-----------------------------------------");
        batchEnrolmentSyncManager.sync(objectType, offset, limit, resetProgress);
        System.out.println("-----------------------------------------");
    }
}
