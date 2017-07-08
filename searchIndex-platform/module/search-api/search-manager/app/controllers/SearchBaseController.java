package controllers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;

import play.mvc.Controller;
import play.mvc.Http.RequestBody;

public class SearchBaseController extends Controller {

	private static final String API_ID_PREFIX = "ekstep";
	protected ObjectMapper mapper = new ObjectMapper();
	private static ILogger LOGGER = PlatformLogManager.getLogger();

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
		LOGGER.log(apiId);
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
			}
		} else {
			request.setId(apiId);
			request.setVer(getAPIVersion(path));
		}
		return request;
	}

}
