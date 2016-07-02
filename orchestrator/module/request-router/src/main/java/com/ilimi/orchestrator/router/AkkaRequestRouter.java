package com.ilimi.orchestrator.router;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.orchestrator.dac.model.ActorPath;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class AkkaRequestRouter {

	public static final long timeout = 30000;
	public static final Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));

	public static Response sendRequest(Request request, ActorPath actorPath) {
		if (StringUtils.isBlank(request.getManagerName()) || StringUtils.isBlank(request.getOperation()))
			throw new MiddlewareException(RouterErrorCodes.ERR_EXEC_INVALID_REQUEST.name(),
					"Manager and operation cannot be blank in the request");
		try {
			String graphId = (String) request.getContext().get(GraphDACParams.graph_id.name());
			ActorRef actor = ServiceLocator.getActorRef(actorPath, graphId);
			Future<Object> future = Patterns.ask(actor, request, timeout);
			Object result = Await.result(future, WAIT_TIMEOUT.duration());
			if (result instanceof Response) {
				return (Response) result;
			} else {
				throw new MiddlewareException(RouterErrorCodes.ERR_EXEC_SYSTEM_ERROR.name(),
						request.getOperation() + " returned an invalid response");
			}
		} catch (MiddlewareException e) {
			throw e;
		} catch (Exception e) {
			throw new MiddlewareException(RouterErrorCodes.ERR_EXEC_SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}
	
	public static Response sendRequestAsync(Request request, ActorPath actorPath) {
        if (StringUtils.isBlank(request.getManagerName()) || StringUtils.isBlank(request.getOperation()))
            throw new MiddlewareException(RouterErrorCodes.ERR_EXEC_INVALID_REQUEST.name(),
                    "Manager and operation cannot be blank in the request");
        try {
        	String graphId = (String) request.getContext().get(GraphDACParams.graph_id.name());
            ActorRef actor = ServiceLocator.getActorRef(actorPath, graphId);
            actor.tell(request, actor);
            Response response = new Response();
            ResponseParams params = new ResponseParams();
            params.setErr("0");
            params.setStatus(StatusType.successful.name());
            response.setParams(params);
            return response;
        } catch (MiddlewareException e) {
            throw e;
        } catch (Exception e) {
            throw new MiddlewareException(RouterErrorCodes.ERR_EXEC_SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
}
