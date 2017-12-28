package org.ekstep.telemetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.telemetry.dto.Actor;
import org.ekstep.telemetry.dto.Context;
import org.ekstep.telemetry.dto.Producer;
import org.ekstep.telemetry.dto.Telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryGenerator {

	
	private static ObjectMapper mapper = new ObjectMapper();

	public static String access(Map<String, Object> params, Map<String, String> context) {
		String event = "";
		try {
			String actorId = context.get("cid");
			Actor actor = new Actor(actorId, "System");
			Context eventContext = getContext(context);
			Map<String, Object> edata = new HashMap<String, Object>();
			edata.put("type", "api_access");
			edata.put("level", "INFO");
			edata.put("message", "");
			edata.put("params", getParamsList(params));
			Telemetry tel = new Telemetry("LOG", actor, eventContext, edata);

			event = mapper.writeValueAsString(tel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}

	public static String log() {
		return null;
	}

	public static String search() {
		return null;
	}

	private static Context getContext(Map<String, String> context) {
		String channel = (String) ExecutionContext.getCurrent().getGlobalContext().get("channel");
		String env = context.get("env");
		Context eventContext = new Context(channel, env, new Producer("org.ekstep.platform", "1.0"));
		String sid = context.get("sid");
		if (StringUtils.isNotBlank(sid))
			eventContext.setSid(sid);
		String did = context.get("did");
		if (StringUtils.isNotBlank(did))
			eventContext.setDid(did);

		return eventContext;
	}

	private static List<Map<String, Object>> getParamsList(Map<String, Object> params) {
		List<Map<String, Object>> paramsList = new ArrayList<Map<String, Object>>();
		if (null != params && !params.isEmpty()) {
			for (Entry<String, Object> entry : params.entrySet()) {
				Map<String, Object> param = new HashMap<String, Object>();
				param.put(entry.getKey(), entry.getValue());
				paramsList.add(param);
			}
		}
		return paramsList;
	}
}
