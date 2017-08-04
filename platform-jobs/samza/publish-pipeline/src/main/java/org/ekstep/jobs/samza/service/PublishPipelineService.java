package org.ekstep.jobs.samza.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.publish.PublishManager;
import org.ekstep.content.util.PublishWebHookInvoker;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.PublishPipelineParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;

public class PublishPipelineService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(PublishPipelineService.class);

	private String contentId;

	private Map<String, Object> parameterMap;

	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

	private ControllerUtil util = new ControllerUtil();

	@SuppressWarnings("unused")
	private Config config = null;

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		S3PropertyReader.loadProperties(props);
		Configuration.loadProperties(props);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
		JedisFactory.initialize(props);
		LOGGER.info("Redis connection factory initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		Map<String, Object> eks = getPublishLifecycleData(message);

		if (null == eks) {
			metrics.incSkippedCounter();
			return;
		}
		try {
			String nodeId = (String) eks.get(PublishPipelineParams.id.name());
			Node node = util.getNode(PublishPipelineParams.domain.name(), nodeId);
			String mimeType = (String) node.getMetadata().get(PublishPipelineParams.mimeType.name());
			if ((null != node) && (node.getObjectType().equalsIgnoreCase(PublishPipelineParams.content.name()))) {
				publishContent(node, mimeType);
				metrics.incSuccessCounter();
			} else {
				metrics.incSkippedCounter();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to process message", message, e);
			metrics.incFailedCounter();
		}
	}

	private void publishContent(Node node, String mimeType) {
		LOGGER.info("Publish processing start for content");
		if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType)) {
			List<NodeDTO> nodes = util.getNodesForPublish(node);
			Stream<NodeDTO> nodesToPublish = filterAndSortNodes(nodes);
			nodesToPublish.forEach(nodeDTO -> publishCollectionNode(nodeDTO));
		}
		publishNode(node, mimeType);
	}

	private List<NodeDTO> dedup(List<NodeDTO> nodes) {
		List<String> ids = new ArrayList<String>();
		List<String> addedIds = new ArrayList<String>();
		List<NodeDTO> list = new ArrayList<NodeDTO>();
		for (NodeDTO node : nodes) {
			if (isImageNode(node.getIdentifier()) && !ids.contains(node.getIdentifier())) {
				ids.add(node.getIdentifier());
			}
		}
		for (NodeDTO node : nodes) {
			if (!ids.contains(node.getIdentifier()) && !ids.contains(getImageNodeID(node.getIdentifier()))) {
				ids.add(node.getIdentifier());
			}
		}

		for (NodeDTO node : nodes) {
			if (ids.contains(node.getIdentifier()) && !addedIds.contains(node.getIdentifier())
					&& !addedIds.contains(getImageNodeID(node.getIdentifier()))) {
				list.add(node);
				addedIds.add(node.getIdentifier());
			}
		}
		return list;
	}

	private boolean isImageNode(String identifier) {
		return StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
	}

	private String getImageNodeID(String identifier) {
		return identifier + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
	}

	public Stream<NodeDTO> filterAndSortNodes(List<NodeDTO> nodes) {
		return dedup(nodes).stream()
				.filter(node -> StringUtils.equalsIgnoreCase(node.getMimeType(),
						"application/vnd.ekstep.content-collection")
						|| StringUtils.equalsIgnoreCase(node.getStatus(), "Draft"))
				.filter(node -> StringUtils.equalsIgnoreCase(node.getVisibility(), "parent"))
				.sorted(new Comparator<NodeDTO>() {
					@Override
					public int compare(NodeDTO o1, NodeDTO o2) {
						return o2.getDepth().compareTo(o1.getDepth());
					}
				});
	}

	private void publishCollectionNode(NodeDTO node) {
		Node graphNode = util.getNode("domain", node.getIdentifier());
		publishNode(graphNode, node.getMimeType());
	}

	private void publishNode(Node node, String mimeType) {
		String nodeId = node.getIdentifier().replace(".img", "");
		LOGGER.info("Publish processing start for node", nodeId);
		try {
			setContentBody(node, mimeType);
			parameterMap.put(PublishPipelineParams.node.name(), node);
			parameterMap.put(PublishPipelineParams.ecmlType.name(),
					PublishManager.isECMLContent(mimeType));
			InitializePipeline pipeline = new InitializePipeline(PublishManager.getBasePath(nodeId), nodeId);
			pipeline.init(PublishPipelineParams.publish.name(), parameterMap);
		} catch (Exception e) {
			LOGGER
					.info("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: "
							+ nodeId + "]", e.getMessage());
			node.getMetadata().put(PublishPipelineParams.publishError.name(), e.getMessage());
			node.getMetadata().put(PublishPipelineParams.status.name(),
					PublishPipelineParams.Failed.name());
			util.updateNode(node);
			PublishWebHookInvoker.invokePublishWebKook(contentId, ContentWorkflowPipelineParams.Failed.name(),
					e.getMessage());
		}
	}

	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			node.getMetadata().put(PublishPipelineParams.body.name(), PublishManager.getContentBody(node.getIdentifier()));
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getPublishLifecycleData(Map<String, Object> message) {
		String eid = (String) message.get("eid");
		if (null == eid || !StringUtils.equalsIgnoreCase(eid, PublishPipelineParams.BE_OBJECT_LIFECYCLE.name())) {
			return null;
		}

		Map<String, Object> edata = (Map<String, Object>) message.get("edata");
		if (null == edata) {
			return null;
		}

		Map<String, Object> eks = (Map<String, Object>) edata.get("eks");
		if (null == eks) {
			return null;
		}
		if (StringUtils.equalsIgnoreCase((String) eks.get("state"), "Processing")) {
			{
				return eks;
			}
		}
		return null;
	}
}