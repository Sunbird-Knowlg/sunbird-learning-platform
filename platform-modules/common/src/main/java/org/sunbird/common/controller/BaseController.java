package org.sunbird.common.controller;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.RequestParams;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

	public static final String API_VERSION = "1.0";
	public static final String API_VERSION_2 = "2.0";
	public static final String API_VERSION_3 = "3.0";

	protected ObjectMapper mapper = new ObjectMapper();

	protected ResponseEntity<Response> getResponseEntity(Response response, String apiId, String msgId) {
		int statusCode = response.getResponseCode().code();
		HttpStatus status = getStatus(statusCode);
		setResponseEnvelope(response, apiId, msgId);
		return new ResponseEntity<Response>(response, status);
	}

	protected ResponseEntity<Response> getResponseEntity(Response response, String apiId, String msgId,
			String resMsgId) {
		int statusCode = response.getResponseCode().code();
		HttpStatus status = getStatus(statusCode);
		setResponseEnvelope(response, apiId, msgId, resMsgId);
		return new ResponseEntity<Response>(response, status);
	}

	protected Response getErrorResponse(Exception e) {
		Response response = new Response();
		ResponseParams resStatus = new ResponseParams();
		String message = setMessage(e);
		resStatus.setErrmsg(message);
		resStatus.setStatus(StatusType.failed.name());
		if (e instanceof MiddlewareException) {
			MiddlewareException me = (MiddlewareException) e;
			resStatus.setErr(me.getErrCode());
			response.setResponseCode(me.getResponseCode());
		} else {
			resStatus.setErr(TaxonomyErrorCodes.SYSTEM_ERROR.name());
			response.setResponseCode(ResponseCode.SERVER_ERROR);
		}
		response.setParams(resStatus);
		return response;
	}

	protected String setMessage(Exception e) {
		if (e instanceof MiddlewareException) {
			return e.getMessage();
		} else {
			return "Something went wrong in server while processing the request";
		}
	}

	protected HttpStatus getHttpStatus(Exception e) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if (e instanceof ClientException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (e instanceof ResourceNotFoundException) {
			status = HttpStatus.NOT_FOUND;
		}
		return status;
	}

	protected ResponseEntity<Response> getExceptionResponseEntity(Exception e, String apiId, String msgId) {
		HttpStatus status = getHttpStatus(e);
		Response response = getErrorResponse(e);
		setResponseEnvelope(response, apiId, msgId);
		return new ResponseEntity<Response>(response, status);
	}

	protected HttpStatus getStatus(int statusCode) {
		HttpStatus status = null;
		try {
			status = HttpStatus.valueOf(statusCode);
		} catch (Exception e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return status;
	}

	protected void writeToResponse(ResponseParams params, String content, String contentType,
			HttpServletResponse response) throws Exception {
		response.setContentType(contentType);
		OutputStream resOs = response.getOutputStream();
		OutputStream buffOs = new BufferedOutputStream(resOs);
		OutputStreamWriter outputwriter = new OutputStreamWriter(buffOs);
		outputwriter.write(params.toString() + "\n");
		if (StringUtils.isNotBlank(content)) {
			outputwriter.write(content);
		}
		outputwriter.flush();
		outputwriter.close();
	}

	protected void writeError(Exception e, HttpServletResponse response) {
		Response appResponse = getErrorResponse(e);
		HttpStatus status = getHttpStatus(e);
		response.setStatus(status.value());
		try {
			writeToResponse(appResponse.getParams(), e.getMessage(), "text/csv;charset=utf-8", response);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap) {
		Request request = new Request();
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
		return request;
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

	protected Response copyResponse(Response res) {
		Response response = new Response();
		response.setResponseCode(res.getResponseCode());
		response.setParams(res.getParams());
		return response;
	}

	private void setResponseEnvelope(Response response, String apiId, String msgId) {
		if (null != response) {
			response.setId(apiId);
			response.setVer(getAPIVersion());
			response.setTs(getResponseTimestamp());
			ResponseParams params = response.getParams();
			if (null == params)
				params = new ResponseParams();
			if (StringUtils.isNotBlank(msgId))
				params.setMsgid(msgId);
			params.setResmsgid(getUUID());
			if (StringUtils.equalsIgnoreCase(ResponseParams.StatusType.successful.name(), params.getStatus())) {
				params.setErr(null);
				params.setErrmsg(null);
			}
			response.setParams(params);
		}
	}

	private void setResponseEnvelope(Response response, String apiId, String msgId, String resMsgId) {
		if (null != response) {
			response.setId(apiId);
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

	protected String getAPIVersion() {
		return API_VERSION;
	}

	private String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}

	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}

	@SuppressWarnings("unchecked")
	protected Request getBundleRequest(Map<String, Object> requestMap, String errorCode) {
		Request request = getRequest(requestMap);
		Map<String, Object> map = request.getRequest();
		if (null != map && !map.isEmpty()) {
			List<String> contentIdentifiers = (List<String>) map.get("content_identifiers");
			String fileName = (String) map.get("file_name");
			if (null == contentIdentifiers || contentIdentifiers.isEmpty())
				throw new MiddlewareException(errorCode,
						"Atleast one content identifier should be provided to create ECAR file");
			request.put("content_identifiers", contentIdentifiers);
			request.put("file_name", fileName);
		} else if (null != map && map.isEmpty()) {
			throw new MiddlewareException(errorCode, "Invalid request body");
		}
		return request;
	}

	protected List<String> convertStringArrayToList(String[] array) {
		List<String> list = new ArrayList<String>();
		if (null != array) {
			try {
				list = Arrays.asList(array);
			} catch (Exception e) {
				throw e;
			}
		}
		return list;
	}
}