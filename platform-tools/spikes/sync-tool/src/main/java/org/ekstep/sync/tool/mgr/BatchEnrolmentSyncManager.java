package org.ekstep.sync.tool.mgr;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class BatchEnrolmentSyncManager {

    private static ObjectMapper mapper = new ObjectMapper();
    private static int batchSize = Platform.config.hasPath("batch.size") ? Platform.config.getInt("batch.size"): 50;
    Map<String, String> esIndexObjecTypeMap = new HashMap<String, String>() {{
        put("course-batch", "course-batch");
        put("user-courses", "user-courses");
    }};

    private static Map<String, String> tableObjecTypeMap = new HashMap<String, String>() {{
        put("course-batch", "course_batch");
        put("user-courses", "user_courses");
    }};

    private static final String keyspace = Platform.config.hasPath("courses.keyspace.name") ? Platform.config.getString("courses.keyspace.name"): "sunbird_courses";
    public void sync(String objectType, String offset, String limit, Boolean resetProgress) throws Exception {
        String index = esIndexObjecTypeMap.get(objectType);
        ElasticSearchUtil.initialiseESClient(index, Platform.config.getString("search.es_conn_info"));

        //FetchData from cassandra
        int lmt = (StringUtils.isNotBlank(limit)) ? Integer.parseInt(limit) : 0;
        List<Row> rows = read(tableObjecTypeMap.get(objectType), lmt);
        System.out.println("Number of rows to be synced : " + rows.size());
        //Prepare ES Docs
        List<String> docids = new ArrayList<>();
        if(StringUtils.equalsIgnoreCase("course-batch", objectType))
            docids = Arrays.asList("batchId");
        if(StringUtils.equalsIgnoreCase("user-courses", objectType)){
            docids = Arrays.asList("batchId", "userID");
        }

        pushDocsToES(rows, 0, docids, index);
        //TODO: If resetProgress Push the events to kafka

    }

    private void pushDocsToES(List<Row> rows, int count, List<String> docids, String index) throws Exception {
        long startTime = System.currentTimeMillis();
        long total = ((Number) rows.size()).longValue();
        long current = 0;
        while (CollectionUtils.isNotEmpty(rows)){
            Map<String , Object> esDocs = new HashMap<>();
            int currentBatchSize = (rows.size() >= batchSize) ? batchSize : rows.size();
            List<Row> dbRows = rows.subList(0, currentBatchSize);

            for(Row row : dbRows) {
                String docString = row.getString("[json]");
                Map<String, Object> docMap = mapper.readValue(docString, Map.class);
                String docId = docids.stream().map(key -> (String) docMap.get(key.toLowerCase())).collect(Collectors.toList())
                        .stream().collect(Collectors.joining("_"));
                esDocs.put(docId, docMap);
            }
            if(MapUtils.isNotEmpty(esDocs)) {
                ElasticSearchUtil.bulkIndexWithIndexId(index, "_doc", esDocs);
            }
            current +=dbRows.size();
            printProgress(startTime, total, current);

            rows.subList(0, currentBatchSize).clear();
        }
    }

    private static List<Row> read(String table, int limit) {
        Session session = CassandraConnector.getSession("platform-courses");
        Select selectQuery = QueryBuilder.select().json().all().from(keyspace, table);
        if(limit != 0)
            selectQuery.limit(limit);
        ResultSet results = session.execute(selectQuery);
        return results.all();
    }

    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }
}
