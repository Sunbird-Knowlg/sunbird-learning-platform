package org.sunbird.sync.tool.util;

import java.util.HashMap;
import java.util.Map;

public class CassandraColumns {


    public static final Map<String, String> COLUMNS = new HashMap<String, String>(){{
        put("courseid", "courseId");
        put("batchid", "batchId");
        put("createdby", "createdBy");
        put("created_date","createdDate");
        put("createdfor", "createdFor");
        put("description", "description");
        put("end_date", "endDate");
        put("enrollment_enddate","enrollmentEndDate");
        put("enrollmenttype", "enrollmentType");
        put("mentors", "mentors");
        put("name","name");
        put("start_date", "startDate");
        put("status", "status");
        put("updated_date","updatedDate");
        put("userid", "userId");
        put("addedby", "addedBy");
        put("completed_on", "completedOn");
        put("active","active");
        put("completionpercentage","completionPercentage");
        put("contentstatus","contentStatus");
        put("datetime","dateTime");
        put("delta","delta");
        put("enrolled_date","enrolledDate");
        put("grade","grade");
        put("lastreadcontentid","lastReadContentId");
        put("lastreadcontentstatus","lastReadContentStatus");
        put("progress","progress");
        put("status","status");
    }};
}
