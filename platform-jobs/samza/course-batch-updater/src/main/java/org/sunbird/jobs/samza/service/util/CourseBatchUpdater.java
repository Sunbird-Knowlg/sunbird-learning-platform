package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.CourseBatchParams;
import org.sunbird.jobs.samza.util.ESUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class CourseBatchUpdater extends BaseCourseBatchUpdater {

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdater.class);
    private String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private String table = "user_courses";
    private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";
    private int leafNodesTTL = Platform.config.hasPath("content.leafnodes.ttl")
            ? Platform.config.getInt("content.leafnodes.ttl"): 3600;
    private Jedis redisConnect= null;
    private Session cassandraSession = null;

    public CourseBatchUpdater(Jedis redisConnect, Session cassandraSession) {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
        this.redisConnect = redisConnect;
        this.cassandraSession = cassandraSession;
    }

    public void updateBatchStatus(Map<String, Object> edata, Map<String, Object> batchProgressEvents) throws Exception {
        //Get data from content read
        String courseId = (String) edata.get("courseId");
        List<String> leafNodes = getLeafNodes(courseId);
        if(CollectionUtils.isEmpty(leafNodes)){
            LOGGER.info("Content does not have leafNodes : " + courseId);
        } else {
            //Compute status
            updateData(edata, leafNodes, batchProgressEvents);
        }
    }

    private List<String> getLeafNodes(String courseId) throws Exception {
        String key = courseId + ":leafnodes";
        List<String> leafNodes = getStringList(key);
        if (CollectionUtils.isEmpty(leafNodes)) {
            Map<String, Object> content = getContent(courseId, "leafNodes");
            leafNodes = (List<String>) content.getOrDefault("leafNodes", new ArrayList<String>());
            if (CollectionUtils.isNotEmpty(leafNodes))
                RedisStoreUtil.saveStringList(key, leafNodes, leafNodesTTL);
        }
        return leafNodes;
    }

    private void updateData(Map<String, Object> edata, List<String> leafNodes, Map<String, Object> batchProgressEvents)  throws Exception {
        List<Map<String, Object>> contents = (List<Map<String, Object>>) edata.get("contents");
        String batchId = (String)edata.get("batchId");
        String userId = (String)edata.get("userId");
        
        if(CollectionUtils.isNotEmpty(contents)) {
            Map<String, Object> contentStatus = new HashMap<>();
            String lastReadContent = null;
            int lastReadContentStatus = 0;
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("batchid", edata.get("batchId"));
                put("userid", edata.get("userId"));
            }};
            ResultSet resultSet =  SunbirdCassandraUtil.read(cassandraSession, keyspace, table, dataToSelect);
            List<Row> rows = resultSet.all();
            if(CollectionUtils.isNotEmpty(rows)){
                Map<String, Integer> contentStatusMap =  rows.get(0).getMap("contentStatus", String.class, Integer.class);
                lastReadContent = rows.get(0).getString("lastreadcontentid");
                lastReadContentStatus = rows.get(0).getInt("lastreadcontentstatus");
                if(MapUtils.isNotEmpty(contentStatusMap))
                    contentStatus.putAll(contentStatusMap);
            }

            contentStatus.putAll(contents.stream().collect(Collectors.toMap(c -> (String) c.get("contentId"), c -> {
                if(contentStatus.containsKey((String) c.get("contentId")))
                   return Math.max(((Integer) contentStatus.get((String) c.get("contentId"))), ((Integer)c.get("status")));
                else return c.get("status");
            })));

            List<String> completedIds = contentStatus.entrySet().stream()
                    .filter(entry -> (2 == ((Number) entry.getValue()).intValue()))
                    .map(entry -> entry.getKey()).distinct().collect(Collectors.toList());

            int size = CollectionUtils.intersection(completedIds, leafNodes).size();
            double completionPercentage = (((Number)size).doubleValue()/((Number)leafNodes.size()).doubleValue())*100;

            int status = (size == leafNodes.size()) ? 2 : 1;

            Map<String, Object> dataToUpdate =  new HashMap<String, Object>() {{
                put("contentStatus", contentStatus);
                put("status", status);
                put("completionPercentage", ((Number)completionPercentage).intValue());
                put("progress", size);
                if(status == 2)
                    put("completedOn", new Timestamp(new Date().getTime()));
                
            }};
            dataToUpdate.put("lastReadContentId", lastReadContent);
            dataToUpdate.put("lastReadContentStatus", lastReadContentStatus);

            if(MapUtils.isNotEmpty(dataToUpdate)) {
                batchProgressEvents.put(batchId + "_" + userId, dataToUpdate);
            }
        }
    }


    public List<String> getStringList(String key) {
        try {
            Set<String> set = redisConnect.smembers(key);
            List<String> list = new ArrayList<String>(set);
            return list;
        } catch (Exception e) {
            throw new ServerException("ERR_CACHE_GET_PROPERTY_ERROR", e.getMessage());
        }
    }

    public void updateBatchProgress(Session cassandraSession, Map<String, Object> batchProgressEvents) {
        if (MapUtils.isNotEmpty(batchProgressEvents)) {
            List<Update.Where> updateQueryList = new ArrayList<>();
            batchProgressEvents.entrySet().forEach(event -> {
                try {
                    Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                        put("batchid", event.getKey().split("_")[0]);
                        put("userid", event.getKey().split("_")[1]);
                    }};
                    Map<String, Object> dataToUpdate = (Map<String, Object>) event.getValue();
                    //Update cassandra
                    updateQueryList.add(SunbirdCassandraUtil.updateBatch(cassandraSession, keyspace, table, dataToUpdate, dataToSelect));
                    ESUtil.updateCoureBatch(ES_INDEX_NAME, ES_DOC_TYPE, dataToUpdate, dataToSelect);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //TODO: enhance this to contain only 65k queries in a batch. It is the batch limit from cassandra.
            if(CollectionUtils.isNotEmpty(updateQueryList)){
                Batch batch = QueryBuilder.batch(updateQueryList.toArray(new RegularStatement[updateQueryList.size()]));
                cassandraSession.execute(batch);
            }
            try {
                ESUtil.updateBatches(ES_INDEX_NAME, ES_DOC_TYPE, batchProgressEvents);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void processBatchProgress(Map<String, Object> message, Map<String, Object> batchProgressEvents) {
        try {
            Map<String, Object> eData = (Map<String, Object>) message.get(CourseBatchParams.edata.name());
            updateBatchStatus(eData, batchProgressEvents);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
