package org.ekstep.content.operation.finalizer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.logger.LoggerEnum;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.common.util.LogTelemetryEventUtil;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.dac.model.Node;

public class ReviewFinalizer extends BaseFinalizer {

	/** The logger. */
	

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;
	
	private static String actorId = "Publish Samza Job";
	private static String actorType = "System";
	private static String pdataId = "org.ekstep.platform";
	private static String pdataVersion = "1.0";
	private static String action = "publish";
	private static String status = "Pending";

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
		PlatformLogger.log("Parameter Map: ", parameterMap);
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
		String publishType = null;
		if (BooleanUtils.isTrue(isPublishOperation)) {
			PlatformLogger.log("Changing the Content Status to 'Pending'.", LoggerEnum.INFO.name());
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Pending.name());
			publishType = (String)node.getMetadata().get("publish_type");
			node.getMetadata().remove("publish_type");
		} else {
			PlatformLogger.log("Changing the Content Status to 'Review'.", LoggerEnum.INFO.name());
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Review.name());
		}
		if(StringUtils.equalsIgnoreCase(prevState, ContentWorkflowPipelineParams.FlagDraft.name())){
			PlatformLogger.log("Setting status to flagReview from previous state : " + prevState, LoggerEnum.INFO.name());
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.FlagReview.name());
		}
		// Clean-Up
		node.getMetadata().put(ContentWorkflowPipelineParams.reviewError.name(), null);
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());
		
		PlatformLogger.log("Updating the Node: ", node.getIdentifier(), LoggerEnum.INFO.name());
		Response response = updateContentNode(contentId, newNode, null);
		PlatformLogger.log("Generating Telemetry Event. | [Content ID: " + contentId + "]", node);
		newNode.getMetadata().put(ContentWorkflowPipelineParams.prevState.name(), prevState);
		newNode.getMetadata().put("publish_type", publishType);
		
		if (BooleanUtils.isTrue(isPublishOperation)) {
			Map<String,Object> actor = new HashMap<String,Object>();
			Map<String,Object> context = new HashMap<String,Object>();
			Map<String,Object> object = new HashMap<String,Object>();
			Map<String,Object> edata = new HashMap<String,Object>();
			
			generateInstructionEventMetadata(actor, context, object, edata, newNode.getMetadata(), contentId);
			LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
		}
		node.getMetadata().put("publish_type", publishType); //Added for executing publish operation locally
		return response;
	}
	
	private void generateInstructionEventMetadata(Map<String,Object> actor, Map<String,Object> context, 
			Map<String,Object> object, Map<String,Object> edata, Map<String, Object> metadata, String contentId) {
		
		actor.put("id", actorId);
		actor.put("type", actorType);
		
		context.put("channel", metadata.get("channel")); 
		Map<String, Object> pdata = new HashMap<>();
		pdata.put("id", pdataId); 
		pdata.put("ver", pdataVersion);
		context.put("pdata", pdata);
		if (Platform.config.hasPath("s3.env")) {
			String env = Platform.config.getString("s3.env");
			context.put("env", env);
		}
		
		object.put("id", contentId);
		object.put("type", metadata.get("contentType"));
		object.put("ver", metadata.get("versionKey"));
		
		edata.put("action", action);
		edata.put("status", status);
		edata.put("publish_type", (String)metadata.get("publish_type"));
	}

}
