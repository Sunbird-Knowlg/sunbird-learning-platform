package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.sunbird.jobs.samza.util.ESUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchStatusUpdater extends BaseCourseBatchUpdater {

    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private static final String courseBatchTable = "course_batch";
    private static final String ES_INDEX_NAME = "course-batch";
    private BatchCountUpdater batchCountUpdater = null;
    private Session cassandraSession = null;
    
    public BatchStatusUpdater(Session cassandraSession) {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
        batchCountUpdater = new BatchCountUpdater(cassandraSession);
        this.cassandraSession = cassandraSession;
    }

    public void update(Map<String, Object> edata) throws Exception {
        String batchId = (String) edata.get(CourseBatchParams.batchId.name());
        String courseId = (String) edata.get(CourseBatchParams.courseId.name());
        int status = (int) edata.get(CourseBatchParams.status.name());
        updateStatusOfBatch(batchId, courseId, status);
        batchCountUpdater.update(edata);
    }

    private void updateStatusOfBatch(String batchId, String courseId, int status) throws Exception {
        Map<String, Object> dataToUpdate = new HashMap<String, Object>() {{
            put(CourseBatchParams.status.name(), status);
        }};
        Map<String, Object> dataToFetch = new HashMap<String, Object>() {{
            put(CourseBatchParams.batchId.name(), batchId);
            put(CourseBatchParams.courseId.name(), courseId);
        }};
        SunbirdCassandraUtil.update(cassandraSession, keyspace, courseBatchTable, dataToUpdate, dataToFetch);
        dataToFetch.remove(CourseBatchParams.courseId.name());
        ESUtil.updateCoureBatch(ES_INDEX_NAME, "_doc", dataToUpdate, dataToFetch);
    }
}
