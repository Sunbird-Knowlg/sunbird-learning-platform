package org.esktep.search.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.search.router.SearchRequestRouterPool;

import com.ilimi.common.dto.CoverageIgnore;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseSearchManager, provides functionality to call search actor operations.
 *
 * @author karthik
 */
public class BaseSearchManager {

	/**
	 * Sets the search context.
	 *
	 * @param request the request
	 * @param manager the manager
	 * @param operation the operation
	 * @return the request
	 */
	protected Request setSearchContext(Request request, String manager, String operation) {
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }

    /**
     * Gets the search request.
     *
     * @param manager the manager
     * @param operation the operation
     * @return the search request
     */
    protected Request getSearchRequest(String manager, String operation) {
        Request request = new Request();
        return setSearchContext(request, manager, operation);
    }

    /**
     * Gets the search response.
     *
     * @param request the request
     * @param logger the logger
     * @return the search response
     */
    @CoverageIgnore
	protected Response getSearchResponse(Request request) {
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
            PlatformLogger.log("Exception", e.getMessage(), e);
            return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
        }
    }

    /**
     * Error.
     *
     * @param errorCode the error code
     * @param errorMessage the error message
     * @param responseCode the response code
     * @return the response
     */
    @CoverageIgnore
    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }

    /**
     * Check error.
     *
     * @param response the response
     * @return true, if successful
     */
    @CoverageIgnore
    protected boolean checkError(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the error message.
     *
     * @param response the response
     * @return the error message
     */
    @CoverageIgnore
    protected String getErrorMessage(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            return params.getErrmsg();
        }
        return null;
    }

    /**
     * Gets the error status.
     *
     * @param errorCode the error code
     * @param errorMessage the error message
     * @return the error status
     */
    @CoverageIgnore
    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

}
