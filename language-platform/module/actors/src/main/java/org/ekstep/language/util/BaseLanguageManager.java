package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.telemetry.logger.PlatformLogger;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * The Class BaseLanguageManager provides operations to manipulate akka requests
 * and responses.
 * 
 * @author Rayulu
 */
public abstract class BaseLanguageManager extends BaseManager {

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
	protected Request setLanguageContext(Request request, String graphId, String manager, String operation) {
		request.getContext().put(LanguageParams.language_id.name(), graphId);
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	/**
	 * Gets the request from the Language request router.
	 *
	 * @param graphId
	 *            the graph id
	 * @param manager
	 *            the manager
	 * @param operation
	 *            the operation
	 * @return the language request
	 */
	protected Request getLanguageRequest(String graphId, String manager, String operation) {
		Request request = new Request();
		return setLanguageContext(request, graphId, manager, operation);
	}

	/**
	 * Makes an async request to the Language request router.
	 *
	 * @param request
	 *            the request
	 * @param logger
	 *            the logger
	 */
	public void makeAsyncLanguageRequest(Request request) {
		ActorRef router = LanguageRequestRouterPool.getRequestRouter();
		try {
			router.tell(request, router);
		} catch (Exception e) {
			PlatformLogger.log("Exception",e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	/**
	 * Gets the response from the Language request router.
	 *
	 * @param request
	 *            the request
	 * @param logger
	 *            the logger
	 * @return the language response
	 */
	protected Response getLanguageResponse(Request request) {
		ActorRef router = LanguageRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, LanguageRequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, LanguageRequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				return (Response) obj;
			} else {
				return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			PlatformLogger.log("Exception Occured", e.getMessage(), e);
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	/**
	 * Gets all responses for the list of requests and accumulates them as a single response.
	 *
	 * @param requests
	 *            the requests
	 * @param logger
	 *            the logger
	 * @param paramName
	 *            the param name to be fetched from each response
	 * @param returnParam
	 *            the final param name of the accumulated responses
	 * @return the language response
	 */
	protected Response getLanguageResponse(List<Request> requests, String paramName,
			String returnParam) {
		if (null != requests && !requests.isEmpty()) {
			ActorRef router = LanguageRequestRouterPool.getRequestRouter();
			try {
				List<Future<Object>> futures = new ArrayList<Future<Object>>();
				for (Request request : requests) {
					Future<Object> future = Patterns.ask(router, request, LanguageRequestRouterPool.REQ_TIMEOUT);
					futures.add(future);
				}
				Future<Iterable<Object>> objects = Futures.sequence(futures,
						RequestRouterPool.getActorSystem().dispatcher());
				Iterable<Object> responses = Await.result(objects, LanguageRequestRouterPool.WAIT_TIMEOUT.duration());
				if (null != responses) {
					List<Object> list = new ArrayList<Object>();
					Response response = new Response();
					for (Object obj : responses) {
						if (obj instanceof Response) {
							Response res = (Response) obj;
							if (!checkError(res)) {
								Object vo = res.get(paramName);
								response = copyResponse(response, res);
								if (null != vo) {
									list.add(vo);
								}
							} else {
								return res;
							}
						} else {
							return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error",
									ResponseCode.SERVER_ERROR);
						}
					}
					response.put(returnParam, list);
					return response;
				} else {
					return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
				}
			} catch (Exception e) {
				PlatformLogger.log("Exception", e.getMessage(), e);
				throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
			}
		} else {
			return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
		}
	}
}