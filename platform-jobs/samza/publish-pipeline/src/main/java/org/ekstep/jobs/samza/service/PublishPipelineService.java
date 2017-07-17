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
			Map<String,Object> eksMap = getMap(message);
			Node node = getNode(eksMap);
			String contentId = (String) eksMap.get(PublishPipelineParams.id.name());
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(ContentAPIParams.node.name(), node);
			String mimeType = (String) node.getMetadata().get("mimeType");
			LOGGER.info("Checking if node mimeType is ecml" + mimeType);
			if (StringUtils.equalsIgnoreCase(mimeType, "application/vnd.ekstep.ecml-archive")) {
				parameterMap.put(PublishPipelineParams.ecmlType.name(), true);
			} else {
				parameterMap.put(PublishPipelineParams.ecmlType.name(), false);
			}
			parameterMap.put(PublishPipelineParams.node.name(), node);
			LOGGER.info("Start Publish Pipeline | Initiatizing Publish Pipeline");
			InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), parameterMap);
			LOGGER.info("Node fetched from graph");
		} catch (Exception e) {
			LOGGER.info("Something went wrong while publishing the content" + e.getMessage());
			e.printStackTrace();
		}
	}

	private Node getNode(Map<String, Object> message) throws Exception {
		String nodeId = (String) message.get(PublishPipelineParams.id.name());
		String state = (String) message.get(PublishPipelineParams.State.name());
		LOGGER.info("Checking if node status is Processing" + state);
		if (StringUtils.equalsIgnoreCase(PublishPipelineParams.Processing.name(), state)) {
			LOGGER.info("Fetching Node required for Publish Operation" + nodeId);
			return getNodeForOperation(PublishPipelineParams.domain.name(), nodeId);
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String,Object> getMap(Map<String, Object> message){
		Map<String,Object> edata = (Map)message.get(PublishPipelineParams.edata.name()); 
		Map<String,Object> eks = (Map)edata.get(PublishPipelineParams.eks.name());
		return eks;
	}
	
	private Node getNodeForOperation(String taxonomyId, String contentId) {
		Node node = new Node();
		String contentImageId = getContentImageIdentifier(contentId);

		LOGGER.info("Fetching the Content Image Node for Content Id: " + contentId);
		Node imageNode = util.getNode(taxonomyId, contentImageId);
		if (null == imageNode) {
			LOGGER.info("Unable to Fetch Content Image Node for Content Id: " + contentId);
			LOGGER.info("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId);
			node = util.getNode(taxonomyId, contentId);
			if (null != node) {
				LOGGER.info("Fetched Content Node: "+ node);
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
		LOGGER.info("Creating Image Node");
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

	private String getContentImageIdentifier(String contentId) {
		String contentImageId = "";
		if (StringUtils.isNotBlank(contentId))
			contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return contentImageId;
	}
}
