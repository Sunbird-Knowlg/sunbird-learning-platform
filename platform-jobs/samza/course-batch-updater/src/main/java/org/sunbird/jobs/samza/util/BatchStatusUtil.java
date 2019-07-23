package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class BatchStatusUtil {

    private static String jobTimeZone = Platform.config.hasPath("job.time_zone") ? Platform.config.getString("job.time_zone"): "IST";
    private static JobLogger LOGGER = new JobLogger(BatchStatusUtil.class);
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";



    public static void updateOnGoingBatch() {
        try {
            Date currentDate = format.parse(format.format(new Date()));
            String table = "course-batch";
            format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("status", 0);
            }};
            List<Row> rows = SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
            if(CollectionUtils.isNotEmpty(rows)) {
                List<String> batchIds = new ArrayList<>();
                for (Row row : rows) {
                    if (StringUtils.isNotBlank(row.getString("startdate"))) {
                        Date startDate = format.parse(row.getString("startdate"));
                        if (currentDate.compareTo(startDate) >= 0) {
                            batchIds.add(row.getString("batchid"));
                        }

                    }
                }
                updateStatusOfBatch(batchIds, 1, table);
                LOGGER.info("BatchIds updated to in-progress : " + batchIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateCompletedBatch() {
        try {
            Date currentDate = format.parse(format.format(new Date()));
            String table = "course-batch";
            format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("status", 1);
            }};
            List<Row> rows = SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
            if(CollectionUtils.isNotEmpty(rows)) {
                List<String> batchIds = new ArrayList<>();
                for (Row row : rows) {
                    if (StringUtils.isNotBlank(row.getString("enddate"))) {
                        Date startDate = format.parse(row.getString("enddate"));
                        if (currentDate.compareTo(startDate) >= 0) {
                            batchIds.add(row.getString("batchid"));
                        }

                    }
                }
                updateStatusOfBatch(batchIds, 2, table);
                LOGGER.info("BatchIds updated to completed : " + batchIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void updateStatusOfBatch(List<String> batchIds, int status, String table) {
        if(CollectionUtils.isNotEmpty(batchIds)){
            Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
                put("status", status);
            }};
            Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
                put("batchid", batchIds);
            }};
            SunbirdCassandraUtil.update(keyspace, table, dataToUpdate, dataToFetch);
        }
    }


}
