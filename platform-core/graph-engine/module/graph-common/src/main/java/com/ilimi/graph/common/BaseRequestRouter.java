package com.ilimi.graph.common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.dto.Status.StatusType;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.exception.MiddlewareException;
import com.ilimi.graph.common.exception.ResourceNotFoundException;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.exception.ServerException;

public abstract class BaseRequestRouter extends UntypedActor {

    protected long timeout = 30000;

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
            request.getContext().put(GraphHeaderParams.START_TIME.name(), startTime);
            perfLogger.info(request.getContext().get(GraphHeaderParams.SCENARIO_NAME.name()) + ","
                    + request.getContext().get(GraphHeaderParams.REQUEST_ID.name()) + "," + request.getManagerName() + ","
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
                long exeTime = endTime - (Long) request.getContext().get(GraphHeaderParams.START_TIME.name());
                Response res = (Response) arg0;
                Status status = res.getStatus();
                perfLogger.info(request.getContext().get(GraphHeaderParams.SCENARIO_NAME.name()) + ","
                        + request.getContext().get(GraphHeaderParams.REQUEST_ID.name()) + "," + request.getManagerName() + ","
                        + request.getOperation() + ",ENDTIME," + endTime);
                perfLogger.info(request.getContext().get(GraphHeaderParams.SCENARIO_NAME.name()) + ","
                        + request.getContext().get(GraphHeaderParams.REQUEST_ID.name()) + "," + request.getManagerName() + ","
                        + request.getOperation() + "," + status.getStatus() + "," + exeTime);
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
        long exeTime = System.currentTimeMillis() - (Long) request.getContext().get(GraphHeaderParams.START_TIME.name());
        perfLogger.info(request.getContext().get(GraphHeaderParams.SCENARIO_NAME.name()) + ","
                + request.getContext().get(GraphHeaderParams.REQUEST_ID.name()) + "," + request.getManagerName() + ","
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
                            if (StringUtils.equals("com.ilimi.graph.common.Request", parameters[0].getCanonicalName()))
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
