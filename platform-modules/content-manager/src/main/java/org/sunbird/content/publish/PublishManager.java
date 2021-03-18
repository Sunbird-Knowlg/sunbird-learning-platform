package org.sunbird.content.publish;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.enums.TaxonomyErrorCodes;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.common.router.RequestRouterPool;
import org.sunbird.content.pipeline.initializer.InitializePipeline;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.common.enums.LearningActorNames;
import org.sunbird.learning.contentstore.ContentStoreOperations;
import org.sunbird.learning.contentstore.ContentStoreParams;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class PublishManager extends BaseManager {

	private static final String tempFileLocation = "/data/contentBundle";

	private ExecutorService executor = Executors.newFixedThreadPool(4); // Parallel execution of 4 publish processes

	public Response publish(String contentId, Node node) {

		String mimeType = getMimeType(node);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		if (isECMLContent(mimeType)) {
			parameterMap.put(ContentAPIParams.ecmlType.name(), isECMLContent(mimeType));
			node.getMetadata().put(ContentAPIParams.body.name(), PublishManager.getContentBody(node.getIdentifier()));
		}
		parameterMap.put("mimeType", mimeType);

		// Review the content
		Response response = review(contentId, parameterMap);
		if (!checkError(response)) {
			if (null == response)
				response = new Response();
			response.put(ContentAPIParams.publishStatus.name(), "Publish Operation for Content Id '" + contentId
					+ "' Started Successfully!");
			if (Platform.config.hasPath("content.publish_task.enabled")) {
				if (Platform.config.getBoolean("content.publish_task.enabled")) {
					TelemetryManager.info("Publish task execution starting for content Id: " + contentId);
					executor.submit(new PublishTask(parameterMap));
				}
			}
		}

		return response;
	}

	public String getMimeType(Node node) {
		String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "assets";
		}
		return mimeType;
	}

	public static boolean isECMLContent(String mimeType) {
		return StringUtils.equalsIgnoreCase("application/vnd.ekstep.ecml-archive", mimeType);
	}

	public Response review(String contentId, Map<String, Object> parameterMap) {

		Response response = new Response();
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId, null), contentId);
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		return response;
	}

	public static String getBasePath(String contentId, String tmpLocation) {
		if(null == tmpLocation)
			return tempFileLocation + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name() + File.separator + contentId;
		else
			return tmpLocation + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name() + File.separator + contentId;
	}
	
	@Override
	protected void finalize() throws Throwable {
		executor.shutdown();
		super.finalize();
	}

	public static String getContentBody(String contentId) {
		Request request = new Request();
		request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name());
		request.setOperation(ContentStoreOperations.getContentBody.name());
		request.put(ContentStoreParams.content_id.name(), contentId);
		Response response = makeLearningRequest(request);
		return (String) response.get(ContentStoreParams.body.name());
	}

	/**
	 * Make a sync request to LearningRequestRouter
	 *
	 * @param request the request object
	 * @param logger the logger object
	 * @return the LearningActor response
	 */
	private static Response makeLearningRequest(Request request) {
		Response response = new Response();
		ActorRef router = LearningRequestRouterPool.getRequestRouter();
		try {
			Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
			Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				response = (Response) obj;
				TelemetryManager.log("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode() + " | Result: "
						+ response.getResult().keySet());
				return response;
			}
		} catch (Exception e) {
			TelemetryManager.error("Error! Something went wrong:"+ e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), " System Error", e);
		}
		return response;
	}

}