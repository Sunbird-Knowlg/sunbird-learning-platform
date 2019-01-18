package org.ekstep.async.router;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Initializes the Actor System and creates {@link AsyncRequestRouter}
 *
 * @see AsyncRequestRouter
 */
public class AsyncRequestRouterPool {

    private static final String DEFAULT_SYSTEM_NAME = "AsyncActorSystem";
    private static ActorSystem system;
    private static ActorRef actor;
    private static int nr = 5;
    private static Map<Class<Actor>, Boolean> actors;

    static { setActors(); }

    public static void init(ActorSystem system) {
        AsyncRequestRouterPool.system = system;
        init();
    }

    public static void init() {
        if (null == system)
            system = ActorSystem.create(DEFAULT_SYSTEM_NAME, Platform.config.getConfig(DEFAULT_SYSTEM_NAME));
        Props actorProps = Props.create(AsyncRequestRouter.class);
        actor = system.actorOf(new SmallestMailboxPool(nr).props(actorProps));
        actor.tell("init", system.deadLetters());
    }

    public static ActorSystem getActorSystem() {
        if (null == system)
            throw new ServerException("ERR_DEFAULT_ACTOR_SYSTEM_NOT_CREATED", "Actor system is not initialised");
        return system;
    }

    public static ActorRef getRequestRouter() {
        if (null == actor) {
            throw new ServerException("ERR_DEFAULT_REQUEST_ROUTER_NOT_FOUND", "Async Request Router not found");
        }
        return actor;
    }

    public static Map<Class<Actor>, Boolean> getActors() { return actors; }

    public static void setActors() {
        Map<String, Object> actorMap =
                Platform.config.hasPath("async.router.actors")
                        ? Platform.config.getObject("async.router.actors").unwrapped()
                        : Collections.emptyMap();

        if (!actorMap.isEmpty()) {
            actors = new LinkedHashMap<>();
            actorMap.entrySet().stream().forEach(e -> actors.put(getClass(e.getKey()), (Boolean) e.getValue()));
        }
    }

    private static Class getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            TelemetryManager.error("AsyncRequestRouterPool#setActors | " +
                    "Error getting class from name , Class with given Name not present");
            throw new ServerException("ERR_GETTING_CLASS_FROM_NAME",
                    "Error getting class from name , Class with given Name not present", e);
        }
    }

}
