package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.common.ExtractionType;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.content.util.ContentPackageExtractionUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

public class H5PMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		Response response = new Response();
		if (isValidH5PContentPackage(uploadFile)) {
			response = uploadH5PContent(contentId, node, uploadFile);
		}
		return response;
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
			if (null != file && file.exists())
				file.delete();
		}
	}

	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {
		PlatformLogger.log("Node: ", node.getIdentifier());

		Response response = new Response();
		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		PlatformLogger.log("Calling the 'Review' Initializer for Node Id: " + contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		PlatformLogger.log("Review Operation Finished Successfully for Node ID: " , contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			PlatformLogger.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: " ,contentId);

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
		PlatformLogger.log("Node: ", node.getIdentifier());

		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: ", node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Calling the 'Review' Initializer for Node ID: ", node.getIdentifier());
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

	private boolean isValidH5PContentPackage(File uploadedFile) {
		boolean isValid = true;
		if (BooleanUtils
				.isFalse(hasGivenFile(uploadedFile, ContentConfigurationConstants.DEFAULT_H5P_JSON_FILE_LOCATION)))
			throw new ClientException(ContentErrorCodeConstants.UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR
							+ " | ['h5p.json' File is Missing.]");
		return isValid;
	}

	private Response uploadH5PContent(String contentId, Node node, File uploadFile) {
		Response response = new Response();
		// Upload H5P File to AWS
		String[] urlArray = uploadArtifactToAWS(uploadFile, contentId);
		// Update Node with 'artifactUrl'
		node.getMetadata().put("s3Key", urlArray[0]);
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
		// Extract Content Package
		ContentPackageExtractionUtil contentPackageExtractionUtil = new ContentPackageExtractionUtil();
		contentPackageExtractionUtil.extractContentPackage(contentId, node, uploadFile, ExtractionType.snapshot, false);
		response = updateContentNode(contentId, node, urlArray[1]);
		return response;
	}

}
