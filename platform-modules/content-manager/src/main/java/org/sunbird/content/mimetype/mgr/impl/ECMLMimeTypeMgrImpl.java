package org.sunbird.content.mimetype.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.router.RequestRouterPool;
import org.sunbird.content.common.ContentOperations;
import org.sunbird.content.mimetype.mgr.IMimeTypeManager;
import org.sunbird.content.pipeline.initializer.InitializePipeline;
import org.sunbird.content.util.AsyncContentOperationUtil;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.common.mgr.BaseManager#getResponse(java.util.List,
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
	 * org.sunbird.taxonomy.mgr.IMimeTypeManager#publish(org.sunbird.graph.dac.model
	 * .Node)
	 */
	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {

		Response response = new Response();
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline for Node Id: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);
		
		TelemetryManager.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		TelemetryManager.log("Calling the 'Review' Initializer for Node Id: " + contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		TelemetryManager.log("Review Operation Finished Successfully for Node ID: " + contentId);
		
		if (!checkError(response)) {
			if (BooleanUtils.isTrue(isAsync)) {
				AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
				TelemetryManager.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + contentId);

				response.put(ContentAPIParams.publishStatus.name(),
						"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
			} else {
				TelemetryManager.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " + contentId);
				response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
			}
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.taxonomy.mgr.IMimeTypeManager#upload(org.sunbird.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadedFile, boolean isAsync) {
		TelemetryManager.log("Uploaded File: " + uploadedFile.getName());

		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.file.name(), uploadedFile);
		parameterMap.put(ContentAPIParams.node.name(), node);

		TelemetryManager.log("Calling the 'Upload' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.upload.name(), parameterMap);
	}
	
	@Override
	public Response upload(String contentId, Node node, String fileUrl) {
		File file = null;
		try {
			file = copyURLToFile(fileUrl);
			return upload(contentId, node, file, false);
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != file && file.exists()) file.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.taxonomy.mgr.IMimeTypeManager#review(org.sunbird.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response review(String contentId,Node node, boolean isAsync) {
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);

		TelemetryManager.log("Calling the 'Review' Initializer for Node ID: " + contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
