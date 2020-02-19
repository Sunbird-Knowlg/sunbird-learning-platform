package org.ekstep.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiUtil {

	private static Gson gsonObj = new Gson();
	private static ObjectMapper objMapper = new ObjectMapper();

	public static Response makeKeyWordsPostRequest(String identifier, Map<String, Object> requestMap) {
		String uri = "https://api.aylien.com/api/v1/entities";
		TelemetryManager.log("ApiUtil:makePostRequest |  Request Url:" + uri);
		TelemetryManager.log("ApiUtil:makePostRequest |  Request Body:" + requestMap);
		HttpResponse<String> response = null;

		Map<String, String> headerParam = new HashMap<String, String>(){{
			put("X-AYLIEN-TextAPI-Application-Key","7f6ddf5416da2b022145472610f03f08");
			put("X-AYLIEN-TextAPI-Application-ID","68f7c606");
			put("Content-Type","application/x-www-form-urlencoded");
		}};

		if (null == requestMap)
			throw new ServerException("ERR_INVALID_REQUEST_BODY", "Request Body is Manadatory");

		try {
			response = Unirest.post(uri).headers(headerParam)
					.field("text", requestMap.get("text")).asString();
		} catch (Exception e) {
			throw new ServerException("ERR_CALL_API",
					"Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
		Response resp = getSuccessResponse();
		String body = "";
		Map<String,Object> result = null;
		try {
			body = response.getBody();
			if (StringUtils.isNotBlank(body))
				result = objMapper.readValue(body, new TypeReference<Map<String, Object>>() {
				});
		} catch (Exception  e) {
			TelemetryManager.info("Exception:::::"+ e);
		}
		List<String> keywords = (List<String>)((Map<String,Object>)result.get("entities")).get("keyword");
		System.out.println("keywords generated for content id : "+keywords);
		if(null!= result && !result.isEmpty())
			resp.getResult().put("keywords",keywords);
		return resp;
	}

	private static Response getSuccessResponse() {
		Response resp = new Response();
		ResponseParams respParam = new ResponseParams();
		respParam.setStatus("successful");
		resp.setParams(respParam);
		return resp;
	}

}
