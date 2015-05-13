package com.ilimi.graph.engine.router;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.BaseRequestRouter;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.exception.RequestRouterErrorCodes;

/**
 * Actor to handle all requests to DAC actors. A pool of DAC actors is created
 * on startup and all subsequent requests are processed using the actors from
 * the pool.
 * 
 * @author rayulu
 * 
 */
public class DACRequestRouter extends BaseRequestRouter {

    /**
     * Initialize the DAC actor pool.
     */
    protected void initActorPool() {

    }

    /**
     * Get the ActorRef corresponding to the input graph id and manager name. If
     * no actor pool is configured for the given graph id, actor from the
     * default pool is returned. If no actor is found for the input manager
     * name, an exception is thrown.
     * 
     * @param request
     * @return
     */
    protected ActorRef getActorFromPool(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        if (StringUtils.isBlank(graphId))
            throw new ClientException(RequestRouterErrorCodes.ERR_ROUTER_INVALID_GRAPH_ID.name(), "GraphId cannot be empty");
        String manager = request.getManagerName();
        ActorRef ref = GraphDACActorPoolMgr.getActorRefFromPool(graphId, manager);
        if (null == ref)
            throw new ClientException(RequestRouterErrorCodes.ERR_ROUTER_ACTOR_NOT_FOUND.name(),
                    "Actor not found in the pool for manager: " + manager);
        return ref;
    }

}
