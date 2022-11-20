package org.sunbird.sync.tool.mgr;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.sync.tool.util.KafkaUtil;
import org.sunbird.telemetry.util.LogTelemetryEventUtil;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class CSPMigrationMessageGenerator {

	private ControllerUtil util = new ControllerUtil();
	private static int batchSize = 100;
	private ObjectMapper mapper = new ObjectMapper();
	private static String actorId = "csp-migration";
	private static String actorType = "System";
	private static String pdataId = "org.sunbird.platform";
	private static String pdataVersion = "1.0";
	private static String action = "csp-migration";
	private static String migrationTopicName = Platform.config.getString("csp.migration.request.topic");

	@PostConstruct
	private void init() throws Exception {
		int batch = Platform.config.hasPath("csp.migration.batch.size") ? Platform.config.getInt("csp.migration.batch.size") : 100;
		batchSize = batch;
	}

	public void generateMgrMsg(String graphId, String[] objectTypes, String[] mimeTypes, String[] status, Integer limit, Integer delay) throws Exception {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("ERR_INVALID_GRAPH_ID", "Graph Id is blank.");
		if (null == objectTypes || objectTypes.length == 0)
			throw new ClientException("ERR_EMPTY_OBJECT_TYPE", "Object Type is blank.");
		List<String> mimeTypeList = new ArrayList<String>();
		List<String> statusList = new ArrayList<String>();
		if (null != mimeTypes && mimeTypes.length > 0)
			mimeTypeList = Arrays.asList(mimeTypes);
		if (null != status && status.length > 0)
			statusList = Arrays.asList(status);
		Map<String, String> errors = new HashMap<>();
		long startTime = System.currentTimeMillis();
		System.out.println("-----------------------------------------");
		System.out.println("\nMigration Event Generation starting at " + startTime);
		Map<String, Long> counts = util.getCSPMigrationObjectCount(graphId, Arrays.asList(objectTypes));
		if (counts.isEmpty()) {
			System.out.println("No objects found in this graph.");
		} else {
			List<String> objTypes = counts.keySet().stream().filter(key -> Arrays.asList(objectTypes).contains(key)).collect(Collectors.toList());
			for (String objectType : objTypes) {
				Long count = counts.get(objectType);
				System.out.println(count + " - " + objectType + " nodes available for migration");
			}
		}
		for (String objectType : objectTypes) {
			Long count = counts.get(objectType);
			if (count > 0) {
				System.out.println("-----------------------------------------");
				System.out.println("\nGenerating event for object of type " + objectType + " with batch size of " + batchSize + " having delay " + delay + "ms for each batch.\n");
				int start = 0;
				int current = 0;
				long total = counts.get(objectType);
				long stopLimit;
				if (limit > 0) {
					if (limit < batchSize || limit % batchSize != 0) {
						System.out.println("Limit value should be minimum " + batchSize + ". The limit value should be multiple of " + batchSize + ". Setting limit to minimum value. i.e " + batchSize);
						stopLimit = batchSize;
					} else stopLimit = limit;
				} else stopLimit = total;
				boolean found = true;
				while (found && start < stopLimit) {
					List<Node> nodes = null;
					try {
						nodes = util.getNodes(graphId, objectType.trim(), mimeTypeList, statusList, start, batchSize);
					} catch (ResourceNotFoundException e) {
						System.out.println("Error while fetching neo4j records for objectType=" + objectType + ", start=" + start + ",batchSize=" + batchSize);
						start += batchSize;
						continue;
					}
					if (CollectionUtils.isNotEmpty(nodes)) {
						start += batchSize;
						Map<String, String> events = generateMigrationEvent(nodes, errors);
						sendEvent(events, errors);
						current += events.size();
						printProgress(startTime, total, current);
						if (delay > 0) {
							Thread.sleep(delay);
						}
					} else {
						found = false;
						break;
					}
				}
				if (!errors.isEmpty())
					System.out.println("Error! while generating migration event data from nodes, below nodes are ignored. \n" + errors);
				long endTime = System.currentTimeMillis();
				System.out.println("\nMigration Event Generation completed for object of type " + objectType + " in: " + (endTime - startTime) + "ms");
			} else {
				System.out.println("\nSkipped Generating migration event for objectType: " + objectType);
			}
		}
		System.out.println("-----------------------------------------");
		long endTime = System.currentTimeMillis();
		System.out.println("Migration Event Generation completed at " + endTime);
		System.out.println("Time taken to generate Events: " + (endTime - startTime) + "ms");
	}

	private void sendEvent(Map<String, String> events, Map<String, String> errors) {
		for (String id : events.keySet()) {
			try {
				KafkaUtil.send(events.get(id), migrationTopicName);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error Message :"+e.getMessage() );
				errors.put(id, "Error While Sending Migration Event for " + id);
			}
		}
	}

	private Map<String, String> generateMigrationEvent(List<Node> nodes, Map<String, String> errors) {
		Map<String, String> events = new HashMap<String, String>();
		for (Node node : nodes) {
			String message = getEvent(node, errors);
			if (StringUtils.isNotBlank(message))
				events.put(node.getIdentifier(), message);
		}
		return events;
	}

	private String getEvent(Node node, Map<String, String> errors) {
		Map<String, Object> actor = new HashMap<String, Object>() {{
			put("id", actorId);
			put("type", actorType);
		}};
		Map<String, Object> context = new HashMap<String, Object>() {{
			put("channel", node.getMetadata().getOrDefault("channel", ""));
			put("pdata", new HashMap<String, Object>() {{
				put("id", pdataId);
				put("ver", pdataVersion);
			}});
		}};
		if (Platform.config.hasPath("cloud_storage.env")) {
			String env = Platform.config.getString("cloud_storage.env");
			context.put("env", env);
		}
		Map<String, Object> object = new HashMap<String, Object>() {{
			put("id", node.getIdentifier());
			put("ver", node.getMetadata().get("versionKey"));
		}};
		Map<String, Object> edata = new HashMap<String, Object>() {{
			put("action", action);
			put("metadata", new HashMap<String, Object>() {{
				put("pkgVersion", node.getMetadata().get("pkgVersion"));
				put("mimeType", node.getMetadata().get("mimeType"));
				put("status", node.getMetadata().get("status"));
				put("identifier", node.getIdentifier());
				put("objectType", node.getObjectType());
			}});
		}};
		String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
		if (StringUtils.isBlank(beJobRequestEvent)) {
			errors.put(node.getIdentifier(), "Error While Generating Migration Event for " + node.getIdentifier());
		}
		return beJobRequestEvent;
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

	public static void filterMigrationNodes(List<Node> nodes, Integer limit) {
		nodes.removeIf(n -> SystemNodeTypes.DEFINITION_NODE.name().equals(n.getNodeType()));
	}
}
