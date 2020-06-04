package org.ekstep.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ServerException;

import java.util.Map;

public class UnirestUtil {

	private static ObjectMapper mapper = new ObjectMapper();

	public static Response post(String url, Map<String, Object> requestMap, Map<String, String> headerParam)
			throws Exception {
		validateRequest(url, headerParam);
		if (MapUtils.isEmpty(requestMap))
			throw new ServerException("ERR_INVALID_REQUEST_BODY", "Request Body is Missing!");
		try {
			HttpResponse<String> response = Unirest.post(url).headers(headerParam).body(mapper.writeValueAsString(requestMap)).asString();
			return getResponse(response);
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	public static Response post(String url, String paramName, String value, Map<String, String> headerParam)
			throws Exception {
		validateRequest(url, headerParam);
		if (null == value || null == value)
			throw new ServerException("ERR_INVALID_REQUEST_PARAM", "Invalid Request Param!");
		try {
			HttpResponse<String> response = Unirest.post(url).headers(headerParam).multiPartContent().field(paramName, value).asString();
			return getResponse(response);
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	public static Response get(String url, String queryParam, Map<String, String> headerParam)
			throws Exception {
		validateRequest(url, headerParam);
		String reqUrl = StringUtils.isNotBlank(queryParam) ? url + "?" + queryParam : url;
		try {
			HttpResponse<String> response = Unirest.get(reqUrl).headers(headerParam).asString();
			return getResponse(response);
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	private static void validateRequest(String url, Map<String, String> headerParam) {
		if (StringUtils.isBlank(url))
			throw new ServerException("ERR_INVALID_URL", "Url Parameter is Missing!");
		if (null == headerParam)
			throw new ServerException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Missing!");
	}

	private static Response getResponse(HttpResponse<String> response) {
		if (null != response && StringUtils.isNotBlank(response.getBody())) {
			try {
				return mapper.readValue(response.getBody(), Response.class);
			} catch (Exception e) {
				throw new ServerException("ERR_DATA_PARSER", "Unable to parse data! | Error is: " + e.getMessage());
			}
		} else
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Null Response Received While Making Api Call!");
	}

}
