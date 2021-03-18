package org.sunbird.telemetry.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.ExecutionContext;
import org.sunbird.common.dto.HeaderParam;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.telemetry.TelemetryParams;
import org.sunbird.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryAccessEventUtil {

	protected static ObjectMapper mapper = new ObjectMapper();

	public static void writeTelemetryEventLog(Map<String, Object> data) {
		if (data != null) {
			long timeDuration = System.currentTimeMillis() - (long) data.get("StartTime");
			Request request = (Request) data.get("Request");
			Response response = (Response) data.get("Response");

			Map<String, Object> params = new HashMap<String, Object>();
			if(null != response)
				params.put("rid", response.getId());
			params.put("uip", (String) data.get("RemoteAddress"));
			params.put("url", (String) data.get("path"));
			params.put("size", ((Number) data.get("ContentLength")).intValue());
			params.put("duration", timeDuration);
			params.put("status", (int) data.get("Status"));
			params.put("protocol", data.get("Protocol"));
			params.put("method", (String) data.get("Method"));
			if (null != request) {
				params.put("req", getRequestString(request));
			}

			Map<String, String> context = new HashMap<String, String>();
			Map<String, Object> fwApis = getFrameworkAPIs();
			if (null != response && fwApis.containsKey(response.getId()))
				context.put(TelemetryParams.ENV.name(), (String) fwApis.get(response.getId()));
			else
				context.put(TelemetryParams.ENV.name(), (String) data.get("env"));
			ExecutionContext.getCurrent().getGlobalContext().put(TelemetryParams.ENV.name(), (String) data.get("env"));
			context.put(TelemetryParams.CHANNEL.name(),
					(String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.name()));
			if (null != data.get("X-Session-ID")) {
				context.put("sid", (String) data.get("X-Session-ID"));
			} else if (null != request && null != request.getParams()) {
				if (null != request.getParams().getSid()) {
					context.put("sid", request.getParams().getSid());
				}
			}
			if (null != data.get("X-Consumer-ID")) {
				String consumerId = (String) data.get("X-Consumer-ID");
				context.put(TelemetryParams.ACTOR.name(), consumerId);
				ExecutionContext.getCurrent().getGlobalContext().put(TelemetryParams.ACTOR.name(), consumerId);
			} else if (null != request && null != request.getParams()) {
				if (null != request.getParams().getCid()) {
					String consumerId = request.getParams().getCid();
					context.put(TelemetryParams.ACTOR.name(), consumerId);
					ExecutionContext.getCurrent().getGlobalContext().put(TelemetryParams.ACTOR.name(), consumerId);
				}
			}
			if (null != data.get("X-Device-ID")) {
				context.put("did", (String) data.get("X-Device-ID"));
			} else if (null != request && null != request.getParams()) {
				if (null != request.getParams().getDid()) {
					context.put("did", request.getParams().getDid());
				}
			}
			if (null != data.get("X-Authenticated-Userid")) {
				context.put("uid", (String) data.get("X-Authenticated-Userid"));
			} else if (null != request && null != request.getParams()) {
				if (null != request.getParams().getUid()) {
					context.put("uid", request.getParams().getUid());
				}
			}
			if (StringUtils.isNotBlank((String) data.get(HeaderParam.APP_ID.name())))
				context.put(HeaderParam.APP_ID.name(), (String) data.get(HeaderParam.APP_ID.name()));
			TelemetryManager.access(context, params);
		}

	}

	/**
	 * @return
	 */
	private static Map<String, Object> getFrameworkAPIs() {
		Map<String, Object> apis = new HashMap<String, Object>();

		apis.put("ekstep.learning.categoryinstance.create", "categoryinstance");
		apis.put("ekstep.learning.categoryinstance.read", "categoryinstance");
		apis.put("ekstep.learning.categoryinstance.update", "categoryinstance");
		apis.put("ekstep.learning.categoryinstance.search", "categoryinstance");
		apis.put("ekstep.learning.categoryinstance.retire", "categoryinstance");

		apis.put("ekstep.learning.category.term.create", "term");
		apis.put("ekstep.learning.category.term.read", "term");
		apis.put("ekstep.learning.category.term.update", "term");
		apis.put("ekstep.learning.category.term.search", "term");
		apis.put("ekstep.learning.category.term.retire", "term");

		apis.put("ekstep.learning.framework.term.create", "term");
		apis.put("ekstep.learning.framework.term.read", "term");
		apis.put("ekstep.learning.framework.term.update", "term");
		apis.put("ekstep.learning.framework.term.search", "term");
		apis.put("ekstep.learning.framework.term.retire", "term");

		apis.put("ekstep.learning.channel.term.create", "term");
		apis.put("ekstep.learning.channel.term.read", "term");
		apis.put("ekstep.learning.channel.term.update", "term");
		apis.put("ekstep.learning.channel.term.search", "term");
		apis.put("ekstep.learning.channel.term.retire", "term");

		return apis;
	}

	private static String getRequestString(Request request) {
		try {
			int trimLength = Platform.config.hasPath("learning.telemetry_req_length")?Platform.config.getInt("learning.telemetry_req_length"):-1;
			String requestStr = mapper.writeValueAsString(request.getRequest());
			if(trimLength >= 0) {
				requestStr = StringUtils.left(requestStr, trimLength);
			}
			return requestStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
