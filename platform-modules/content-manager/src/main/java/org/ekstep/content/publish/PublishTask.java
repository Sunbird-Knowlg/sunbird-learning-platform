package org.ekstep.content.publish;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.PublishWebHookInvoker;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

public class PublishTask implements Runnable {

	private String contentId;
	private Map<String, Object> parameterMap;

	private ControllerUtil util = new ControllerUtil();

	public PublishTask(String contentId, Map<String, Object> parameterMap) {
		this.contentId = contentId;
		this.parameterMap = parameterMap;
	}

	@Override
	public void run() {
		Node node = (Node) this.parameterMap.get(ContentWorkflowPipelineParams.node.name());
		String mimeType = (String) this.parameterMap.get("mimeType");
		publishContent(node, mimeType);
	}

	private void publishContent(Node node, String mimeType) {
		PlatformLogger.log("Publish processing start for content", this.contentId, LoggerEnum.INFO.name());
		if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType)) {
			List<NodeDTO> nodes = util.getNodesForPublish(node);
			Stream<NodeDTO> nodesToPublish = filterAndSortNodes(nodes);
			nodesToPublish.forEach(nodeDTO -> publishCollectionNode(nodeDTO));
		}
		publishNode(node, mimeType);
	}

	private Stream<NodeDTO> filterAndSortNodes(List<NodeDTO> nodes) {
		return nodes
				.stream()
				.filter(node -> StringUtils.equalsIgnoreCase(node.getMimeType(), "application/vnd.ekstep.content-collection")
						|| StringUtils.equalsIgnoreCase(node.getStatus(), "Draft"))
				.filter(node -> StringUtils.equalsIgnoreCase(node.getVisibility(), "parent")).sorted(new Comparator<NodeDTO>() {
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

		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(), ContentErrorMessageConstants.INVALID_CONTENT
					+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
		PlatformLogger.log("Publish processing start for node", node.getIdentifier(), LoggerEnum.INFO.name());
		try {
			setContentBody(node, mimeType);
			this.parameterMap.put(ContentWorkflowPipelineParams.node.name(), node);
			this.parameterMap.put(ContentWorkflowPipelineParams.ecmlType.name(), PublishManager.isECMLContent(mimeType));
			InitializePipeline pipeline = new InitializePipeline(PublishManager.getBasePath(node.getIdentifier()), node.getIdentifier());
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), this.parameterMap);
		} catch (Exception e) {
			PlatformLogger.log("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: "
					+ node.getIdentifier() + "]", e);
			node.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), e.getMessage());
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Failed.name());
			util.updateNode(node);
			PublishWebHookInvoker.invokePublishWebKook(contentId, ContentWorkflowPipelineParams.Failed.name(), e.getMessage());
		}
	}

	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			node.getMetadata().put(ContentAPIParams.body.name(), PublishManager.getContentBody(node.getIdentifier()));
		}
	}

}
