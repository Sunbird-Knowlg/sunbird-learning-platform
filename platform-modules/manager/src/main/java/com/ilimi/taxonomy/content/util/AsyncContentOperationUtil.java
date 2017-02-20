package com.ilimi.taxonomy.content.util;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
						if (null == node)
							throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(),
									ContentErrorMessageConstants.INVALID_CONTENT
											+ " | ['null' or Invalid Content Node (Object). Async Upload Operation Failed.]");
						try {
							InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()),
									node.getIdentifier());
							pipeline.init(ContentWorkflowPipelineParams.upload.name(), parameterMap);
						} catch (Exception e) {
							LOGGER.error(
									"Something Went Wrong While Performing 'Content Upload' Operation in Async Mode. | [Content Id: "
											+ node.getIdentifier() + "]");
							node.getMetadata().put(ContentWorkflowPipelineParams.uploadError.name(), e.getMessage());
							node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
									ContentWorkflowPipelineParams.Failed.name());
							UpdateDataNodeUtil updateDataNodeUtil = new UpdateDataNodeUtil();
							updateDataNodeUtil.updateDataNode(node);
						}
					}
						break;

					case "publish":
					case "PUBLISH": {
						Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
						if (null == node)
							throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(),
									ContentErrorMessageConstants.INVALID_CONTENT
											+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
						try {
							InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()),
									node.getIdentifier());
							pipeline.init(ContentWorkflowPipelineParams.publish.name(), parameterMap);
						} catch (Exception e) {
							LOGGER.error(
									"Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: "
											+ node.getIdentifier() + "]");
							node.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), e.getMessage());
							node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
									ContentWorkflowPipelineParams.Failed.name());
							UpdateDataNodeUtil updateDataNodeUtil = new UpdateDataNodeUtil();
							updateDataNodeUtil.updateDataNode(node);
						}
					}
						break;

					case "bundle":
					case "BUNDLE": {
						try {
							InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
							pipeline.init(ContentWorkflowPipelineParams.bundle.name(), parameterMap);
						} catch (Exception e) {
							LOGGER.error(
									"Something Went Wrong While Performing 'Content Bundle' Operation in Async Mode.");
						}
					}
						break;

					case "review":
					case "REVIEW": {
						Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
						if (null == node)
							throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(),
									ContentErrorMessageConstants.INVALID_CONTENT
											+ " | ['null' or Invalid Content Node (Object). Async Review Operation Failed.]");
						try {
							InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()),
									node.getIdentifier());
							pipeline.init(ContentWorkflowPipelineParams.review.name(), parameterMap);
						} catch (Exception e) {
							LOGGER.error(
									"Something Went Wrong While Performing 'Content Review (Send For Review)' Operation in Async Mode. | [Content Id: "
											+ node.getIdentifier() + "]");
							node.getMetadata().put(ContentWorkflowPipelineParams.reviewError.name(), e.getMessage());
							node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
									ContentWorkflowPipelineParams.Failed.name());
							UpdateDataNodeUtil updateDataNodeUtil = new UpdateDataNodeUtil();
							updateDataNodeUtil.updateDataNode(node);
						}
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

	private static String getBasePath(String contentId) {
		String path = "";
		if (!StringUtils.isBlank(contentId))
			path = tempFileLocation + File.separator + System.currentTimeMillis()
					+ ContentWorkflowPipelineParams._temp.name() + File.separator + contentId;
		return path;
	}
}
