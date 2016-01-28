package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.router.LanguageRequestRouterPool;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public abstract class BaseLanguageManager extends BaseManager {
	
	protected Request setLanguageContext(Request request, String graphId, String manager, String operation) {
        request.getContext().put(LanguageParams.language_id.name(), graphId);
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }

    protected Request getLanguageRequest(String graphId, String manager, String operation) {
        Request request = new Request();
        return setLanguageContext(request, graphId, manager, operation);
    }
	
	protected Response getLanguageResponse(Request request, Logger logger) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            Future<Object> future = Patterns.ask(router, request, LanguageRequestRouterPool.REQ_TIMEOUT);
            Object obj = Await.result(future, LanguageRequestRouterPool.WAIT_TIMEOUT.duration());
            if (obj instanceof Response) {
                return (Response) obj;
            } else {
                return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }

    protected Response getLanguageResponse(List<Request> requests, Logger logger, String paramName, String returnParam) {
        if (null != requests && !requests.isEmpty()) {
            ActorRef router = LanguageRequestRouterPool.getRequestRouter();
            try {
                List<Future<Object>> futures = new ArrayList<Future<Object>>();
                for (Request request : requests) {
                    Future<Object> future = Patterns.ask(router, request, LanguageRequestRouterPool.REQ_TIMEOUT);
                    futures.add(future);
                }
                Future<Iterable<Object>> objects = Futures.sequence(futures, RequestRouterPool.getActorSystem().dispatcher());
                Iterable<Object> responses = Await.result(objects, LanguageRequestRouterPool.WAIT_TIMEOUT.duration());
                if (null != responses) {
                    List<Object> list = new ArrayList<Object>();
                    Response response = new Response();
                    for (Object obj : responses) {
                        if (obj instanceof Response) {
                            Response res = (Response) obj;
                            if (!checkError(res)) {
                                Object vo = res.get(paramName);
                                response = copyResponse(response, res);
                                if (null != vo) {
                                    list.add(vo);
                                }
                            } else {
                                return res;
                            }
                        } else {
                            return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
                        }
                    }
                    response.put(returnParam, list);
                    return response;
                } else {
                    return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
            }
        } else {
            return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
        }
    }
}