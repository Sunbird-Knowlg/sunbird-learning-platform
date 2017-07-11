package org.ekstep.language.router;

import java.util.concurrent.TimeUnit;

import org.ekstep.language.common.enums.LanguageErrorCodes;

import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;

public class LanguageRequestRouterPool {
    
    private static ActorRef actor;
    private static int count = 5;

    public static long REQ_TIMEOUT = 30000;
    public static long BULK_REQ_TIMEOUT = 120000;
    public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    public static Timeout BULK_WAIT_TIMEOUT = new Timeout(Duration.create(2, TimeUnit.MINUTES));
    
    public static void init() {
        ActorSystem system = RequestRouterPool.getActorSystem();
        Props actorProps = Props.create(LanguageRequestRouter.class);
        actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
        actor.tell("init", system.deadLetters());
    }
    
    public static ActorRef getRequestRouter() {
        if (null == actor) {
            throw new ServerException(LanguageErrorCodes.ERR_LANGUAGE_REQUEST_ROUTER_NOT_FOUND.name(), "Request Router not found");
        }
        return actor;
    }
}
