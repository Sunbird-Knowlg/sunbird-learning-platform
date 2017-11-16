/**
 * 
 */
package com.ilimi.graph.common.mgr;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;

import akka.actor.ActorRef;

/**
 * @author pradyumna
 *
 */
public class GraphDACMgr {

	private static final String ekstep = "org.ekstep.";
	private static final String ilimi = "com.ilimi.";
	private static final String java = "java.";
	private static final String default_err_msg = "Something went wrong in server while processing the request";

	protected Response OK() {
		Response response = new Response();
		response.setParams(getSucessStatus());
		return response;
	}

	protected Response OK(String responseIdentifier, Object vo) {
		Response response = new Response();
		response.put(responseIdentifier, vo);
		response.setParams(getSucessStatus());
		return response;
	}

	protected Response OK(Map<String, Object> responseObjects) {
		Response response = new Response();
		if (null != responseObjects && responseObjects.size() > 0) {
			for (Entry<String, Object> entry : responseObjects.entrySet()) {
				response.put(entry.getKey(), entry.getValue());
			}
		}
		response.setParams(getSucessStatus());
		return response;
	}

	protected Response handleException(Throwable e) {
		PlatformLogger.log("Exception occured in class:" + e.getClass().getName(), null, e);
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus(StatusType.failed.name());
		if (e instanceof MiddlewareException) {
			MiddlewareException mwException = (MiddlewareException) e;
			params.setErr(mwException.getErrCode());
		} else {
			params.setErr(GraphEngineErrorCodes.ERR_SYSTEM_EXCEPTION.name());
		}
		params.setErrmsg(setErrMessage(e));
		response.setParams(params);
		setResponseCode(response, e);
		return response;
	}

	private ResponseParams getSucessStatus() {
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(StatusType.successful.name());
		params.setErrmsg("Operation successful");
		return params;
	}

	private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	private String setErrMessage(Throwable e) {
		Class<? extends Throwable> className = e.getClass();
		if (className.getName().contains(ekstep) || className.getName().contains(ilimi)) {
			PlatformLogger.log("Setting error message sent from class " + className, e.getMessage(),
					LoggerEnum.ERROR.name());
			return e.getMessage();
		} else if (className.getName().startsWith(java)) {
			PlatformLogger.log("Setting default err msg " + className, e.getMessage(), LoggerEnum.ERROR.name());
			return default_err_msg;
		}
		return null;
	}

	private void setResponseCode(Response res, Throwable e) {
		if (e instanceof ClientException) {
			res.setResponseCode(ResponseCode.CLIENT_ERROR);
		} else if (e instanceof ServerException) {
			res.setResponseCode(ResponseCode.SERVER_ERROR);
		} else if (e instanceof ResourceNotFoundException) {
			res.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
		} else {
			res.setResponseCode(ResponseCode.SERVER_ERROR);
		}
	}

	public boolean validateRequired(Object... objects) {
		boolean valid = true;
		for (Object baseValueObject : objects) {
			if (null == baseValueObject) {
				valid = false;
				break;
			}
			if (baseValueObject instanceof String) {
				if (StringUtils.isBlank((String) baseValueObject)) {
					valid = false;
					break;
				}
			}
			if (baseValueObject instanceof List<?>) {
				List<?> list = (List<?>) baseValueObject;
				if (null == list || list.isEmpty()) {
					valid = false;
					break;
				}
			}
			if (baseValueObject instanceof Map<?, ?>) {
				Map<?, ?> map = (Map<?, ?>) baseValueObject;
				if (null == map || map.isEmpty()) {
					valid = false;
					break;
				}
			}
			if (baseValueObject instanceof Property) {
				Property property = (Property) baseValueObject;
				if (StringUtils.isBlank(property.getPropertyName())
						|| (null == property.getPropertyValue() && null == property.getDateValue())) {
					valid = false;
					break;
				}
			}
		}
		return valid;
	}

	public Response ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier,
			Object vo) {
		PlatformLogger.log(errorCode + ", " + errorMessage, null, LoggerEnum.ERROR.name());
		Response response = new Response();
		response.put(responseIdentifier, vo);
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(code);
		return response;
	}

	public Response ERROR(Throwable e) {
		return handleException(e);
	}

	public Response ERROR(Throwable e, String responseIdentifier, Object vo) {
		Response response = new Response();
		response.put(responseIdentifier, vo);
		ResponseParams params = new ResponseParams();
		params.setStatus(StatusType.failed.name());
		if (e instanceof MiddlewareException) {
			MiddlewareException mwException = (MiddlewareException) e;
			params.setErr(mwException.getErrCode());
		} else {
			params.setErr(GraphEngineErrorCodes.ERR_SYSTEM_EXCEPTION.name());
		}
		PlatformLogger.log("Exception occured in class :" + e.getClass().getName() + "with message :" + e.getMessage());
		params.setErrmsg(setErrMessage(e));
		response.setParams(params);
		setResponseCode(response, e);
		return response;
	}

	public Response getErrorResponse(String errorCode, String errorMessage, ResponseCode code) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(code);
		return response;
	}

	public Response ERROR(String errorCode, String errorMessage, ResponseCode code, ActorRef parent) {
		PlatformLogger.log(errorCode + ", " + errorMessage, null, LoggerEnum.ERROR.name());
		return getErrorResponse(errorCode, errorMessage, code);
	}

	public boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}

}
