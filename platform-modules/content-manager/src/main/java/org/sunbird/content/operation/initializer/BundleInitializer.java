package org.sunbird.content.operation.initializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.content.client.PipelineRequestorClient;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.pipeline.finalizer.FinalizePipeline;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.content.validator.ContentValidator;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;


/**
 * The Class BundleInitializer, extends BaseInitializer which mainly holds
 * methods to get ECML and ECRFtype from the ContentBody. BundleInitializer
 * holds methods which perform ContentBundlePipeline operations
 */
public class BundleInitializer extends BaseInitializer {

	private static final String TAXONOMY_ID = "domain";
	
	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	/** The Constant ECML_MIME_TYPE. */
	private static final String ECML_MIME_TYPE = "application/vnd.ekstep.ecml-archive";

	/**
	 * Instantiates a new BundleInitializer and sets the base path and current
	 * content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
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
	 * @param Map
	 *            the parameterMap
	 * 
	 *            checks if nodes, contentIdList, bundleFileName,
	 *            manifestVersion exists in the parameterMap else throws
	 *            ClientException
	 * 
	 *            Validates the availability of all the Requested Contents
	 *            Populates the Content Hierarchical Data (Include Children
	 *            Content also) validates the ContentNode based on MimeType Sets
	 *            Attribute Value Checks if Compression is Required Gets ECRF
	 *            Object Gets Pipeline Object Starts Pipeline Operation Calls
	 *            Finalizer
	 * 
	 * @return the response
	 */
	@SuppressWarnings("unchecked")
	public Response initialize(Map<String, Object> parameterMap) {
		Response response = new Response();
		TelemetryManager.log("Fetching the Parameters From BundleInitializer.");
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

		TelemetryManager.log("Total Input Content Ids: " + contentIdList.size());

		// Validate the availability of all the Requested Contents
		if (nodes.size() < contentIdList.size())
			throw new ResourceNotFoundException(ContentErrorCodeConstants.MISSING_CONTENT.name(),
					ContentErrorMessageConstants.MISSING_BUNDLE_CONTENT);

		// Populate the Content Hierarchical Data (Include Children Content
		// also)
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		TelemetryManager.log("Populating the Recursive (Children) Contents.");
		getContentBundleData(TAXONOMY_ID, nodes, contents, childrenIds, false);

		TelemetryManager.log("Total Content To Bundle: " + nodes.size());
		ContentValidator validator = new ContentValidator();
		Map<String, Object> bundleMap = new HashMap<String, Object>();
		for (Node node : nodes) {
			// Checking if the node is not retired
			if (null != node && StringUtils.equalsIgnoreCase(
					((String) node.getMetadata().get(ContentWorkflowPipelineParams.status.name())),
					ContentWorkflowPipelineParams.Retired.name()))
				throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(),
						ContentErrorMessageConstants.INVALID_CONTENT + " | [Content Id: " + node.getIdentifier()
								+ " is 'Retired' Content.]");
			// Validating the Content Node
			if (validator.isValidContentNode(node)) {
				Map<String, Object> nodeMap = new HashMap<String, Object>();

				Boolean ecmlContent = StringUtils.equalsIgnoreCase(ECML_MIME_TYPE,
						(String) node.getMetadata().get(ContentWorkflowPipelineParams.mimeType.name()));
				ecmlContent = (null == ecmlContent) ? false : ecmlContent;

				TelemetryManager.log("Is ECML Mime-Type? " + ecmlContent);
				TelemetryManager.log("Processing Content Id: " + node.getIdentifier());

				// Setting Attribute Value
				this.basePath = getBasePath(node.getIdentifier());
				this.contentId = node.getIdentifier();
				TelemetryManager.log("Base Path For Content Id '" + this.contentId + "' is " + this.basePath);

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
