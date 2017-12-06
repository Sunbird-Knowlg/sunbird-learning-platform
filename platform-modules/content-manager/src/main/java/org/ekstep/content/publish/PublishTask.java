package org.ekstep.content.publish;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.logger.LoggerEnum;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.PublishWebHookInvoker;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.dto.NodeDTO;

public class PublishTask implements Runnable {

	private String contentId;
	private Map<String, Object> parameterMap;
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

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

	// TODO: add try catch here.
	private void publishContent(Node node, String mimeType) {
		PlatformLogger.log("Publish processing start for content", this.contentId, LoggerEnum.INFO.name());
		if (StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType)) {
			List<NodeDTO> nodes = util.getNodesForPublish(node);
			Stream<NodeDTO> nodesToPublish = filterAndSortNodes(nodes);
			nodesToPublish.forEach(nodeDTO -> publishCollectionNode(nodeDTO, (String)node.getMetadata().get("publish_type")));
			if (!nodes.isEmpty()) {
				int compatabilityLevel = getCompatabilityLevel(nodes);
				node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), compatabilityLevel);
			}
		}
		publishNode(node, mimeType);
	}
	
	private Integer getCompatabilityLevel(List<NodeDTO> nodes) {
		final Comparator<NodeDTO> comp = (n1, n2) -> Integer.compare( n1.getCompatibilityLevel(), n2.getCompatibilityLevel());
		Optional<NodeDTO> maxNode = nodes.stream().max(comp);
		if (maxNode.isPresent())
			return maxNode.get().getCompatibilityLevel();
		else 
			return 1;
	}

	private List<NodeDTO> dedup(List<NodeDTO> nodes) {
		List<String> ids = new ArrayList<String>();
		List<String> addedIds = new ArrayList<String>();
		List<NodeDTO> list = new ArrayList<NodeDTO>();
		for (NodeDTO node : nodes) {
			if(isImageNode(node.getIdentifier()) && !ids.contains(node.getIdentifier())) {
				ids.add(node.getIdentifier());
			}
		}
		for (NodeDTO node : nodes) {
			if(!ids.contains(node.getIdentifier()) && !ids.contains(getImageNodeID(node.getIdentifier()))) {
				ids.add(node.getIdentifier());
			}
		}
		
		for (NodeDTO node : nodes) {
			if(ids.contains(node.getIdentifier()) && !addedIds.contains(node.getIdentifier()) && !addedIds.contains(getImageNodeID(node.getIdentifier()))) {
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
		return dedup(nodes)
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

	public static void main(String[] args) {
		List<NodeDTO> nodes = new ArrayList<NodeDTO>();
		nodes.add(new NodeDTO("org.ekstep.lit.haircut.story", "Annual Haircut Day", 3, "Live", "application/vnd.ekstep.ecml-archive",
				"Default"));
		nodes.add(new NodeDTO("do_11226563129515212812", "send for review", 3, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("do_11229278865285120011", "My first story", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("domain_58339", "The Ghatotkach", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("domain_58339", "The Ghatotkach", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("do_11229291440146841611", "Test Collection 1", 2, "Live", "application/vnd.ekstep.content-collection",
				"Default"));
		nodes.add(new NodeDTO("do_11229278865285120011", "My first story", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("do_11229292215258316813", "Chapter 1", 1, "Live", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292248830771214", "Chapter 2", 1, "Live", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292248830771214.img", "Chapter 2", 1, "Draft", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292215258316813.img", "Chapter 1", 1, "Draft", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292183262822412", "Testbook 5", 0, "Live", "application/vnd.ekstep.content-collection", "Default"));
		
		
		PublishTask task = new PublishTask("", null);
		Stream<NodeDTO> stream = task.filterAndSortNodes(nodes);
		stream.forEach(nodeDTO -> System.out.println(nodeDTO.getIdentifier() + "," + nodeDTO.getMimeType()));
	}

	private void publishCollectionNode(NodeDTO node, String publishType) {
		Node graphNode = util.getNode("domain", node.getIdentifier());
		if(StringUtils.isNotEmpty(publishType)) {
			graphNode.getMetadata().put("publish_type", publishType);
		}
		publishNode(graphNode, node.getMimeType());
	}

	private void publishNode(Node node, String mimeType) {

		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(), ContentErrorMessageConstants.INVALID_CONTENT
					+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
		String nodeId = node.getIdentifier().replace(".img", "");
		PlatformLogger.log("Publish processing start for node", nodeId, LoggerEnum.INFO.name());
		try {
			setContentBody(node, mimeType);
			this.parameterMap.put(ContentWorkflowPipelineParams.node.name(), node);
			this.parameterMap.put(ContentWorkflowPipelineParams.ecmlType.name(), PublishManager.isECMLContent(mimeType));
			InitializePipeline pipeline = new InitializePipeline(PublishManager.getBasePath(nodeId, null), nodeId);
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), this.parameterMap);
		} catch (Exception e) {
			PlatformLogger.log("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: " + nodeId
					+ "]", e);
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