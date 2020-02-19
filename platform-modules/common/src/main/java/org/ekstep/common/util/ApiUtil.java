package org.ekstep.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiUtil {

	private static Gson gsonObj = new Gson();
	private static ObjectMapper objMapper = new ObjectMapper();
	private static String LANGUAGEAPIKEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIyNDUzMTBhZTFlMzc0NzU1ODMxZTExZmQyMGRjMDg0MiIsImlhdCI6bnVsbCwiZXhwIjpudWxsLCJhdWQiOiIiLCJzdWIiOiIifQ.M1H_Z7WvwRPM0suBCofHs7iuDMMHyBjIRd3xGS4hqy8";//Platform.config.getString("language.api.key");

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
	
	public static Map<String, Object> languageAnalysisAPI(String text){
		
		Map<String, Object> request = new HashMap<String, Object>() {{put("request", new HashMap<String, Object>(){{put("language_id", "en");put("text", text);}});}};
		Map<String, Object> languageAnalysisMap = null;
		try {
			String body = objMapper.writeValueAsString(request);
			System.out.println("LANGUAGEAPIKEY :: " + LANGUAGEAPIKEY);
			HttpResponse<String> httpResponse = Unirest.post("https://api.ekstep.in/language/v3/tools/text/analysis").header("Content-Type", "application/json").header("Authorization", "Bearer "+ LANGUAGEAPIKEY).body(body).asString();
			
			if(httpResponse.getStatus() == 200) {
				Map<String, Object> responseMap = objMapper.readValue(httpResponse.getBody(), Map.class);
				if (MapUtils.isEmpty(responseMap)) {
					return languageAnalysisMap;
				}
				Map<String, Object> result = (Map<String, Object>) responseMap.get("result");
				if (MapUtils.isEmpty(result)) {
					return languageAnalysisMap;
				}
				Map<String, Object> text_complexity = (Map<String, Object>) result.get("text_complexity");
				if (MapUtils.isEmpty(text_complexity)) {
					return languageAnalysisMap;
				}
				
				Map<String, Object> thresholdVocabulary = (Map<String, Object>) text_complexity.get("thresholdVocabulary");
				Integer totalWordCount = (Integer) text_complexity.get("totalWordCount");
				Map<String, Object> partsOfSpeech = (Map<String, Object>) text_complexity.get("partsOfSpeech");
				Map<String, Object> nonThresholdVocabulary = (Map<String, Object>) text_complexity.get("nonThresholdVocabulary");
				
				languageAnalysisMap = new HashMap<String, Object>() {{
					put("thresholdVocabulary", thresholdVocabulary);
					put("totalWordCount", totalWordCount);
					put("partsOfSpeech", partsOfSpeech);
					put("nonThresholdVocabulary", nonThresholdVocabulary);
				}};
				System.out.println("languageAnalysisMap:: " + languageAnalysisMap);
			}else {
				System.out.println("Error:: " + httpResponse.getStatus());
				System.out.println("Error:: " + httpResponse.getBody());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("End languageAnalysisMap:: " + languageAnalysisMap);
		return languageAnalysisMap;
	}

	private static Response getSuccessResponse() {
		Response resp = new Response();
		ResponseParams respParam = new ResponseParams();
		respParam.setStatus("successful");
		resp.setParams(respParam);
		return resp;
	}

}
