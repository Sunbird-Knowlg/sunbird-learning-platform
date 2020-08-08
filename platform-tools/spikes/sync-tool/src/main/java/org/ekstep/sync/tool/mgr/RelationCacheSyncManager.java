package org.ekstep.sync.tool.mgr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.ekstep.common.Platform;
import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.ekstep.kafka.KafkaClient;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class RelationCacheSyncManager {

    private static String graphId = "domain";
    private static ObjectMapper mapper = new ObjectMapper();
    private static final String KAFKA_TOPIC = Platform.config.hasPath("content.postpublish.topic")? Platform.config.getString("content.postpublish.topic"): "local.content.postpublish.request";
    int defaultBatch = 100;

    public void syncAllCollections(int total, int limit, int offset, boolean verbose) throws Exception {
        if (total > 0) {
            defaultBatch = (defaultBatch > limit) ?  limit: defaultBatch;
            while (offset < limit) {
                int finalIndex = ((defaultBatch + offset) > limit)? total: (defaultBatch + offset);
                System.out.println("Processing contents with index: " + offset +" to " + finalIndex);
                List<Map<String, Object>> list = getCollectionProps(offset, defaultBatch);
                offset += defaultBatch;

                for (Map<String, Object> content : list) {
                    String event = generateKafkaEvent(content);
                    KafkaClient.send(event, KAFKA_TOPIC);
                    if (verbose) {
                        System.out.println(event);
                    }
                }
            }
        }
    }

    public int getCollectionCount() {

        Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
        try (Session session = driver.session()) {
            StatementResult result = session.run("MATCH (n:domain{IL_FUNC_OBJECT_TYPE:\"Content\", mimeType: \"application/vnd.ekstep.content-collection\", visibility: \"Default\"}) WHERE n.status in [\"Live\", \"Unlisted\"] RETURN count(n.IL_UNIQUE_ID) as totalContents;");
            int totalCount = 0;
            if (null != result && CollectionUtils.isNotEmpty(result.keys())) {
                totalCount = result.single().get("totalContents", 0);
            }
            return totalCount;
        }
    }

    public List<Map<String, Object>> getCollectionProps(int offset, int limit)  {
        List<Map<String, Object>> list = new ArrayList<>();
        String query = "MATCH (n:domain{IL_FUNC_OBJECT_TYPE:\"Content\", mimeType: \"application/vnd.ekstep.content-collection\", visibility: \"Default\"}) WHERE n.status in [\"Live\", \"Unlisted\"] RETURN n.IL_UNIQUE_ID AS identifier, n.contentType as contentType, n.pkgVersion as pkgVersion, n.status as status, n.name as name, n.createdBy as createdBy SKIP " + offset + " LIMIT " + limit + ";";
        Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
        try (Session session = driver.session()) {
            StatementResult result = session.run(query);
            if (null != result && result.hasNext()) {
                for (Record record : result.list()) {
                    String identifier = record.get("identifier", "");
                    String name = record.get("name", "");
                    String status = record.get("status", "");
                    String contentType = record.get("contentType", "");
                    int pkgVersion = record.get("pkgVersion", 0);

                    String createdBy = record.get("createdBy", "");
                    Map<String, Object> collection = new HashMap<String, Object>(){{
                        put("id", identifier);
                        put("name", name);
                        put("contentType", contentType);
                        put("status", status);
                        put("contentType", contentType);
                        put("pkgVersion", pkgVersion);
                        put("mimeType", "application/vnd.ekstep.content-collection");
                    }};
                    list.add(collection);
                }
            }
        }
        return list;
    }


    public String generateKafkaEvent(Map<String, Object> rowMap) throws JsonProcessingException {
        rowMap.put("action", "link-dialcode");
        rowMap.put("iteration", 1);
        Map<String, Object> event = new HashMap<String, Object>() {{
            put("eid", "BE_JOB_REQUEST");
            put("ets", System.currentTimeMillis());
            put("mid", "LP." + System.currentTimeMillis() +"." + UUID.randomUUID());
            put("actor", new HashMap<String, Object>(){{
                put("type", "System");
                put("id", "Post Publish Processor");
            }});
            put("context", new HashMap<String, Object>(){{
                put("pdata", new HashMap<String, Object>(){{
                    put("id", "org.sunbird.platform");
                    put("ver", "1.0");
                }});
            }});
            put("object", new HashMap<String, Object>(){{
                put("type", rowMap.get("contentType"));
                put("id", rowMap.get("id"));
                put("version", rowMap.get("pkgVersion"));
            }});
            put("edata", rowMap);
        }};
        return mapper.writeValueAsString(event);

    }

}
