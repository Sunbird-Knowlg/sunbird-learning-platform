package com.ilimi.orchestrator.router;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.router.LanguageRequestRouterPool;

import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.orchestrator.dac.model.ActorPath;
import com.ilimi.orchestrator.dac.model.RequestRouters;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class ServiceLocator {

	public static ActorSelection getActorPath(Map<String, Object> context) {
		// TODO: do a lookup and get the actor path
		return null;
	}

	public static ActorRef getActorRef(ActorPath actorPath, String graphId) {
		if (StringUtils.equalsIgnoreCase(RequestRouters.LANGUAGE_REQUEST_ROUTER.name(), actorPath.getRouter()))
			return LanguageRequestRouterPool.getRequestRouter();
		List<String> graphIds = Configuration.graphIds;
		if (null != graphIds && !graphIds.isEmpty()) {
			if (StringUtils.isNotBlank(graphId) && !graphIds.contains(graphId))
				throw new ServerException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(),
						graphId + " not supported by this service");
		}
		return RequestRouterPool.getRequestRouter();
	}
}
