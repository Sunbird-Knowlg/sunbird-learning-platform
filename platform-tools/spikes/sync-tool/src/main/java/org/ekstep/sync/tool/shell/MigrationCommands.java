package org.ekstep.sync.tool.shell;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ResourceNotFoundException;
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
                    //migrate image node, if exist
                    Boolean isImageExist = migrateNeo4jImageData(graphId, getImageId(contentId));
                    // migrate live node, if image node not exist
                    if (!isImageExist)
                        migrateNeo4jData(node);
                    // migrate image hierarchy data
                    migrateCassandraData(getImageId(contentId));
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

    private void migrateNeo4jData(Node node) {
        try {
            List<String> dials = (List<String>) node.getMetadata().get("dialcodes");
            String dialReq = (String) node.getMetadata().get("dialcodeRequired");
            if (CollectionUtils.isNotEmpty(dials) && !StringUtils.equalsIgnoreCase("Yes", dialReq)) {
                node.getMetadata().put("dialcodeRequired", "Yes");
                node.getMetadata().put("versionKey", PASSPORT_KEY);
                util.updateNode(node);
                System.out.println("Neo4j Node Updated for : " + node.getIdentifier());
                //Clear TextBook Cache
                RedisStoreUtil.delete(node.getIdentifier());
            }
        } catch (Exception ex) {
            System.out.println("Exception Occurred While Migrating Neo4j Node : " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    private Boolean migrateNeo4jImageData(String graphId, String contentId) {
        Boolean result = false;
        try {
            Node node = util.getNode(graphId, contentId);
            if (null != node) {
                result = true;
                List<String> dials = (List<String>) node.getMetadata().get("dialcodes");
                String dialReq = (String) node.getMetadata().get("dialcodeRequired");
                if (CollectionUtils.isNotEmpty(dials) && !StringUtils.equalsIgnoreCase("Yes", dialReq)) {
                    node.getMetadata().put("dialcodeRequired", "Yes");
                    node.getMetadata().put("versionKey", PASSPORT_KEY);
                    util.updateNode(node);
                    System.out.println("Neo4j Node Updated for : " + node.getIdentifier());
                }
            }
        } catch (ResourceNotFoundException e) {

        } catch (Exception ex) {
            System.out.println("Exception Occurred While Migrating Neo4j Node : " + ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    private String getImageId(String id) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(id) ? id + ".img" : null;
    }

    private void migrateCassandraData(String contentId) {
        try {
            // Get Hierarchy Data
            Map<String, Object> hierarchy = hierarchyStore.getHierarchy(contentId);
            if (MapUtils.isNotEmpty(hierarchy)) {
                List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchy.get("children");
                updateUnits(contentId, children);
                // write hierarchy into cassandra.
                hierarchyStore.saveOrUpdateHierarchy(contentId, hierarchy);
            } else {
                System.out.println("Got Null Hierarchy for " + contentId);
            }
        } catch (Exception e) {
            System.out.println("Exception Occurred While Processing Hierarchy for : " + contentId);
            e.printStackTrace();
        }
    }

    private void updateUnits(String id, List<Map<String, Object>> children) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(child -> {
                try {
                    List<String> dials = (List<String>) child.get("dialcodes");
                    String dialReq = (String) child.get("dialcodeRequired");
                    String visibility = (String) child.get("visibility");
                    if (StringUtils.equalsIgnoreCase("Parent", visibility) && CollectionUtils.isNotEmpty(dials)
                            && !StringUtils.equalsIgnoreCase("Yes", dialReq)) {
                        child.put("dialcodeRequired", "Yes");
                    }
                    if (StringUtils.equalsIgnoreCase("Parent", visibility))
                        updateUnits(id, (List<Map<String, Object>>) child.get("children"));
                } catch (Exception e) {
                    System.out.println("Exception Occurred While Processing Children for : " + id);
                    e.printStackTrace();
                }
            });
        }
    }


}
