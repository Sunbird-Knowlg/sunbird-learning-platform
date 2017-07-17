package org.ekstep.jobs.samza.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.PublishPipelineParams;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;

public class PublishPipelineService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(PublishPipelineService.class);

	@SuppressWarnings("unused")
	private Config config = null;

	private static final String tempFileLocation = "/data/contentBundle/";

	private ControllerUtil util = new ControllerUtil();

	private static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	private static final String CONTENT_IMAGE_OBJECT_TYPE = "ContentImage";

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		Configuration.loadProperties(props);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Learning actors initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		try {
			Node node = getNode(message);
			String contentId = (String) message.get(PublishPipelineParams.nodeUniqueId.name());
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(ContentAPIParams.node.name(), node);
			String mimeType = (String) node.getMetadata().get("mimeType");
			if (StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.ecml-archive")) {
				parameterMap.put(PublishPipelineParams.ecmlType.name(), true);
			} else {
				parameterMap.put(PublishPipelineParams.ecmlType.name(), false);
			}
			parameterMap.put(PublishPipelineParams.node.name(), node);
			InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), parameterMap);
			LOGGER.info("Node fetched from graph");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Node getNode(Map<String, Object> message) throws Exception {
		String nodeId = (String) message.get(PublishPipelineParams.nodeUniqueId.name());
		Map<String, Object> statusMap = getStateChangeEvent(message);
		String prevstate = (String) statusMap.get("ov");
		String state = (String) statusMap.get("nv");
		if (StringUtils.equalsIgnoreCase("Draft", prevstate) && StringUtils.equalsIgnoreCase("Processing", state)) {
			return getNodeForOperation(PublishPipelineParams.domain.name(), nodeId);
		}
		return null;
	}

	private Node getNodeForOperation(String taxonomyId, String contentId) {
		Node node = new Node();

		String contentImageId = getContentImageIdentifier(contentId);
		LOGGER.info("Fetching the Content Node. | [Content ID: " + contentId + "]");

		LOGGER.info("Fetching the Content Image Node for Content Id: " + contentId);
		Node imageNode = util.getNode(taxonomyId, contentImageId);
		if (null == imageNode) {
			LOGGER.info("Unable to Fetch Content Image Node for Content Id: " + contentId);
			LOGGER.info("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId);
			node = util.getNode(taxonomyId, contentId);
			if (null != node) {
				PlatformLogger.log("Fetched Content Node: ", node);
				String status = (String) node.getMetadata().get(PublishPipelineParams.status.name());
				if (StringUtils.isNotBlank(status)
						&& (StringUtils.equalsIgnoreCase(PublishPipelineParams.Live.name(), status)
								|| StringUtils.equalsIgnoreCase(PublishPipelineParams.Flagged.name(), status))) {
					node = createContentImageNode(taxonomyId, contentImageId, node);
				}
			}
			return node;
		} else {
			return imageNode;
		}
	}

	private Node createContentImageNode(String taxonomyId, String contentImageId, Node node) {
		Node imageNode = new Node(taxonomyId, SystemNodeTypes.DATA_NODE.name(), CONTENT_IMAGE_OBJECT_TYPE);
		imageNode.setGraphId(taxonomyId);
		imageNode.setIdentifier(contentImageId);
		imageNode.setMetadata(node.getMetadata());
		imageNode.setInRelations(node.getInRelations());
		imageNode.setOutRelations(node.getOutRelations());
		imageNode.setTags(node.getTags());
		imageNode.getMetadata().put(PublishPipelineParams.status.name(), PublishPipelineParams.Draft.name());
		util.createDataNode(imageNode);
		Node nodeData = util.getNode(taxonomyId, contentImageId);
		return nodeData;
	}

	private static String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = tempFileLocation + File.separator + System.currentTimeMillis()
					+ ContentWorkflowPipelineParams._temp.name() + File.separator + contentId;
		return path;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getStateChangeEvent(Map<String, Object> message) {

		if (!message.containsKey(PublishPipelineParams.nodeUniqueId.name()))
			return null;
		if (!message.containsKey(PublishPipelineParams.transactionData.name()))
			return null;
		Map<String, Object> transactionMap = (Map<String, Object>) message
				.get(PublishPipelineParams.transactionData.name());

		if (!transactionMap.containsKey(PublishPipelineParams.properties.name()))
			return null;
		Map<String, Object> propertiesMap = (Map<String, Object>) transactionMap
				.get(PublishPipelineParams.properties.name());

		if (propertiesMap.containsKey(PublishPipelineParams.status.name())) {
			return (Map<String, Object>) propertiesMap.get(PublishPipelineParams.status.name());
		}

		return null;
	}

	private String getContentImageIdentifier(String contentId) {
		String contentImageId = "";
		if (StringUtils.isNotBlank(contentId))
			contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return contentImageId;
	}
}
