package org.sunbird.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.telemetry.dto.Actor;
import org.sunbird.telemetry.dto.Context;
import org.sunbird.telemetry.dto.Producer;
import org.sunbird.telemetry.dto.Target;
import org.sunbird.telemetry.dto.Telemetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link TelemetryGenerator} uses context and other parameters to generate
 * event JSON in string format.
 * 
 * @author Mahesh
 *
 */
public class TelemetryGenerator {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final String ENVIRONMENT = Platform.config.hasPath("telemetry_env")?Platform.config.getString("telemetry_env"):"dev";
	private static final String INSTALLATION_ID = Platform.config.hasPath("installation.id")?Platform.config.getString("installation.id"):"ekstep";
	private static final String DEFAULT_PRODUCER_ID = ENVIRONMENT + "." + INSTALLATION_ID + ".learning.platform";
	private static final String PRODUCER_VERSION = "1.0";
	private static String PRODUCER_PID = "";

	public static void setComponent(String component) {
		PRODUCER_PID = component;
	}

	/**
	 * To generate api_access LOG telemetry JSON string.
	 * 
	 * @param context context of the event
	 * @param params params of the event
	 * @return String event in JSON fromat
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
	 * 
	 * @param context context of the event
	 * @param type type of the event
	 * @param level log level of the event
	 * @param message message of the event
	 * @param pageid page id of the event
	 * @param params params of the event
	 * @return String event string in JSON format.
	 */
	public static String log(Map<String, String> context, String type, String level, String message, String pageid,
			Map<String, Object> params) {
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
	 * 
	 * @param context context of the event
	 * @param type type of the event
	 * @param level log level of the event
	 * @param message message of the event
	 * @return String event string in JSON format.
	 */
	public static String log(Map<String, String> context, String type, String level, String message) {
		return log(context, type, level, message, null, null);
	}

	/**
	 * To generate ERROR telemetry JSON string with all params.
	 * 
	 * @param context context of the event
	 * @param code code of the event
	 * @param type type of the event
	 * @param stacktrace stacktrace of the exception
	 * @param pageid page id
	 * @param object object of the event
	 * @return String event string in JSON format.
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
	 * 
	 * @param context context of the event
	 * @param code code of the event
	 * @param type type of the event
	 * @param stacktrace stacktrace of the exception.
	 * @return String event string in JSON format.
	 */
	public static String error(Map<String, String> context, String code, String type, String stacktrace) {
		return error(context, code, type, stacktrace, null, null);
	}

    /**
     * @param context context of the event
     * @param query query of the search
     * @param filters filters for search
     * @param sort sort value for search
     * @param cData cData of the event
     * @param size size of the search result
     * @param topN top N results
     * @param type type of the event
     * @return String event string in JSON format
     */
    public static String search(Map<String, String> context, String query, Object filters, Object sort,
                                List<Map<String, Object>> cData, int size, Object topN, String type) {
        Actor actor = getActor(context);
        Context eventContext = getContext(context);
        Map<String, Object> edata = new HashMap<String, Object>();
        edata.put("type", type);
        edata.put("query", query);
        edata.put("filters", filters);
        edata.put("sort", sort);
        edata.put("size", size);
        edata.put("topn", topN);
        Telemetry telemetry;
        if (null != cData) {
            telemetry = new Telemetry("SEARCH", actor, eventContext, edata, cData);
        } else {
            telemetry = new Telemetry("SEARCH", actor, eventContext, edata);
        }
		if (null != context.get("objectId") && null != context.get("objectType")) {
			Target object = new Target(context.get("objectId"), context.get("objectType"));
			telemetry.setObject(object);
		}
        return getTelemetry(telemetry);
    }

	/**
	 * @param context context of the event
	 * @param props props of the event
	 * @param state state of the event
	 * @param prevState prevState of the object in event
	 * @param cdata cData of the event
	 * @return String event string in JSON format
	 */
	public static String audit(Map<String, String> context, List<String> props, String state, String prevState,
			List<Map<String, Object>> cdata) {
		Telemetry telemetry = null;
		Actor actor = getActor(context);
		Context eventContext = getContext(context);
		Map<String, Object> edata = new HashMap<String, Object>();
		edata.put("props", props);
		if (StringUtils.isNotBlank(state))
			edata.put("state", state);
		if (StringUtils.isNotBlank(prevState))
			edata.put("prevstate", prevState);
		if(StringUtils.isNotBlank(context.get("duration")))
			edata.put("duration",Long.valueOf(context.get("duration")));
		if (null != cdata && !cdata.isEmpty())
			telemetry = new Telemetry("AUDIT", actor, eventContext, edata, cdata);
		else
			telemetry = new Telemetry("AUDIT", actor, eventContext, edata);
		Target object = new Target(context.get("objectId"), context.get("objectType"));
		String pkgVersion = (String) context.get("pkgVersion");
		if (StringUtils.isNotBlank(pkgVersion))
			object.setVer(pkgVersion);
		telemetry.setObject(object);
		return getTelemetry(telemetry);
	}

	/**
	 * @param context context of the event
	 * @param props props of the event
	 * @param state state of the event
	 * @param prevState prevState of the event
	 * @return String event string in JSON format.
	 */
	public static String audit(Map<String, String> context, List<String> props, String state, String prevState) {
		return audit(context, props, state, prevState, null);
	}

	private static Actor getActor(Map<String, String> context) {
		String actorId = context.get(TelemetryParams.ACTOR.name());
		if (StringUtils.isNotBlank(actorId))
			return new Actor(actorId, "User");

		return new Actor("org.sunbird.learning.platform", "System");

	}

	private static Context getContext(Map<String, String> context) {
		String channel = (String) context.get(TelemetryParams.CHANNEL.name());
		String env = context.get(TelemetryParams.ENV.name());
		Context eventContext = new Context(channel, env, getProducer(context));
		String sid = context.get("sid");
		if (StringUtils.isNotBlank(sid))
			eventContext.setSid(sid);
		String did = context.get("did");
		if (StringUtils.isNotBlank(did))
			eventContext.setDid(did);

		return eventContext;
	}

	/**
	 * This Method Returns Producer Object for Telemetry Event.
	 * @param context context of the event
	 * @return Producer producer for the event
	 */
	private static Producer getProducer(Map<String, String> context) {
		String appId = context.get(TelemetryParams.APP_ID.name());
		if (StringUtils.isNotBlank(appId))
			return new Producer(appId, PRODUCER_PID, PRODUCER_VERSION);
		else
			return new Producer(DEFAULT_PRODUCER_ID, PRODUCER_PID, PRODUCER_VERSION);
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