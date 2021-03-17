package org.sunbird.learning.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.common.router.RequestRouterPool;
import org.sunbird.learning.common.enums.LearningErrorCodes;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseLearningManager, provides basic functionality to handle
 * learning request for learning actors
 *
 * @author karthik
 */
public abstract class BaseLearningManager extends BaseManager {
	/**
	 * Sets the context.
	 *
	 * @param request
	 *            the request
	 * @param manager
	 *            the manager
	 * @param operation
	 *            the operation
	 * @return the request
	 */
	protected Request setLearningContext(Request request, String manager, String operation) {
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	/**
	 * Gets the request from the Learning request router.
	 *
	 * @param manager
	 *            the manager
	 * @param operation
	 *            the operation
	 * @return the language request
	 */
	protected Request getLearningRequest(String manager, String operation) {
		Request request = new Request();
		return setLearningContext(request, manager, operation);
	}

	/**
	 * Makes an async request to the Learning request router.
	 *
	 * @param request
	 *            the request
	 * @param logger
	 *            the logger
	 */
	public void makeAsyncLearningRequest(Request request) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			router.tell(request, router);
		} catch (Exception e) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}

	/**
	 * Gets the response from the Learning request router.
	 *
	 * @param request
	 *            the request
	 * @param logger
	 *            the logger
	 * @return the language response
	 */
	protected Response getLearningResponse(Request request) {
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, LearningRequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, LearningRequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				return (Response) obj;
			} else {
				return ERROR(LearningErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new ServerException(LearningErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
	}

	/**
	 * Gets all responses for the list of requests and accumulates them as a
	 * single response.
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
	protected Response getLearningResponse(List<Request> requests, Logger logger, String paramName,
			String returnParam) {
		if (null != requests && !requests.isEmpty()) {
			ActorRef router = LearningRequestRouterPool.getRequestRouter();
			try {
				List<Future<Object>> futures = new ArrayList<Future<Object>>();
				for (Request request : requests) {
					Future<Object> future = Patterns.ask(router, request, LearningRequestRouterPool.REQ_TIMEOUT);
					futures.add(future);
				}
				Future<Iterable<Object>> objects = Futures.sequence(futures,
						RequestRouterPool.getActorSystem().dispatcher());
				Iterable<Object> responses = Await.result(objects, LearningRequestRouterPool.WAIT_TIMEOUT.duration());
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
							return ERROR(LearningErrorCodes.SYSTEM_ERROR.name(), "System Error",
									ResponseCode.SERVER_ERROR);
						}
					}
					response.put(returnParam, list);
					return response;
				} else {
					return ERROR(LearningErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new ServerException(LearningErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
			}
		} else {
			return ERROR(LearningErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
		}
	}
}
