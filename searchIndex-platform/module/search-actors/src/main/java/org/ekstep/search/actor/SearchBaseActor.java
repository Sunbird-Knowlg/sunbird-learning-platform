package org.ekstep.search.actor;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public abstract class SearchBaseActor extends UntypedActor {

    private static Logger LOGGER = LogManager.getLogger(SearchBaseActor.class.getName());

    @CoverageIgnore
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Request) {
            Request request = (Request) message;
            invokeMethod(request, getSender());
        } else if (message instanceof Response) {
            // do nothing
        } else {
            unhandled(message);
        }
    }

    protected abstract void invokeMethod(Request request, ActorRef parent);

    public void OK(String responseIdentifier, Object vo, ActorRef parent) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getSucessStatus());
        parent.tell(response, getSelf());
    }

    @CoverageIgnore
    public void OK(Map<String, Object> responseObjects, ActorRef parent) {
        Response response = new Response();
        if (null != responseObjects && responseObjects.size() > 0) {
            for (Entry<String, Object> entry : responseObjects.entrySet()) {
                response.put(entry.getKey(), entry.getValue());
            }
        }
        response.setParams(getSucessStatus());
        parent.tell(response, getSelf());
    }

    @CoverageIgnore
    public void ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, Object vo, ActorRef parent) {
        LOGGER.error(errorCode + ", " + errorMessage);
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        parent.tell(response, getSelf());
    }

    @CoverageIgnore
    public void handleException(Throwable e, ActorRef parent) {
        LOGGER.error(e.getMessage(), e);
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr(GraphEngineErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        params.setErrmsg(e.getClass().getName() + ": " + e.getMessage());
        response.setParams(params);
        setResponseCode(response, e);
        parent.tell(response, getSelf());
    }

    private ResponseParams getSucessStatus() {
        ResponseParams params = new ResponseParams();
        params.setErr("0");
        params.setStatus(StatusType.successful.name());
        params.setErrmsg("Operation successful");
        return params;
    }

    @CoverageIgnore
    private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

    @CoverageIgnore
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
