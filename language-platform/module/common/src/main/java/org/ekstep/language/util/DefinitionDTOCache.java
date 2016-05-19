package org.ekstep.language.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class DefinitionDTOCache {

	private static Logger LOGGER = LogManager.getLogger(DefinitionDTOCache.class.getName());
	private static Map<String, Map<String, DefinitionDTO>> definitionCache = new HashMap<String, Map<String, DefinitionDTO>>();

	public static DefinitionDTO getDefinitionDTO(String definitionName, String graphId) {
		Map<String, DefinitionDTO> defintionMap = definitionCache.get(graphId);
		if (defintionMap == null) {
			defintionMap = new HashMap<String, DefinitionDTO>();
		}
		DefinitionDTO definition = defintionMap.get(definitionName);
		if (definition == null) {
			Request requestDefinition = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
					GraphDACParams.object_type.name(), definitionName);
			Response responseDefiniton = getResponse(requestDefinition, LOGGER);
			if (checkError(responseDefiniton)) {
				return null;
			} else {
				definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
				defintionMap.put(definitionName, definition);
				definitionCache.put(graphId, defintionMap);
			}
		}
		return definition;
	}

	private static ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	private static Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	private static Response getResponse(Request request, Logger logger) {
		ActorRef router = RequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				return (Response) obj;
			} else {
				return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	private static Request setContext(Request request, String graphId, String manager, String operation) {
		request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	private static Request getRequest(String graphId, String manager, String operation) {
		Request request = new Request();
		return setContext(request, graphId, manager, operation);
	}

	private static Request getRequest(String graphId, String manager, String operation, String paramName, Object vo) {
		Request request = getRequest(graphId, manager, operation);
		request.put(paramName, vo);
		return request;
	}

	private static boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}

	public static void syncDefintion(String definitionName, String graphId) throws Exception {
		Map<String, DefinitionDTO> defintionMap = definitionCache.get(graphId);
		if (defintionMap == null) {
			defintionMap = new HashMap<String, DefinitionDTO>();
		}
		DefinitionDTO definition = defintionMap.get(definitionName);
		Request requestDefinition = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), definitionName);
		Response responseDefiniton = getResponse(requestDefinition, LOGGER);
		if (checkError(responseDefiniton)) {
			throw new Exception("Unable to sync definition: " + definitionName + " for graph: " + graphId);
		} else {
			definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
			defintionMap.put(definitionName, definition);
			definitionCache.put(graphId, defintionMap);
		}
	}
}
