package org.ekstep.sync.tool.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.sync.tool.mgr.CassandraESSyncManager;
import org.ekstep.sync.tool.util.MigrationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class CourseTypeSyncCommand implements CommandMarker {

    private static final List<String> finalStatus = Arrays.asList("Live", "Unlisted", "Flagged");
    @CliCommand(value = "migratecoursetype", help = "Set the value of existing Courses without courseType to 'TrainingCourse'")
    public void migrateCourseTypeByIds(
            @CliOption(key = {"graphId"}, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
            @CliOption(key = {"ids"}, mandatory = true, help = "Unique Id of node object") final String[] ids) throws Exception {
        long startTime = System.currentTimeMillis();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.now();
        Map<String, Object> metadataToBeMigrated = new HashMap<String, Object>();
        metadataToBeMigrated.put("courseType", "TrainingCourse");
        updateCourses(graphId, new ArrayList<>(Arrays.asList(ids)), metadataToBeMigrated );
        long endTime = System.currentTimeMillis();
        long exeTime = endTime - startTime;
        System.out.println("Total time of execution: " + exeTime + "ms");
        LocalDateTime end = LocalDateTime.now();
        System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
    }

    private void updateCourses(String graphId, List<String> identifiers, Map<String, Object> metadataToUpdate) {
        System.out.println("Total Number of object Received for migration : " + identifiers.size());
        System.out.println("Ids of objects Received for migration : " + identifiers);
        System.out.println("------------------------------------------------------------------------------------");
        MigrationHelper migrationHelperUtil = new MigrationHelper();
        identifiers.forEach(identifier -> {
            //Migrate Neo4j Content
            migrationHelperUtil.migrateMetadataInNeo4j(graphId, identifier, metadataToUpdate);
            //Migrate ContentImage
            migrationHelperUtil.migrateMetadataInNeo4j(graphId, getImageId(identifier), metadataToUpdate);
            //Migrate Cassandra Root Data
            if (finalStatus.contains(migrationHelperUtil.getNodeStatus(graphId, identifier)))
                migrationHelperUtil.migrateRootDataInCassandra(identifier, metadataToUpdate);
        });
        Map<String, Object> migratedIdStatusMap = migrationHelperUtil.getMigrationStatusOfIds();
        if (CollectionUtils.isNotEmpty((HashSet<String>) migratedIdStatusMap.get("neo4jSuccess")))
            System.out.println("Successfully Migrated Ids (Neo4j): " + migratedIdStatusMap.get("neo4jSuccess"));
        if (CollectionUtils.isNotEmpty((HashSet<String>) migratedIdStatusMap.get("cassandraSuccess")))
            System.out.println("Successfully Migrated Ids (Cassandra): " + migratedIdStatusMap.get("cassandraSuccess"));
        System.out.println("======================================= INVALID DATA IDS =================================================");

        if (MapUtils.isNotEmpty((Map<String, Object>) migratedIdStatusMap.get("neo4jFailure"))) {
            System.out.println("Neo4j Migration Failed for " + ((Map<String, Object>) migratedIdStatusMap.get("neo4jFailure")).keySet().size() + " objects.");
            System.out.println("Neo4j Migration Failed for " + ((Map<String, Object>) migratedIdStatusMap.get("neo4jFailure")).keySet());
            System.out.println("Error Map : " + migratedIdStatusMap.get("neo4jFailure"));
        }
        if (MapUtils.isNotEmpty((Map<String, Object>) migratedIdStatusMap.get("neo4jFailure"))) {
            System.out.println("Cassandra Migration Failed for " + ((Map<String, Object>) migratedIdStatusMap.get("cassandraFailure")).keySet().size() + " objects.");
            System.out.println("Cassandra Migration Failed for " + ((Map<String, Object>) migratedIdStatusMap.get("cassandraFailure")).keySet());
            System.out.println("Error Map : " + migratedIdStatusMap.get("cassandraFailure"));
        }
        System.out.println("======================================= NOT APPLICABLE NON MIGRATED DATA =============================================");
        if (CollectionUtils.isNotEmpty((HashSet<String>) migratedIdStatusMap.get("neo4jNotApplicable")))
            System.out.println("Identifiers with courseType present in neo4j: " + migratedIdStatusMap.get("neo4jNotApplicable"));
        if (CollectionUtils.isNotEmpty((HashSet<String>) migratedIdStatusMap.get("cassandraNotApplicable")))
            System.out.println("Identifiers with courseType present in cassandra: " + migratedIdStatusMap.get("cassandraNotApplicable"));

        System.out.println("------------------------------------------------------------------------------------");
        System.out.println("Course Type Migration Successfully processed for " + CollectionUtils.union((HashSet<String>) migratedIdStatusMap.get("neo4jSuccess"), (HashSet<String>) migratedIdStatusMap.get("cassandraSuccess")) + " Ids.");
    }

    private String getImageId(String id) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(id) ? id + ".img" : null;
    }
}
