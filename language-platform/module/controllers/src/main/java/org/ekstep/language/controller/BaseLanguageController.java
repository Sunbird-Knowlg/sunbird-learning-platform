package org.ekstep.language.controller;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.telemetry.logger.PlatformLogger;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public abstract class BaseLanguageController extends BaseController {
    
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
    
	protected Response getResponse(Request request) {
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
            PlatformLogger.log("Exception", e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }   
    }
    
	protected Response getNonLanguageResponse(Request request) {
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
            PlatformLogger.log("Exception",e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }   
    }
    
	public void makeAsyncRequest(Request request) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            router.tell(request, router);
        } catch (Exception e) {
        	PlatformLogger.log("Exception", e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
    
	protected Response getBulkOperationResponse(Request request) {
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
            PlatformLogger.log("Exception", e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Something went wrong while processing the request", e);
        }   
    }
    
    protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }
    
    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }
}
