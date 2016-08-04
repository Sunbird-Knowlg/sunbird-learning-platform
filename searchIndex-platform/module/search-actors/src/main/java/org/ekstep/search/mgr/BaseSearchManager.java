package org.ekstep.search.mgr;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.search.router.SearchRequestRouterPool;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class BaseSearchManager {

	protected Request setSearchContext(Request request, String manager, String operation) {
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }

    protected Request getSearchRequest(String manager, String operation) {
        Request request = new Request();
        return setSearchContext(request, manager, operation);
    }

    public void makeAsyncSearchRequest(Request request, Logger logger) {
        ActorRef router = SearchRequestRouterPool.getRequestRouter();
        try {
            router.tell(request, router);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }

	protected Response getSearchResponse(Request request, Logger logger) {
        ActorRef router = SearchRequestRouterPool.getRequestRouter();
        try {
            Future<Object> future = Patterns.ask(router, request, SearchRequestRouterPool.REQ_TIMEOUT);
            Object obj = Await.result(future, SearchRequestRouterPool.WAIT_TIMEOUT.duration());
            if (obj instanceof Response) {
                return (Response) obj;
            } else {
                return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }

    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }

    protected Response ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier,
            Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;
    }

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

    protected boolean checkError(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
                return true;
            }
        }
        return false;
    }

    protected String getErrorMessage(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            return params.getErrmsg();
        }
        return null;
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

    private ResponseParams getSucessStatus() {
        ResponseParams params = new ResponseParams();
        params.setErr("0");
        params.setStatus(StatusType.successful.name());
        params.setErrmsg("Operation successful");
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
