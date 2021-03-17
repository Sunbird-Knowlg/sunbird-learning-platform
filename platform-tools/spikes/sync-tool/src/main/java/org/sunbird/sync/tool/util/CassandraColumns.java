package org.sunbird.sync.tool.util;

import java.util.HashMap;
import java.util.Map;

public class CassandraColumns {


    public static final Map<String, String> COLUMNS = new HashMap<String, String>(){{
        put("courseid", "courseId");
        put("batchid", "batchId");
        put("createdby", "createdBy");
        put("createddate","createdDate");
        put("createdfor", "createdFor");
        put("description", "description");
        put("enddate", "endDate");
        put("enrollmentenddate","enrollmentEndDate");
        put("enrollmenttype", "enrollmentType");
        put("mentors", "mentors");
        put("name","name");
        put("startdate", "startDate");
        put("status", "status");
        put("updateddate","updatedDate");
        put("userid", "userId");
        put("addedby", "addedBy");
        put("completedon", "completedOn");
        put("active","active");
        put("completionpercentage","completionPercentage");
        put("contentstatus","contentStatus");
        put("datetime","dateTime");
        put("delta","delta");
        put("enrolleddate","enrolledDate");
        put("grade","grade");
        put("lastreadcontentid","lastReadContentId");
        put("lastreadcontentstatus","lastReadContentStatus");
        put("progress","progress");
        put("status","status");
    }};
}
