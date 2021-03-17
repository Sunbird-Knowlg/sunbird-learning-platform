package org.sunbird.graph.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import scala.concurrent.Future;

public abstract class BaseRequestRouter extends UntypedActor {

    protected long timeout = Platform.config.hasPath("akka.request_timeout") ? (Platform.config.getLong("akka.request_timeout") * 1000): 30000;

    private static final Logger perfLogger = LogManager.getLogger("PerformanceTestLogger");

    

    protected abstract void initActorPool();

    protected abstract ActorRef getActorFromPool(Request request);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            if (StringUtils.equalsIgnoreCase("init", message.toString())) {
                // initializes the actor pool for actors
                initActorPool();
                getSender().tell("initComplete", getSelf());
            } else {
                getSender().tell(message, getSelf());
            }
        } else if (message instanceof Request) {
            // routes the incoming Request to API actor. All forward calls
            // are "ask" calls and this actor throws an timeout exception after
            // the configured timeout.
            Request request = (Request) message;
            long startTime = System.currentTimeMillis();
            request.getContext().put(GraphHeaderParams.start_time.name(), startTime);
            perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
                    + request.getContext().get(GraphHeaderParams.request_id.name()) + "," + request.getManagerName() + ","
                    + request.getOperation() + ",STARTTIME," + startTime);
            ActorRef parent = getSender();
            try {
                ActorRef actorRef = getActorFromPool(request);
                Future<Object> future = Patterns.ask(actorRef, request, timeout);
                handleFuture(request, future, parent);
            } catch (Exception e) {
                handleException(request, e, parent);
            }
        } else if (message instanceof Response) {
            // do nothing
        } else {
            unhandled(message);
        }
    }

    protected void handleFuture(final Request request, Future<Object> future, final ActorRef parent) {
        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object arg0) throws Throwable {
                parent.tell(arg0, getSelf());
                long endTime = System.currentTimeMillis();
                long exeTime = endTime - (Long) request.getContext().get(GraphHeaderParams.start_time.name());
                Response res = (Response) arg0;
                ResponseParams params = res.getParams();
//                PlatformLogger.log(request.getRequestId() + " | " + request.getManagerName() + "," + request.getOperation() + ", SUCCESS, " + params.toString());
                perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
                        + request.getContext().get(GraphHeaderParams.request_id.name()) + "," + request.getManagerName() + ","
                        + request.getOperation() + ",ENDTIME," + endTime);
                perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
                        + request.getContext().get(GraphHeaderParams.request_id.name()) + "," + request.getManagerName() + ","
                        + request.getOperation() + "," + params.getStatus() + "," + exeTime);
            }
        }, getContext().dispatcher());

        future.onFailure(new OnFailure() {
            @Override
            public void onFailure(Throwable e) throws Throwable {
                handleException(request, e, parent);
            }
        }, getContext().dispatcher());
    }

    protected void handleException(final Request request, Throwable e, final ActorRef parent) {
//        PlatformLogger.log(request.getRequestId() + " | " + request.getManagerName() + "," + request.getOperation() , e.getMessage(), LoggerEnum.WARN.name());
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr(GraphEngineErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        params.setErrmsg(e.getMessage());
        response.setParams(params);
        setResponseCode(response, e);
        parent.tell(response, getSelf());
        long exeTime = System.currentTimeMillis() - (Long) request.getContext().get(GraphHeaderParams.start_time.name());
        perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
                + request.getContext().get(GraphHeaderParams.request_id.name()) + "," + request.getManagerName() + ","
                + request.getOperation() + ",ERROR," + exeTime);
    }

    public static Map<String, Method> getMethodMap(Class<?> cls) {
        if (null != cls) {
            Map<String, Method> map = new HashMap<String, Method>();
            Method[] methods = cls.getDeclaredMethods();
            if (null != methods && methods.length > 0) {
                for (Method method : methods) {
                    Class<?>[] parameters = method.getParameterTypes();
                    Class<?> returnType = method.getReturnType();
                    if (returnType.getCanonicalName() == "void") {
                        if (null != parameters && parameters.length == 1) {
                            if (StringUtils.equals(Request.class.getName(), parameters[0].getCanonicalName()))
                                map.put(method.getName(), method);
                        }
                    }
                }
            }
            return map;
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
}
