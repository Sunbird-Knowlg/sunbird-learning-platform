package org.ekstep.content.operation.finalizer;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

public class ReviewFinalizer extends BaseFinalizer {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	/**
	 * Instantiates a new ReviewFinalizer and sets the base path and current
	 * content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public ReviewFinalizer(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	/**
	 * finalize()
	 *
	 * @param Map
	 *            the parameterMap
	 * 
	 *            checks if Node, ecrfType,ecmlType exists in the parameterMap
	 *            else throws ClientException Output only ECML format create
	 *            'artifactUrl' Get Content String write ECML File Create 'ZIP'
	 *            Package Upload Package Upload to S3 Set artifact file For Node
	 *            Download App Icon and create thumbnail Set Package Version
	 *            Create ECAR Bundle Delete local compressed artifactFile
	 *            Populate Fields and Update Node
	 * @return the response
	 */
	public Response finalize(Map<String, Object> parameterMap) {
		LOGGER.log("Parameter Map: "+ parameterMap);
		if (null == parameterMap)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_PARAMETER_MAP + " | [Parameter Map Cannot be 'null']");
		
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");

		String prevState = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
		
		Boolean isPublishOperation = (Boolean) parameterMap
				.get(ContentWorkflowPipelineParams.isPublishOperation.name());
	
		if (BooleanUtils.isTrue(isPublishOperation)) {
			LOGGER.log("Changing the Content Status to 'Processing'.");
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Processing.name());
		} else {
			LOGGER.log("Changing the Content Status to 'Review'.");
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Review.name());
		}
		if(StringUtils.equalsIgnoreCase(prevState, ContentWorkflowPipelineParams.FlagDraft.name())){
			LOGGER.log("Setting status to flagReview from previous state : " + prevState);
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.FlagReview.name());
		}
		// Clean-Up
		node.getMetadata().put(ContentWorkflowPipelineParams.reviewError.name(), null);
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		
		LOGGER.log("Updating the Node: ", node.getIdentifier());
		Response response = updateContentNode(contentId, newNode, null);
		LOGGER.log("Generating Telemetry Event. | [Content ID: " + contentId + "]", node);
		newNode.getMetadata().put(ContentWorkflowPipelineParams.prevState.name(), prevState);
		LogTelemetryEventUtil.logContentLifecycleEvent(newNode.getIdentifier(), newNode.getMetadata());
		return response;
	}

}
