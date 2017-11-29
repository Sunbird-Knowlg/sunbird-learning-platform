package com.ilimi.common.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.Platform;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.TelemetryBEAccessEvent;
import com.ilimi.common.dto.TelemetryBEEvent;
import com.ilimi.common.dto.TelemetryPBIEvent;
import com.ilimi.common.logger.PlatformLogger;

public class LogTelemetryEventUtil {

	
	private static final Logger telemetryEventLogger = LogManager.getLogger("TelemetryEventLogger");
	private static final Logger objectLifecycleEventLogger = LogManager.getLogger("ObjectLifecycleLogger");
	private static final Logger instructionEventLogger = LogManager.getLogger("InstructionEventLogger");
	private static ObjectMapper mapper = new ObjectMapper();
	private static String mid = "LP."+System.currentTimeMillis()+"."+UUID.randomUUID();
	private static String eventId = "BE_CONTENT_PUBLISH";
	private static String actorId = "Publish Samza Job";
	private static String actorType = "System";
	private static String pdataId = "org.ekstep.platform";
	private static String pdataVersion = "1.0";
	private static String action = "publish";
	private static String status = "Pending";
	private static int iteration = 0;
	
	
	
	public static String logInstructionBasedContentPublishEvent(String contentId, Map<String, Object> metadata) {
		TelemetryPBIEvent te = new TelemetryPBIEvent();
		long unixTime = System.currentTimeMillis();
		Map<String,Object> actor = new HashMap<String,Object>();
		Map<String,Object> context = new HashMap<String,Object>();
		Map<String,Object> object = new HashMap<String,Object>();
		Map<String,Object> edata = new HashMap<String,Object>();
		
		te.setEid(eventId);
		te.setEts(unixTime);
		te.setMid(mid);
		
		actor.put("id", actorId);//publish job
		actor.put("type", actorType);
		te.setActor(actor);
		
		context.put("channel", metadata.get("channel")); 
		Map<String, Object> pdata = new HashMap<>();
		pdata.put("id", pdataId); 
		pdata.put("ver", pdataVersion);
		context.put("pdata", pdata);
		if (Platform.config.hasPath("s3.env")) {
			String env = Platform.config.getString("s3.env");
			context.put("env", env);
		}
		te.setContext(context);
		
		
		
		
		object.put("id", contentId);
		object.put("type", metadata.get("contentType"));
		object.put("ver", metadata.get("version"));
		te.setObject(object);
		
		edata.put("action", action);
		edata.put("status", status);
		edata.put("iteration", iteration);
		edata.put("publish_type", (String)metadata.get("publish_type"));
		te.setEdata(edata);
		
		String jsonMessage = null;
		try {
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				instructionEventLogger.info(jsonMessage);
		} catch (Exception e) {
			PlatformLogger.log("Error logging BE_CONTENT_PUBLISH event", e.getMessage(), e);
		}
		return jsonMessage;
	}
	
	public static String logContentLifecycleEvent(String contentId, Map<String, Object> metadata) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		Map<String,Object> data = new HashMap<String,Object>();
		te.setEid("BE_CONTENT_LIFECYCLE");
		te.setVer("2.0");
		te.setMid(mid);
		te.setEts(unixTime);
		te.setPdata("org.ekstep.content.platform", "", "1.0", "");
		data.put("cid", contentId);
		data.put("size", metadata.get("size"));
		data.put("organization", metadata.get("organization"));
		data.put("createdFor", metadata.get("createdFor"));
		data.put("creator", metadata.get("creator"));
		data.put("udpdater", metadata.get("udpdater"));
		data.put("reviewer", metadata.get("reviewer"));
		data.put("pkgVersion", metadata.get("pkgVersion"));
		data.put("concepts", metadata.get("concepts"));
		data.put("state", metadata.get("status"));
		data.put("prevstate", metadata.get("prevState"));
		data.put("downloadUrl", metadata.get("downloadUrl"));
		data.put("contentType", metadata.get("contentType"));
		data.put("mediaType", metadata.get("mediaType"));
		data.put("flags",metadata.get("flags"));
		te.setEdata(data);
		
		String jsonMessage = null;
		try {
//			jsonMessage = mapper.writeValueAsString(te);
//			if (StringUtils.isNotBlank(jsonMessage))
////				telemetryEventLogger.info(jsonMessage);
		} catch (Exception e) {
			PlatformLogger.log("Error logging BE_CONTENT_LIFECYCLE event", e.getMessage(), e);
		}
		return jsonMessage;
	}

	public static String logContentSearchEvent(String query, Object filters, Object sort, String correlationId, int size, Request req) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		String jsonMessage = null;
		try {
			long unixTime = System.currentTimeMillis();
			te.setEid("BE_CONTENT_SEARCH");
			te.setEts(unixTime);
			te.setMid(mid);
			te.setVer("2.0");
			if(null != req && null != req.getParams() && !StringUtils.isBlank(req.getParams().getDid())){
				te.setPdata("org.ekstep.search.platform",req.getParams().getDid() , "1.0", "");
			}else {
				te.setPdata("org.ekstep.search.platform","" , "1.0", "");
			}
			te.setEdata(query, filters, sort, correlationId, size);
	
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventLogger.info(jsonMessage);
		} catch (Exception e) {
			PlatformLogger.log("Error logging BE_CONTENT_LIFECYCLE event" + e.getMessage(),null, e);
		}
		return jsonMessage;
	}
	
	@SuppressWarnings("unchecked")
	public static String logAccessEvent(TelemetryBEAccessEvent accessData) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		te.setEid("BE_ACCESS");
		te.setEts(unixTime);
		te.setMid(mid);
		te.setVer("2.0");
		te.setPdata("org.ekstep.content.platform", accessData.getContext().get("did"), "1.0", "");
		String jsonMessage = null;
		try {
		Map<String, Object> eData = mapper.convertValue(accessData, Map.class);
		te.setEdata(eData);
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventLogger.info(jsonMessage);
		} catch (Exception e) {
			PlatformLogger.log("Error logging BE_ACCESS event" + e.getMessage(),null,e);
		}
		return jsonMessage;
	}
	
	public static String logObjectLifecycleEvent(String objectId, Map<String, Object> metadata){
			TelemetryBEEvent te = new TelemetryBEEvent();
			Map<String,Object> data = new HashMap<String,Object>();
			te.setEid("BE_OBJECT_LIFECYCLE");
			long ets = (long)metadata.get("ets");
			te.setEts(ets);
			te.setVer("2.0");
			te.setChannel((String)metadata.get("channel"));
			te.setPdata("org.ekstep.platform", "", "1.0", "");
			data.put("id", objectId);
			data.put("parentid", metadata.get("parentid"));
			data.put("parenttype", metadata.get("parenttype"));
			data.put("type", metadata.get("objectType"));
			data.put("subtype", metadata.get("subtype"));
			data.put("code", metadata.get("code"));
			data.put("name", metadata.get("name"));
			data.put("state", metadata.get("state"));
			data.put("prevstate", metadata.get("prevstate"));
			te.setEdata(data);
			String mid = getMD5Hash(te, data);
			te.setMid(mid);
			String jsonMessage = null;
			try {
				jsonMessage = mapper.writeValueAsString(te);
				if (StringUtils.isNotBlank(jsonMessage))
					objectLifecycleEventLogger.info(jsonMessage);
			} catch (Exception e) {
				PlatformLogger.log("Error logging OBJECT_LIFECYCLE event: " +e.getMessage(),null, e);
			}
			return jsonMessage;
	}
	
	public static String getMD5Hash(TelemetryBEEvent event, Map<String,Object> data){
		MessageDigest digest = null;
		try {
			String id = (String)data.get("id");
			String state = (String)data.get("state");
			String prevstate = (String)data.get("prevstate");
			String val = event.getEid()+event.getEts()+id+state+prevstate;
			digest = MessageDigest.getInstance("MD5");
			digest.update(val.getBytes());
			byte[] digestMD5 = digest.digest();
			StringBuffer mid_val = new StringBuffer();
			for(byte bytes : digestMD5){
				mid_val.append(String.format("%02x", bytes & 0xff));
			}
			String messageId = "LP:"+mid_val;
			return messageId;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}