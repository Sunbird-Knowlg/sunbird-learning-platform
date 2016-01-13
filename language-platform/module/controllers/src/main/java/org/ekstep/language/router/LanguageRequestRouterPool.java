package org.ekstep.language.router;

import java.util.concurrent.TimeUnit;

import org.ekstep.language.router.LanguageRequestRouter;

import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ServerException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;

public class LanguageRequestRouterPool {
    
    private static ActorSystem system;
    private static ActorRef actor;
    private static int count = 5;

    private static final String DEFAULT_SYSTEM_NAME = "LanguageActorSystem";

    public static long REQ_TIMEOUT = 30000;
    public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    
    static {
        Config config = ConfigFactory.load();
        system = ActorSystem.create(DEFAULT_SYSTEM_NAME, config.getConfig(DEFAULT_SYSTEM_NAME));
        Props actorProps = Props.create(LanguageRequestRouter.class);
        actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
        actor.tell("init", system.deadLetters());
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
