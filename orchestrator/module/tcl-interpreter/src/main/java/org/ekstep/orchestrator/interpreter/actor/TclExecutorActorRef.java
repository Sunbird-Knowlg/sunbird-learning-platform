package org.ekstep.orchestrator.interpreter.actor;

import java.util.List;
import java.util.Map;

import org.ekstep.graph.engine.router.ActorBootstrap;

import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.orchestrator.dac.model.OrchestratorScript;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;

public class TclExecutorActorRef {

    public static final int poolSize = 8;

    private static ActorRef actorRef;

    public static void initExecutorActor(List<OrchestratorScript> commands) {
    	
        ActorSystem system = RequestRouterPool.getActorSystem();
        Map<String, Integer> actorMap = ActorBootstrap.getActorCountMap();
    	Integer count = actorMap.get("TclExecutor");
    	if (null == count)
    		count = poolSize;
    	System.out.println("Creating " + count + " TclExecutor actors");
        Props actorProps = Props.create(TclExecutorActor.class, commands);
        actorRef = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
    }

    public static ActorRef getRef() {
        return TclExecutorActorRef.actorRef;
    }
}
