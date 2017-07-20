package org.ekstep.content.publish;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.model.Node;

public class PublishManager extends BaseManager {

	private static final String tempFileLocation = "/data/contentBundle/";

	private ExecutorService executor = Executors.newFixedThreadPool(4); // Parallel execution of 4 publish processes

	public Response publish(String contentId, Node node) {

		String mimeType = getMimeType(node);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), isECMLContent(mimeType));
		parameterMap.put("mimeType", mimeType);

		// Review the content
		Response response = review(contentId, node, parameterMap);
		if (!checkError(response)) {
			if (null == response)
				response = new Response();
			response.put(ContentAPIParams.publishStatus.name(), "Publish Operation for Content Id '" + contentId
					+ "' Started Successfully!");
			executor.submit(new PublishTask(contentId, parameterMap));
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

	public Response review(String contentId, Node node, Map<String, Object> parameterMap) {

		Response response = new Response();
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		return response;
	}

	public static String getBasePath(String contentId) {
		return tempFileLocation + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name() + File.separator + contentId;
	}

	@Override
	protected void finalize() throws Throwable {
		executor.shutdown();
		super.finalize();
	}

}
