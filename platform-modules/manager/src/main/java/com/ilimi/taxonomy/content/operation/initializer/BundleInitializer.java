package com.ilimi.taxonomy.content.operation.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.client.PipelineRequestorClient;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.finalizer.FinalizePipeline;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.validator.ContentValidator;

/**
 * The Class BundleInitializer, extends BaseInitializer which
 * mainly holds methods to get ECML and ECRFtype from the ContentBody.
 * BundleInitializer holds methods which perform ContentBundlePipeline operations
 */
public class BundleInitializer extends BaseInitializer {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BundleInitializer.class.getName());

	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;

	/** The Constant ECML_MIME_TYPE. */
	private static final String ECML_MIME_TYPE = "application/vnd.ekstep.ecml-archive";

	/**
	 * BundleInitializer()
	 * sets the basePath and ContentId
	 *
	 * @param BasePath the basePath
	 * @param contentId the contentId
	 * checks if the basePath is valid else throws ClientException
	 * checks if the ContentId is not null else throws ClientException
	 */
	public BundleInitializer(String basePath, String contentId) {
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
	 * checks if nodes, contentIdList, bundleFileName, manifestVersion 
	 * exists in the parameterMap else throws ClientException
	 * 
	 * Validates the availability of all the Requested Contents
	 * Populates the Content Hierarchical Data (Include Children Content also)
	 * validates the ContentNode based on MimeType
	 * Sets Attribute Value
	 * Checks if Compression is Required
	 * Gets ECRF Object
	 * Gets Pipeline Object
	 * Starts Pipeline Operation
	 * Calls Finalizer
	 * 
	 * @return the response
	 */
	public Response initialize(Map<String, Object> parameterMap) {
		Response response = new Response();
		LOGGER.info("Fetching the Parameters From BundleInitializer.");
		List<Node> nodes = (List<Node>) parameterMap.get(ContentWorkflowPipelineParams.nodes.name());
		List<String> contentIdList = (List<String>) parameterMap
				.get(ContentWorkflowPipelineParams.contentIdList.name());
		String bundleFileName = (String) parameterMap.get(ContentWorkflowPipelineParams.bundleFileName.name());
		String manifestVersion = (String) parameterMap.get(ContentWorkflowPipelineParams.manifestVersion.name());

		if (null == nodes)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_INIT_PARAM + " | [Invalid or null Node List.]");
		if (null == contentIdList || contentIdList.isEmpty())
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_INIT_PARAM + " | [Invalid or null Content Id List.]");
		if (StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_INIT_PARAM + " | [Invalid Bundle File Name.]");
		if (StringUtils.isBlank(manifestVersion))
			manifestVersion = ContentConfigurationConstants.DEFAULT_CONTENT_MANIFEST_VERSION;

		LOGGER.info("Total Input Content Ids: " + contentIdList.size());
		
		// Validate the availability of all the Requested Contents 
		if (nodes.size() < contentIdList.size())
			throw new ResourceNotFoundException(ContentErrorCodeConstants.MISSING_CONTENT.name(),
					ContentErrorMessageConstants.MISSING_BUNDLE_CONTENT);
		
		// Populate the Content Hierarchical Data (Include Children Content also) 
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		LOGGER.info("Populating the Recursive (Children) Contents.");
		getContentBundleData(ContentConfigurationConstants.GRAPH_ID, nodes, contents, childrenIds, false);

		LOGGER.info("Total Content To Bundle: " + nodes.size());
		ContentValidator validator = new ContentValidator();
		Map<String, Object> bundleMap = new HashMap<String, Object>();
		for (Node node : nodes) {
			// Validating the Content Node 
			if (validator.isValidContentNode(node)) {
				Map<String, Object> nodeMap = new HashMap<String, Object>();

				Boolean ecmlContent = StringUtils.equalsIgnoreCase(ECML_MIME_TYPE,
						(String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()));
				ecmlContent = (null == ecmlContent) ? false : ecmlContent;

				LOGGER.info("Is ECML Mime-Type? " + ecmlContent);
				LOGGER.info("Processing Content Id: " + node.getIdentifier());

				// Setting Attribute Value 
				this.basePath = getBasePath(node.getIdentifier());
				this.contentId = node.getIdentifier();
				LOGGER.info("Base Path For Content Id '" + this.contentId + "' is " + this.basePath);

				// Check if Compression Required 
				boolean isCompressRequired = ecmlContent && isCompressRequired(node);

				// Get ECRF Object 
				Plugin ecrf = getECRFObject((String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name()));

				if (isCompressRequired) {
					// Get Pipeline Object 
					AbstractProcessor pipeline = PipelineRequestorClient
							.getPipeline(ContentWorkflowPipelineParams.compress.name(), basePath, contentId);

					// Start Pipeline Operation 
					ecrf = pipeline.execute(ecrf);
				}
				nodeMap.put(ContentWorkflowPipelineParams.ecrf.name(), ecrf);
				nodeMap.put(ContentWorkflowPipelineParams.isCompressionApplied.name(), isCompressRequired);
				nodeMap.put(ContentWorkflowPipelineParams.basePath.name(), basePath);
				nodeMap.put(ContentWorkflowPipelineParams.node.name(), node);
				nodeMap.put(ContentWorkflowPipelineParams.ecmlType.name(),
						getECMLType((String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name())));
				bundleMap.put(contentId, nodeMap);
			} else {
				throw new ClientException(ContentErrorCodeConstants.VALIDATOR_ERROR.name(),
						ContentErrorMessageConstants.MISSING_REQUIRED_FIELDS + " | [Content of Mime-Type '"
								+ node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name())
								+ "' require few mandatory fields for further processing.]");
			}
		}

		// Call Finalizer 
		FinalizePipeline finalize = new FinalizePipeline(basePath, contentId);
		Map<String, Object> finalizeParamMap = new HashMap<String, Object>();
		finalizeParamMap.put(ContentWorkflowPipelineParams.Contents.name(), contents);
		finalizeParamMap.put(ContentWorkflowPipelineParams.children.name(), childrenIds);
		finalizeParamMap.put(ContentWorkflowPipelineParams.bundleMap.name(), bundleMap);
		finalizeParamMap.put(ContentWorkflowPipelineParams.bundleFileName.name(), bundleFileName);
		finalizeParamMap.put(ContentWorkflowPipelineParams.manifestVersion.name(), manifestVersion);
		response = finalize.finalyze(ContentWorkflowPipelineParams.bundle.name(), finalizeParamMap);
		return response;
	}

}
