package org.ekstep.orchestrator.router;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.graph.common.exception.GraphEngineErrorCodes;
import org.ekstep.graph.common.mgr.Configuration;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.orchestrator.dac.model.ActorPath;
import org.ekstep.orchestrator.dac.model.RequestRouters;
import org.ekstep.search.router.SearchRequestRouterPool;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public class ServiceLocator {

	public static ActorSelection getActorPath(Map<String, Object> context) {
		// TODO: do a lookup and get the actor path
		return null;
	}

	public static ActorRef getActorRef(ActorPath actorPath, String graphId) {
		if (StringUtils.equalsIgnoreCase(RequestRouters.SEARCH_REQUEST_ROUTER.name(), actorPath.getRouter()))
			return SearchRequestRouterPool.getRequestRouter();
		else if (StringUtils.equalsIgnoreCase(RequestRouters.LANGUAGE_REQUEST_ROUTER.name(), actorPath.getRouter()))
 			return LanguageRequestRouterPool.getRequestRouter();
		else if (StringUtils.equalsIgnoreCase(RequestRouters.LEARNING_REQUEST_ROUTER.name(), actorPath.getRouter()))
			return LearningRequestRouterPool.getRequestRouter();
		List<String> graphIds = Configuration.graphIds;
		if (null != graphIds && !graphIds.isEmpty()) {
			if (StringUtils.isNotBlank(graphId) && !graphIds.contains(graphId))
				throw new ServerException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(),
						graphId + " not supported by this service");
		}
		return RequestRouterPool.getRequestRouter();
	}
}
