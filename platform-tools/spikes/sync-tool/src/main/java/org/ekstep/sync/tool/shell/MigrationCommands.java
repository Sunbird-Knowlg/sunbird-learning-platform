package org.ekstep.sync.tool.shell;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class MigrationCommands implements CommandMarker {

	@Autowired
	CassandraESSyncManager cassandraSyncMgr;

	private ControllerUtil util = new ControllerUtil();
	private HierarchyStore hierarchyStore = new HierarchyStore();
	private ObjectMapper mapper = new ObjectMapper();

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
		ArrayList<String> migrProcessingResult = new ArrayList<String>();
		Set<String> neo4jMigrSuccess = new HashSet<String>();
		Set<String> cassandraMigrSuccess = new HashSet<String>();
		ArrayList<String> invalidIds = new ArrayList<String>();
		ArrayList<String> invalidHierarchy = new ArrayList<String>();
		Map<String, Object> failedMigrIds = new HashMap<String, Object>();
		for (String contentId : identifiers) {
			try {
				// Get Neo4j Object
				Node node = util.getNode(graphId, contentId);
				if (null != node && StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", (String) node.getMetadata().get("mimeType"))) {
					//migrate image node, if exist
					Boolean isImageExist = migrateNeo4jImageData(graphId, getImageId(contentId), neo4jMigrSuccess);
					// migrate live node, if image node not exist
					if (!isImageExist)
						migrateNeo4jData(node, neo4jMigrSuccess);
					// migrate image hierarchy data
					migrateCassandraData(getImageId(contentId), cassandraMigrSuccess, invalidHierarchy);
					migrProcessingResult.add(contentId);
				} else {
					invalidIds.add(contentId);
				}
			} catch (Exception e) {
				failedMigrIds.put(contentId, e.getMessage());
				e.printStackTrace();
			}
		}
		if (CollectionUtils.isNotEmpty(neo4jMigrSuccess))
			System.out.println("Successfully Migrated Ids (Neo4j): " + neo4jMigrSuccess);
		if (CollectionUtils.isNotEmpty(neo4jMigrSuccess))
			System.out.println("Successfully Migrated Ids (Cassandra): " + cassandraMigrSuccess);
		if (CollectionUtils.isNotEmpty(invalidIds))
			System.out.println("Invalid Identifiers: " + invalidIds);
		if (CollectionUtils.isNotEmpty(invalidHierarchy))
			System.out.println("Got Null Hierarchy for : " + invalidHierarchy);
		if (MapUtils.isNotEmpty(failedMigrIds)) {
			System.out.println("Migration Failed for Ids : " + failedMigrIds.keySet());
			System.out.println("Error Map : " + failedMigrIds);
		}
		System.out.println("DIAL Migration Successfully processed for Ids : " + migrProcessingResult);
	}

	private void migrateNeo4jData(Node node, Set<String> migrationSuccess) {
		try {
			List<String> dials = getDialCodes(node.getMetadata());
			String dialReq = (String) node.getMetadata().get("dialcodeRequired");
			if (CollectionUtils.isNotEmpty(dials) && !StringUtils.equals("Yes", dialReq)) {
				node.getMetadata().put("dialcodeRequired", "Yes");
				node.getMetadata().put("versionKey", PASSPORT_KEY);
				util.updateNode(node);
				migrationSuccess.add(node.getIdentifier());
				//Clear TextBook Cache
				RedisStoreUtil.delete(node.getIdentifier());
			}
		} catch (Exception ex) {
			System.out.println("Exception Occurred While Migrating Neo4j Node : " + ex.getMessage());
			ex.printStackTrace();
		}

	}

	private Boolean migrateNeo4jImageData(String graphId, String contentId, Set<String> migrationSuccess) {
		Boolean result = false;
		try {
			Node node = util.getNode(graphId, contentId);
			if (null != node) {
				result = true;
				List<String> dials = getDialCodes(node.getMetadata());
				String dialReq = (String) node.getMetadata().get("dialcodeRequired");
				if (CollectionUtils.isNotEmpty(dials) && !StringUtils.equals("Yes", dialReq)) {
					node.getMetadata().put("dialcodeRequired", "Yes");
					node.getMetadata().put("versionKey", PASSPORT_KEY);
					util.updateNode(node);
					migrationSuccess.add(node.getIdentifier().replace(".img", ""));
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

	public List<String> getDialCodes(Map<String, Object> map) {
		if (MapUtils.isNotEmpty(map) && map.containsKey("dialcodes")) {
			List<String> dialcodes = mapper.convertValue(map.get("dialcodes"), new TypeReference<List<String>>() {
			});
			return (dialcodes.stream().filter(f -> StringUtils.isNotBlank(f)).collect(toList()));
		}
		return new ArrayList<>();
	}

	private void migrateCassandraData(String contentId, Set<String> cassandraMigrSuccess, List<String> invalidHierarchy) {
		List<String> childIds = new ArrayList<String>();
		try {
			// Get Hierarchy Data
			Map<String, Object> hierarchy = hierarchyStore.getHierarchy(contentId);
			if (MapUtils.isNotEmpty(hierarchy)) {
				List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchy.get("children");
				updateUnits(contentId, children, childIds);
				// write hierarchy into cassandra, if hierarchy is modified
				if (CollectionUtils.isNotEmpty(childIds)) {
					hierarchyStore.saveOrUpdateHierarchy(contentId, hierarchy);
					cassandraMigrSuccess.add(contentId);
				} else {

				}
			} else {
				invalidHierarchy.add(contentId);
			}
		} catch (Exception e) {
			System.out.println("Exception Occurred While Processing Hierarchy for : " + contentId);
			e.printStackTrace();
		}
	}

	private void updateUnits(String id, List<Map<String, Object>> children, List<String> childIds) {
		if (CollectionUtils.isNotEmpty(children)) {
			children.forEach(child -> {
				try {
					List<String> dials = getDialCodes(child);
					String dialReq = (String) child.get("dialcodeRequired");
					String visibility = (String) child.get("visibility");
					String identifier = (String) child.get("identifier");
					if (StringUtils.equalsIgnoreCase("Parent", visibility) && CollectionUtils.isNotEmpty(dials)
							&& !StringUtils.equalsIgnoreCase("Yes", dialReq)) {
						child.put("dialcodeRequired", "Yes");
						childIds.add(identifier);
					}
					if (StringUtils.equalsIgnoreCase("Parent", visibility))
						updateUnits(id, (List<Map<String, Object>>) child.get("children"), childIds);
				} catch (Exception e) {
					System.out.println("Exception Occurred While Processing Children for : " + id);
					e.printStackTrace();
				}
			});
		}
	}


}
