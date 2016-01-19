package com.ilimi.orchestrator.interpreter.actor;

import java.util.List;

import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;

public class TclExecutorActorRef {

    public static final int poolSize = 8;

    private static ActorRef actorRef;

    public static void initExecutorActor(List<OrchestratorScript> commands) {
        ActorSystem system = RequestRouterPool.getActorSystem();
        Props actorProps = Props.create(TclExecutorActor.class, commands);
        actorRef = system.actorOf(new SmallestMailboxPool(poolSize).props(actorProps));
    }

    public static ActorRef getRef() {
        return TclExecutorActorRef.actorRef;
    }
}
