package org.ekstep.common.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.TelemetryBEAccessEvent;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.util.LogTelemetryEventUtil;

public class TelemetryAccessEventUtil {

	
	private static ObjectMapper mapper = new ObjectMapper();

	public static void writeTelemetryEventLog(Map<String, Object> data) {
		try {
			if (data != null) {
				long timeDuration = System.currentTimeMillis() - (long) data.get("StartTime");
				Request request = (Request) data.get("Request");
				Response response = (Response) data.get("Response");
				TelemetryBEAccessEvent accessData = new TelemetryBEAccessEvent();
				accessData.setRid(response.getId());
				accessData.setUip((String) data.get("RemoteAddress"));
				accessData.setType("api");
				accessData.setPath((String)data.get("path"));
				accessData.setSize((int) data.get("ContentLength"));
				accessData.setDuration(timeDuration);
				accessData.setStatus((int) data.get("Status"));
				accessData.setProtocol((String) data.get("Protocol"));
				accessData.setMethod((String) data.get("Method"));
				String did = "", cid = "", uid = "", sid = "";
				if (null != data.get("X-Session-ID")) {
					sid = (String) data.get("X-Session-ID");
				} else if (null != request && null != request.getParams()) {
					if(null != request.getParams().getSid()){
						sid = request.getParams().getSid();
					}
				}
				if (null != data.get("X-Consumer-ID")) {
					cid = (String) data.get("X-Consumer-ID");
				} else if (null != request && null != request.getParams()) {
					if(null != request.getParams().getCid()){
						cid = request.getParams().getCid();
					}
				}
				if (null != data.get("X-Device-ID")) {
					did = (String) data.get("X-Device-ID");
				} else if (null != request && null != request.getParams()) {
					if(null != request.getParams().getDid()){
						did = request.getParams().getDid();
					}
				}
				if (null != data.get("X-Authenticated-Userid")) {
					uid = (String) data.get("X-Authenticated-Userid");
				} else if (null != request && null != request.getParams()) {
					if(null != request.getParams().getUid()){
						uid = request.getParams().getUid();
					}
				}
				Map<String, String> context = new HashMap<String, String>();
				context.put("did", did);
				context.put("cid", cid);
				context.put("uid", uid);
				context.put("sid", sid);
				accessData.setContext(context);
				if (null != request) {
					accessData.setParams(request.getRequest());
				}
				LogTelemetryEventUtil.logAccessEvent(accessData);
			}
		} catch (NullPointerException e) {
			PlatformLogger.log("Exception", e.getMessage(), e);
			e.printStackTrace();
		} catch (Exception e) {
			PlatformLogger.log("Exception", e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public static void writeTelemetryEventLog(RequestWrapper requestWrapper, ResponseWrapper responseWrapper) {
		Map<String, Object> data = new HashMap<String, Object>();
		Request request = null;
		try {
			data.put("StartTime", requestWrapper.getAttribute("startTime"));
			String body = requestWrapper.getBody();
			if (body.contains("request") && body.length() > 0 && !body.contains("SCRIPT")) {
				request = mapper.readValue(body, Request.class);
			}else if (null != body) {
					int index = body.indexOf("filename=");
					String fileName = "";
					if (index > 0) {
						fileName = body.substring(index + 10, body.indexOf("\"", index+10));
						request = new Request();
						Map<String, Object> req = new HashMap<String,Object>();
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
			data.put("path", requestWrapper.getRequestURI().toString());
			data.put("Method", requestWrapper.getMethod());
			data.put("X-Session-ID", requestWrapper.getHeader("X-Session-ID"));
			data.put("X-Consumer-ID", requestWrapper.getHeader("X-Consumer-ID"));
			data.put("X-Device-ID", requestWrapper.getHeader("X-Device-ID"));
			data.put("X-Authenticated-Userid", requestWrapper.getHeader("X-Authenticated-Userid"));
			writeTelemetryEventLog(data);
		} catch (IOException e) {
			PlatformLogger.log("Exception", e.getMessage(), e);
		
		}

	}
}
