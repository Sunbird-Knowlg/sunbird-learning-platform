package org.ekstep.content.operation.finalizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.NodeDTO;
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
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;

public class ReviewFinalizer extends BaseFinalizer {

	/** The logger. */
	

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;
	
	private static final Map<String, Object> actor = new HashMap<>();
	private static String pdataId = "org.ekstep.platform";
	private static String pdataVersion = "1.0";
	private static String action = "publish";
	private static final String COLLECTION_CONTENT_MIMETYPE = "application/vnd.ekstep.content-collection";
	private ControllerUtil util = new ControllerUtil();
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	
	static {
		actor.put("id", "Publish Samza Job");
		actor.put("type", "System");
	}
	
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
			
			
			String publishType = (String)node.getMetadata().get("publish_type");
			node.getMetadata().remove("publish_type");
			if (StringUtils.equalsIgnoreCase((String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()),
					COLLECTION_CONTENT_MIMETYPE)) {
				List<NodeDTO> nodes = util.getNodesForPublish(node);
				Stream<NodeDTO> nodesToPublish = filterAndSortNodes(nodes);
				nodesToPublish.forEach(nodeDTO -> {
					try {
						pushInstructionEvent(node, nodeDTO, publishType);
						TelemetryManager.info("Content: " + node.getIdentifier() + " pushed to kafka for publish operation.");
						
					} catch (Exception e) {
						throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.name(),
								"Error occured during content publish", e);
					}
				});
				if (!nodes.isEmpty()) {
					node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(),
							getCompatabilityLevel(nodes));
				}
			}
			try {
				pushInstructionEvent(node, null, publishType);
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
			newNode.setInRelations(null);
			response = updateContentNode(contentId, newNode, null);
		}
		
		return response;
	}
	
	private Integer getCompatabilityLevel(List<NodeDTO> nodes) {
		final Comparator<NodeDTO> comp = (n1, n2) -> Integer.compare(n1.getCompatibilityLevel(),
				n2.getCompatibilityLevel());
		Optional<NodeDTO> maxNode = nodes.stream().max(comp);
		if (maxNode.isPresent())
			return maxNode.get().getCompatibilityLevel();
		else
			return 1;
	}
	
	private Stream<NodeDTO> filterAndSortNodes(List<NodeDTO> nodes) {
		return dedup(nodes).stream().filter(
				node -> StringUtils.equalsIgnoreCase(node.getMimeType(), COLLECTION_CONTENT_MIMETYPE)
						|| StringUtils.equalsIgnoreCase(node.getStatus(), "Draft") || StringUtils.equalsIgnoreCase(node.getStatus(), "Failed"))
				.filter(node -> StringUtils.equalsIgnoreCase(node.getVisibility(), "parent"))
				.sorted(new Comparator<NodeDTO>() {
					@Override
					public int compare(NodeDTO o1, NodeDTO o2) {
						return o2.getDepth().compareTo(o1.getDepth());
					}
				});
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
	
	private void pushInstructionEvent(Node parentNode, NodeDTO nodeDto, String publishType) throws Exception{
		Map<String,Object> context = new HashMap<String,Object>();
		Map<String,Object> object = new HashMap<String,Object>();
		Map<String,Object> edata = new HashMap<String,Object>();
		
		if(null == nodeDto) {
			nodeDto = new NodeDTO(parentNode.getIdentifier(), (String)parentNode.getMetadata().get("name"), (String)parentNode.getMetadata().get("mimeType"), 
					(Double)parentNode.getMetadata().get("pkgVersion"), (String)parentNode.getMetadata().get("channel"), 
					(String)parentNode.getMetadata().get("lastPublishedBy"), (String)parentNode.getMetadata().get("versionKey"), 
					(String)parentNode.getMetadata().get("contentType"), (Integer)parentNode.getMetadata().get("compatibilityLevel"));
		}else {
			nodeDto.setChannel((String)parentNode.getMetadata().get("channel"));
			nodeDto.setLastPublishedBy((String)parentNode.getMetadata().get("lastPublishedBy"));
		}
		
		generateInstructionEventMetadata(context, object, edata, nodeDto, publishType);
		String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
		String topic = Platform.config.getString("kafka.topics.instruction");
		if(StringUtils.isBlank(beJobRequestEvent)) {
			TelemetryManager.error("Instruction event is not generated properly. # beJobRequestEvent : " + beJobRequestEvent);
			throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
		}
		if(StringUtils.isNotBlank(topic)) {
			KafkaClient.send(parentNode.getIdentifier(), beJobRequestEvent, topic);
		} else {
			TelemetryManager.error("Invalid topic id. # topic : " + topic);
			throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Invalid topic id.");
		}
	}
	
	private void generateInstructionEventMetadata(Map<String,Object> context, 
			Map<String,Object> object, Map<String,Object> edata, NodeDTO nodeDto, String publishType) {
		
		context.put("channel", nodeDto.getChannel()); 
		Map<String, Object> pdata = new HashMap<>();
		pdata.put("id", pdataId); 
		pdata.put("ver", pdataVersion);
		context.put("pdata", pdata);
		if (Platform.config.hasPath("cloud_storage.env")) {
			String env = Platform.config.getString("cloud_storage.env");
			context.put("env", env);
		}
		
		object.put("id", nodeDto.getIdentifier());
		object.put("ver", nodeDto.getVersionKey());
		object.put("parentNodeId", contentId);

		Map<String, Object> instructionEventMetadata = new HashMap<>();
		instructionEventMetadata.put("pkgVersion", nodeDto.getPkgVersion());
		instructionEventMetadata.put("mimeType", nodeDto.getMimeType());
		instructionEventMetadata.put("lastPublishedBy", nodeDto.getLastPublishedBy());
		instructionEventMetadata.put("compatibilityLevel", nodeDto.getCompatibilityLevel());

		edata.put("action", action);
		edata.put("metadata", instructionEventMetadata);
		edata.put("publish_type", publishType);
		edata.put("contentType", nodeDto.getContentType());
	}
}
