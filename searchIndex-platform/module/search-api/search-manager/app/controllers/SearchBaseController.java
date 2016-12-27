package controllers;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;

import play.mvc.Controller;
import play.mvc.Http.RequestBody;

public class SearchBaseController extends Controller {

	private static final String API_ID_PREFIX = "ekstep";
	public static final String API_VERSION = "1.0";

	protected ObjectMapper mapper = new ObjectMapper();
	private static Logger LOGGER = LogManager.getLogger(SearchBaseController.class.getName());

	protected String getAPIVersion() {
		return API_VERSION;
	}

	@SuppressWarnings("unchecked")
	protected Request getRequest(RequestBody requestBody, String apiId) {
		LOGGER.info(apiId);
		Request request = new Request();
		if (null != requestBody) {
			JsonNode data = requestBody.asJson();
			Map<String, Object> requestMap = mapper.convertValue(data, Map.class);
			if (null != requestMap && !requestMap.isEmpty()) {
				String id = requestMap.get("id") == null ? API_ID_PREFIX + "." + apiId : (String) requestMap.get("id");
				String ver = requestMap.get("ver") == null ? getAPIVersion() : (String) requestMap.get("id");
				String ts = (String) requestMap.get("id");
				request.setId(id);
				request.setVer(ver);
				request.setTs(ts);
				Object reqParams = requestMap.get("params");
				if (null != reqParams) {
					try {
						RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
						request.setParams(params);
					} catch (Exception e) {
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
					}
				}
			}
		}
		return request;
	}

}
