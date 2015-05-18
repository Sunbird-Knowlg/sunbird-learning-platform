package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;
import com.ilimi.taxonomy.router.RequestRouterPool;

public abstract class BaseManager {

    protected void setMetadataFields(Node node, String[] fields) {
        if (null != fields && fields.length > 0) {
            if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                Map<String, Object> metadata = new HashMap<String, Object>();
                List<String> fieldList = Arrays.asList(fields);
                for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
                    if (fieldList.contains(entry.getKey())) {
                        metadata.put(entry.getKey(), entry.getValue());
                    }
                }
                node.setMetadata(metadata);
            }
        }
    }

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
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }

    protected Request getRequest(String graphId, String manager, String operation) {
        Request request = new Request();
        return setContext(request, graphId, manager, operation);
    }

    protected Request getRequest(String graphId, String manager, String operation, String paramName, Object vo) {
        Request request = getRequest(graphId, manager, operation);
        request.put(paramName, vo);
        return request;
    }

    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }

    protected boolean checkError(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
                return true;
            }
        }
        return false;
    }

    protected Response copyResponse(Response res) {
        Response response = new Response();
        response.setResponseCode(res.getResponseCode());
        response.setParams(res.getParams());
        return response;
    }

    protected Response copyResponse(Response to, Response from) {
        to.setResponseCode(from.getResponseCode());
        to.setParams(from.getParams());
        return to;
    }

    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

    protected boolean validateRequired(Object... objects) {
        boolean valid = true;
        for (Object baseValueObject : objects) {
            if (null == baseValueObject) {
                valid = false;
                break;
            }
            if (baseValueObject instanceof String) {
                if (StringUtils.isBlank(((String) baseValueObject))) {
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

}
