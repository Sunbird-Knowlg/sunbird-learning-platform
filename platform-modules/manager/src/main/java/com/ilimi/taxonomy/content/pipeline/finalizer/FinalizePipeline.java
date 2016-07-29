package com.ilimi.taxonomy.content.pipeline.finalizer;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.operation.finalizer.BundleFinalizer;
import com.ilimi.taxonomy.content.operation.finalizer.PublishFinalizer;
import com.ilimi.taxonomy.content.operation.finalizer.UploadFinalizer;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;

public class FinalizePipeline extends BasePipeline {

	private static Logger LOGGER = LogManager.getLogger(FinalizePipeline.class.getName());

	protected String basePath;
	protected String contentId;

	public FinalizePipeline(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	public Response finalyze(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && !StringUtils.isBlank(operation)) {
			LOGGER.info("Performing Operation: " + operation);
			switch (operation) {
				case "upload":
				case "UPLOAD": {
						UploadFinalizer uploadFinalizer = new UploadFinalizer(basePath, contentId);
						response = uploadFinalizer.finalize(parameterMap);
					}
					break;
					
				case "publish":
				case "PUBLISH": {
						PublishFinalizer publishFinalizer = new PublishFinalizer(basePath, contentId);
						response = publishFinalizer.finalize(parameterMap);
					}
					break;
					
				case "bundle":
				case "BUNDLE": {
						BundleFinalizer bundleFinalizer = new BundleFinalizer(basePath, contentId);
						response = bundleFinalizer.finalize(parameterMap);
					}
					break;
					
				default:
					LOGGER.info("Invalid Operation: " + operation);
					break;
			}
		}
		try {
			FileUtils.deleteDirectory(new File(basePath));
		} catch (Exception e) {
			LOGGER.error("Error deleting directory: " + basePath, e);
		}
		return response;
	}

}
