package org.ekstep.content.publish;

import org.ekstep.common.exception.ClientException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Arrays;
import java.util.Map;

public class PublishTask implements Runnable {

	private String contentId;
	private Map<String, Object> parameterMap;
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	/** The SimpleDateformatter. */
	private ControllerUtil util = new ControllerUtil();
	private HierarchyStore hierarchyStore = new HierarchyStore();

	public PublishTask(String contentId, Map<String, Object> parameterMap) {
		this.contentId = contentId;
		this.parameterMap = parameterMap;
	}

	@Override
	public void run() {
		Node node = (Node) this.parameterMap.get(ContentWorkflowPipelineParams.node.name());
		try {
			publishContent(node);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void publishContent(Node node) throws Exception{
		TelemetryManager.info("Publish processing start for content" + node.getIdentifier());
		publishNode(node, (String) node.getMetadata().get("mimeType"));
		TelemetryManager.info("Publish processing done for content: "+ node.getIdentifier());
	}
	
		

	private void publishNode(Node node, String mimeType) {

		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(), ContentErrorMessageConstants.INVALID_CONTENT
					+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
		String nodeId = node.getIdentifier().replace(".img", "");
		TelemetryManager.info("Publish processing start for node: "+ nodeId);
		try {
			setContentBody(node, mimeType);
			this.parameterMap.put(ContentWorkflowPipelineParams.node.name(), node);
			this.parameterMap.put(ContentWorkflowPipelineParams.ecmlType.name(), PublishManager.isECMLContent(mimeType));
			InitializePipeline pipeline = new InitializePipeline(PublishManager.getBasePath(nodeId, null), nodeId);
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), this.parameterMap);
		} catch (Exception e) {
			TelemetryManager.error("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: " + nodeId
					+ "]", e);
			node.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), e.getMessage());
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Failed.name());
			util.updateNode(node);
		}
	}

	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			node.getMetadata().put(ContentAPIParams.body.name(), PublishManager.getContentBody(node.getIdentifier()));
		}
	}

}