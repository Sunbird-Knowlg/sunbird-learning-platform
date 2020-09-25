package org.ekstep.sync.tool.shell;


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
                                  "objectType"}, mandatory = false, help = "course-batch or user-courses or batch-detail-update") final String objectType,
                          @CliOption(key = {
                                  "offset"}, mandatory = false, help = "offset") final String offset,
                          @CliOption(key = {
                                  "limit"}, mandatory = false, help = "limit") final String limit,
                          @CliOption(key = {
                                  "reset-progress"}, mandatory = false, unspecifiedDefaultValue="false", specifiedDefaultValue="true",  help = "ignored identifiers to sync") final String resetProgress,
                          @CliOption(key = {
                                  "ids"}, mandatory = false, help = "batchIds") final String[] batchIds,
                          @CliOption(key = {
                                  "courseIds"}, mandatory = false, help = "CourseIds") final String[] courseIds)
            throws Exception {
        System.out.println("Fetching data from cassandra for: " + objectType + ".");
        System.out.println("-----------------------------------------");
        batchEnrolmentSyncManager.sync(objectType, offset, limit, resetProgress, batchIds, courseIds);
        System.out.println("-----------------------------------------");
    }

    @CliCommand(value = "syncenrolment", help = "Sync data from lms cassandra to elasticsearch")
    public void syncEnrolment(
                          @CliOption(key = {
                                  "userId"}, mandatory = true, help = "userId") final String userId,
                          @CliOption(key = {
                                  "batchId"}, mandatory = true, help = "batchid") final String batchId)
            throws Exception {
        System.out.println("-----------------------------------------");
        batchEnrolmentSyncManager.syncEnrol(userId, batchId);
        System.out.println("-----------------------------------------");
    }
    
}
