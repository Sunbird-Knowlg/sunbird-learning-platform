package org.sunbird.common.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ServerException;
import org.sunbird.telemetry.logger.TelemetryManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * Utility Class to make REST API Calls
 * 
 * @author gauraw
 *
 */
public class HttpRestUtil {

	private static final String EKSTEP_PLATFORM_API_USERID = "System";
	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	private static String EKSTEP_API_AUTHORIZATION_KEY = null;

	private static Gson gsonObj = new Gson();
	private static ObjectMapper objMapper = new ObjectMapper();

	static {
		String key = Platform.config.hasPath("dialcode.api.authorization")
				? Platform.config.getString("dialcode.api.authorization")
				: "";
		EKSTEP_API_AUTHORIZATION_KEY = "Bearer " + key;
		Unirest.setDefaultHeader("Content-Type", DEFAULT_CONTENT_TYPE);
		Unirest.setDefaultHeader("Authorization", EKSTEP_API_AUTHORIZATION_KEY);
		Unirest.setDefaultHeader("user-id", EKSTEP_PLATFORM_API_USERID);
	}

	/**
	 * @param uri
	 * @param requestMap
	 * @param headerParam
	 * @return
	 * @throws Exception
	 */
	public static Response makePostRequest(String uri, Map<String, Object> requestMap, Map<String, String> headerParam)
			throws Exception {
		TelemetryManager.log("HttpRestUtil:makePostRequest |  Request Url:" + uri);
		TelemetryManager.log("HttpRestUtil:makePostRequest |  Request Body:" + requestMap);

		HttpResponse<String> response = null;

		if (null == headerParam)
			throw new ServerException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.");

		if (null == requestMap)
			throw new ServerException("ERR_INVALID_REQUEST_BODY", "Request Body is Manadatory");

		try {
			response = Unirest.post(uri).headers(headerParam)
					.body(gsonObj.toJson(requestMap)).asString();
		} catch (Exception e) {
			throw new ServerException("ERR_CALL_API",
					"Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}

		return getResponse(response);
	}

	/**
	 * @param urlWithIdentifier
	 * @param queryParam
	 * @param headerParam
	 * @return
	 * @throws Exception
	 */
	public static Response makeGetRequest(String urlWithIdentifier, String queryParam, Map<String, String> headerParam)
			throws Exception {
		if (null == headerParam)
			throw new ServerException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.");

		String req = (null != queryParam) ? urlWithIdentifier + queryParam : urlWithIdentifier;

		HttpResponse<String> response = Unirest.get(req).headers(headerParam).asString();
		return getResponse(response);
	}

	private static Response getResponse(HttpResponse<String> response) {
		String body = null;
		Response resp = new Response();
		try {
			body = response.getBody();
			if (StringUtils.isNotBlank(body))
				resp = objMapper.readValue(body, Response.class);
		} catch (UnsupportedEncodingException e) {
			TelemetryManager.info("UnsupportedEncodingException:::::"+ e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage());
		} catch (Exception e) {
			TelemetryManager.info("Exception:::::"+ e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage());
		}
		return resp;
	}

}
