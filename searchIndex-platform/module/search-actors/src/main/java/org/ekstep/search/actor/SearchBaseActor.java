package org.ekstep.search.actor;

import java.util.Map;
import java.util.Map.Entry;

import akka.actor.AbstractActor;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.elasticsearch.action.search.SearchPhaseExecutionException;

import akka.actor.ActorRef;
import scala.concurrent.Future;

public abstract class SearchBaseActor extends AbstractActor {

//    public void onReceive(Object message) throws Exception {
//        if (message instanceof Request) {
//            Request request = (Request) message;
//            invokeMethod(request, getSender());
//        } else if (message instanceof Response) {
//            // do nothing
//        } else {
//            unhandled(message);
//        }
//    }

    public abstract Future<Response> onReceive(Request request) throws Throwable;

    private Future<Response> internalOnReceive(Request request) {
        try {
            return onReceive(request);
        } catch (Throwable e) {
            return ERROR(request.getOperation(), e);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Request.class, message -> {
            Patterns.pipe(internalOnReceive(message), getContext().dispatcher()).to(sender());
        }).build();
    }

    public Future<Response> ERROR(String operation) {
        Response response = getErrorResponse(new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid operation provided in request to process: " + operation));
        return Futures.successful(response);
    }

    protected Future<Response> ERROR(String operation, Throwable exception) {
        return Futures.successful(getErrorResponse(exception));
    }

    private Response getErrorResponse(Throwable e) {
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(ResponseParams.StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
            response.put("messages", mwException.getMessage());
        } else {
            TelemetryManager.error("Error while processing", e);
            params.setErr("ERR_SYSTEM_EXCEPTION");
        }
        System.out.println("Exception occurred - class :" + e.getClass().getName() + " with message :" + e.getMessage());
        params.setErrmsg(setErrMessage(e));
        response.setParams(params);
        setResponseCode(response, e);
        return response;
    }

    public Response OK(String responseIdentifier, Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getSucessStatus());
        return response;
    }

    public Response OK(Map<String, Object> responseObjects) {
        Response response = new Response();
        if (null != responseObjects && responseObjects.size() > 0) {
            for (Entry<String, Object> entry : responseObjects.entrySet()) {
                response.put(entry.getKey(), entry.getValue());
            }
        }
        response.setParams(getSucessStatus());
        return response;
    }

    public Response ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, Object vo) {
        TelemetryManager.log("ErrorCode: "+ errorCode + " :: Error message: " + errorMessage);
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;
    }

    public void handleException(Throwable e, ActorRef parent) {
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr("ERR_SYSTEM_EXCEPTION");
        }
        TelemetryManager.log("Exception occured in class :"+ e.getClass().getName() + " message: " + e.getMessage());
        params.setErrmsg(setErrMessage(e));
        response.setParams(params);
        setResponseCode(response, e);
        parent.tell(response, getSelf());
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
		if (e instanceof MiddlewareException)
        		return e.getMessage();
		else {
             if(e.getSuppressed().length > 0) {
                 return e.getSuppressed()[0].getMessage();
             } else {
                 return e.getMessage();
             }
        }
    }
}
