package org.sunbird.graph.engine.router;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.BaseRequestRouter;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.exception.RequestRouterErrorCodes;

import akka.actor.ActorRef;

/**
 * Actor to handle all requests to Graph Engine. A pool of Graph Engine actors
 * and DAC Request Router actor is created on startup and all subsequent
 * requests are processed using the actors from the pool.
 * 
 * @author rayulu
 * 
 */
public class RequestRouter extends BaseRequestRouter {

    /**
     * Initializes the Graph Engine actor pool and DAC Request router actor.
     * Sends a message to DAC Request Router to initialize the DAC actor pool.
     */
    protected void initActorPool() {
        ActorBootstrap.loadConfiguration();
    }

    protected ActorRef getActorFromPool(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        if (StringUtils.isBlank(graphId))
            throw new ClientException(RequestRouterErrorCodes.ERR_ROUTER_INVALID_GRAPH_ID.name(), "GraphId cannot be empty");
        String manager = request.getManagerName();
        ActorRef ref = GraphEngineActorPoolMgr.getActorRefFromPool(graphId, manager);
        if (null == ref)
            throw new ClientException(RequestRouterErrorCodes.ERR_ROUTER_ACTOR_NOT_FOUND.name(),
                    "Actor not found in the pool for manager: " + manager);
        return ref;
    }

}
