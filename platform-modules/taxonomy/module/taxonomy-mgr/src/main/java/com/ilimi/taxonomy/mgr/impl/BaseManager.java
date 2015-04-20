package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.LongIdentifier;
import com.ilimi.graph.common.dto.Property;
import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.dto.Status.StatusType;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;
import com.ilimi.taxonomy.router.RequestRouterPool;

public abstract class BaseManager {

    protected Response getResponse(Request request, Logger logger) {
        ActorRef router = RequestRouterPool.getRequestRouter();
        try {
            Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
            Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
            if (obj instanceof Response) {
                return (Response) obj;
            } else {
                return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }

    protected Response getResponse(List<Request> requests, Logger logger, String paramName, String returnParam) {
        if (null != requests && !requests.isEmpty()) {
            ActorRef router = RequestRouterPool.getRequestRouter();
            try {
                List<Future<Object>> futures = new ArrayList<Future<Object>>();
                for (Request request : requests) {
                    Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
                    futures.add(future);
                }
                Future<Iterable<Object>> objects = Futures.sequence(futures, RequestRouterPool.getActorSystem().dispatcher());
                Iterable<Object> responses = Await.result(objects, RequestRouterPool.WAIT_TIMEOUT.duration());
                if (null != responses) {
                    BaseValueObjectList<BaseValueObject> list = new BaseValueObjectList<BaseValueObject>();
                    list.setValueObjectList(new ArrayList<BaseValueObject>());
                    Response response = new Response();
                    for (Object obj : responses) {
                        if (obj instanceof Response) {
                            Response res = (Response) obj;
                            if (!checkError(res)) {
                                BaseValueObject vo = res.get(paramName);
                                response = copyResponse(response, res);
                                if (null != vo) {
                                    list.getValueObjectList().add(vo);
                                }
                            } else {
                                return res;
                            }
                        } else {
                            return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
                        }
                    }
                    response.put(returnParam, list);
                    return response;
                } else {
                    return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
            }
        } else {
            return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
        }
    }

    protected Request setContext(Request request, String graphId, String manager, String operation) {
        request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }

    protected Request getRequest(String graphId, String manager, String operation) {
        Request request = new Request();
        return setContext(request, graphId, manager, operation);
    }

    protected Request getRequest(String graphId, String manager, String operation, String paramName, BaseValueObject vo) {
        Request request = getRequest(graphId, manager, operation);
        request.put(paramName, vo);
        return request;
    }

    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setStatus(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }

    protected boolean checkError(Response response) {
        Status status = response.getStatus();
        if (null != status) {
            if (StringUtils.equals(StatusType.ERROR.name(), status.getStatus())) {
                return true;
            }
        }
        return false;
    }

    protected Response copyResponse(Response res) {
        Response response = new Response();
        response.setResponseCode(res.getResponseCode());
        response.setStatus(res.getStatus());
        return response;
    }

    protected Response copyResponse(Response to, Response from) {
        to.setResponseCode(from.getResponseCode());
        to.setStatus(from.getStatus());
        return to;
    }

    private Status getErrorStatus(String errorCode, String errorMessage) {
        Status status = new Status();
        status.setCode(errorCode);
        status.setStatus(StatusType.ERROR.name());
        status.setMessage(errorMessage);
        return status;
    }

    protected boolean validateRequired(BaseValueObject... objects) {
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

}
