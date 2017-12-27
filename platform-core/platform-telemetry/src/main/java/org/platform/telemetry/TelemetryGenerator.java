package org.platform.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.platform.telemetry.dto.Actor;
import org.platform.telemetry.dto.Context;
import org.platform.telemetry.dto.Producer;
import org.platform.telemetry.dto.Telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryGenerator {

	ObjectMapper mapper = new ObjectMapper();
	
	public static String apiAccess(Map<String, Object> data) {
		
		String channel = (String) data.get("channel");
		String env = "dev";
		Context context = new Context(channel, env, new Producer("org.ekstep.content.platform", "1.0"));
		
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("type", "api_access");
		edata.put("level", "INFO");
		edata.put("message","");
		Telemetry tel = new Telemetry("LOG", actor, context, edata);
		
		return mapper.writeValueAsString(tel);
	}
	
	public static String log() {
		return null;
	}
}
