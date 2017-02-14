package com.ilimi.taxonomy.content.util;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.common.ContentOperations;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.initializer.InitializePipeline;

public class AsyncContentOperationUtil {

	private static final String tempFileLocation = "/data/contentBundle/";
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(AsyncContentOperationUtil.class.getName());
	
	public static void makeAsyncOperation(ContentOperations operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Content Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		if (null == operation)
			throw new ClientException(ContentErrorCodeConstants.INVALID_OPERATION.name(),
					ContentErrorMessageConstants.INVALID_OPERATION + " | [Invalid or 'null' operation.]");

		if (null == parameterMap)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_ASYNC_OPERATION_PARAMETER_MAP
							+ " | [Invalid or 'null' Parameter Map.]");

		Runnable task = new Runnable() {

			@Override
			public void run() {
				try {
					String opt = operation.name();
					switch (opt) {
					case "upload":
					case "UPLOAD": {
						Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
						InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
						pipeline.init(ContentWorkflowPipelineParams.upload.name(), parameterMap);
					}
						break;

					case "publish":
					case "PUBLISH": {
						Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
						InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
						pipeline.init(ContentWorkflowPipelineParams.publish.name(), parameterMap);
					}
						break;

					case "bundle":
					case "BUNDLE": {
						InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
						pipeline.init(ContentWorkflowPipelineParams.bundle.name(), parameterMap);
					}
						break;

					case "review":
					case "REVIEW": {
						Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
						InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
						pipeline.init(ContentAPIParams.review.name(), parameterMap);
					}
						break;

					default:
						LOGGER.error("Invalid Async Operation.");
						break;
					}
				} catch (Exception e) {
					LOGGER.error("Error! While Making Async Call for Content Operation: " + operation.name(), e);
				}
			}
		};
		new Thread(task, "AsyncContentOperationThread").start();
	}

	protected static String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = tempFileLocation + File.separator + System.currentTimeMillis() + ContentAPIParams._temp.name()
					+ File.separator + contentId;
		return path;
	}
}
