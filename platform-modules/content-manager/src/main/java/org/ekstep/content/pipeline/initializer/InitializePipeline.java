package org.ekstep.content.pipeline.initializer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.operation.initializer.BundleInitializer;
import org.ekstep.content.operation.initializer.PublishInitializer;
import org.ekstep.content.operation.initializer.ReviewInitializer;
import org.ekstep.content.operation.initializer.UploadInitializer;
import org.ekstep.content.pipeline.BasePipeline;
import org.ekstep.telemetry.logger.TelemetryManager;


/**
 * The Class InitializePipeline is a PipelineClass, extends the BasePipline which holds all 
 * commmon methods for a ContentNode and its operations
 * Based on the ContentOperation specfied InitializePipeline Initiates the respective OperationIntializers
 */
public class InitializePipeline extends BasePipeline {

	/** The basePath. */
	protected String basePath;
	
	/** The contentId. */
	protected String contentId;

	/**
	 * InitializePipeline()
	 * sets the basePath and ContentId
	 *
	 * @param BasePath the basePath
	 * @param contentId the contentId
	 * checks if the basePath is valid else throws ClientException
	 * checks if the ContentId is not null else throws ClientException
	 */
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

	/**
	 * Init() which marks the begin of the InitializePipeline
	 *
	 * @param operation the Operation
	 * @param Map the parameterMap
	 * checks if operation or parameterMap is empty, throws ClientException
	 * else based on the OPERATION(upload, publish or bundle) initializes 
	 * respective ContentOperationInitializers
	 * @return the response
	 */
	public Response init(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && StringUtils.isNotBlank(operation)) {
			TelemetryManager.log("Performing Content Operation: " , operation);
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
				response = publishInitializer.initialize(parameterMap);
			}
				break;

			case "bundle":
			case "BUNDLE": {
				BundleInitializer bundleInitializer = new BundleInitializer(basePath, contentId);
				response = bundleInitializer.initialize(parameterMap);
			}
				break;
				
			case "review":
			case "REVIEW": {
				ReviewInitializer reviewInitializer = new ReviewInitializer(basePath, contentId);
				response = reviewInitializer.initialize(parameterMap);
			}
				break;

			default:
				TelemetryManager.log("Invalid Content Operation: " , operation);
				break;
			}

		}
		return response;
	}

}
