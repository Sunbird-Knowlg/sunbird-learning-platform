package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchParams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.util.LogTelemetryEventUtil;

import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;

public class SearchBaseController extends Controller {
	private static final String API_ID_PREFIX = "ekstep";
	public static final String API_VERSION = "1.0";

	private static Logger LOGGER = LogManager.getLogger(SearchBaseController.class.getName());
	protected ObjectMapper mapper = new ObjectMapper();

	protected String getAPIVersion() {
		return API_VERSION;
	}

	@SuppressWarnings("unchecked")
	protected Request getRequest(RequestBody requestBody) {
		Request request = new Request();
		if (null != requestBody) {
			JsonNode data = requestBody.asJson();
			Map<String, Object> requestMap = mapper.convertValue(data, Map.class);
			if (null != requestMap && !requestMap.isEmpty()) {
				String id = (String) requestMap.get("id");
				String ver = (String) requestMap.get("ver");
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

	protected Result getResult(Response response, String apiId, String msgId, String resMsgId) {
		Result result = ok();
		try {
			setResponseEnvelope(response, apiId, msgId, resMsgId);
			String responseJson = mapper.writeValueAsString(response);
			result = ok(responseJson);
		} catch (JsonProcessingException e) {
			LOGGER.error(e);
		}
		return result;
	}

	protected boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}

	private void setResponseEnvelope(Response response, String apiId, String msgId, String resMsgId) {
		if (null != response) {
			response.setId(API_ID_PREFIX + "." + apiId);
			response.setVer(getAPIVersion());
			response.setTs(getResponseTimestamp());
			ResponseParams params = response.getParams();
			if (null == params)
				params = new ResponseParams();
			if (StringUtils.isNotBlank(msgId))
				params.setMsgid(msgId);
			if (StringUtils.isNotBlank(resMsgId))
				params.setResmsgid(resMsgId);
			else
				params.setResmsgid(getUUID());
			if (StringUtils.equalsIgnoreCase(ResponseParams.StatusType.successful.name(), params.getStatus())) {
				params.setErr(null);
				params.setErrmsg(null);
			}
			response.setParams(params);
		}
	}

	private String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}

	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}
	
	protected void writeTelemetryLog(Request request, String correlationId, Integer count) {
		String queryString = (String) request.get(CompositeSearchParams.query.name());
		Object filters = request.get(CompositeSearchParams.filters.name());
		Object sort = request.get(CompositeSearchParams.sort_by.name());
		LogTelemetryEventUtil.logContentSearchEvent(queryString, filters, sort, correlationId, count);
	}

}
