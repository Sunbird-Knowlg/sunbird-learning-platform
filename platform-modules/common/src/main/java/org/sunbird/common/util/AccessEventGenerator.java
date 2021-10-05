package org.sunbird.common.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.HeaderParam;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.telemetry.util.TelemetryAccessEventUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
			data.put("path", requestWrapper.getRequestURI() + ((StringUtils.isNotBlank(requestWrapper.getQueryString
					())) ? "?" + requestWrapper.getQueryString() : ""));
			data.put("Method", requestWrapper.getMethod());
			data.put("X-Session-ID", requestWrapper.getHeader("X-Session-ID"));
			data.put("X-Consumer-ID", requestWrapper.getHeader("X-Consumer-ID"));
			data.put("X-Device-ID", requestWrapper.getHeader("X-Device-ID"));
			data.put(HeaderParam.APP_ID.name(), requestWrapper.getHeader("X-App-Id"));
			data.put("X-Authenticated-Userid", requestWrapper.getHeader("X-Authenticated-Userid"));
			writeTelemetryEventLog(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
