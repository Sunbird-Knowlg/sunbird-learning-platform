package org.ekstep.common.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.TelemetryBEAccessEvent;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.LogTelemetryEventUtil;

public class TelemetryAccessEventUtil {

	private static LogHelper LOGGER = LogHelper.getInstance(TelemetryAccessEventUtil.class.getName());
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
				accessData.setSize((int) data.get("ContentLength"));
				accessData.setDuration(timeDuration);
				accessData.setStatus((int) data.get("Status"));
				accessData.setProtocol((String) data.get("Protocol"));
				accessData.setMethod((String) data.get("Method"));
				String did = null, cid = null, uid = null, sid = null;
				if (null != data.get("X-Session-ID")) {
					sid = (String) data.get("X-Session-ID");
				} else if (null != request && null != request.getParams()) {
					sid = request.getParams().getSid();
				}
				if (null != data.get("X-Consumer-ID")) {
					cid = (String) data.get("X-Consumer-ID");
				} else if (null != request && null != request.getParams()) {
					cid = request.getParams().getCid();
				}
				if (null != data.get("X-Device-ID")) {
					did = (String) data.get("X-Device-ID");
				} else if (null != request && null != request.getParams()) {
					did = request.getParams().getDid();
				}
				if (null != data.get("X-Authenticated-Userid")) {
					uid = (String) data.get("X-Authenticated-Userid");
				} else if (null != request && null != request.getParams()) {
					uid = request.getParams().getUid();
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
			LOGGER.error(e);
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
	}

	public static void writeTelemetryEventLog(RequestWrapper requestWrapper, ResponseWrapper responseWrapper) {
		Map<String, Object> data = new HashMap<String, Object>();
		Request request = null;
		try {
			data.put("StartTime", requestWrapper.getAttribute("startTime"));
			String body = requestWrapper.getBody();
			boolean isMultipart = (requestWrapper.getHeader("content-type") != null
					&& requestWrapper.getHeader("content-type").indexOf("multipart/form-data") != -1);
			if ("POST".equalsIgnoreCase(requestWrapper.getMethod()) && !isMultipart) {
				request = mapper.readValue(body, Request.class);
			}
			byte responseContent[] = responseWrapper.getData();
			Response response = mapper.readValue(responseContent, Response.class);
			data.put("Request", request);
			data.put("Response", response);
			data.put("RemoteAddress", requestWrapper.getRemoteHost());
			data.put("ContentLength", responseContent.length);
			data.put("Status", responseWrapper.getStatus());
			data.put("Protocol", requestWrapper.getProtocol());
			data.put("Method", requestWrapper.getMethod());
			data.put("X-Session-ID", requestWrapper.getHeader("X-Session-ID"));
			data.put("X-Consumer-ID", requestWrapper.getHeader("X-Consumer-ID"));
			data.put("X-Device-ID", requestWrapper.getHeader("X-Device-ID"));
			data.put("X-Authenticated-Userid", requestWrapper.getHeader("X-Authenticated-Userid"));
			writeTelemetryEventLog(data);
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}

	}
}
