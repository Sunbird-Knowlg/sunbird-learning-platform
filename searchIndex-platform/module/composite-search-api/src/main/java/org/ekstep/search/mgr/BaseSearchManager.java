package org.ekstep.search.mgr;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.search.router.SearchRequestRouterPool;

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

    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

}
