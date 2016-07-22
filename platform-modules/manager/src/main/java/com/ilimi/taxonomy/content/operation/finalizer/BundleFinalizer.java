package com.ilimi.taxonomy.content.operation.finalizer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.HttpDownloadUtility;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class BundleFinalizer extends BaseFinalizer {
	
	private static Logger LOGGER = LogManager.getLogger(BundleFinalizer.class.getName());
	
	private static final int IDX_S3_URL = 1;
	
	protected String basePath;
	protected String contentId;

	public BundleFinalizer(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}
	
	@SuppressWarnings({ "unchecked" })
	public Response finalize(Map<String, Object> parameterMap) {
		Response response = new Response();
		Map<String, Object> bundleMap = (Map<String, Object>) parameterMap.get(ContentWorkflowPipelineParams.bundleMap.name());
		List<Map<String, Object>> contents = (List<Map<String, Object>>) parameterMap.get(ContentWorkflowPipelineParams.Contents.name());
		String bundleFileName = (String) parameterMap.get(ContentWorkflowPipelineParams.bundleFileName.name());
		String manifestVersion = (String) parameterMap.get(ContentWorkflowPipelineParams.manifestVersion.name());
		
		if (StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid Bundle File Name.]");
		if (StringUtils.isBlank(bundleFileName))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid or null Parameters.]");
		if (null == contents || contents.isEmpty())
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid or null Content List (Un-Populate Content Data).]");
		
		List<File> zipPackages = new ArrayList<File>();
		
		LOGGER.info("Fetching the Parameters From BundleFinalizer.");
		for(Entry<String, Object> entry: bundleMap.entrySet()) {
			LOGGER.info("Processing Content Id: " + entry.getKey());
			
			Map<String, Object> nodeMap = (Map<String, Object>) entry.getValue();
			
			if (null == nodeMap)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [All the Content for Bundling should be Valid, Invalid or null Content Cannnot be Bundled (Content Id - " + entry.getKey() + ").]");
			
			LOGGER.info("Fetching the Parameters For Content Id: " + entry.getKey());
			Plugin ecrf = (Plugin) nodeMap.get(ContentWorkflowPipelineParams.ecrf.name());
			Node node = (Node) nodeMap.get(ContentWorkflowPipelineParams.node.name());
			boolean isCompressionApplied = (boolean) nodeMap.get(ContentWorkflowPipelineParams.isCompressionApplied.name());
			String path = (String) nodeMap.get(ContentWorkflowPipelineParams.basePath.name());
			String ecmlType = (String) nodeMap.get(ContentWorkflowPipelineParams.ecmlType.name());
			
			if (null == ecrf)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM
								+ " | [Invalid or null ECRF Object.]");
			if (null == node)
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid or null Node.]");
			if (null == path || !isValidBasePath(path))
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Path does not Exist.]");
			if (StringUtils.isBlank(ecmlType))
				throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
						ContentErrorMessageConstants.INVALID_CWP_OP_FINALIZE_PARAM + " | [Invalid ECML Type.]");
			
			// Setting Attribute Value
			this.basePath = path;
			this.contentId = node.getIdentifier();
			LOGGER.info("Base Path For Content Id '" + this.contentId + "' is " + this.basePath);
			
			LOGGER.info("Is Compression Applied ? " + isCompressionApplied);
			
			if (BooleanUtils.isTrue(isCompressionApplied)) {
				// Get Content String
				String ecml = getECMLString(ecrf, ecmlType);
				
				// Write ECML File
				writeECMLFile(basePath, ecml, ecmlType);
				
				// Create 'ZIP' Package
				String zipFileName = basePath + File.separator + System.currentTimeMillis() + "_" + contentId
						+ ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR
						+ ContentConfigurationConstants.DEFAULT_ZIP_EXTENSION;
				createZipPackage(basePath, zipFileName);
				zipPackages.add(new File(zipFileName));
			} else {
				// Download From 'artifactUrl'
				String artifactUrl = (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
				if (HttpDownloadUtility.isValidUrl(artifactUrl))
					zipPackages.add(HttpDownloadUtility.downloadFile(artifactUrl, basePath));
			}
			
			// Download App Icon and create thumbnail
			createThumbnail(basePath, node);
		}
		
		// Create Manifest JSON File
		File manifestFile = new File(basePath + File.separator + ContentWorkflowPipelineParams.manifest.name() + File.separator +  ContentConfigurationConstants.CONTENT_BUNDLE_MANIFEST_FILE_NAME);
		createManifestFile(manifestFile, manifestVersion, contents);
		zipPackages.add(manifestFile);
		
		// Create ECAR File
		File file = createBundle(zipPackages, bundleFileName);
		
		// Upload ECAR to S3
		String[] urlArray = uploadToAWS(file, getUploadFolderName());
		if (null != urlArray && urlArray.length >= 2)
			response.put(ContentWorkflowPipelineParams.ECAR_URL.name(), urlArray[IDX_S3_URL]);
		
		return response;
	}
}
