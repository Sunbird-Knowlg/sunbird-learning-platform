package org.sunbird.telemetry.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.dto.Request;
import org.sunbird.telemetry.dto.TelemetryBEEvent;
import org.sunbird.telemetry.dto.TelemetryBJREvent;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LogTelemetryEventUtil {

	
	private static final Logger telemetryEventLogger = LogManager.getLogger("TelemetryEventLogger");
	private static ObjectMapper mapper = new ObjectMapper();
	private static String beJobRequesteventId = "BE_JOB_REQUEST";
	private static int iteration = 1;
	
	public static String logInstructionEvent(Map<String,Object> actor, Map<String,Object> context, Map<String,Object> object, Map<String,Object> edata) {
		
		TelemetryBJREvent te = new TelemetryBJREvent();
		long unixTime = System.currentTimeMillis();
		String mid = "LP."+System.currentTimeMillis()+"."+UUID.randomUUID();
		edata.put("iteration", iteration);
		
		te.setEid(beJobRequesteventId);
		te.setEts(unixTime);
		te.setMid(mid);
		te.setActor(actor);
		te.setContext(context);
		te.setObject(object);
		te.setEdata(edata);
		
		String jsonMessage = null;
		try {
			jsonMessage = mapper.writeValueAsString(te);
		} catch (Exception e) {
			TelemetryManager.error("Error logging BE_JOB_REQUEST event: " + e.getMessage(), e);
		}
		return jsonMessage;
	}

	public static String logContentSearchEvent(String query, Object filters, Object sort, String correlationId, int size, Request req) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		String jsonMessage = null;
		String mid = "LP."+System.currentTimeMillis()+"."+UUID.randomUUID();
		try {
			long unixTime = System.currentTimeMillis();
			te.setEid("BE_CONTENT_SEARCH");
			te.setEts(unixTime);
			te.setMid(mid);
			te.setVer("2.0");
			if(null != req && null != req.getParams() && !StringUtils.isBlank(req.getParams().getDid())){
				te.setPdata("org.sunbird.search.platform",req.getParams().getDid() , "1.0", "");
			}else {
				te.setPdata("org.sunbird.search.platform","" , "1.0", "");
			}
			te.setEdata(query, filters, sort, correlationId, size);
	
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventLogger.info(jsonMessage);
		} catch (Exception e) {
			TelemetryManager.error("Error logging BE_CONTENT_LIFECYCLE event: " + e.getMessage(), e);
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