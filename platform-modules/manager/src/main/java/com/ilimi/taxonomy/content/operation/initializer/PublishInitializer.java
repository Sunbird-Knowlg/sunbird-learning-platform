package com.ilimi.taxonomy.content.operation.initializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.client.PipelineRequestorClient;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.finalizer.FinalizePipeline;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.validator.ContentValidator;

/**
 * The Class PublishInitializer, extends BaseInitializer which
 * mainly holds methods to get ECML and ECRFtype from the ContentBody.
 * PublishInitializer holds methods which perform ContentPublishPipeline operations
 */
public class PublishInitializer extends BaseInitializer {
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BundleInitializer.class.getName());

	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;

	/**
	 * PublishInitializer()
	 * sets the basePath and ContentId
	 *
	 * @param BasePath the basePath
	 * @param contentId the contentId
	 * checks if the basePath is valid else throws ClientException
	 * checks if the ContentId is not null else throws ClientException
	 */
	public PublishInitializer(String basePath, String contentId) {
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
	 * initialize()
	 *
	 * @param Map the parameterMap
	 * 
	 * checks if nodes exists in the parameterMap else throws ClientException
	 * validates the ContentNode based on MimeType and metadata
	 * Gets ECRF Object
	 * Gets Pipeline Object
	 * Starts Pipeline Operation
	 * Calls Finalizer
	 * 
	 * @return the response
	 */
	public Response initialize(Map<String, Object> parameterMap) {
		Response response = new Response();

		LOGGER.info("Fetching The Parameters From Parameter Map");

		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		Boolean ecmlContent = (Boolean) parameterMap.get(ContentWorkflowPipelineParams.ecmlType.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [Invalid or null Node.]");

		// Validating the Content Node 
		ContentValidator validator = new ContentValidator();
		if (validator.isValidContentNode(node)) {
			ecmlContent = (null == ecmlContent) ? false : ecmlContent;
			boolean isCompressRequired = ecmlContent && isCompressRequired(node);

			// Get ECRF Object 
			Plugin ecrf = getECRFObject((String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name()));
			LOGGER.info("ECRF Object Created.");

			if (isCompressRequired) {
				// Get Pipeline Object 
				AbstractProcessor pipeline = PipelineRequestorClient
						.getPipeline(ContentWorkflowPipelineParams.compress.name(), basePath, contentId);

				// Start Pipeline Operation 
				ecrf = pipeline.execute(ecrf);
			}

			// Call Finalyzer 
			LOGGER.info("Calling Finalizer");
			FinalizePipeline finalize = new FinalizePipeline(basePath, contentId);
			Map<String, Object> finalizeParamMap = new HashMap<String, Object>();
			finalizeParamMap.put(ContentWorkflowPipelineParams.node.name(), node);
			finalizeParamMap.put(ContentWorkflowPipelineParams.ecrf.name(), ecrf);
			finalizeParamMap.put(ContentWorkflowPipelineParams.ecmlType.name(),
					getECMLType((String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name())));
			finalizeParamMap.put(ContentWorkflowPipelineParams.isCompressionApplied.name(), isCompressRequired);
			response = finalize.finalyze(ContentWorkflowPipelineParams.publish.name(), finalizeParamMap);
		}
		return response;
	}

}
