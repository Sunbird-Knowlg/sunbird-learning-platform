package org.sunbird.jobs.samza.util;

import java.util.Map;
import java.util.HashMap;

public class SunbirdCassandraColumnMapper {

    private static Map<String, String> COLUMN_MAPPING = new HashMap<>();

    public static Map<String, String> getColumnMapping() {
        if(COLUMN_MAPPING.isEmpty()) {
            //sunbird_courses.user_courses table
            COLUMN_MAPPING.put("batchid", "batchId");
            COLUMN_MAPPING.put("userid", "userId");
            COLUMN_MAPPING.put("active", "active");
            COLUMN_MAPPING.put("addedby", "addedBy");
            COLUMN_MAPPING.put("completedon","completedOn");
            COLUMN_MAPPING.put("completionpercentage","completionPercentage");
            COLUMN_MAPPING.put("contentstatus", "contentStatus");
            COLUMN_MAPPING.put("courseid", "courseId");
            COLUMN_MAPPING.put("datetime", "dateTime");
            COLUMN_MAPPING.put("delta", "delta");
            COLUMN_MAPPING.put("enrolleddate","enrolledDate");
            COLUMN_MAPPING.put("grade","grade");
            COLUMN_MAPPING.put("lastreadcontentid", "lastReadContentId");
            COLUMN_MAPPING.put("lastreadcontentstatus", "lastReadContentStatus");
            COLUMN_MAPPING.put("progress","progress");
            COLUMN_MAPPING.put("status","status");

            //sunbird_courses.content_consumption table
            COLUMN_MAPPING.put("contentid", "contentId");
            COLUMN_MAPPING.put("completedcount", "completedCount");
            COLUMN_MAPPING.put("contentversion", "contentVersion");
            COLUMN_MAPPING.put("lastaccesstime", "lastAccessTime");
            COLUMN_MAPPING.put("lastcompletedtime","lastCompletedTime");
            COLUMN_MAPPING.put("lastupdatedtime","lastUpdatedTime");
            COLUMN_MAPPING.put("result", "result");
            COLUMN_MAPPING.put("score", "score");
            COLUMN_MAPPING.put("viewcount", "viewCount");

            //sunbird_courses.course_batch table
            COLUMN_MAPPING.put("createdby", "createdBy");
            COLUMN_MAPPING.put("createddate","createdDate");
            COLUMN_MAPPING.put("createdfor","createdFor");
            COLUMN_MAPPING.put("description", "description");
            COLUMN_MAPPING.put("enddate", "endDate");
            COLUMN_MAPPING.put("enrollmentenddate","enrollmentEndDate");
            COLUMN_MAPPING.put("enrollmenttype","enrollmentType");
            COLUMN_MAPPING.put("mentors", "mentors");
            COLUMN_MAPPING.put("name", "name");
            COLUMN_MAPPING.put("startdate","startDate");
            COLUMN_MAPPING.put("updateddate","updatedDate");
        }
        return COLUMN_MAPPING;
    }
}
