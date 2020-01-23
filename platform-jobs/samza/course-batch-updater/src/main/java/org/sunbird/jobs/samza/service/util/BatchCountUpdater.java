package org.sunbird.jobs.samza.service.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
    private static String installation = Platform.config.hasPath("sunbird.installation") ? Platform.config.getString("sunbird.installation"): "sunbird_dev";

    public void update(Map<String, Object> edata) throws Exception {
        String courseId = (String) edata.get(CourseBatchParams.courseId.name());
        updateBatchCount(courseId);
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
                if(batch.get("enrollmentType").equals("invite-only") && (Integer)batch.get("status")!= 2){
                    privateBatchCount=privateBatchCount+1;
                }
                if((batch.get("enrollmentType")).equals("open") && StringUtils.isEmpty((String)batch.get("enrollmentEndDate")) && (Integer)batch.get("status")!= 2){
                    openBatchCount=openBatchCount+1;
                }
                if(StringUtils.isNotEmpty((String)batch.get("enrollmentEndDate")) && currentDate.compareTo(format.parse((String)batch.get("enrollmentEndDate")))< 0){
                    openBatchCount=openBatchCount+1;
                }
            }
        }
        System.out.println("open batch "+openBatchCount);
        System.out.println("invite-only "+privateBatchCount);
        Request request = new Request();
        Map<String,Object> contentMap = new HashMap<>();
        contentMap.put("c_" + installation.toLowerCase() + "_open_batch_count".toLowerCase(), openBatchCount);
        contentMap.put("c_" + installation.toLowerCase() + "_private_batch_count".toLowerCase(), privateBatchCount);
        request.put("content", contentMap);
        systemUpdate(courseId, request);
    }
}
