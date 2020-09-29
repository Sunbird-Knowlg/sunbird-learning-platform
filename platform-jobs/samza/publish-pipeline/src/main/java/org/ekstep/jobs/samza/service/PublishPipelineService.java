package org.ekstep.jobs.samza.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.publish.PublishManager;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.exception.PlatformException;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.PublishPipelineParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.dto.TelemetryBJREvent;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.rits.cloning.Cloner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class PublishPipelineService implements ISamzaService {

	private static JobLogger LOGGER = new JobLogger(PublishPipelineService.class);
	
	private Map<String, Object> parameterMap = new HashMap<String, Object>();

	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

	private ControllerUtil util = new ControllerUtil();

	private Config config = null;

	private static int MAXITERTIONCOUNT = 2;

	private SystemStream systemStream = null;
	private SystemStream postPublishStream = null;
	private SystemStream postPublishMVCStream = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static ObjectMapper mapper = new ObjectMapper();
	
	protected int getMaxIterations() {
		if (Platform.config.hasPath("max.iteration.count.samza.job"))
			return Platform.config.getInt("max.iteration.count.samza.job");
		else
			return MAXITERTIONCOUNT;
	}

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
		systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
		LOGGER.info("Stream initialized for Failed Events");
		postPublishStream = new SystemStream("kafka", config.get("post.publish.event.topic"));
		LOGGER.info("Stream initialized for Post Publish Events");
		postPublishMVCStream = new SystemStream("kafka",config.get("post.publish.mvc.topic"));
		LOGGER.info("Stream initialized for Post Publish MVC Content Events");

	}

	@Override
	@SuppressWarnings("unchecked")
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {

		if (null == message) {
			LOGGER.info("Ignoring the message because it is not valid for publishing.");
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get(PublishPipelineParams.edata.name());
		Map<String, Object> object = (Map<String, Object>) message.get(PublishPipelineParams.object.name());

		if (!validateObject(edata) || null == object) {
			LOGGER.info("Ignoring the message because it is not valid for publishing.");
			return;
		}

		String nodeId = (String) object.get(PublishPipelineParams.id.name());
		if (StringUtils.isNotBlank(nodeId)) {
			try {
				Node node = getNode(nodeId);
				if (null != node) {
					if (prePublishValidation(node, (Map<String, Object>) edata.get("metadata"))) {
						LOGGER.info(
								"Node fetched for publish and content enrichment operation : " + node.getIdentifier());
						prePublishUpdate(edata, node);

						processJob(edata, nodeId, metrics, collector);
					}
				} else {
					metrics.incSkippedCounter();
					FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
							PlatformErrorCodes.PROCESSING_ERROR.name(), new ServerException("ERR_PUBLISH_PIPELINE", "Please check neo4j connection or identfier to publish"));
					LOGGER.debug("Invalid Node Object. Unable to process the event", message);
				}
			} catch (PlatformException e) {
				LOGGER.error("Failed to process message", message, e);
				metrics.incFailedCounter();
				FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
						PlatformErrorCodes.PROCESSING_ERROR.name(), e);
			} catch (Exception e) {
				LOGGER.error("Failed to process message", message, e);
				metrics.incErrorCounter();
				FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
						PlatformErrorCodes.SYSTEM_ERROR.name(), e);
			}
		} else {
			FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
					PlatformErrorCodes.SYSTEM_ERROR.name(), new ServerException("ERR_PUBLISH_PIPELINE", "Id is blank"));
			metrics.incSkippedCounter();
			LOGGER.debug("Invalid NodeId. Unable to process the event", message);
		}
	}

	private boolean prePublishValidation(Node node, Map<String, Object> eventMetadata) {
		Map<String, Object> objMetadata = (Map<String, Object>) node.getMetadata();

		double eventPkgVersion = ((eventMetadata.get("pkgVersion") == null) ? 0d
				: ((Number)eventMetadata.get("pkgVersion")).doubleValue());
		double objPkgVersion = ((objMetadata.get("pkgVersion") == null) ? 0d : ((Number) objMetadata.get("pkgVersion")).doubleValue());

		return (objPkgVersion <= eventPkgVersion);
	}

	private void processJob(Map<String, Object> edata, String contentId, JobMetrics metrics, MessageCollector collector) throws Exception {

		Node node = getNode(contentId);
		String publishType = (String) edata.get(PublishPipelineParams.publish_type.name());
		node.getMetadata().put(PublishPipelineParams.publish_type.name(), publishType);
		publishContent(node, edata, metrics, collector);
	}

	@SuppressWarnings("unchecked")
	private void prePublishUpdate(Map<String, Object> edata, Node node) {
		Map<String, Object> metadata = (Map<String, Object>) edata.get("metadata");
		node.getMetadata().putAll(metadata);

		String prevState = (String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name());
		node.getMetadata().put(ContentWorkflowPipelineParams.prevState.name(), prevState);
		node.getMetadata().put("status", "Processing");

		util.updateNode(node);
		edata.put(PublishPipelineParams.status.name(), PublishPipelineParams.Processing.name());
		LOGGER.debug("Node status :: Processing for NodeId :: " + node.getIdentifier());
	}

	private Node getNode(String nodeId) {
		Node node = null;
		String imgNodeId = nodeId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		node = util.getNode(PublishPipelineParams.domain.name(), imgNodeId);
		if (null == node) {
			node = util.getNode(PublishPipelineParams.domain.name(), nodeId);
		}
		return node;
	}

	private void publishContent(Node node, Map<String, Object> edata, JobMetrics metrics, MessageCollector collector) throws Exception {
		boolean published = true;
		LOGGER.debug("Publish processing start for content: " + node.getIdentifier());
		publishNode(node, (String) node.getMetadata().get(PublishPipelineParams.mimeType.name()));
		Node publishedNode = getNode(node.getIdentifier().replace(".img", ""));
		if (StringUtils.equalsIgnoreCase((String) publishedNode.getMetadata().get(PublishPipelineParams.status.name()),
				PublishPipelineParams.Failed.name())) {
			edata.put(PublishPipelineParams.status.name(), PublishPipelineParams.FAILED.name());
			LOGGER.debug("Node publish operation :: FAILED :: For NodeId :: " + node.getIdentifier());
			throw new PlatformException(PlatformErrorCodes.PUBLISH_FAILED.name(),
					"Node publish operation failed for Node Id:" + node.getIdentifier());
		} else {
			metrics.incSuccessCounter();
			edata.put(PublishPipelineParams.status.name(), PublishPipelineParams.SUCCESS.name());
			LOGGER.debug("Node publish operation :: SUCCESS :: For NodeId :: " + node.getIdentifier());
			pushInstructionEvent(publishedNode, collector);
		}
	}

	protected static String format(Date date) {
		if (null != date) {
			try {
				return sdf.format(date);
			} catch (Exception e) {
				TelemetryManager.error("Error! While Converting the Date Format."+ date, e);
			}
		}
		return null;
	}

	private void publishNode(Node node, String mimeType) {
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(),
					ContentErrorMessageConstants.INVALID_CONTENT
							+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
		Cloner cloner = new Cloner();
		Node cloneNode = cloner.deepClone(node);
		String nodeId = node.getIdentifier().replace(".img", "");
		LOGGER.info("Publish processing start for node: " + nodeId);
		String basePath = PublishManager.getBasePath(nodeId, this.config.get("lp.tempfile.location"));
		LOGGER.info("Base path to store files: " + basePath);
		try {
			setContentBody(node, mimeType);
			LOGGER.debug("Fetched body from cassandra");
			parameterMap.put(PublishPipelineParams.node.name(), node);
			parameterMap.put(PublishPipelineParams.ecmlType.name(), PublishManager.isECMLContent(mimeType));
			LOGGER.info("Initializing the publish pipeline for: " + node.getIdentifier());
			InitializePipeline pipeline = new InitializePipeline(basePath, nodeId);
			pipeline.init(PublishPipelineParams.publish.name(), parameterMap);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(
					"Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: "
							+ nodeId + "]",
					e.getMessage());
			cloneNode.getMetadata().put(PublishPipelineParams.publishError.name(), e.getMessage());
			cloneNode.getMetadata().put(PublishPipelineParams.status.name(), PublishPipelineParams.Failed.name());
			util.updateNode(cloneNode);
		} finally {
			try {
				FileUtils.deleteDirectory(new File(basePath.replace(nodeId, "")));
			} catch (Exception e2) {
				LOGGER.error("Error while deleting base Path: " + basePath, e2);
				e2.printStackTrace();
			}
		}
	}

	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			node.getMetadata().put(PublishPipelineParams.body.name(),
					PublishManager.getContentBody(node.getIdentifier()));
		}
	}

	private boolean validateObject(Map<String, Object> edata) {
        String action = (String) edata.get("action");
        String contentType = (String) edata.get(PublishPipelineParams.contentType.name());
        Integer iteration = (Integer) edata.get(PublishPipelineParams.iteration.name());
        //TODO: remove contentType validation
        if (StringUtils.equalsIgnoreCase("publish", action) && (!StringUtils.equalsIgnoreCase(contentType,
                PublishPipelineParams.Asset.name())) &&  (iteration <= getMaxIterations())) {
                return true;
        }
        return false;
    }

	private void pushInstructionEvent(Node node, MessageCollector collector) throws Exception {
		Map<String, Object> actor = new HashMap<String, Object>();
		Map<String, Object> context = new HashMap<String, Object>();
		Map<String, Object> object = new HashMap<String, Object>();
		Map<String, Object> edata = new HashMap<String, Object>();
		String mimeType = (String) node.getMetadata().get("mimeType");
		String sourceURL = node.getMetadata().get("sourceURL") != null ? (String)node.getMetadata().get("sourceURL") : null;
		if(StringUtils.isNotBlank(sourceURL)){
			Map<String, Object> mvcProcessorEvent = generateInstructionEventMetadata(actor, context, object, edata, node.getMetadata(), node.getIdentifier(), "link-dialcode");
			mvcProcessorEvent=  updatevaluesForMVCEvent(mvcProcessorEvent);
			if (MapUtils.isEmpty(mvcProcessorEvent)) {
				TelemetryManager.error("Post Publish event is not generated properly. #postPublishJob : " + mvcProcessorEvent);
				throw new ClientException("MVC_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
			}
			collector.send(new OutgoingMessageEnvelope(postPublishMVCStream, mvcProcessorEvent));
			LOGGER.info("All Events sent to post publish mvc event topic");
		}

        Map<String, Object> postPublishEvent = generateInstructionEventMetadata(actor, context, object, edata, node.getMetadata(), node.getIdentifier(), "post-publish-process");
        if (MapUtils.isEmpty(postPublishEvent)) {
            TelemetryManager.error("Post Publish event is not generated properly. #postPublishJob : " + postPublishEvent);
            throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
        }
        collector.send(new OutgoingMessageEnvelope(postPublishStream, postPublishEvent));

        // TODO: delete below block after flink generic implementation.
        if (StringUtils.isNotBlank(mimeType) && StringUtils.equals(mimeType, "application/vnd.ekstep.content-collection")) {
            Map<String, Object> linkDialcodeEvent = generateInstructionEventMetadata(actor, context, object, edata, node.getMetadata(), node.getIdentifier(), "link-dialcode");

            if (MapUtils.isEmpty(linkDialcodeEvent)) {
                TelemetryManager.error("Post Publish event is not generated properly. #postPublishJob : " + linkDialcodeEvent);
                throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.");
            }
            collector.send(new OutgoingMessageEnvelope(postPublishStream, linkDialcodeEvent));
            LOGGER.info("OLD format Event - link-dialcode - sent to post publish event topic");
        }
        LOGGER.info("All Events sent to post publish event topic");
	}

	Map<String,Object> updatevaluesForMVCEvent(Map<String,Object> mvcProcessorEvent) {
		mvcProcessorEvent.put("eventData",mvcProcessorEvent.get("edata"));
		mvcProcessorEvent.put("eid","MVC_JOB_PROCESSOR");
		mvcProcessorEvent.remove("edata");
		Map<String,Object> eventData = (Map<String,Object>) mvcProcessorEvent.get("eventData");
		eventData.put("identifier",eventData.get("id"));
		eventData.remove("id");
		eventData.remove("iteration");
		eventData.remove("mimeType");
		eventData.remove("contentType");
        eventData.remove("pkgVersion");
        eventData.remove("status");
		eventData.put("action","update-es-index");
		eventData.put("stage",1);
		return mvcProcessorEvent;
	}

	private Map<String, Object> generateInstructionEventMetadata(Map<String, Object> actor, Map<String, Object> context,
																 Map<String, Object> object, Map<String, Object> edata, Map<String, Object> metadata, String contentId, String action) {
		TelemetryBJREvent te = new TelemetryBJREvent();
		actor.put("id", "Post Publish Processor");
		actor.put("type", "System");
		context.put("channel", metadata.get("channel"));
		Map<String, Object> pdata = new HashMap<>();
		pdata.put("id", "org.ekstep.platform");
		pdata.put("ver", "1.0");
		context.put("pdata", pdata);
		if (Platform.config.hasPath("cloud_storage.env")) {
			String env = Platform.config.getString("cloud_storage.env");
			context.put("env", env);
		}

		object.put("id", contentId);
		object.put("ver", metadata.get("versionKey"));

		edata.put("action", action);
		edata.put("contentType", metadata.get("contentType"));
		edata.put("status", metadata.get("status"));
		// TODO: remove 'id' after mvc-processor handled it.
		edata.put("id", contentId);
        edata.put("identifier", contentId);
		edata.put("pkgVersion", metadata.get("pkgVersion"));
		edata.put("mimeType", metadata.get("mimeType"));
        edata.put("name", metadata.get("name"));
        edata.put("createdBy", metadata.get("createdBy"));
        edata.put("createdFor", metadata.get("createdFor"));
        edata.put("trackable", metadata.get("trackable"));

		// generate event structure
		long unixTime = System.currentTimeMillis();
		String mid = "LP." + System.currentTimeMillis() + "." + UUID.randomUUID();
		edata.put("iteration", 1);
		te.setEid("BE_JOB_REQUEST");
		te.setEts(unixTime);
		te.setMid(mid);
		te.setActor(actor);
		te.setContext(context);
		te.setObject(object);
		te.setEdata(edata);
		Map<String, Object> event = null;
		try {
			event = mapper.convertValue(te, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			TelemetryManager.error("Error Generating BE_JOB_REQUEST event: " + e.getMessage(), e);
		}
		return event;
	}

}
