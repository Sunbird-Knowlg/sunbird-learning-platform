package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.springframework.stereotype.Component;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.pipeline.initializer.InitializePipeline;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;
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
@Component
public class ECMLMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ECMLMimeTypeMgrImpl.class.getName());

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
				logger.error(e.getMessage(), e);
				throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
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
	public Response publish(Node node) {
		LOGGER.debug("Node: ", node);

		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);

		LOGGER.info("Calling the 'Publish' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.publish.name(), parameterMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(Node node, File uploadedFile) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadedFile.getName());

		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.file.name(), uploadedFile);
		parameterMap.put(ContentAPIParams.node.name(), node);

		LOGGER.info("Calling the 'Upload' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.upload.name(), parameterMap);
	}

}
