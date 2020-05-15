package org.ekstep.sync.tool.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.ControllerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigrationHelper {

    private HierarchyStore hierarchyStore = new HierarchyStore();
    private ControllerUtil util = new ControllerUtil();
    private static final String PASSPORT_KEY = Platform.config.getString("graph.passport.key.base");
    private Map<String, Object> migrationStatusOfIds = new HashMap<String, Object>();

    private Set<String> neo4jSuccess = new HashSet<String>();
    private Map<String, Object> neo4jFailed = new HashMap<String, Object>();
    private Set<String> neo4jNotApplicable = new HashSet<>();


    private Set<String> cassandraSuccess = new HashSet<String>();
    private Map<String, Object> cassandraFailed = new HashMap<String, Object>();
    private Set<String> cassandraNotApplicable = new HashSet<>();

    public void migrateMetadataInNeo4j(String graphId, String contentId, Map<String, Object> metaDataToBeMigrated) {
        try {
            Node node = util.getNode(graphId, contentId);
            if (null != node) {
                if (StringUtils.equalsIgnoreCase("Course", (String) node.getMetadata().get("contentType"))
                        && StringUtils.isBlank((String) node.getMetadata().get("courseType"))) {
                    node.getMetadata().putAll(metaDataToBeMigrated);
                    node.getMetadata().put("versionKey", PASSPORT_KEY);
                    Response resp = util.updateNode(node);
                    if (null != resp && ResponseCode.OK == resp.getResponseCode()) {
                        if (!StringUtils.contains(contentId, ".img"))
                            RedisStoreUtil.delete(node.getIdentifier());
                        neo4jSuccess.add(contentId);
                    } else neo4jFailed.put(contentId, resp.getResponseCode());
                } else neo4jNotApplicable.add(contentId);
            } else neo4jFailed.put(contentId, "Node is Null");
        } catch (Exception ex) {
            System.out.println("Exception Occurred While Migrating Neo4j Node : " + ex.getMessage());
            ex.printStackTrace();
            neo4jFailed.put(contentId, ex.getMessage());
        }
    }

    // Use this Method only for Live Hierarchy in Cassandra
    public void migrateRootDataInCassandra(String contentId, Map<String, Object> metaDataToBeMigrated) {
        try {
            Map<String, Object> hierarchy = hierarchyStore.getHierarchy(contentId);
            if (MapUtils.isNotEmpty(hierarchy)) {
                if (StringUtils.equalsIgnoreCase("Course", (String) hierarchy.get("contentType"))
                        && StringUtils.isBlank((String) hierarchy.get("courseType"))) {
                    hierarchy.putAll(metaDataToBeMigrated);
                    hierarchyStore.saveOrUpdateHierarchy(contentId, hierarchy);
                    cassandraSuccess.add(contentId);
                } else
                    cassandraNotApplicable.add(contentId);
            } else cassandraFailed.put(contentId, "Empty Hierarchy");
        } catch (Exception e) {
            System.out.println("Exception Occurred While Processing Hierarchy for : " + contentId);
            e.printStackTrace();
            cassandraFailed.put(contentId, e.getMessage());
        }
    }

    public Map<String, Object> getMigrationStatusOfIds() {
        this.migrationStatusOfIds.put("neo4jSuccess", neo4jSuccess);
        this.migrationStatusOfIds.put("neo4jFailure", neo4jFailed);
        this.migrationStatusOfIds.put("neo4jNotApplicable", neo4jNotApplicable);
        this.migrationStatusOfIds.put("cassandraSuccess", cassandraSuccess);
        this.migrationStatusOfIds.put("cassandraFailure", cassandraFailed);
        this.migrationStatusOfIds.put("cassandraNotApplicable", cassandraNotApplicable);
        return this.migrationStatusOfIds;
    }

    public String getNodeStatus(String graphId, String contentId) {
        try {
            Node node = util.getNode(graphId, contentId);
            if (null != node) {
                return (String) node.getMetadata().get("status");
            } else  return  "";
        } catch (Exception e) {
            return "";
        }
    }

}
