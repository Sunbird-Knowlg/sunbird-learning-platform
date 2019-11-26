package org.sunbird.jobs.samza.service.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.jobs.samza.util.ESUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseBatchUpdater extends BaseCourseBatchUpdater {

    private static JobLogger LOGGER = new JobLogger(CourseBatchUpdater.class);
    private String keyspace = Platform.config.hasPath("courses.keyspace.name")
            ? Platform.config.getString("courses.keyspace.name")
            : "sunbird_courses";
    private String table = "user_courses";
    private static final String ES_INDEX_NAME = "user-courses";
    private static final String ES_DOC_TYPE = "_doc";


    public CourseBatchUpdater() {
        ElasticSearchUtil.initialiseESClient(ES_INDEX_NAME, Platform.config.getString("search.es_conn_info"));
    }

    public void updateBatchStatus(Map<String, Object> edata) throws Exception {
        //Get data from content read
        String courseId = (String) edata.get("courseId");
        Map<String, Object> content = getContent(courseId, "leafNodes");
        List<String> leafNodes = (List<String>) content.get("leafNodes");
        if(CollectionUtils.isEmpty((List<String>) content.get("leafNodes"))){
            LOGGER.info("Content does not have leafNodes : " + courseId);
        } else {
            //Compute status
            updateData(edata, leafNodes);
        }
    }

    private void updateData(Map<String, Object> edata, List<String> leafNodes)  throws Exception {
        List<Map<String, Object>> contents = (List<Map<String, Object>>) edata.get("contents");
        if(CollectionUtils.isNotEmpty(contents)) {
            Map<String, Object> contentStatus = new HashMap<>();
            String lastReadContent = null;
            int lastReadContentStatus = 0;
            Map<String, Object> dataToSelect = new HashMap<String, Object>() {{
                put("batchid", edata.get("batchId"));
                put("userid", edata.get("userId"));
            }};
            ResultSet resultSet =  SunbirdCassandraUtil.read(keyspace, table, dataToSelect);
            List<Row> rows = resultSet.all();
            if(CollectionUtils.isNotEmpty(rows)){
                Map<String, Integer> contentStatusMap =  rows.get(0).getMap("contentStatus", String.class, Integer.class);
                lastReadContent = rows.get(0).getString("lastreadcontentid");
                lastReadContentStatus = rows.get(0).getInt("lastreadcontentstatus");
                if(MapUtils.isNotEmpty(contentStatusMap))
                    contentStatus.putAll(contentStatusMap);
            }

            contentStatus.putAll(contents.stream().collect(Collectors.toMap(c -> (String) c.get("contentId"), c -> c.get("status"))));

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

            if(MapUtils.isNotEmpty(dataToUpdate)) {
                //Update cassandra
                SunbirdCassandraUtil.update(keyspace, table, dataToUpdate, dataToSelect);
                dataToUpdate.put("contentStatus", mapper.writeValueAsString(dataToUpdate.get("contentStatus")));
                dataToUpdate.put("lastReadContentId", lastReadContent);
                dataToUpdate.put("lastReadContentStatus", lastReadContentStatus);
                ESUtil.updateCoureBatch(ES_INDEX_NAME, ES_DOC_TYPE, dataToUpdate, dataToSelect);
            }
        }
    }



}
