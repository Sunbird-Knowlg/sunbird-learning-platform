package org.ekstep.jobs.samza.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import java.util.Map;

public class CollectionMigrationService implements ISamzaService {

	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	private static JobLogger LOGGER = new JobLogger(CollectionMigrationService.class);
	private Config config = null;
	private SystemStream systemStream = null;
	private ControllerUtil util = new ControllerUtil();

	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized.");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
		systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
		LOGGER.info("Stream initialized for Failed Events");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		if (null == message) {
			LOGGER.info("Ignoring the message because it is not valid for collection migration.");
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get("edata");
		Map<String, Object> object = (Map<String, Object>) message.get("object");

		if (!validateEdata(edata) || null == object) {
			LOGGER.info("Ignoring the message because it is not valid for collection migration.");
			return;
		}
		String nodeId = (String) object.get("id");
		if (StringUtils.isNotBlank(nodeId)) {
			// TODO: Get the hierarchy from cassandra and skip if exists.
			Node node = getNode(nodeId);
			if (null != node && validNode(node)) {
				LOGGER.info("Initializing migration for collection ID: " + node.getIdentifier());
				// TODO: Execute migration.
				LOGGER.info("Migration completed for collection ID: " + node.getIdentifier());
			} else {
				metrics.incSkippedCounter();
				FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
						PlatformErrorCodes.PROCESSING_ERROR.name(), new ServerException("ERR_COLLECTION_MIGRATION", "Please check neo4j connection or identfier to migrate."));
				LOGGER.info("Invalid Node Object. Unable to process the event", message);
			}
		} else {
			FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
					PlatformErrorCodes.SYSTEM_ERROR.name(), new ServerException("ERR_COLLECTION_MIGRATION", "Id is blank"));
			metrics.incSkippedCounter();
			LOGGER.info("Invalid NodeId. Unable to process the event", message);
		}
	}

	/**
	 * Checking is it a valid node for migration.
	 *
	 * @param node
	 * @return boolean
	 */
	private boolean validNode(Node node) {
		Map<String, Object> metadata = (Map<String, Object>) node.getMetadata();
		String visibility = (String) metadata.get("visibility");
		String mimeType = (String) metadata.get("mimeType");
		if (StringUtils.equalsIgnoreCase("Default", visibility) && StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType))
			return true;
		else
			return false;
	}

	private boolean validateEdata(Map<String, Object> edata) {
		String action = (String) edata.get("action");
		if (StringUtils.equalsIgnoreCase("collection-migration", action)) {
			return true;
		}
		return false;
	}

	private Node getNode(String nodeId) {
		String imgNodeId = nodeId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		Node node = util.getNode("domain", imgNodeId);
		if (null == node) {
			node = util.getNode("domain", nodeId);
		}
		return node;
	}
}