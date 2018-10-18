package org.ekstep.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.util.RequestWrapper;
import org.ekstep.common.util.ResponseWrapper;
import org.ekstep.telemetry.util.TelemetryAccessEventUtil;

public class AccessEventGenerator extends TelemetryAccessEventUtil {
	
	public static void writeTelemetryEventLog(RequestWrapper requestWrapper, ResponseWrapper responseWrapper) {
		Map<String, Object> data = new HashMap<String, Object>();
		Request request = null;
		try {
			data.put("StartTime", requestWrapper.getAttribute("startTime"));
			data.put("env", requestWrapper.getAttribute("env"));
			String body = requestWrapper.getBody();
			if (body.contains("request") && body.length() > 0 && !body.contains("SCRIPT")) {
				request = mapper.readValue(body, Request.class);
				if(null != request.getRequest().get("content")) {
					Map<String, Object> content = (Map<String, Object>) request.getRequest().get("content");
					List<String> ignoredProperties = Platform.config.hasPath("learning.telemetry_ignored_props")?Platform.config.getStringList("learning.telemetry_ignored_props"): Arrays.asList("body","screenshots","stageIcons","editorState");
					content.keySet().removeAll(ignoredProperties);
					request.getRequest().put("content",content);
				}
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
			e.printStackTrace();
		}
	}

}
