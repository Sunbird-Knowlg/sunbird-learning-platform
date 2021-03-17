package org.sunbird.common.router;

import java.util.concurrent.TimeUnit;

import org.sunbird.common.Platform;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.engine.router.ActorBootstrap;
import org.sunbird.graph.engine.router.RequestRouter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;


public class RequestRouterPool {

    private static ActorSystem system;
    private static ActorRef actor;
    private static int count = 5;

    public static long REQ_TIMEOUT = Platform.config.hasPath("akka.request_timeout")? (Platform.config.getLong
            ("akka.request_timeout") * 1000): 30000;
    public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(REQ_TIMEOUT, TimeUnit.MILLISECONDS));

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
