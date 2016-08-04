package org.ekstep.search.router;



import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;

import com.ilimi.common.exception.ServerException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;

public class SearchRequestRouterPool {
    
	private static ActorSystem system;
    private static ActorRef actor;
    private static int count = 5;
    private static final String DEFAULT_SYSTEM_NAME = "ActorSystem";

    
    public static void init() {
    	Config config = ConfigFactory.load();
        system =  ActorSystem.create(DEFAULT_SYSTEM_NAME, config.getConfig(DEFAULT_SYSTEM_NAME));;
        Props actorProps = Props.create(SearchRequestRouter.class);
        actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
        actor.tell("init", system.deadLetters());
    }
    
    public static void init(ActorSystem actorSystem) {
        system = actorSystem;
    	Props actorProps = Props.create(SearchRequestRouter.class);
        actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
        actor.tell("init", system.deadLetters());
    }
    
    public static ActorSystem getActorSystem() {
        if (null == system) {
        	throw new ServerException(CompositeSearchErrorCodes.ERR_SYSTEM_ACTOR_NOT_CREATED.name(), "Actor System not created");
        }
        return system;
    }
    
    public static ActorRef getRequestRouter() {
        if (null == actor) {
            throw new ServerException(CompositeSearchErrorCodes.ERR_ROUTER_ACTOR_NOT_FOUND.name(), "Request Router not found");
        }
        return actor;
    }
    
    public static void destroy() {
    	actor = null;
    }
}
