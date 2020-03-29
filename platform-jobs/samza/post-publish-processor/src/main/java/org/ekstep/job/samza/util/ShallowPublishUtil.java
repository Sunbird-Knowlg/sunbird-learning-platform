package org.ekstep.job.samza.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShallowPublishUtil {

	private static JobLogger LOGGER = new JobLogger(ShallowPublishUtil.class);
	private static final String KAFKA_TOPIC = Platform.config.hasPath("content.publish.topic")
			? Platform.config.getString("content.publish.topic") : "local.learning.job.request";
	private static final String KP_SEARCH_URL = Platform.config.getString("kp.search_service_url");
	private static final List<String> SEARCH_FIELDS = Arrays.asList("identifier", "mimeType", "contentType", "versionKey", "channel", "status", "lastPublishedBy", "origin", "originData");
	private static ObjectMapper mapper = new ObjectMapper();
	private ControllerUtil util = new ControllerUtil();
	private static String actorId = "Publish Samza Job";
	private static String actorType = "System";
	private static String pdataId = "org.ekstep.platform";
	private static String pdataVersion = "1.0";
	private static String action = "publish";

	public void publish(String contentId, Double pkgVersion, MessageCollector collector) {
		int counter = 0;
		Map<String, Object> copiedIdMap = getCopiedContentIds(contentId);
		if(MapUtils.isNotEmpty(copiedIdMap)){
			for(Map.Entry<String, Object> entry : copiedIdMap.entrySet()){
				if(isShallowCopy((Map<String, Object>)entry.getValue())){
					//TODO: Node Metadata can be passed here.
					pushPublishEvent(entry.getKey(), collector);
					++counter;
				}
			}
		}else LOGGER.info("Received Zero Copied Content Ids For Origin Content Id: " + contentId);
		LOGGER.info("Total "+counter+" Shallow Publish Event Pushed For Origin Content Id: " + contentId);

	}

	private Map<String, Object> getCopiedContentIds(String originId) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			Map<String, Object> request = new HashMap<String, Object>() {{
				put(PostPublishParams.request.name(), new HashMap<String, Object>() {{
					put(PostPublishParams.filters.name(), new HashMap<String, Object>() {{
						put(PostPublishParams.status.name(), new ArrayList<String>());
						put(PostPublishParams.origin.name(), originId);
					}});
					put("fields", SEARCH_FIELDS);
				}});
			}};

			Map<String, String> headerParam = new HashMap<String, String>() {{
				//TODO: will channel of copied content be same as origin content?. If yes, get it from event and put it.
				//put("X-Channel-Id", (String) node.getMetadata().get(PostPublishParams.channel.name()));
				put("Content-Type", "application/json");
			}};
			HttpResponse<String> httpResponse = Unirest.post(KP_SEARCH_URL)
					.headers(headerParam)
					.body(mapper.writeValueAsString(request)).asString();
			Response response = getResponse(httpResponse);
			if (response.getResponseCode() == ResponseCode.OK) {
				if (MapUtils.isNotEmpty(response.getResult())) {
					List<Object> contents = (List<Object>) response.getResult().get(PostPublishParams.content.name());
					contents.stream().map(obj -> (Map<String, Object>) obj).forEach(map -> {
						String identifier = (String) map.get(PostPublishParams.identifier.name());
						String origin = (String) map.get(PostPublishParams.origin.name());
						if (StringUtils.isNotBlank(identifier) && StringUtils.equals(originId, origin))
							result.put(identifier, map);
					});
					//TODO: Remove Commented Code
					//contents.stream().collect(Collectors.toMap(rec->(String)rec.get("identifier"), rec-> rec))
				}
				else
					LOGGER.info("Empty Result Received While Searching Shallow Copied Contents For Origin : " + originId);
			} else {
				LOGGER.info("Error Response Received While Searching Shallow Copied Contents For Origin : " + originId + " | Error Response Code is :" + response.getResponseCode() + "| Error Result : " + response.getResult());
			}
		} catch (Exception e) {
			LOGGER.error("Exception Occurred While Searching Shallow Copied Contents For Origin : " + originId + " | Exception is :" , e);
			e.printStackTrace();
		}
		return result;
	}

	private static Response getResponse(HttpResponse<String> response) {
		String body = null;
		Response resp = new Response();
		try {
			body = response.getBody();
			if (org.apache.commons.lang3.StringUtils.isNotBlank(body))
				resp = mapper.readValue(body, Response.class);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("UnsupportedEncodingException:::::" , e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Exception:::::" , e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage());
		}
		return resp;
	}

	private boolean isShallowCopy(Map<String, Object> value) {
		boolean result = false;
		try {
			String jsonString = (String) value.getOrDefault(PostPublishParams.originData.name(), "");
			Map<String, Object> originData = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
			if(MapUtils.isNotEmpty(originData) && StringUtils.equalsIgnoreCase(PostPublishParams.shallow.name(), (String) originData.get(PostPublishParams.copyType.name())))
				result = true;
			else LOGGER.info("Shallow publish event skipped for identifier :" + value.get("identifier") + " | copyType is : "+ originData.get(PostPublishParams.copyType.name()));

		} catch (Exception e) {
			LOGGER.error("Error while parsing originData for identifier :" + value.get("identifier"), e);
			e.printStackTrace();
		}
		return result;
	}

	private void pushPublishEvent(String contentId, MessageCollector collector) {
		try {
			//TODO: Remove Neo4j Call, instead get all required metadata from search call.
			Node node = util.getNode(SamzaCommonParams.domain.name(), contentId);
			if (null != node) {
				Map<String, Object> event = generatePublishEvent(contentId, node);
				collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", KAFKA_TOPIC), event));
			} else LOGGER.info("Found null Node Object (shallow copied) for identifier : " + contentId);
		} catch (Exception e) {
			LOGGER.error("Error while pushing the shallow publish event for identifier :" + contentId, e);
		}
	}

	private Map<String, Object> generatePublishEvent(String contentId, Node node) throws Exception {
		Map<String, Object> actor = new HashMap<String, Object>();
		Map<String, Object> context = new HashMap<String, Object>();
		Map<String, Object> object = new HashMap<String, Object>();
		Map<String, Object> edata = new HashMap<String, Object>();
		//TODO: Review logic for publishType
		String status = (String) node.getMetadata().getOrDefault("status", "");
		String publishType = StringUtils.equalsIgnoreCase("Unlisted", status) ? "unlisted" : "public";
		generateInstructionEventMetadata(actor, context, object, edata, node.getMetadata(), contentId, publishType);
		String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
		return mapper.readValue(beJobRequestEvent, new TypeReference<Map<String, Object>>() {
		});
	}

	private void generateInstructionEventMetadata(Map<String, Object> actor, Map<String, Object> context,
	                                              Map<String, Object> object, Map<String, Object> edata, Map<String, Object> metadata, String contentId, String publishType) {

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
