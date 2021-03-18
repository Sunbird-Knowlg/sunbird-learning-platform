package org.sunbird.graph.common.mgr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import scala.concurrent.Future;

public abstract class BaseGraphManager extends UntypedActor {
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Request) {
            Request request = (Request) message;
            invokeMethod(request, getSender());
        } else if (message instanceof Response) {
            // do nothing
        } else {
            unhandled(message);
        }
    }

    protected abstract void invokeMethod(Request request, ActorRef parent);

    public void OK(ActorRef parent) {
        Response response = new Response();
        response.setParams(getSucessStatus());
        parent.tell(response, getSelf());
    }

    public void OK(String responseIdentifier, Object vo, ActorRef parent) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getSucessStatus());
        parent.tell(response, getSelf());
    }

    public void OK(Map<String, Object> responseObjects, ActorRef parent) {
        Response response = new Response();
        if (null != responseObjects && responseObjects.size() > 0) {
            for (Entry<String, Object> entry : responseObjects.entrySet()) {
                response.put(entry.getKey(), entry.getValue());
            }
        }
        response.setParams(getSucessStatus());
        parent.tell(response, getSelf());
    }
    
    public void sendResponse(Response response, ActorRef parent) {
    	parent.tell(response, getSelf());
    }

    public void ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, Object vo, ActorRef parent) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        parent.tell(response, getSelf());
    }

    public void ERROR(Throwable e, ActorRef parent) {
        handleException(e, parent);
    }
    
    public void ERROR(Throwable e, String responseIdentifier, Object vo, ActorRef parent) {
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
         params.setErrmsg(setErrMessage(e));
         response.setParams(params);
         setResponseCode(response, e);
         parent.tell(response, getSelf());
    }
    
    public Response getErrorResponse(String errorCode, String errorMessage, ResponseCode code) {
    	Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;
    }

    public void ERROR(String errorCode, String errorMessage, ResponseCode code, ActorRef parent) {
        parent.tell(getErrorResponse(errorCode, errorMessage, code), getSelf());
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

    public String getErrorMessage(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            String msg = params.getErrmsg();
            if (StringUtils.isNotBlank(msg))
                return msg;
            return params.getErr();
        }
        return null;
    }

    public void returnResponse(Future<Object> response, final ActorRef parent) {
        onSuccessResponse(response, parent);
        onFailureResponse(response, parent);
    }
    
    public void returnResponseOnFailure(Future<Object> response, final ActorRef parent) {
        onFailureResponse(response, parent);
    }

    public void onSuccessResponse(Future<Object> response, final ActorRef parent) {
        response.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object arg0) throws Throwable {
                parent.tell(arg0, getSelf());
            }
        }, getContext().dispatcher());
    }

    public void onFailureResponse(Future<Object> response, final ActorRef parent) {
        response.onFailure(new OnFailure() {
            @Override
            public void onFailure(Throwable e) throws Throwable {
                handleException(e, parent);
            }
        }, getContext().dispatcher());
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

    public void handleException(Throwable e, ActorRef parent) {
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
        parent.tell(response, getSelf());
    }

    public boolean checkResponseObject(Throwable arg0, Object arg1, ActorRef parent, String errorCode, String errorMsg) {
        if (null != arg0) {
            ERROR(arg0, parent);
        } else {
            if (arg1 instanceof Response) {
                Response res = (Response) arg1;
                if (checkError(res)) {
                    ERROR(errorCode, getErrorMessage(res), res.getResponseCode(), parent);
                } else {
                    return true;
                }
            } else {
                ERROR(errorCode, errorMsg, ResponseCode.SERVER_ERROR, parent);
            }
        }
        return false;
    }

    public Future<List<String>> convertFuture(Future<Map<String, List<String>>> future) {
        Future<List<String>> listFuture = future.map(new Mapper<Map<String, List<String>>, List<String>>() {
            @Override
            public List<String> apply(Map<String, List<String>> parameter) {
                List<String> messages = new ArrayList<String>();
                if (null != parameter && !parameter.isEmpty()) {
                    for (List<String> list : parameter.values()) {
                        if (null != list && !list.isEmpty()) {
                            for (String msg : list) {
                                if (StringUtils.isNotBlank(msg))
                                    messages.add(new String(msg));
                            }
                        }
                    }
                }
                return messages;
            }
        }, getContext().dispatcher());
        return listFuture;
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
    
    protected String setErrMessage(Throwable e){
        if (e instanceof MiddlewareException) {
        		return e.getMessage();
        } else {
        		return "Something went wrong in server while processing the request";
        }
    }
}
