package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.router.LanguageRequestRouterPool;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.dac.model.Node;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * The Class BaseLanguageController provides operations to create message requests and receive responses for the Akka actor routers.
 * 
 * @author Rayulu
 * 
 */
public abstract class BaseLanguageController extends BaseController {
    
    /**
     * Creates and returns the request object.
     *
     * @param requestMap the request map
     * @return the request object
     */
    protected Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        Map<String, Object> map = request.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        if (null != map && !map.isEmpty()) {
            try {
                Object obj = map.get("content");
                if (null != obj) {
                    Node content = (Node) mapper.convertValue(obj, Node.class);
                    request.put("content", content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }
    
    /**
     * Gets the response from the Language akka actors.
     *
     * @param request the request
     * @param logger the logger
     * @return the response
     */
    protected Response getResponse(Request request, Logger logger) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            Future<Object> future = Patterns.ask(router, request, LanguageRequestRouterPool.REQ_TIMEOUT);
            Object obj = Await.result(future, LanguageRequestRouterPool.WAIT_TIMEOUT.duration());
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
    
    /**
     * Gets the response from the non language akka actors.
     *
     * @param request the request
     * @param logger the logger
     * @return the non language response
     */
    protected Response getNonLanguageResponse(Request request, Logger logger) {
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
    
    /**
     * Makes a async request to the router.
     *
     * @param request the request
     * @param logger the logger
     */
    public void makeAsyncRequest(Request request, Logger logger) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            router.tell(request, router);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
    
    /**
     * Gets the response from a bulk operation request.
     *
     * @param request the request
     * @param logger the logger
     * @return the bulk operation response
     */
    protected Response getBulkOperationResponse(Request request, Logger logger) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            request.getContext().put(LanguageParams.bulk_request.name(), true);
            Future<Object> future = Patterns.ask(router, request, LanguageRequestRouterPool.BULK_REQ_TIMEOUT);
            Object obj = Await.result(future, LanguageRequestRouterPool.BULK_WAIT_TIMEOUT.duration());
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
    
    /**
     * Returns an Error response.
     *
     * @param errorCode the error code
     * @param errorMessage the error message
     * @param responseCode the response code
     * @return the response
     */
    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }
    
    /**
     * Gets the error status.
     *
     * @param errorCode the error code
     * @param errorMessage the error message
     * @return the error status
     */
    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }
}
