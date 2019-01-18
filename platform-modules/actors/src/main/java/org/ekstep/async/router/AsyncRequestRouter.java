package org.ekstep.async.router;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.FromConfig;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ClientException;
import org.ekstep.telemetry.logger.TelemetryManager;

import static org.ekstep.async.router.AsyncActorPool.addActorRefToPool;
import static org.ekstep.async.router.AsyncRequestRouterPool.getActors;

/**
 * Routing Actor to send {@link ActorRef#tell(Object, ActorRef)} a message to Async Actors
 *
 * @see AsyncActorPool
 * @see AsyncRequestRouterPool
 */
public class AsyncRequestRouter extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        if (message instanceof String) {
            if (StringUtils.equalsIgnoreCase("init", message.toString())) {
                initActorPool();
            } else {
                getSender().tell(message, getSelf());
            }
        } else if (message instanceof Request) {
            Request request = (Request) message;
            try {
                ActorRef actorRef = getActorFromPool(request);
                actorRef.tell(request, self());
            } catch (Exception e) {
                TelemetryManager.error("AsyncActor#onReceive | Exception occurred while executing requesting");
            }
        }
    }

    /**
     * Initializes the actors and add to actor pool {@link AsyncActorPool}
     *
     * @see AsyncActorPool#addActorRefToPool(String, ActorRef)
     */
    private void initActorPool() {
        getActors().forEach((actorClass, init) ->
                        addActorRefToPool(actorClass.getSimpleName(), createAndInitActor(actorClass, init)));
    }

    private ActorRef createAndInitActor(Class<Actor> actorClass, boolean init) {
        ActorSystem system = AsyncRequestRouterPool.getActorSystem();
        ActorRef actor = system.actorOf(FromConfig.getInstance().props(Props.create(actorClass)), actorClass.getSimpleName());
        if (init) actor.tell("init", system.deadLetters());
        return actor;
    }

    /**
     * Get Actor From Pool {@link AsyncActorPool#getActorRefFromPool(String)}
     *
     * @see AsyncActorPool
     * @param request
     * @return
     */
    private ActorRef getActorFromPool(Request request) {
        String manager = request.getManagerName();
        ActorRef ref = AsyncActorPool.getActorRefFromPool(manager);
        if (null == ref) {
            TelemetryManager.error(
                    "AsyncRequestRouter#getActorFromPool | Actor not found in the pool for manager: " + manager);
            throw new ClientException(
                    "ERR_ROUTER_ACTOR_NOT_FOUND", "Actor not found in the pool for manager: " + manager);
        }
        return ref;
    }

}
