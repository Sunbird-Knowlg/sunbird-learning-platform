package org.sunbird.jobs.samza.service.util;

import apoc.coll.Coll;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class BatchCountUpdater extends BaseCourseBatchUpdater {
    private static String jobTimeZone = Platform.config.hasPath("job.time_zone") ? Platform.config.getString("job.time_zone"): "IST";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private static final String courseBatchTable = "course_batch";
    private static String installation = Platform.config.hasPath("sunbird.installation") ? Platform.config.getString("sunbird.installation").toLowerCase(): "sunbird_dev";

    public void update(Map<String, Object> edata) throws Exception {
        String courseId = (String) edata.get(CourseBatchParams.courseId.name());
        String batchId = (String) edata.get(CourseBatchParams.batchId.name());
        updateBatchCount(courseId);
        addBatchDetailsToCourse(courseId,batchId);
    }

    private void updateBatchCount(String courseId) throws Exception{
        Date currentDate = format.parse(format.format(new Date()));
        format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
        int openBatchCount = 0;
        int privateBatchCount = 0;
        Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
            put("courseid", courseId);
        }};
        List<Map<String,Object>> courseBatches = SunbirdCassandraUtil.readAsListOfMap(keyspace,courseBatchTable,dataToSelect);
        if(CollectionUtils.isNotEmpty(courseBatches)){
            for(Map<String,Object> batch : courseBatches){
                String enrollmentType =(String) batch.get("enrollmentType");
                int batchStatus = (Integer)batch.get("status");

                if(batchStatus< 2 && enrollmentType.equals("invite-only")){
                    privateBatchCount=privateBatchCount+1;
                }
                if(batchStatus< 2 && StringUtils.isEmpty((String)batch.get("enrollmentEndDate")) && enrollmentType.equals("open")){
                    openBatchCount=openBatchCount+1;
                }
                if(StringUtils.isNotEmpty((String)batch.get("enrollmentEndDate")) && currentDate.compareTo(format.parse((String)batch.get("enrollmentEndDate")))< 0){
                    openBatchCount=openBatchCount+1;
                }
            }
        }
        Request request = new Request();
        Map<String,Object> contentMap = new HashMap<>();
        contentMap.put("c_" + installation + "_open_batch_count".toLowerCase(), openBatchCount);
        contentMap.put("c_" + installation + "_private_batch_count".toLowerCase(), privateBatchCount);
        request.put("content", contentMap);
        systemUpdate(courseId, request);
    }

    private void addBatchDetailsToCourse(String courseId, String batchId) throws Exception{
        Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
            put("courseid",courseId);
            put("batchid",batchId);
        }};
        Map<String,Object> batchDetails = SunbirdCassandraUtil.readAsListOfMap(keyspace,courseBatchTable,dataToSelect).get(0);
        Map<String,Object> courseDetails = getContent(courseId,"batchDetails");
        Map<String, Object> batchDataForCourse= new HashMap<String, Object>(){{
            put(CourseBatchParams.batchId.name(),batchId);
            put("startDate",batchDetails.get("startDate"));
            put("endDate",batchDetails.get("endDate"));
            put("enrollmentEndDate",batchDetails.get("enrollmentEndDate"));
        }};
        List<Map<String,Object>> courseBatchMetaData =(List<Map<String,Object>>) courseDetails.get("batchDetails");

        if(CollectionUtils.isNotEmpty(courseBatchMetaData)){
            courseBatchMetaData.add(batchDataForCourse);
        }
        else{
            courseBatchMetaData = new ArrayList<>();
            courseBatchMetaData.add(batchDataForCourse);
        }
        Request request = new Request();
        Map<String,Object> contentMap = new HashMap<>();
        contentMap.put("batchDetails",courseBatchMetaData);
        request.put("content", contentMap);
        systemUpdate(courseId,request);
    }
}
