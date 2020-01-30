package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class BatchStatusUtil {

    private static String jobTimeZone = Platform.config.hasPath("job.time_zone") ? Platform.config.getString("job.time_zone"): "IST";
    private static JobLogger LOGGER = new JobLogger(BatchStatusUtil.class);
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private static final String table = Platform.config.hasPath("courses.table.course.batch.name")
            ? Platform.config.getString("courses.table.course.batch.name")
            : "course_batch";
    private static final String topic = Platform.config.getString("task.inputs").replace("kafka.","");

    public static void updateOnGoingBatch(MessageCollector collector) {
        try {
            Date currentDate = format.parse(format.format(new Date()));
            format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("status", 0);
            }};
            ResultSet resultSet = SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
            List<Row> rows = resultSet.all();
            if(CollectionUtils.isNotEmpty(rows)) {
                List<String> batchIds = new ArrayList<>();
                for (Row row : rows) {

                    if (StringUtils.isNotBlank(row.getString("startdate"))) {
                        Date startDate = format.parse(row.getString("startdate"));
                        if (currentDate.compareTo(startDate) >= 0) {
                            batchIds.add(row.getString("batchid"));
                            Map<String, Object> event = getBatchStatusEvent(row.getString("batchid"), row.getString("courseid"), 1);
                            collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", topic), event));
                        }

                    }
                }
                LOGGER.info("BatchIds updated to in-progress : " + batchIds);
            } else {
                LOGGER.info("No batch data to update the status to in-progress");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateCompletedBatch(MessageCollector collector) {
        try {
            Date currentDate = format.parse(format.format(new Date()));
            format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("status", 1);
            }};
            ResultSet resultSet = SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
            List<Row> rows = resultSet.all();
            if(CollectionUtils.isNotEmpty(rows)) {
                List<String> batchIds = new ArrayList<>();
                List<String> courseIds = new ArrayList<>();
                for (Row row : rows) {
                    if (StringUtils.isNotBlank(row.getString("enddate"))) {
                        Date startDate = format.parse(row.getString("enddate"));
                        if (currentDate.compareTo(startDate) > 0) {
                            batchIds.add(row.getString("batchid"));
                            Map<String, Object> event = getBatchStatusEvent(row.getString("batchid"), row.getString("courseid"), 2);
                            collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", topic), event));
                        }

                    }
                    if(StringUtils.isNotEmpty(row.getString("enrollmentenddate"))){
                        Date enrollmentEndDate = format.parse(row.getString("enrollmentenddate"));
                        if(currentDate.compareTo(enrollmentEndDate) > 0){
                            courseIds.add(row.getString("courseid"));
                            Map<String, Object> event = getBatchCountEvent(row.getString("batchid"), row.getString("courseid"));
                            collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", topic), event));
                        }
                    }

                }
                LOGGER.info("CourseIds updated for batchCount : " + courseIds);
                LOGGER.info("BatchIds updated to completed : " + batchIds);
            } else {
                LOGGER.info("No batch data to update the status to completed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Map<String, Object> getBatchStatusEvent(String batchId, String courseId, int status) {
       return new HashMap<String, Object>() {{
           put("actor", new HashMap<String, Object>(){{
               put("id", "Course Batch Updater");
               put("type", "System");
           }});
           put("eid", "BE_JOB_REQUEST");
           put("edata",  new HashMap<String, Object>(){{
               put("action", "batch-status-update");
               put("iteration", 1);
               put("courseId", courseId);
               put("batchId", batchId);
               put("status", status);
           }});
           put("ets", System.currentTimeMillis());
           put("context", new HashMap<String, Object>(){{
               put("pdata", new HashMap<String, Object>(){{
                   put("ver", "1.0");
                   put("id", "org.sunbird.platform");
               }});
           }});
           put("mid", "LP." + System.currentTimeMillis() + "." + UUID.randomUUID());
           put("object", new HashMap<String, Object>(){{
               put("id", batchId);
               put("type", "CourseBatchEnrolment");
           }});
        }};
    }

    private static Map<String, Object> getBatchCountEvent(String batchId, String courseId) {
        return new HashMap<String, Object>() {{
            put("actor", new HashMap<String, Object>(){{
                put("id", "Course Batch Count Updater");
                put("type", "System");
            }});
            put("eid", "BE_JOB_REQUEST");
            put("edata",  new HashMap<String, Object>(){{
                put("action", "course-batch-update");
                put("iteration", 1);
                put("courseId", courseId);
                put("batchId", batchId);
            }});
            put("ets", System.currentTimeMillis());
            put("context", new HashMap<String, Object>(){{
                put("pdata", new HashMap<String, Object>(){{
                    put("ver", "1.0");
                    put("id", "org.sunbird.platform");
                }});
            }});
            put("mid", "LP." + System.currentTimeMillis() + "." + UUID.randomUUID());
            put("object", new HashMap<String, Object>(){{
                put("id", courseId +"_"+ batchId);
                put("type", "CourseBatchEnrolment");
            }});
        }};
    }
}
