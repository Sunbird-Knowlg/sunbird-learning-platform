package controllers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.RequestParams;
import org.ekstep.telemetry.TelemetryParams;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.RequestBody;

public class SearchBaseController extends Controller {

	private static final String API_ID_PREFIX = "ekstep";
	protected ObjectMapper mapper = new ObjectMapper();
	

	protected String getAPIId(String apiId) {
		return API_ID_PREFIX + "." + apiId;
	}

	protected String getAPIVersion(String path) {
		String version = "3.0";
		if (path.contains("/v2") || path.contains("/search-service")) {
			version = "2.0";
		} else if (path.contains("/v3")) {
			version = "3.0";
		}
		return version;
	}

	@SuppressWarnings("unchecked")
	protected Request getRequest(RequestBody requestBody, String apiId, String path) {
		TelemetryManager.log(apiId);
		Request request = new Request();
		if (null != requestBody) {
			JsonNode data = requestBody.asJson();
			Map<String, Object> requestMap = mapper.convertValue(data, Map.class);
			if (null != requestMap && !requestMap.isEmpty()) {
				String id = (requestMap.get("id") == null || StringUtils.isBlank((String)requestMap.get("id")))
						? getAPIId(apiId) : (String) requestMap.get("id");
				String ver = (requestMap.get("ver") == null || StringUtils.isBlank((String)requestMap.get("ver")))
						? getAPIVersion(path) : (String) requestMap.get("ver");
				String ts = (String) requestMap.get("ts");
				request.setId(id);
				request.setVer(ver);
				request.setTs(ts);
				Object reqParams = requestMap.get("params");
				if (null != reqParams) {
					try {
						RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
						request.setParams(params);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Object requestObj = requestMap.get("request");
				if (null != requestObj) {
					try {
						String strRequest = mapper.writeValueAsString(requestObj);
						Map<String, Object> map = mapper.readValue(strRequest, Map.class);
						if (null != map && !map.isEmpty())
							request.setRequest(map);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				request.setId(getAPIId(apiId));
				request.setVer(getAPIVersion(path));
			}
		} else {
			request.setId(apiId);
			request.setVer(getAPIVersion(path));
		}
		return request;
	}

	/**
	 *
	 * @param httpRequest
	 * @param searchRequest
	 */
	protected void setHeaderContext(Http.Request httpRequest, Request searchRequest) {
		String sessionId = httpRequest.getHeader("X-Session-ID");
		String consumerId = httpRequest.getHeader("X-Consumer-ID");
		String deviceId = httpRequest.getHeader("X-Device-ID");
		String authUserId = httpRequest.getHeader("X-Authenticated-Userid");
		String channelId = httpRequest.getHeader("X-Channel-ID");
		String appId = httpRequest.getHeader("X-App-Id");

		if (StringUtils.isNotBlank(sessionId))
			searchRequest.getContext().put("SESSION_ID", sessionId);
		if (StringUtils.isNotBlank(consumerId))
			searchRequest.getContext().put(HeaderParam.CONSUMER_ID.name(), consumerId);
		if (StringUtils.isNotBlank(deviceId))
			searchRequest.getContext().put(HeaderParam.DEVICE_ID.name(), deviceId);
		if (StringUtils.isNotBlank(authUserId))
			searchRequest.getContext().put(HeaderParam.CONSUMER_ID.name(), authUserId);
		if (StringUtils.isNotBlank(channelId))
			searchRequest.getContext().put(HeaderParam.CHANNEL_ID.name(), channelId);
		else
			searchRequest.getContext().put(HeaderParam.CHANNEL_ID.name(), Platform.config.getString("channel.default"));
		if (StringUtils.isNotBlank(appId))
			searchRequest.getContext().put(HeaderParam.APP_ID.name(), appId);

		searchRequest.getContext().put(TelemetryParams.ENV.name(), "search");

		if (null != searchRequest.getContext().get(HeaderParam.CONSUMER_ID.name())) {
			searchRequest.put(TelemetryParams.ACTOR.name(), searchRequest.getContext().get(HeaderParam.CONSUMER_ID.name()));
		} else if (null != searchRequest && null != searchRequest.getParams().getCid()) {
			searchRequest.put(TelemetryParams.ACTOR.name(), searchRequest.getParams().getCid());
		} else {
			searchRequest.put(TelemetryParams.ACTOR.name(), "learning.platform");
		}

	}

}
