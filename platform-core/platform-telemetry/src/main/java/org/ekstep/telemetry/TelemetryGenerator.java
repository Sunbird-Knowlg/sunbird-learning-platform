package org.ekstep.telemetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.telemetry.dto.Actor;
import org.ekstep.telemetry.dto.Context;
import org.ekstep.telemetry.dto.Producer;
import org.ekstep.telemetry.dto.Telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link TelemetryGenerator} uses context and other parameters to generate event JSON in string format.
 * @author Mahesh
 *
 */

public class TelemetryGenerator {

	private static ObjectMapper mapper = new ObjectMapper();
	private static Producer producer = new Producer("org.ekstep.learning.platform", "1.0");

	
	public static void setComponent(String component) {
		producer.setPid(component);
	}
	
	/**
	 * To generate api_access LOG telemetry JSON string.
	 * @param context
	 * @param params
	 * @return
	 */
	public static String access(Map<String, String> context, Map<String, Object> params) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("type", "api_access");
		edata.put("level", "INFO");
		edata.put("message", "");
		edata.put("params", getParamsList(params));
		Telemetry telemetry = new Telemetry("LOG", actor, eventContext, edata);
		return getTelemetry(telemetry);
	}
	
	/**
	 * To generate normal LOG telemetry JSON string with all params.
	 * @param context
	 * @param type
	 * @param level
	 * @param message
	 * @param pageid
	 * @param params
	 * @return
	 */
	public static String log(Map<String, String> context, String type, String level, String message, String pageid, Map<String, Object> params) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("type", type);
		edata.put("level", level);
		edata.put("message", message);
		if (StringUtils.isNotBlank(pageid))
			edata.put("pageid", pageid);
		if (null != params && !params.isEmpty())
			edata.put("params", getParamsList(params));
		Telemetry telemetry = new Telemetry("LOG", actor, eventContext, edata);
		return getTelemetry(telemetry);
	}
	
	/**
	 * To generate normal LOG telemetry JSON string with required params.
	 * @param context
	 * @param type
	 * @param level
	 * @param message
	 * @return
	 */
	public static String log(Map<String, String> context, String type, String level, String message) {
		return log(context, type, level, message, null, null);
	}

	/**
	 * To generate ERROR telemetry JSON string with all params.
	 * @param context
	 * @param code
	 * @param type
	 * @param stacktrace
	 * @param pageid
	 * @param object
	 * @return
	 */
	public static String error(Map<String, String> context, String code, String type, String stacktrace, String pageid,
			Object object) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("err", code);
		edata.put("errtype", type);
		edata.put("stacktrace", stacktrace);
		if (StringUtils.isNotBlank(pageid))
			edata.put("pageid", pageid);
		if (null != object)
			edata.put("object", object);
		Telemetry telemetry = new Telemetry("ERROR", actor, eventContext, edata);
		return getTelemetry(telemetry);

	}

	/**
	 * To generate ERROR telemetry JSON string with required params.
	 * @param context
	 * @param code
	 * @param type
	 * @param stacktrace
	 * @return
	 */
	public static String error(Map<String, String> context, String code, String type, String stacktrace) {
		return error(context, code, type, stacktrace, null, null);
	}

	
	public static String search(Map<String, String> context) {
		return null;
	}
	
	public static String audit(Map<String, String> context) {
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		return null;
	}
	
	private static Actor getActor(Map<String, String> context) {
		String actorId = context.get(TelemetryParams.ACTOR.name());
		if (StringUtils.isBlank(actorId))
			actorId = "org.ekstep.learning.platform";
		return new Actor(actorId, "System");
		
	}

	private static Context getContext(Map<String, String> context) {
		String channel = (String) context.get(TelemetryParams.CHANNEL.name());
		String env = context.get(TelemetryParams.ENV.name());
		Context eventContext = new Context(channel, env, producer);
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

	private static String getTelemetry(Telemetry telemetry) {
		String event = "";
		try {
			event = mapper.writeValueAsString(telemetry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}
}
