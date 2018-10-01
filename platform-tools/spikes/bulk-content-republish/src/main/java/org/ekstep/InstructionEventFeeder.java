package org.ekstep;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.kafka.KafkaClient;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.telemetry.util.LogTelemetryEventUtil;

public class InstructionEventFeeder {

	private static String actorId = "Publish Samza Job";
	private static String actorType = "System";
	private static String pdataId = "org.ekstep.platform";
	private static String pdataVersion = "1.0";
	private static String action = "republish";

	public void push(Node node, String publishType) throws Exception{
		Map<String,Object> actor = new HashMap<String,Object>();
		Map<String,Object> context = new HashMap<String,Object>();
		Map<String,Object> object = new HashMap<String,Object>();
		Map<String,Object> edata = new HashMap<String,Object>();
		
		if(node.getMetadata().get(GraphDACParams.status.name()).toString().equalsIgnoreCase("Unlisted"))
			publishType="Unlisted";
		
		generateInstructionEventMetadata(actor, context, object, edata, node.getMetadata(), node.getIdentifier(), publishType);
		String beJobRequestEvent = LogTelemetryEventUtil.logInstructionEvent(actor, context, object, edata);
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
		Map<String, Object> pdata = new HashMap<String, Object>();
		pdata.put("id", pdataId); 
		pdata.put("ver", pdataVersion);
		context.put("pdata", pdata);
		if (Platform.config.hasPath("s3.env")) {
			String env = Platform.config.getString("s3.env");
			context.put("env", env);
		}
		
		object.put("id", contentId);
		object.put("ver", metadata.get("versionKey"));
		
		edata.put("action", action);
		edata.put("metadata", metadata);
		edata.put("publish_type", publishType);
		edata.put("contentType", metadata.get("contentType"));
	}
}
