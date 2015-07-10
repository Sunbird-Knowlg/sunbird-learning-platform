package com.ilimi.common.router;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;

import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.engine.router.ActorBootstrap;
import com.ilimi.graph.engine.router.RequestRouter;


public class RequestRouterPool {

    private static ActorSystem system;
    private static ActorRef actor;
    private static int count = 5;

    public static long REQ_TIMEOUT = 30000;
    public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));

    static {
        system = ActorBootstrap.getActorSystem();
        Props actorProps = Props.create(RequestRouter.class);
        actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
    }

    public static ActorSystem getActorSystem() {
        if (null == system)
            throw new ServerException(TaxonomyErrorCodes.ERR_TAXONOMY_ACTOR_SYSTEM_NOT_CREATED.name(), "Actor system is not initialised");
        return system;
    }

    public static ActorRef getRequestRouter() {
        if (null == actor) {
            throw new ServerException(TaxonomyErrorCodes.ERR_TAXONOMY_REQUEST_ROUTER_NOT_FOUND.name(), "Request Router not found");
        }
        return actor;
    }

}
