package org.ekstep.content.publish;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.router.LearningRequestRouterPool;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.dac.model.Node;

public class PublishManager extends BaseManager {

	private static final String tempFileLocation = "/data/contentBundle/";

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
//			executor.submit(new PublishTask(contentId, parameterMap));
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
				PlatformLogger.log("Response Params: " + response.getParams() + " | Code: " + response.getResponseCode() + " | Result: "
						+ response.getResult().keySet());
				return response;
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! Something went wrong", e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", e);
		}
		return response;
	}

}
