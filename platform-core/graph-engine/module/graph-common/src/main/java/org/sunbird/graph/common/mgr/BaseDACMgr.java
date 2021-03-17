/**
 * 
 */
package org.sunbird.graph.common.mgr;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;

/**
 * @author pradyumna
 *
 */
public class BaseDACMgr {

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

	protected String setErrMessage(Throwable e) {
		if (e instanceof MiddlewareException) {
			return e.getMessage();
		} else {
			return "Something went wrong in server while processing the request";
		}
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
		TelemetryManager.error(errorCode + ", " + errorMessage);
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
		TelemetryManager.log("Exception occured in class :" + e.getClass().getName() + "with message :" + e.getMessage());
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
		TelemetryManager.error(errorCode + ", " + errorMessage);
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
