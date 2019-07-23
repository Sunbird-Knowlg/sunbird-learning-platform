package org.sunbird.jobs.samza.util;

import com.datastax.driver.core.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class BatchStatusUtil {

    private static String jobTimeZone = Platform.config.hasPath("job.time_zone") ? Platform.config.getString("job.time_zone"): "IST";

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";


    public static void updateCourseBatchStatus(int status) throws Exception{
        String table = "course-batch";
        format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
        Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
            put("status", status);
        }};
        List<Row> rows = SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
        if(CollectionUtils.isNotEmpty(rows)){
            Map<String, Object> startBatchdata = new HashMap<>();
            Map<String, Object> endBatchdata = new HashMap<>();
            for(Row row: rows) {
                if(StringUtils.isNotBlank(row.getString("startdate")) && (row.getInt("status") < 1)){
                    Date startDate = format.parse(row.getString("startdate"));
                    startBatchdata.put(row.getString("batchid"), startDate);
                }

                if(StringUtils.isNotBlank(row.getString("enddate")) && (row.getInt("status") < 2)) {
                    Date endDate = format.parse(row.getString("enddate"));
                    endBatchdata.put(row.getString("batchid"), endDate);
                }
            }
            Date currentDate = format.parse(format.format(new Date()));
            List<String> inProgressBatchIds = startBatchdata.entrySet().stream()
                    .filter(entry -> (currentDate.compareTo((Date) entry.getValue()) >= 0))
                    .map(entry -> {return entry.getKey();}).collect(Collectors.toList());

            List<String> completedBatchIds = endBatchdata.entrySet().stream()
                    .filter(entry -> (currentDate.compareTo((Date) entry.getValue()) >= 0))
                    .map(entry -> {return entry.getKey();}).collect(Collectors.toList());

            updateStatusOfBatch(inProgressBatchIds, 1, table);
            updateStatusOfBatch(completedBatchIds, 2, table);
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
