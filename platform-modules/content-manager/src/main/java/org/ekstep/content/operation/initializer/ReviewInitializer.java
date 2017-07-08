package org.ekstep.content.operation.initializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.client.PipelineRequestorClient;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.finalizer.FinalizePipeline;
import org.ekstep.content.processor.AbstractProcessor;
import org.ekstep.content.validator.ContentValidator;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.dac.model.Node;

public class ReviewInitializer extends BaseInitializer {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	/**
	 * Instantiates a new ReviewInitializer and sets the base path and current
	 * content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public ReviewInitializer(String basePath, String contentId) {
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
	 * @param Map
	 *            the parameterMap
	 * 
	 *            checks if nodes exists in the parameterMap else throws
	 *            ClientException validates the ContentNode based on MimeType
	 *            and metadata Gets ECRF Object Gets Pipeline Object Starts
	 *            Pipeline Operation Calls Finalizer
	 * 
	 * @return the response
	 */
	public Response initialize(Map<String, Object> parameterMap) {
		LOGGER.log("Parameter Map: ", parameterMap);
		if (null == parameterMap)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_PARAMETER_MAP + " | [Parameter Map Cannot be 'null']");

		Response response = new Response();

		LOGGER.log("Fetching The Parameters From Parameter Map");

		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		Boolean isECMLContent = (Boolean) parameterMap.get(ContentWorkflowPipelineParams.ecmlType.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [Invalid or null Node.]");

		LOGGER.log("Node: ", node);
		LOGGER.log("Is ECML Content ? ", isECMLContent);

		// Validating the Content Node
		ContentValidator validator = new ContentValidator();
		if (validator.isValidContentNode(node)) {
			isECMLContent = (null == isECMLContent) ? false : isECMLContent;

			// If Compression is Required then only Validation Can be applied
			boolean isValidationRequired = isECMLContent && isCompressRequired(node);

			// Get ECRF Object
			Plugin ecrf = getECRFObject((String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name()));
			LOGGER.log("ECRF Object Created.");

			if (isValidationRequired) {
				// Get Pipeline Object
				AbstractProcessor pipeline = PipelineRequestorClient
						.getPipeline(ContentWorkflowPipelineParams.validate.name(), basePath, contentId);

				// Start Pipeline Operation
				ecrf = pipeline.execute(ecrf);
			}

			// Call Finalyzer
			LOGGER.log("Calling Finalizer");
			FinalizePipeline finalize = new FinalizePipeline(basePath, contentId);
			Map<String, Object> finalizeParamMap = new HashMap<String, Object>();
			finalizeParamMap.put(ContentWorkflowPipelineParams.node.name(), node);
			finalizeParamMap.put(ContentWorkflowPipelineParams.isPublishOperation.name(),
					parameterMap.get(ContentWorkflowPipelineParams.isPublishOperation.name()));
			response = finalize.finalyze(ContentWorkflowPipelineParams.review.name(), finalizeParamMap);
			// TODO: Make first Parameter for all operation's initialize and
			// finalyze methods as ContentOperation Enum.
		}
		return response;
	}
}
