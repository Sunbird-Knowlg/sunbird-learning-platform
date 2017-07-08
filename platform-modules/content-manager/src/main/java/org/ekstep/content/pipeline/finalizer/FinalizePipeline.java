package org.ekstep.content.pipeline.finalizer;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.operation.finalizer.BundleFinalizer;
import org.ekstep.content.operation.finalizer.PublishFinalizer;
import org.ekstep.content.operation.finalizer.ReviewFinalizer;
import org.ekstep.content.operation.finalizer.UploadFinalizer;
import org.ekstep.content.pipeline.BasePipeline;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

/**
 * The Class FinalizePipeline is a PipelineClass, extends the BasePipeline which holds all 
 * commmon methods for a ContentNode and its operations
 * Based on the ContentOperation specified initiates the respective OperationFinalizers
 */
public class FinalizePipeline extends BasePipeline {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The basePath. */
	protected String basePath;
	
	/** The contentId. */
	protected String contentId;

	/**
	 * FinalizePipeLine()
	 * sets the basePath and contentId
	 *  
	 * @param BasePath the basePath
	 * @param contentId the contentId
	 * checks if the basePath is valid else throws ClientException
	 * checks if the ContentId is not null else throws ClientException
	 */
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

	/**
	 * finalyze() which marks the begin of the FinalyzerPipeline
	 *
	 * @param operation the Operation
	 * @param Map the parameterMap
	 * checks if operation or parameterMap is empty throws ClientException
	 * else based on the OPERATION(upload, publish or bundle) calls the  
	 * respective ContentOperationFinalizers
	 * @return the response
	 */
	public Response finalyze(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && !StringUtils.isBlank(operation)) {
			LOGGER.log("Performing Operation: " , operation);
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
					
				case "review":
				case "REVIEW": {
						ReviewFinalizer reviewFinalizer = new ReviewFinalizer(basePath, contentId);
						response = reviewFinalizer.finalize(parameterMap);
					}
					break;	
					
				default:
					LOGGER.log("Invalid Operation: " + operation);
					break;
			}
		}
		try {
			FileUtils.deleteDirectory(new File(basePath));
		} catch (Exception e) {
			LOGGER.log("Error deleting directory: " , basePath, e);
		}
		return response;
	}

}
