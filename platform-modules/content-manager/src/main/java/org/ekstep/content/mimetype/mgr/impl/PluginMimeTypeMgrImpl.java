package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.content.validator.ContentValidator;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.telemetry.handler.Level;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The Class PluginMimeTypeMgrImpl.
 */
public class PluginMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager{
	
	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.IMimeTypeManager#upload(org.ekstep.graph.dac.model.Node, java.io.File)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		TelemetryManager.log("Uploaded File: " , uploadFile.getName(), null, Level.INFO.name());

		ContentValidator validator = new ContentValidator();
		if (validator.isValidPluginPackage(uploadFile)) {
			TelemetryManager.log("Calling Upload Content For Node ID: " + contentId, null, Level.INFO.name());
			String basePath = getBasePath(contentId);
			// Extract the ZIP File 
			extractContentPackage(uploadFile, basePath);
			
			// read manifest json
			File jsonFile = new File(basePath + File.separator + "manifest.json");
			try {
				if (jsonFile.exists()) {
					String manifest = FileUtils.readFileToString(jsonFile);
					String version = getVersion(contentId, manifest);
					node.getMetadata().put(ContentAPIParams.semanticVersion.name(), version);
				}
			} catch (IOException e) {
				throw new ServerException(ContentErrorCodeConstants.MANIFEST_FILE_READ.name(),
						ContentErrorMessageConstants.MANIFEST_FILE_READ_ERROR, e);
			}
			try {
				FileUtils.deleteDirectory(new File(basePath));
			} catch (Exception e) {
				TelemetryManager.log("Error deleting directory: " , basePath, e);
			}
			return uploadContentArtifact(contentId, node, uploadFile, true);
		} else {
			return ERROR(ContentErrorCodeConstants.VALIDATOR_ERROR.name(), 
					"Invalid plugin package file", ResponseCode.CLIENT_ERROR);
		}
	}
	
	@Override
	public Response upload(String contentId, Node node, String fileUrl) {
		File file = null;
		try {
			file = copyURLToFile(fileUrl);
			return upload(contentId, node, file, false);
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != file && file.exists()) file.delete();
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
		TelemetryManager.log("pluginId:" + pluginId + "ManifestId:" + id, null, Level.INFO.name());
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
	 * @see org.ekstep.taxonomy.mgr.IMimeTypeManager#publish(org.ekstep.graph.dac.model.Node)
	 */
	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {
		Response response = new Response();
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);
		
		TelemetryManager.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		TelemetryManager.log("Calling the 'Review' Initializer for Node Id: " , contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		TelemetryManager.log("Review Operation Finished Successfully for Node ID: " , contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			TelemetryManager.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " , contentId);

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		} else {
			TelemetryManager.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " , contentId);

			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}

		return response;
	}
	
	@Override
	public Response review(String contentId, Node node, boolean isAsync) {

		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		TelemetryManager.log("Calling the 'Review' Initializer for Node ID: " , contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
