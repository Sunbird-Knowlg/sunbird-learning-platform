package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.content.validator.ContentValidator;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class PluginMimeTypeMgrImpl.
 */
public class PluginMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager{
	
	private static Logger LOGGER = LogManager.getLogger(PluginMimeTypeMgrImpl.class.getName());

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.Node, java.io.File)
	 */
	@Override
	public Response upload(Node node, File uploadFile, boolean isAsync) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadFile.getName());

		ContentValidator validator = new ContentValidator();
		if (validator.isValidPluginPackage(uploadFile)) {
			LOGGER.info("Calling Upload Content For Node ID: " + node.getIdentifier());
			String basePath = getBasePath(node.getIdentifier());
			// Extract the ZIP File 
			extractContentPackage(uploadFile, basePath);
			
			// read manifest json
			File jsonFile = new File(basePath + File.separator + "manifest.json");
			try {
				LOGGER.info("Reading ECML File.");
				if (jsonFile.exists()) {
					String manifest = FileUtils.readFileToString(jsonFile);
					String pluginId = node.getIdentifier();
					LOGGER.info("replacing image identifier with node identifier for plugin id");
					if(StringUtils.endsWith(node.getIdentifier(), ".img") && StringUtils.equalsIgnoreCase(node.getObjectType(), ContentWorkflowPipelineParams.ContentImage.name())){
						pluginId = pluginId.replace(".img", "");
					}
					LOGGER.info("stripped pluginId" + pluginId);
					String version = getVersion(pluginId, manifest);
					node.getMetadata().put(ContentAPIParams.semanticVersion.name(), version);
				}
			} catch (IOException e) {
				throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_READ.name(),
						ContentErrorMessageConstants.MANIFEST_FILE_READ_ERROR, e);
			}
			try {
				FileUtils.deleteDirectory(new File(basePath));
			} catch (Exception e) {
				LOGGER.error("Error deleting directory: " + basePath, e);
			}
			return uploadContentArtifact(node, uploadFile);
		} else {
			return ERROR(ContentErrorCodeConstants.VALIDATOR_ERROR.name(), 
					"Invalid plugin package file", ResponseCode.CLIENT_ERROR);
		}
	}
	
	private String getVersion(String pluginId, String manifest) {
		String id = null;
		String version = null;
		try {
			Gson gson = new Gson();
			JsonObject root = gson.fromJson(manifest, JsonObject.class);
			id = root.get("id").getAsString();
			version = root.get("ver").getAsString();
		} catch (Exception e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_MANIFEST_PARSE_ERROR.name(),
					ContentErrorMessageConstants.MANIFEST_PARSE_CONFIG_ERROR, e);
		}
		LOGGER.info("pluginId:" + pluginId + "ManifestId:" + id);
		if (!StringUtils.equals(pluginId, id))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_INVALID_PLUGIN_ID.name(),
					ContentErrorMessageConstants.INVALID_PLUGIN_ID_ERROR);
		if (StringUtils.isBlank(version))
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_MISSING_VERSION.name(),
					ContentErrorMessageConstants.INVALID_PLUGIN_VER_ERROR);
		else
			return StringUtils.deleteWhitespace(version);
		
	}

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model.Node)
	 */
	@Override
	public Response publish(Node node, boolean isAsync) {
		LOGGER.debug("Node: ", node);

		Response response = new Response();
		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);
		
		LOGGER.debug("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		LOGGER.info("Calling the 'Review' Initializer for Node Id: " + node.getIdentifier());
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		LOGGER.info("Review Operation Finished Successfully for Node ID: " + node.getIdentifier());

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, parameterMap);
			LOGGER.info("Publish Operation Started Successfully in 'Async Mode' for Node Id: " + node.getIdentifier());

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + node.getIdentifier() + "' Started Successfully!");
		} else {
			LOGGER.info("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " + node.getIdentifier());

			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}

		return response;
	}
	
	@Override
	public Response review(Node node, boolean isAsync) {
		LOGGER.debug("Node: ", node);

		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		LOGGER.info("Calling the 'Review' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
