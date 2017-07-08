package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.Logger;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class ECMLMimeTypeMgrImpl is a implementation of IMimeTypeManager for
 * Mime-Type as <code>application/vnd.ekstep.ecml-archive</code> or for ECML
 * Content.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see IMimeTypeManager
 * @see HTMLMimeTypeMgrImpl
 * @see APKMimeTypeMgrImpl
 * @see CollectionMimeTypeMgrImpl
 * @see AssetsMimeTypeMgrImpl
 */
public class ECMLMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.common.mgr.BaseManager#getResponse(java.util.List,
	 * org.apache.logging.log4j.Logger, java.lang.String, java.lang.String)
	 */
	protected Response getResponse(List<Request> requests, Logger logger, String paramName, String returnParam) {
		if (null != requests && !requests.isEmpty()) {
			ActorRef router = RequestRouterPool.getRequestRouter();
			try {
				List<Future<Object>> futures = new ArrayList<Future<Object>>();
				for (Request request : requests) {
					Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
					futures.add(future);
				}
				Future<Iterable<Object>> objects = Futures.sequence(futures,
						RequestRouterPool.getActorSystem().dispatcher());
				Iterable<Object> responses = Await.result(objects, RequestRouterPool.WAIT_TIMEOUT.duration());
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
							return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error",
									ResponseCode.SERVER_ERROR);
						}
					}
					response.put(returnParam, list);
					return response;
				} else {
					return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
				}
			} catch (Exception e) {
				LOGGER.log("Exception",e.getMessage(), e);
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
			}
		} else {
			return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model
	 * .Node)
	 */
	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {

		Response response = new Response();
		LOGGER.log("Preparing the Parameter Map for Initializing the Pipeline for Node Id: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);
		
		LOGGER.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		LOGGER.log("Calling the 'Review' Initializer for Node Id: " , contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		LOGGER.log("Review Operation Finished Successfully for Node ID: " , contentId);
		
		if (!checkError(response)) {
			if (BooleanUtils.isTrue(isAsync)) {
				AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
				LOGGER.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " , contentId);

				response.put(ContentAPIParams.publishStatus.name(),
						"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
			} else {
				LOGGER.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " , contentId);
				response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
			}
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadedFile, boolean isAsync) {
		LOGGER.log("Uploaded File: " + uploadedFile.getName());

		LOGGER.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.file.name(), uploadedFile);
		parameterMap.put(ContentAPIParams.node.name(), node);

		LOGGER.log("Calling the 'Upload' Initializer for Node ID: " , node.getIdentifier());
		return pipeline.init(ContentAPIParams.upload.name(), parameterMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#review(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response review(String contentId,Node node, boolean isAsync) {
		LOGGER.log("Node: ", node.getIdentifier());

		LOGGER.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);

		LOGGER.log("Calling the 'Review' Initializer for Node ID: " , contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
