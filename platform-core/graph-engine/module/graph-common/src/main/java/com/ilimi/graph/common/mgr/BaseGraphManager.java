package com.ilimi.graph.common.mgr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.Mapper;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.Property;
import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.dto.Status.StatusType;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.exception.MiddlewareException;
import com.ilimi.graph.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.exception.ServerException;

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
        response.setStatus(getSucessStatus());
        parent.tell(response, getSelf());
    }

    public void OK(String responseIdentifier, BaseValueObject vo, ActorRef parent) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setStatus(getSucessStatus());
        parent.tell(response, getSelf());
    }

    public void OK(Map<String, BaseValueObject> responseObjects, ActorRef parent) {
        Response response = new Response();
        if (null != responseObjects && responseObjects.size() > 0) {
            for (Entry<String, BaseValueObject> entry : responseObjects.entrySet()) {
                response.put(entry.getKey(), entry.getValue());
            }
        }
        response.setStatus(getSucessStatus());
        parent.tell(response, getSelf());
    }

    public void ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, BaseValueObject vo,
            ActorRef parent) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setStatus(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        parent.tell(response, getSelf());
    }

    public void ERROR(Throwable e, ActorRef parent) {
        handleException(e, parent);
    }

    public void ERROR(String errorCode, String errorMessage, ResponseCode code, ActorRef parent) {
        Response response = new Response();
        response.setStatus(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        parent.tell(response, getSelf());
    }

    public boolean checkError(Response response) {
        Status status = response.getStatus();
        if (null != status) {
            if (StringUtils.equals(StatusType.ERROR.name(), status.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public String getErrorMessage(Response response) {
        Status status = response.getStatus();
        if (null != status) {
            String msg = status.getMessage();
            if (StringUtils.isNotBlank(msg))
                return msg;
            return status.getCode();
        }
        return null;
    }

    public void returnResponse(Future<Object> response, final ActorRef parent) {
        onSuccessResponse(response, parent);
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

    public boolean validateRequired(BaseValueObject... objects) {
        boolean valid = true;
        for (BaseValueObject baseValueObject : objects) {
            if (null == baseValueObject) {
                valid = false;
                break;
            }
            if (baseValueObject instanceof StringValue) {
                if (StringUtils.isBlank(((StringValue) baseValueObject).getId())) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof BaseValueObjectList<?>) {
                BaseValueObjectList<?> list = (BaseValueObjectList<?>) baseValueObject;
                if (null == list.getValueObjectList() || list.getValueObjectList().isEmpty()) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof BaseValueObjectMap<?>) {
                BaseValueObjectMap<?> map = (BaseValueObjectMap<?>) baseValueObject;
                if (null == map.getBaseValueMap() || map.getBaseValueMap().isEmpty()) {
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
            if (baseValueObject instanceof Identifier) {
                Identifier longVal = (Identifier) baseValueObject;
                if (null == longVal.getId()) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof LongIdentifier) {
                LongIdentifier longVal = (LongIdentifier) baseValueObject;
                if (null == longVal.getId()) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    public void handleException(Throwable e, ActorRef parent) {
        Response response = new Response();
        Status status = new Status();
        status.setStatus(StatusType.ERROR.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            status.setCode(mwException.getErrCode());
        } else {
            status.setCode(GraphEngineErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        status.setMessage(e.getMessage());
        response.setStatus(status);
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

    public Future<List<StringValue>> convertFuture(Future<Map<String, List<String>>> future) {
        Future<List<StringValue>> listFuture = future.map(new Mapper<Map<String, List<String>>, List<StringValue>>() {
            @Override
            public List<StringValue> apply(Map<String, List<String>> parameter) {
                List<StringValue> messages = new ArrayList<StringValue>();
                if (null != parameter && !parameter.isEmpty()) {
                    for (List<String> list : parameter.values()) {
                        if (null != list && !list.isEmpty()) {
                            for (String msg : list) {
                                if (StringUtils.isNotBlank(msg))
                                    messages.add(new StringValue(msg));
                            }
                        }
                    }
                }
                return messages;
            }
        }, getContext().dispatcher());
        return listFuture;
    }

    private Status getSucessStatus() {
        Status status = new Status();
        status.setCode("0");
        status.setStatus(StatusType.SUCCESS.name());
        status.setMessage("Operation successful");
        return status;
    }

    private Status getErrorStatus(String errorCode, String errorMessage) {
        Status status = new Status();
        status.setCode(errorCode);
        status.setStatus(StatusType.ERROR.name());
        status.setMessage(errorMessage);
        return status;
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
}
