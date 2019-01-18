package org.ekstep.learning.util;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.learning.common.enums.LearningErrorCodes;
import org.ekstep.telemetry.logger.TelemetryManager;
import scala.concurrent.Future;

import java.util.Map;

/**
 * Utility class for Akka Actors
 */
public class ActorUtils {

    public static void handleFuture(final Request request,
                                Future<Object> future,
                                final ActorRef parent,
                                final ActorRef self,
                                final ActorContext context) {
        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object arg0) throws Throwable {
                parent.tell(arg0, self);
                Response res = (Response) arg0;
                ResponseParams params = res.getParams();
                TelemetryManager.log(
                        request.getManagerName() + "," + request.getOperation() + ", SUCCESS, " + params.toString());
            }
        }, context.dispatcher());

        future.onFailure(new OnFailure() {
            @Override
            public void onFailure(Throwable e) throws Throwable {
                handleException(request, e, parent, self);
            }
        }, context.dispatcher());
    }

    public static void handleException(final Request request,
                                          Throwable e,
                                          final ActorRef parent,
                                          final ActorRef self) {
        TelemetryManager.warn(request.getManagerName() + "," + request.getOperation() + ", ERROR: " + e.getMessage());
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr(LearningErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        params.setErrmsg(setErrMessage(e));
        response.setParams(params);
        setResponseCode(response, e);
        parent.tell(response, self);
    }

    private static String setErrMessage(Throwable e) {
        if (e instanceof MiddlewareException) {
            return e.getMessage();
        } else {
            return "Something went wrong in server while processing the request";
        }
    }

    public static void setResponseCode(Response res, Throwable e) {
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

    public static void OK(String responseIdentifier, Object vo, ActorRef parent, ActorRef self) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getSuccessStatus());
        parent.tell(response, self);
    }

    public static void OK(Map<String, Object> responseObjects, ActorRef parent, ActorRef self) {
        Response response = new Response();
        if (null != responseObjects && responseObjects.size() > 0) {
            responseObjects.entrySet().forEach(e -> response.put(e.getKey(), e.getValue()));
        }
        response.setParams(getSuccessStatus());
        parent.tell(response, self);
    }

    private static ResponseParams getSuccessStatus() {
        ResponseParams params = new ResponseParams();
        params.setErr("0");
        params.setStatus(StatusType.successful.name());
        params.setErrmsg("Operation successful");
        return params;
    }

}
