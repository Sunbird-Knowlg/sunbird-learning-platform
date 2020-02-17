package org.ekstep.content.operation.finalizer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.kafka.KafkaClient;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;

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

	private static Boolean asyncCurationEnabled = Platform.config.hasPath("async.curation.enabled")?Platform.config.getBoolean("async.curation.enabled"):false;
	
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
	 *            Package Upload Package Upload to AWS/Azure Set artifact file For Node
	 *            Download App Icon and create thumbnail Set Package Version
	 *            Create ECAR Bundle Delete local compressed artifactFile
	 *            Populate Fields and Update Node
	 * @return the response
	 */
	public Response finalize(Map<String, Object> parameterMap) {
		TelemetryManager.log("Parameter Map: ", parameterMap);
		Response response;
		if (null == parameterMap)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_PARAMETER_MAP + " | [Parameter Map Cannot be 'null']");
		
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
		
		Boolean isPublishOperation = (Boolean) parameterMap.get(ContentWorkflowPipelineParams.isPublishOperation.name());
		if (BooleanUtils.isTrue(isPublishOperation)) {
			//TelemetryManager.info("Changing the Content Status to 'Pending' for content id: " + node.getIdentifier());
			String publishType = (String)node.getMetadata().get("publish_type");
			node.getMetadata().remove("publish_type");
			try {
				pushInstructionEvent(node, publishType);
				TelemetryManager.info("Content: " + node.getIdentifier() + " pushed to kafka for publish operation.");
			} catch (Exception e) {
				throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
						"Error occured during content publish", e);
			}
			node.getMetadata().put("publish_type", publishType); //Added for executing publish operation locally
			
			response = new Response();
			ResponseParams param = new ResponseParams();
			param.setStatus(StatusType.successful.name());
			response.setParams(param);
			response.put("publishStatus", "Publish Event for Content Id '" + node.getIdentifier() + "' is pussed Successfully!");
			response.put("node_id", node.getIdentifier());
		} else {
			String prevState = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
			if(StringUtils.equalsIgnoreCase(prevState, ContentWorkflowPipelineParams.FlagDraft.name())){
				TelemetryManager.info("Setting status to flagReview from previous state : " + prevState + " for content id: " + node.getIdentifier());
				node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.FlagReview.name());
			}else {
				TelemetryManager.info("Changing the Content Status to 'Review' for content id: " + node.getIdentifier());
				node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Review.name());
			}
			node.getMetadata().put(ContentWorkflowPipelineParams.reviewError.name(), null);
			Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
			newNode.setGraphId(node.getGraphId());
			newNode.setMetadata(node.getMetadata());

			response = updateContentNode(contentId, newNode, null);
			// Code For Automated Curation Process - start
			if(!checkError(response)){
				String identifier = newNode.getIdentifier();
				String mimeType= (String) newNode.getMetadata().get("mimeType");
				if(StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive",mimeType) || StringUtils.equalsIgnoreCase("application/pdf",mimeType))
					try{
						curateContent(identifier, mimeType, newNode);
					}catch(Exception e){
						e.printStackTrace();
						TelemetryManager.error("Error Occurred While Content Curation Process : ", e);
					}
			}
			// Code For Automated Curation Process - end

		}
		
		return response;
	}

	// method for curate content
	private void curateContent(String identifier, String mimeType, Node newNode) throws Exception {
		if(asyncCurationEnabled){
			pushCurationEvent(identifier, newNode);
		}else {
			curateContentSync(identifier, mimeType, newNode);
		}

	}

	private void curateContentSync(String identifier, String mimeType, Node newNode) {
		System.out.println("Content Curation Process Started In Synchronous Mode..");
		// TODO: code here for same functionality without samza.
	}

	private void pushCurationEvent(String identifier, Node node) throws Exception {
		Map<String, Object> metadata = node.getMetadata();
		Map<String,Object> actor = new HashMap<String,Object>();
		Map<String,Object> context = new HashMap<String,Object>();
		Map<String,Object> object = new HashMap<String,Object>();
		Map<String,Object> edata = new HashMap<String,Object>();

		actor.put("id", actorId);
		actor.put("type", actorType);

		context.put("channel", metadata.get("channel"));
		Map<String, Object> pdata = new HashMap<>();
		pdata.put("id", pdataId);
		pdata.put("ver", pdataVersion);
		context.put("pdata", pdata);
		if (Platform.config.hasPath("cloud_storage.env")) {
			String env = Platform.config.getString("cloud_storage.env");
			context.put("env", env);
		}

		object.put("id", contentId);
		object.put("ver", metadata.get("versionKey"));
		List<String> actions = (StringUtils.equalsIgnoreCase("application/pdf", (String) metadata.get("mimeType")))? Platform.config.getStringList("curation.pdf.action"):Platform.config.getStringList("curation.ecml.action");
		edata.put("action", actions);
		String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
		String topic = Platform.config.getString("kafka.topics.curation");
		if(StringUtils.isBlank(beJobRequestEvent)) {
			TelemetryManager.error("Instruction event is not generated properly. # beJobRequestEvent : " + beJobRequestEvent);
			throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
		}
		if(StringUtils.isNotBlank(topic)) {
			KafkaClient.send(beJobRequestEvent, topic);
		} else {
			TelemetryManager.error("Invalid topic id. # topic : " + topic);
			throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Invalid topic id.");
		}
		System.out.println("Content Curation Instruction Event Pushed to Kafka..");

	}

	private void pushInstructionEvent(Node node, String publishType) throws Exception{
		Map<String,Object> actor = new HashMap<String,Object>();
		Map<String,Object> context = new HashMap<String,Object>();
		Map<String,Object> object = new HashMap<String,Object>();
		Map<String,Object> edata = new HashMap<String,Object>();
		
		generateInstructionEventMetadata(actor, context, object, edata, node.getMetadata(), contentId, publishType);
		String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
		System.out.println("Curation Event : "+beJobRequestEvent);
		String topic = Platform.config.getString("kafka.topics.instruction");
		if(StringUtils.isBlank(beJobRequestEvent)) {
			TelemetryManager.error("Instruction event is not generated properly. # beJobRequestEvent : " + beJobRequestEvent);
			throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
		}
		if(StringUtils.isNotBlank(topic)) {
			KafkaClient.send(beJobRequestEvent, topic);
		} else {
			TelemetryManager.error("Invalid topic id. # topic : " + topic);
			throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Invalid topic id.");
		}
	}
	
	private void generateInstructionEventMetadata(Map<String,Object> actor, Map<String,Object> context, 
			Map<String,Object> object, Map<String,Object> edata, Map<String, Object> metadata, String contentId, String publishType) {
		
		actor.put("id", actorId);
		actor.put("type", actorType);
		
		context.put("channel", metadata.get("channel")); 
		Map<String, Object> pdata = new HashMap<>();
		pdata.put("id", pdataId); 
		pdata.put("ver", pdataVersion);
		context.put("pdata", pdata);
		if (Platform.config.hasPath("cloud_storage.env")) {
			String env = Platform.config.getString("cloud_storage.env");
			context.put("env", env);
		}
		
		object.put("id", contentId);
		object.put("ver", metadata.get("versionKey"));

		Map<String, Object> instructionEventMetadata = new HashMap<>();
		instructionEventMetadata.put("pkgVersion", metadata.get("pkgVersion"));
		instructionEventMetadata.put("mimeType", metadata.get("mimeType"));
		instructionEventMetadata.put("lastPublishedBy", metadata.get("lastPublishedBy"));

		edata.put("action", action);
		edata.put("metadata", instructionEventMetadata);
		edata.put("publish_type", publishType);
		edata.put("contentType", metadata.get("contentType"));
	}
}
