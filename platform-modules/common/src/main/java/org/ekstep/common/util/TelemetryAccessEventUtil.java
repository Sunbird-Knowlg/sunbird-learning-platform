package org.ekstep.common.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.telemetry.TelemetryParams;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryAccessEventUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	public static void writeTelemetryEventLog(Map<String, Object> data) {
		if (data != null) {
			long timeDuration = System.currentTimeMillis() - (long) data.get("StartTime");
			Request request = (Request) data.get("Request");
			Response response = (Response) data.get("Response");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("rid", response.getId());
			params.put("uip", (String) data.get("RemoteAddress"));
			params.put("url", (String) data.get("path"));
			params.put("size", (int) data.get("ContentLength"));
			params.put("duration", timeDuration);
			params.put("status", (int) data.get("Status"));
			params.put("protocol", data.get("Protocol"));
			params.put("method", (String) data.get("Method"));
			if (null != request) {
				params.put("req", getRequestString(request));
			}

			Map<String, String> context = new HashMap<String, String>();
			context.put(TelemetryParams.ENV.name(), (String) data.get("env"));
			ExecutionContext.getCurrent().getGlobalContext().put(TelemetryParams.ENV.name(), (String) data.get("env"));
			context.put(TelemetryParams.CHANNEL.name(), (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.name()));
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
			TelemetryManager.access(context, params);
		}

	}

	
	private static String getRequestString(Request request) {
		try {
			return mapper.writeValueAsString(request.getRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeTelemetryEventLog(RequestWrapper requestWrapper, ResponseWrapper responseWrapper) {
		Map<String, Object> data = new HashMap<String, Object>();
		Request request = null;
		try {
			data.put("StartTime", requestWrapper.getAttribute("startTime"));
			data.put("env", requestWrapper.getAttribute("env"));
			String body = requestWrapper.getBody();
			if (body.contains("request") && body.length() > 0 && !body.contains("SCRIPT")) {
				request = mapper.readValue(body, Request.class);
			} else if (null != body) {
				int index = body.indexOf("filename=");
				String fileName = "";
				if (index > 0) {
					fileName = body.substring(index + 10, body.indexOf("\"", index + 10));
					request = new Request();
					Map<String, Object> req = new HashMap<String, Object>();
					req.put("fileName", fileName);
					request.setRequest(req);
				}

			}
			Response response = null;
			byte responseContent[] = responseWrapper.getData();
			if (responseContent.length > 0) {
				response = mapper.readValue(responseContent, Response.class);
			} else {
				return;
			}
			data.put("Request", request);
			data.put("Response", response);
			data.put("RemoteAddress", requestWrapper.getRemoteHost());
			data.put("ContentLength", responseContent.length);
			data.put("Status", responseWrapper.getStatus());
			data.put("Protocol", requestWrapper.getProtocol());
			data.put("path", requestWrapper.getRequestURI());
			data.put("Method", requestWrapper.getMethod());
			data.put("X-Session-ID", requestWrapper.getHeader("X-Session-ID"));
			data.put("X-Consumer-ID", requestWrapper.getHeader("X-Consumer-ID"));
			data.put("X-Device-ID", requestWrapper.getHeader("X-Device-ID"));
			data.put("X-Authenticated-Userid", requestWrapper.getHeader("X-Authenticated-Userid"));
			writeTelemetryEventLog(data);
		} catch (IOException e) {
			TelemetryManager.error("Exception: "+ e.getMessage(), e);

		}

	}
}
