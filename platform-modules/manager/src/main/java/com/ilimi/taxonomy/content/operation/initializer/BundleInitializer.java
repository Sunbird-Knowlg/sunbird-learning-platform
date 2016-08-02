package com.ilimi.taxonomy.content.operation.initializer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.HttpDownloadUtility;

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

public class BundleInitializer extends BaseInitializer {

	private static Logger LOGGER = LogManager.getLogger(BundleInitializer.class.getName());

	protected String basePath;
	protected String contentId;

	private static final String ECML_MIME_TYPE = "application/vnd.ekstep.ecml-archive";

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

	@SuppressWarnings("unchecked")
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

		LOGGER.info("Populating the Recursive (Children) Contents.");

		// Populate the Content Hierarchical Data (Include Children Content
		// also)
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		List<String> childrenIds = new ArrayList<String>();
		getContentBundleData(ContentConfigurationConstants.GRAPH_ID, nodes, contents, childrenIds, false);

		// Validate the availability of all the Requested Contents
		if (contents.size() < contentIdList.size())
			throw new ResourceNotFoundException(ContentErrorCodeConstants.MISSING_CONTENT.name(),
					ContentErrorMessageConstants.MISSING_BUNDLE_CONTENT);
		
		// Preparing the List of URL Fields
		List<String> urlFields = new ArrayList<String>();
        urlFields.add(ContentWorkflowPipelineParams.appIcon.name());
        urlFields.add(ContentWorkflowPipelineParams.grayScaleAppIcon.name());
        urlFields.add(ContentWorkflowPipelineParams.posterImage.name());
        urlFields.add(ContentWorkflowPipelineParams.artifactUrl.name());

		// Marking Content Visibility as Parent
		for (Map<String, Object> content : contents) {
			String identifier = (String) content.get(ContentWorkflowPipelineParams.identifier.name());
			if (childrenIds.contains(identifier))
				content.put(ContentWorkflowPipelineParams.visibility.name(),
						ContentWorkflowPipelineParams.Parent.name());
			for (Map.Entry<String, Object> entry : content.entrySet()) {
                if (urlFields.contains(entry.getKey()) && null != entry.getValue() && HttpDownloadUtility.isValidUrl(entry.getValue())) {
                	String file = FilenameUtils.getName(entry.getValue().toString());
                    if (file.endsWith(ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR + ContentConfigurationConstants.DEFAULT_ECAR_EXTENSION)) {
                        entry.setValue(identifier.trim() + File.separator + identifier.trim() + ".zip");
                    } else {
                        entry.setValue(identifier.trim() + File.separator + Slug.makeSlug(file, true));
                    }
                }
			}
		}

		LOGGER.info("Total Content To Bundle: " + nodes.size());

		Map<String, Object> bundleMap = new HashMap<String, Object>();
		for (Node node : nodes) {
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
			boolean isCompressRequired = isCompressRequired(node) && ecmlContent;

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
		}

		// Call Finalizer
		FinalizePipeline finalize = new FinalizePipeline(basePath, contentId);
		Map<String, Object> finalizeParamMap = new HashMap<String, Object>();
		finalizeParamMap.put(ContentWorkflowPipelineParams.bundleMap.name(), bundleMap);
		finalizeParamMap.put(ContentWorkflowPipelineParams.Contents.name(), contents);
		finalizeParamMap.put(ContentWorkflowPipelineParams.bundleFileName.name(), bundleFileName);
		finalizeParamMap.put(ContentWorkflowPipelineParams.manifestVersion.name(), manifestVersion);
		response = finalize.finalyze(ContentWorkflowPipelineParams.bundle.name(), finalizeParamMap);
		return response;
	}

}
