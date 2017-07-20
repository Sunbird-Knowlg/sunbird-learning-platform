package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.enums.ContentErrorCodeConstants;
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
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class PluginMimeTypeMgrImpl.
 */
public class PluginMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager{
	
	

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.Node, java.io.File)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		PlatformLogger.log("Uploaded File: " , uploadFile.getName());

		ContentValidator validator = new ContentValidator();
		if (validator.isValidPluginPackage(uploadFile)) {
			PlatformLogger.log("Calling Upload Content For Node ID: " + contentId);
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
				PlatformLogger.log("Error deleting directory: " , basePath, e);
			}
			return uploadContentArtifact(contentId, node, uploadFile);
		} else {
			return ERROR(ContentErrorCodeConstants.VALIDATOR_ERROR.name(), 
					"Invalid plugin package file", ResponseCode.CLIENT_ERROR);
		}
	}
	
	@Override
	public Response upload(Node node, String fileUrl) {
		File file = null;
		try {
			file = copyURLToFile(fileUrl);
			return upload(node.getIdentifier(), node, file, false);
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
		PlatformLogger.log("pluginId:" + pluginId + "ManifestId:" + id);
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
	public Response publish(String contentId, Node node, boolean isAsync) {
		Response response = new Response();
		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);
		
		PlatformLogger.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		PlatformLogger.log("Calling the 'Review' Initializer for Node Id: " , contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		PlatformLogger.log("Review Operation Finished Successfully for Node ID: " , contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			PlatformLogger.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " , contentId);

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		} else {
			PlatformLogger.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: " , contentId);

			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}

		return response;
	}
	
	@Override
	public Response review(String contentId, Node node, boolean isAsync) {

		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Calling the 'Review' Initializer for Node ID: " , contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
