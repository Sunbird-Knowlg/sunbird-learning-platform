package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.sunbird.jobs.samza.util.ESUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class BatchStatusUpdater extends BaseCourseBatchUpdater {
	
	private static String jobTimeZone = Platform.config.hasPath("job.time_zone") ? Platform.config.getString("job.time_zone"): "IST";
    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private static final String courseBatchTable = "course_batch";
    private static final String ES_INDEX_NAME = "course-batch";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static String installation = Platform.config.hasPath("sunbird.installation") ? Platform.config.getString("sunbird.installation"): "sunbird";

    public BatchStatusUpdater() {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
    }

    public void update(Map<String, Object> edata) throws Exception {
        String batchId = (String) edata.get(CourseBatchParams.batchId.name());
        String courseId = (String) edata.get(CourseBatchParams.courseId.name());
        int status = (int) edata.get(CourseBatchParams.status.name());
        updateStatusOfBatch(batchId, courseId, status);
    }

    private void updateBatchCount(String courseId) throws ParseException,Exception {
        //Get number of batches for courseID
        ResultSet resultSet = SunbirdCassandraUtil.read(keyspace, courseBatchTable, new HashMap<String, Object>(){{put(CourseBatchParams.courseId.name(), courseId);}});
        List<Row> rows = resultSet.all();
        // Get the count of open batch and private batch
        final int[] openBatchCount = {0};
        final int[] privateBatchCount = {0};
    	Date currentDate = format.parse(format.format(new Date()));
        format.setTimeZone(TimeZone.getTimeZone(jobTimeZone));
        rows.forEach(row-> {
            int status = row.getInt(CourseBatchParams.status.name());
            Date enrollmentEndDate = null;
            if (StringUtils.isNotBlank(row.getString("enrollmentenddate"))) {
            	try {
            		enrollmentEndDate = format.parse(row.getString("enrollmentenddate"));
            	}catch(ParseException ex) {
            		ex.printStackTrace();
            	}
            }
            String enrollmentType = row.getString(CourseBatchParams.enrollmentType.name());
            if(StringUtils.equalsIgnoreCase(CourseBatchParams.open.name(), enrollmentType) && (1 == status) && (enrollmentEndDate==null || currentDate.compareTo(enrollmentEndDate) < 0))
                openBatchCount[0] = openBatchCount[0] + 1;
            if(StringUtils.equalsIgnoreCase("invite-only", enrollmentType) && (1 == status))
                privateBatchCount[0] = privateBatchCount[0] + 1;
        });
        
        // SystemUpdate batch count
        Request request = new Request();
        request.put("content", new HashMap<String, Object>() {{
            put("c_" + installation.toLowerCase() + "_open_batch_count".toLowerCase(), openBatchCount[0]);
            put("c_" + installation.toLowerCase() + "_private_batch_count".toLowerCase(), privateBatchCount[0]);
        }});
        systemUpdate(courseId, request);
    }


    private static void updateStatusOfBatch(String batchId, String courseId, int status) throws Exception {
        Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
            put(CourseBatchParams.status.name(), status);
        }};
        Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
            put(CourseBatchParams.batchId.name(), batchId);
            put(CourseBatchParams.courseId.name(), courseId);
        }};
        SunbirdCassandraUtil.update(keyspace, courseBatchTable, dataToUpdate, dataToFetch);
        dataToFetch.remove(CourseBatchParams.courseId.name());
        ESUtil.updateCoureBatch(ES_INDEX_NAME, "_doc", dataToUpdate, dataToFetch);
    }
}
