package com.ilimi.orchestrator.router;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.router.LanguageRequestRouterPool;

import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.orchestrator.dac.model.ActorPath;
import com.ilimi.orchestrator.dac.model.RequestRouters;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class ServiceLocator {

    public static ActorSelection getActorPath(Map<String, Object> context) {
        // TODO: do a lookup and get the actor path
        return null;
    }

    public static ActorRef getActorRef(ActorPath actorPath) {
        if (StringUtils.equalsIgnoreCase(RequestRouters.LANGUAGE_REQUEST_ROUTER.name(), actorPath.getRouter()))
            return LanguageRequestRouterPool.getRequestRouter();
        return RequestRouterPool.getRequestRouter();
    }
}
