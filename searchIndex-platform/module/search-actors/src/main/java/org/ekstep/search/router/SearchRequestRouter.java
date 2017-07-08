package org.ekstep.search.router;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.search.actor.DefinitionSyncScheduler;
import org.ekstep.search.actor.HealthCheckManager;
import org.ekstep.search.actor.SearchManager;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxPool;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.LoggerEnum;
import com.ilimi.common.util.PlatformLogManager;

public class SearchRequestRouter extends UntypedActor{

    private static ILogger LOGGER = PlatformLogManager.getLogger();
    protected long timeout = 30000;
    
	@Override
	public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            if (StringUtils.equalsIgnoreCase("init", message.toString())) {
                initActorPool();
                DefinitionSyncScheduler.init();
                getSender().tell("initComplete", getSelf());
            } else {
                getSender().tell(message, getSelf());
            }
        } else if (message instanceof Request) {
            Request request = (Request) message;
            ActorRef parent = getSender();
            try {
                ActorRef actorRef = getActorFromPool(request);
                long t = timeout;
                Future<Object> future = Patterns.ask(actorRef, request, t);
                handleFuture(request, future, parent);
            } catch (Exception e) {
                handleException(request, e, parent);
            }
        }
	}

	private void initActorPool() {
		ActorSystem system = RequestRouterPool.getActorSystem();
        int poolSize = 5;
        Props SearchProps = Props.create(SearchManager.class);
        ActorRef searchMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(SearchProps));
        SearchActorPool.addActorRefToPool(SearchActorNames.SEARCH_MANAGER.name(), searchMgr);
        
        Props healthCheckProps = Props.create(HealthCheckManager.class);
        ActorRef healthCheckMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(healthCheckProps));
        SearchActorPool.addActorRefToPool(SearchActorNames.HEALTH_CHECK_MANAGER.name(), healthCheckMgr);

	}
	
	private ActorRef getActorFromPool(Request request) {
        String manager = request.getManagerName();
        ActorRef ref = SearchActorPool.getActorRefFromPool(manager);
        if (null == ref)
            throw new ClientException(CompositeSearchErrorCodes.ERR_ROUTER_ACTOR_NOT_FOUND.name(),
                    "Actor not found in the pool for manager: " + manager);
        return ref;
    }

    protected void handleFuture(final Request request, Future<Object> future, final ActorRef parent) {
        future.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object arg0) throws Throwable {
                parent.tell(arg0, getSelf());
                Response res = (Response) arg0;
                ResponseParams params = res.getParams();
                LOGGER.log(
                        request.getManagerName()  , request.getOperation() + ", SUCCESS, " + params.toString());
            }
        }, getContext().dispatcher());

        future.onFailure(new OnFailure() {
            @Override
            public void onFailure(Throwable e) throws Throwable {
                handleException(request, e, parent);
            }
        }, getContext().dispatcher());
    }

    protected void handleException(final Request request, Throwable e, final ActorRef parent) {
        LOGGER.log(request.getManagerName() + "," + request.getOperation() + ", ERROR: " + e.getMessage(), LoggerEnum.WARN.name());
        Response response = new Response();
        ResponseParams params = new ResponseParams();
        params.setStatus(StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException mwException = (MiddlewareException) e;
            params.setErr(mwException.getErrCode());
        } else {
            params.setErr(CompositeSearchErrorCodes.ERR_SYSTEM_EXCEPTION.name());
        }
        params.setErrmsg(e.getMessage());
        response.setParams(params);
        setResponseCode(response, e);
        parent.tell(response, getSelf());
    }

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
