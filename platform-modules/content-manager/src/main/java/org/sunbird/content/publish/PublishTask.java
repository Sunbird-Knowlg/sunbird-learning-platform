package org.sunbird.content.publish;

import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.pipeline.initializer.InitializePipeline;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.rits.cloning.Cloner;

import java.util.Map;

public class PublishTask implements Runnable {

	private Map<String, Object> parameterMap;
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	/** The SimpleDateformatter. */
	private ControllerUtil util = new ControllerUtil();

	public PublishTask(Map<String, Object> parameterMap) {
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
		Cloner cloner = new Cloner();
		Node cloneNode = cloner.deepClone(node);
		String nodeId = node.getIdentifier().replace(".img", "");
		TelemetryManager.info("Publish processing start for node: "+ nodeId);
		String basePath = PublishManager.getBasePath(nodeId, null);
		TelemetryManager.info("Base path to store files: " + basePath);
		try {
			setContentBody(node, mimeType);
			this.parameterMap.put(ContentWorkflowPipelineParams.node.name(), node);
			this.parameterMap.put(ContentWorkflowPipelineParams.ecmlType.name(), PublishManager.isECMLContent(mimeType));
			InitializePipeline pipeline = new InitializePipeline(basePath, nodeId);
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), this.parameterMap);
		} catch (Exception e) {
			TelemetryManager.error("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: " + nodeId
					+ "]", e);
			cloneNode.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), e.getMessage());
			cloneNode.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Failed.name());
			util.updateNode(cloneNode);
		}
	}

	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			node.getMetadata().put(ContentAPIParams.body.name(), PublishManager.getContentBody(node.getIdentifier()));
		}
	}

}