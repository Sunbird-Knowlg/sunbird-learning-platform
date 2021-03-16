package org.ekstep.content.publish;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.mgr.impl.NodeManager;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.contentstore.ContentStore;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.rits.cloning.Cloner;
import com.typesafe.config.Config;

public class PublishContent extends BaseContentManager{
	
	private Map<String, Object> parameterMap = new HashMap<String, Object>();
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	Cloner cloner = new Cloner();

	public PublishContent(Config conf) {
		Platform.config = conf;
	}
	
	public Response publishContent(String identifier, String publishType) {
		TelemetryManager.info("PublishContent:publishContent:: Publish processing start for node: "+ identifier);
		Response response = null;
		Node cloneNode = null;
		try {
			Node node = getNodeForOperation(identifier, "publish", true);
			node.getMetadata().put("publish_type", publishType);

			if (null == node)
				throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(), ContentErrorMessageConstants.INVALID_CONTENT
						+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
			
			cloneNode = cloner.deepClone(node);
			String nodeId = node.getIdentifier().replace(".img", "");
			
			String basePath = PublishManager.getBasePath(nodeId, null);
			TelemetryManager.info("Base path to store files: " + basePath);
		
			String mimeType = getMimeType(node);
			if(PublishManager.isECMLContent(mimeType)) {
				setContentBody(node, mimeType);
				this.parameterMap.put(ContentWorkflowPipelineParams.ecmlType.name(), true);
			}
			this.parameterMap.put(ContentWorkflowPipelineParams.node.name(), node);
			this.parameterMap.put("mimeType", mimeType);
			this.parameterMap.put("disableAkka", true);
			
			InitializePipeline pipeline = new InitializePipeline(basePath, nodeId);
			response = pipeline.init(ContentWorkflowPipelineParams.publish.name(), this.parameterMap);
		} catch (Exception e) {
			TelemetryManager.error("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: " + identifier.replace(".img", "")
					+ "]", e);
			cloneNode.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), e.getMessage());
			cloneNode.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Failed.name());
			updateDataNode(cloneNode);
		}
		TelemetryManager.info("PublishContent:publishContent:: Publish processing finished for node: "+ identifier);
		return response;
	}
	protected Response updateDataNode(Node node) {
		try {
			Request request = new Request();
	        request.getContext().put(GraphHeaderParams.graph_id.name(), TAXONOMY_ID);
			request.put(GraphDACParams.node_id.name(), node.getIdentifier());
			request.put(GraphDACParams.node.name(), node);
			NodeManager nodeManager = new NodeManager();
			return nodeManager.updateDataNode(request);
		}catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage() + ". Please Try Again After Sometime!");
		}
	}
	
	
	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			ContentStore contentStore = new ContentStore();
			node.getMetadata().put(ContentAPIParams.body.name(), contentStore.getContentBody(node.getIdentifier()));
		}
	}

}
