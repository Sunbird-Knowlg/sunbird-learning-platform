package org.ekstep.language.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * The Class DefinitionDTOCache, caches definitions from graph
 *
 * @author amaranath
 */
public class DefinitionDTOCache {

	/** The logger. */
	

	/**
	 * Gets the definition DTO.
	 *
	 * @param definitionName
	 *            the definition name
	 * @param graphId
	 *            the graph id
	 * @return the definition DTO
	 */
	public static DefinitionDTO getDefinitionDTO(String definitionName, String graphId) {
		Map<String, DefinitionDTO> defintionMap = null;
		if (defintionMap == null) {
			defintionMap = new HashMap<String, DefinitionDTO>();
		}
		DefinitionDTO definition = defintionMap.get(definitionName);
		if (definition == null) {
			Request requestDefinition = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
					GraphDACParams.object_type.name(), definitionName);
			Response responseDefiniton = getResponse(requestDefinition);
			if (checkError(responseDefiniton)) {
				return null;
			} else {
				definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
				defintionMap.put(definitionName, definition);
			}
		}
		return definition;
	}

	/**
	 * Gets the error status.
	 *
	 * @param errorCode
	 *            the error code
	 * @param errorMessage
	 *            the error message
	 * @return the error status
	 */
	private static ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	/**
	 * Error.
	 *
	 * @param errorCode
	 *            the error code
	 * @param errorMessage
	 *            the error message
	 * @param responseCode
	 *            the response code
	 * @return the response
	 */
	private static Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	/**
	 * Gets the response.
	 *
	 * @param request
	 *            the request
	 * @param logger
	 *            the logger
	 * @return the response
	 */
	private static Response getResponse(Request request) {
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
			TelemetryManager.log("Exception", e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	/**
	 * Sets the context.
	 *
	 * @param request
	 *            the request
	 * @param graphId
	 *            the graph id
	 * @param manager
	 *            the manager
	 * @param operation
	 *            the operation
	 * @return the request
	 */
	private static Request setContext(Request request, String graphId, String manager, String operation) {
		request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	/**
	 * Gets the request.
	 *
	 * @param graphId
	 *            the graph id
	 * @param manager
	 *            the manager
	 * @param operation
	 *            the operation
	 * @return the request
	 */
	private static Request getRequest(String graphId, String manager, String operation) {
		Request request = new Request();
		return setContext(request, graphId, manager, operation);
	}

	/**
	 * Gets the request.
	 *
	 * @param graphId
	 *            the graph id
	 * @param manager
	 *            the manager
	 * @param operation
	 *            the operation
	 * @param paramName
	 *            the param name
	 * @param vo
	 *            the vo
	 * @return the request
	 */
	private static Request getRequest(String graphId, String manager, String operation, String paramName, Object vo) {
		Request request = getRequest(graphId, manager, operation);
		request.put(paramName, vo);
		return request;
	}

	/**
	 * Check error.
	 *
	 * @param response
	 *            the response
	 * @return true, if successful
	 */
	private static boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sync defintion.
	 *
	 * @param definitionName
	 *            the definition name
	 * @param graphId
	 *            the graph id
	 * @throws Exception
	 *             the exception
	 */
	public static void syncDefintion(String definitionName, String graphId) throws Exception {
		Map<String, DefinitionDTO> defintionMap = null;
		if (defintionMap == null) {
			defintionMap = new HashMap<String, DefinitionDTO>();
		}
		DefinitionDTO definition = defintionMap.get(definitionName);
		Request requestDefinition = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), definitionName);
		Response responseDefiniton = getResponse(requestDefinition);
		if (checkError(responseDefiniton)) {
			throw new Exception("Unable to sync definition: " + definitionName + " for graph: " + graphId);
		} else {
			definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
			defintionMap.put(definitionName, definition);
			//definitionCache.put(graphId, defintionMap);
		}
	}
}
