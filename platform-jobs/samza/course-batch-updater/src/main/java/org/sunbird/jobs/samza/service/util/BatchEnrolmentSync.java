package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.ESUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchEnrolmentSync extends BaseCourseBatchUpdater {

    private static JobLogger LOGGER = new JobLogger(BatchEnrolmentSync.class);
    private String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private String table = "user_enrolments";
    private String consumptionTable = "content_consumption";
    private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";
    private Session cassandraSession = null;
    
    public BatchEnrolmentSync(Session cassandraSession) {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
        this.cassandraSession = cassandraSession;
    }

    public void syncEnrolment(Map<String, Object> edata) throws Exception {
        String courseId = (String) edata.get("courseId");
        Map<String, Object> content = getContent(courseId, "leafNodes");
        List<String> leafNodes = (List<String>) content.get("leafNodes");
        if (CollectionUtils.isEmpty((List<String>) content.get("leafNodes"))) {
            LOGGER.info("Content does not have leafNodes : " + courseId);
        } else {
            Map<String, Object> dataToUpdate = getStatusUpdateData(edata, leafNodes);
            if (MapUtils.isNotEmpty(dataToUpdate)) {
                Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                    put("batchid", edata.get("batchId"));
                    put("userid", edata.get("userId"));
                    put("courseid", courseId);
                }};
                if (CollectionUtils.isNotEmpty((List) edata.get("reset"))) {
                    List<String> dataToReset = (List) edata.get("reset");

                    for(String key: dataToUpdate.keySet()){
                        if(!dataToReset.contains(key)) {
                            dataToUpdate.remove(key);
                        }
                    }
                    ResultSet resultSet = SunbirdCassandraUtil.read(cassandraSession, keyspace, table, dataToSelect);
                    Date existingCompletedOn = resultSet.one().getTimestamp("completedOn");

                    if(null != dataToUpdate.get("status") && (2 == ((Number)dataToUpdate.get("status")).intValue()) && (null == existingCompletedOn))
                        dataToUpdate.put("completedOn", new Timestamp(new Date().getTime()));

                    if(null != dataToUpdate.get("status") && (2 != ((Number)dataToUpdate.get("status")).intValue()) && (null != existingCompletedOn))
                        dataToUpdate.put("completedOn", null);

                    SunbirdCassandraUtil.update(cassandraSession, keyspace, table, dataToUpdate, dataToSelect);
                }
                /*if(dataToUpdate.keySet().contains("contentStatus"))
                    dataToUpdate.put("contentStatus", mapper.writeValueAsString(dataToUpdate.get("contentStatus")));
                ESUtil.updateCoureBatch(ES_INDEX_NAME, ES_DOC_TYPE, dataToUpdate, dataToSelect);*/

            }
        }
    }

    private Map<String, Object> getStatusUpdateData(Map<String, Object> edata, List<String> leafNodes) throws IOException {
        Map<String, Object> contentStatus = new HashMap<>();
        Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
            put("userid", edata.get("userId"));
            put("batchid", edata.get("batchId"));
            put("contentid", leafNodes);
        }};
        Map<String, String> lastReadContents = new HashMap<>();
        ResultSet resultSet = SunbirdCassandraUtil.read(cassandraSession, keyspace, consumptionTable, dataToSelect);
        List<Row> rows = resultSet.all();
        if (CollectionUtils.isNotEmpty(rows)) {
            for(Row row: rows) {
                contentStatus.put(row.getString("contentid"), row.getInt("status"));
                lastReadContents.put(row.getString("contentid"), row.getString("lastaccesstime"));
            }
        }

        if(MapUtils.isNotEmpty(contentStatus)) {
            List<String> completedIds = contentStatus.entrySet().stream()
                    .filter(entry -> (2 == ((Number) entry.getValue()).intValue()))
                    .map(entry -> entry.getKey()).distinct().collect(Collectors.toList());

            int size = CollectionUtils.intersection(completedIds, leafNodes).size();
            double completionPercentage = (((Number) size).doubleValue() / ((Number) leafNodes.size()).doubleValue()) * 100;

            int status = (size == leafNodes.size()) ? 2 : 1;
            String lastReadContent = fetchLatestLastReadContent(lastReadContents);

            return new HashMap<String, Object>() {{
                put("contentStatus", contentStatus);
                put("status", status);
                put("completionPercentage", ((Number) completionPercentage).intValue());
                put("progress", size);
                put("lastReadContentId", lastReadContent);
                if(StringUtils.isNotBlank(lastReadContent))
                    put("lastReadContentStatus", contentStatus.get(lastReadContent));
                else
                    put("lastReadContentStatus", null);
            }};
        } else {
            return new HashMap<String, Object>() {{
                put("lastReadContentId", null);
                put("lastReadContentStatus", null);
            }};
        }
    }


    private String fetchLatestLastReadContent(Map<String, String> lastReadContents) {
        if(MapUtils.isNotEmpty(lastReadContents)) {
            Map<String, String> lrContents = lastReadContents.entrySet().stream().filter(entry -> StringUtils.isNotBlank(entry.getValue())).sorted((Map.Entry.<String, String>comparingByValue().reversed()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            return lrContents.keySet().stream().findFirst().orElse(null);
        } else {
            return null;
        }
    }
}
