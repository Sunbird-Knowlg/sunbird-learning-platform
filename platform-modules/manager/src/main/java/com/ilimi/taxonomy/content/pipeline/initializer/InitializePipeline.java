package com.ilimi.taxonomy.content.pipeline.initializer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.operation.initializer.BundleInitializer;
import com.ilimi.taxonomy.content.operation.initializer.PublishInitializer;
import com.ilimi.taxonomy.content.operation.initializer.UploadInitializer;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;

public class InitializePipeline extends BasePipeline {

	private static Logger LOGGER = LogManager.getLogger(InitializePipeline.class.getName());

	protected String basePath;
	protected String contentId;

	public InitializePipeline(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	public Response init(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && StringUtils.isNotBlank(operation)) {
			LOGGER.info("Performing Content Operation: " + operation);
			switch (operation) {
			case "upload":
			case "UPLOAD": {
				UploadInitializer uploadInitializer = new UploadInitializer(basePath, contentId);
				response = uploadInitializer.initialize(parameterMap);
			}
				break;

			case "publish":
			case "PUBLISH": {
				PublishInitializer publishInitializer = new PublishInitializer(basePath, contentId);
				publishInitializer.initialize(parameterMap);
			}
				break;

			case "bundle":
			case "BUNDLE": {
				BundleInitializer bundleInitializer = new BundleInitializer(basePath, contentId);
				response = bundleInitializer.initialize(parameterMap);
			}
				break;

			default:
				LOGGER.info("Invalid Content Operation: " + operation);
				break;
			}

		}
		return response;
	}

}
