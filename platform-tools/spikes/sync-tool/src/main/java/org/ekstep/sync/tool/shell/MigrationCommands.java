package org.ekstep.sync.tool.shell;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.hadoop.util.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.sync.tool.mgr.CassandraESSyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class MigrationCommands implements CommandMarker {

    @Autowired
    CassandraESSyncManager cassandraSyncMgr;

    private ControllerUtil util = new ControllerUtil();
    private HierarchyStore hierarchyStore = new HierarchyStore();

    private static final String PASSPORT_KEY = Platform.config.getString("graph.passport.key.base");
    private static final String HIERARCHY_CACHE_PREFIX = "hierarchy_";


    @CliCommand(value = "migrate-dialcodeRequired", help = "Set dialcodeRequired as yes if dialcodes are present for Collection MimeTypes")
    public void syncLeafNodesByIds(
            @CliOption(key = {"graphId"}, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
            @CliOption(key = {"ids"}, mandatory = true, help = "Unique Id of node object") final String[] ids) throws Exception {
        long startTime = System.currentTimeMillis();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.now();
        migrateCollectionContent(graphId, new ArrayList<>(Arrays.asList(ids)));
        long endTime = System.currentTimeMillis();
        long exeTime = endTime - startTime;
        System.out.println("Total time of execution: " + exeTime + "ms");
        LocalDateTime end = LocalDateTime.now();
        System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
    }

    private void migrateCollectionContent(String graphId, List<String> identifiers) {
        ArrayList<String> migrResult = new ArrayList<String>();
        for (String contentId : identifiers) {
            try {
                // Get Neo4j Object
                Node node = util.getNode(graphId, contentId);
                if (null != node && StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", (String) node.getMetadata().get("mimeType"))) {
                    // check if neo4j update is required.
                    //Need to handle image content
                    //Boolean isNeo4jMigReq = isNeo4jMigrRequired(node);
                    if (isNeo4jMigReq)
                        migrateNeo4jData(node);
                    // update hierarchy data
                    migrateCassandraData(contentId, isNeo4jMigReq);
                    migrResult.add(contentId);
                } else {
                    System.out.println("node object is null for : " + contentId);
                }
            } catch (Exception e) {
                System.out.println("Exception Occurred for " + contentId);
                e.printStackTrace();
            }
        }
        System.out.println("Migration Success for Ids : " + migrResult);
    }

    private Boolean isNeo4jMigrRequired(Node node) {
        List<String> dials = (List<String>) node.getMetadata().get("dialcodes");
        String dialReq = (String) node.getMetadata().get("dialcodeRequired");
        return CollectionUtils.isNotEmpty(dials) && StringUtils.equalsIgnoreCase("No", dialReq) ? true : false;
    }

    private void migrateNeo4jData(Node node) {
        node.getMetadata().put("dialcodeRequired", "Yes");
        node.getMetadata().put("versionKey", PASSPORT_KEY);
        util.updateNode(node);
        System.out.println("Neo4j Node Updated for : " + node.getIdentifier());
    }

    private void migrateCassandraData(String contentId, Boolean isRootNodeMigReq) {
        List<String> identifiers = Arrays.asList(contentId, contentId + ".img");
        ArrayList<String> unitsToBeSync = new ArrayList<String>();
        for (String id : identifiers) {
            // get hierarchy and update hierarchy data
            try {
                Map<String, Object> hierarchy = hierarchyStore.getHierarchy(id);
                if (MapUtils.isNotEmpty(hierarchy)) {
                    List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchy.get("children");
                    updateUnitsAndPrepareForSync(id, children, unitsToBeSync);
                    if (!id.endsWith(".img") && isRootNodeMigReq) {
                        hierarchy.put("dialcodeRequired", "Yes");
                    }
                    // write hierarchy into cassandra.
                    hierarchyStore.saveOrUpdateHierarchy(id, hierarchy);

                    // sync modified units of live hierarchy
                    if (!id.endsWith(".img")) {
                        syncUnitsAndResetCache(contentId, hierarchy, unitsToBeSync);
                    }
                } else {
                    System.out.println("Got Null Hierarchy for " + id);
                }
            } catch (Exception e) {
                System.out.println("Exception Occurred While Processing Hierarchy for : " + id);
                e.printStackTrace();
            }
        }
    }

    private void syncUnitsAndResetCache(String contentId, Map<String, Object> hierarchy, ArrayList<String> unitsToBeSync) {

        Map<String, Object> units = cassandraSyncMgr.getUnitsMetadata(hierarchy, unitsToBeSync);
        if (MapUtils.isNotEmpty(units)) {
            cassandraSyncMgr.pushToElastic(units);
            List<String> collectionUnitIds = new ArrayList<>(units.keySet());

            //Clear TextBook Cache
            RedisStoreUtil.delete(contentId);

            //Clear TextBook Hierarchy from Redis Cache
            RedisStoreUtil.delete(HIERARCHY_CACHE_PREFIX + contentId);

            //Clear TextBookUnit Hierarchy from Redis Cache
            collectionUnitIds.forEach(id -> RedisStoreUtil.delete(HIERARCHY_CACHE_PREFIX + id));

        }
    }

    private void updateUnitsAndPrepareForSync(String id, List<Map<String, Object>> children, List<String> unitsToBeSync) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                try {
                    List<String> dials = (List<String>) child.get("dialcodes");
                    String dialReq = (String) child.get("dialcodeRequired");
                    String visibility = (String) child.get("visibility");
                    if (StringUtils.equalsIgnoreCase("Parent", visibility) && CollectionUtils.isNotEmpty(dials)
                            && org.apache.commons.lang3.StringUtils.equals("No", dialReq)) {
                        child.put("dialcodeRequired", "Yes");
                        if (!id.endsWith(".img"))
                            unitsToBeSync.add((String) child.get("identifier"));
                    }
                    if (StringUtils.equalsIgnoreCase("Parent", visibility))
                        updateUnitsAndPrepareForSync(id, (List<Map<String, Object>>) child.get("children"), unitsToBeSync);
                } catch (Exception e) {
                    System.out.println("Exception Occurred While Processing Children for : " + id);
                    e.printStackTrace();
                }

            });
        }
    }


}
