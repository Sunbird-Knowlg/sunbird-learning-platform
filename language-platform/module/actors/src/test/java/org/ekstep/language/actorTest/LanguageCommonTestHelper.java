package org.ekstep.language.actorTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.RequestParams;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineActorPoolMgr;
import org.ekstep.telemetry.logger.PlatformLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.ekstep.common.enums.TaxonomyErrorCodes;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class LanguageCommonTestHelper {

	/** The req timeout. */
	public static long REQ_TIMEOUT = 30000;

	/** The wait timeout. */
	public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String API_ID_PREFIX = "ekstep.lp";
	private static final String API_VERSION = "1.0";

	private static void setResponseEnvelope(Response response, String apiId, String msgId) {
		if (null != response) {
			response.setId(API_ID_PREFIX + "." + apiId);
			response.setVer(API_VERSION);
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

	protected static HttpStatus getStatus(int statusCode) {
		HttpStatus status = null;
		try {
			status = HttpStatus.valueOf(statusCode);
		} catch (Exception e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return status;
	}

	private static String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}

	private static String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}

	public static Request getRequestObject(Map<String, Object> requestMap) {
		Request request = getRequest(requestMap);
		Map<String, Object> map = request.getRequest();
		ObjectMapper mapper = new ObjectMapper();
		if (null != map && !map.isEmpty()) {
			try {
				Object obj = map.get("content");
				if (null != obj) {
					Node content = (Node) mapper.convertValue(obj, Node.class);
					request.put("content", content);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return request;
	}

	@SuppressWarnings("unchecked")
	protected static Request getRequest(Map<String, Object> requestMap) {
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

	public static Response getResponse(Request request) {
		ActorRef router = GraphEngineActorPoolMgr.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, REQ_TIMEOUT);
			Object obj = Await.result(future, WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				return (Response) obj;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			PlatformLogger.log("Exception", e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	protected static Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	private static ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	protected static ResponseEntity<Response> getExceptionResponseEntity(Exception e, String apiId, String msgId) {
		HttpStatus status = getHttpStatus(e);
		Response response = getErrorResponse(e);
		setResponseEnvelope(response, apiId, msgId);
		return new ResponseEntity<Response>(response, status);
	}

	protected static HttpStatus getHttpStatus(Exception e) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		if (e instanceof ClientException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (e instanceof ResourceNotFoundException) {
			status = HttpStatus.NOT_FOUND;
		}
		return status;
	}

	protected static Response getErrorResponse(Exception e) {
		Response response = new Response();
		ResponseParams resStatus = new ResponseParams();
		resStatus.setErrmsg(e.getMessage());
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

	public static ResponseEntity<Response> getResponseEntity(Response response, String apiId, String msgId) {
		int statusCode = response.getResponseCode().code();
		HttpStatus status = getStatus(statusCode);
		setResponseEnvelope(response, apiId, msgId);
		return new ResponseEntity<Response>(response, status);
	}
}
