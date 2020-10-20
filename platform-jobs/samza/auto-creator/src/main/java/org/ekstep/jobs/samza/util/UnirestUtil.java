package org.ekstep.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ServerException;

import java.io.File;
import java.util.Map;

public class UnirestUtil {

	private static ObjectMapper mapper = new ObjectMapper();
	private static JobLogger LOGGER = new JobLogger(UnirestUtil.class);
	public static final Long INITIAL_BACKOFF_DELAY = Platform.config.hasPath("auto_creator.internal_api.initial_backoff_delay") ? Platform.config.getLong("auto_creator.internal_api.initial_backoff_delay") : 10000;    // 10 seconds
	public static final Long MAXIMUM_BACKOFF_DELAY = Platform.config.hasPath("auto_creator.internal_api.maximum_backoff_delay") ? Platform.config.getLong("auto_creator.internal_api.initial_backoff_delay") : 300000;    // 5 min
	public static final Integer INCREMENT_BACKOFF_DELAY = Platform.config.hasPath("auto_creator.increment_backoff_delay") ? Platform.config.getInt("auto_creator.increment_backoff_delay") : 2;
	public static Long BACKOFF_DELAY = INITIAL_BACKOFF_DELAY;

	public static Response post(String url, Map<String, Object> requestMap, Map<String, String> headerParam)
			throws Exception {
		Response resp = null;
		validateRequest(url, headerParam);
		if (MapUtils.isEmpty(requestMap))
			throw new ServerException("ERR_INVALID_REQUEST_BODY", "Request Body is Missing!");
		try {
			while (null == resp) {
				HttpResponse<String> response = Unirest.post(url).headers(headerParam).body(mapper.writeValueAsString(requestMap)).asString();
				resp = getResponse(url, response);
			}
			return resp;
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	public static Response patch(String url, Map<String, Object> requestMap, Map<String, String> headerParam)
			throws Exception {
		Response resp = null;
		validateRequest(url, headerParam);
		if (MapUtils.isEmpty(requestMap))
			throw new ServerException("ERR_INVALID_REQUEST_BODY", "Request Body is Missing!");
		try {
			while (null == resp) {
				HttpResponse<String> response = Unirest.patch(url).headers(headerParam).body(mapper.writeValueAsString(requestMap)).asString();
				resp = getResponse(url, response);
			}
			return resp;
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	public static Response post(String url, String paramName, File value, Map<String, String> headerParam)
			throws Exception {
		Response resp = null;
		validateRequest(url, headerParam);
		if (null == value || null == value)
			throw new ServerException("ERR_INVALID_REQUEST_PARAM", "Invalid Request Param!");
		try {
			while (null == resp) {
				HttpResponse<String> response = Unirest.post(url).headers(headerParam).multiPartContent().field(paramName, new File(value.getAbsolutePath())).asString();
				resp = getResponse(url, response);
			}
			return resp;
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	public static Response post(String url, String paramName, String value, Map<String, String> headerParam)
			throws Exception {
		Response resp = null;
		validateRequest(url, headerParam);
		if (null == value || null == value)
			throw new ServerException("ERR_INVALID_REQUEST_PARAM", "Invalid Request Param!");
		try {
			while (null == resp) {
				HttpResponse<String> response = Unirest.post(url).headers(headerParam).multiPartContent().field(paramName, value).asString();
				resp = getResponse(url, response);
			}
			return resp;
		} catch (Exception e) {
			throw new ServerException("ERR_API_CALL", "Something Went Wrong While Making API Call | Error is: " + e.getMessage());
		}
	}

	public static Response get(String url, String queryParam, Map<String, String> headerParam)
			throws Exception {
		Response resp = null;
		validateRequest(url, headerParam);
		String reqUrl = StringUtils.isNotBlank(queryParam) ? url + "?" + queryParam : url;
		try {
			while (null == resp) {
				HttpResponse<String> response = Unirest.get(reqUrl).headers(headerParam).asString();
				resp = getResponse(reqUrl, response);
			}
			return resp;
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

	private static Response getResponse(String url, HttpResponse<String> response) {
		Response resp = null;
		if (null != response && StringUtils.isNotBlank(response.getBody())) {
			try {
				resp = mapper.readValue(response.getBody(), Response.class);
				BACKOFF_DELAY = INITIAL_BACKOFF_DELAY;
			} catch (Exception e) {
				LOGGER.error("UnirestUtil ::: getResponse ::: Error occurred while parsing api response for url ::: " + url + ". | Error is: " + e.getMessage(), e);
				LOGGER.info("UnirestUtil :::: BACKOFF_DELAY ::: " + BACKOFF_DELAY);
				if (BACKOFF_DELAY <= MAXIMUM_BACKOFF_DELAY) {
					long delay = BACKOFF_DELAY;
					BACKOFF_DELAY = BACKOFF_DELAY * INCREMENT_BACKOFF_DELAY;
					LOGGER.info("UnirestUtil :::: BACKOFF_DELAY after increment::: " + BACKOFF_DELAY);
					delay(delay);
				} else throw new ServerException("ERR_API_CALL", "Unable to parse response data for url: "+ url +" | Error is: " + e.getMessage());
			}
		} else {
			LOGGER.info("Null Response Received While Making Api Call!");
			throw new ServerException("ERR_API_CALL", "Null Response Received While Making Api Call!");
		}
		return resp;
	}

	private static void delay(long time) {
		LOGGER.info("UnirestUtil :::: backoff delay is called with : " + time);
		try {
			Thread.sleep(time);
		} catch (Exception e) {

		}
	}

}
