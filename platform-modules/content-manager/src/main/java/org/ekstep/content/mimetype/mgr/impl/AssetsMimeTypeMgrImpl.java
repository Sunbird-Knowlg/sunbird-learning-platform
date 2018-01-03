package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.common.enums.TaxonomyErrorCodes;


// TODO: Auto-generated Javadoc
/**
 * The Class AssetsMimeTypeMgrImpl is a implementation of IMimeTypeManager for
 * Mime-Type as <code>assets</code> or for Asset type Content.
 *
 * @author Azhar
 *
 * @see IMimeTypeManager
 * @see HTMLMimeTypeMgrImpl
 * @see APKMimeTypeMgrImpl
 * @see ECMLMimeTypeMgrImpl
 * @see CollectionMimeTypeMgrImpl
 */
public class AssetsMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ekstep.taxonomy.mgr.IMimeTypeManager#upload(org.ekstep.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		TelemetryManager.log("Node: ", node.getIdentifier());
		TelemetryManager.log("Uploaded File: " + uploadFile.getName());

		Response response = new Response();
		try {
			TelemetryManager.log("Verifying the MimeTypes.");
			Tika tika = new Tika(new MimeTypes());
			String mimeType = tika.detect(uploadFile);
			String nodeMimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
			TelemetryManager.log("Uploaded Asset MimeType: ", mimeType);
			if (!StringUtils.equalsIgnoreCase(mimeType, nodeMimeType))
				TelemetryManager.log("Uploaded File MimeType is not same as Node (Object) MimeType. [Uploaded MimeType: "
						+ mimeType + " | Node (Object) MimeType: " + nodeMimeType + "]");

			TelemetryManager.log("Calling Upload Content Node For Node ID: " + node.getIdentifier());
			String[] urlArray = uploadArtifactToAWS(uploadFile, node.getIdentifier());

			TelemetryManager.log("Updating the Content Node for Node ID: ", node.getIdentifier());
			node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
			node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));
			if (StringUtils.equalsIgnoreCase(node.getMetadata().get("mediaType").toString(), "image")) {
				node.getMetadata().put(ContentAPIParams.status.name(), "Processing");
				response = updateContentNode(contentId, node, urlArray[1]);
			} else {
				node.getMetadata().put(ContentAPIParams.status.name(), "Live");
				response = updateContentNode(contentId, node, urlArray[1]);
			}

		} catch (IOException e) {
			throw new ServerException(ContentAPIParams.FILE_ERROR.name(),
					"Error! While Reading the MimeType of Uploaded File. | [Node Id: " + node.getIdentifier() + "]");
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentAPIParams.SERVER_ERROR.name(),
					"Error! Something went Wrong While Uploading an Asset. | [Node Id: " + node.getIdentifier() + "]");
		}

		return response;
	}
	
	@Override
	public Response upload(String contentId, Node node, String fileUrl) {
		File file = null;
		try {
			file = copyURLToFile(fileUrl);
			node.getMetadata().put(ContentAPIParams.artifactUrl.name(), fileUrl);
			node.getMetadata().put(ContentAPIParams.downloadUrl.name(), fileUrl);
			node.getMetadata().put(ContentAPIParams.size.name(), getFileSize(file));
			Response response;
			if (StringUtils.equalsIgnoreCase(node.getMetadata().get("mediaType").toString(), "image")) {
				node.getMetadata().put(ContentAPIParams.status.name(), "Processing");
				response = updateContentNode(node.getIdentifier(), node, fileUrl);
			} else {
				node.getMetadata().put(ContentAPIParams.status.name(), "Live");
				response = updateContentNode(contentId, node, fileUrl);
			}
			if (null != file && file.exists()) file.delete();
			return response;
		} catch (IOException e) {
			throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE_URL.name(), "fileUrl is invalid.");
		} finally {
			if (null != file && file.exists()) file.delete();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ekstep.taxonomy.mgr.IMimeTypeManager#publish(org.ekstep.graph.dac.model
	 * .Node)
	 */
	@Override
	public Response publish(String contentId, Node node, boolean isAsync) {
		TelemetryManager.log("Node: ", node.getIdentifier());

		Response response = new Response();
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		TelemetryManager.log("Adding 'isPublishOperation' Flag to 'true'");
		parameterMap.put(ContentAPIParams.isPublishOperation.name(), true);

		TelemetryManager.log("Calling the 'Review' Initializer for Node Id: ", contentId);
		response = pipeline.init(ContentAPIParams.review.name(), parameterMap);
		TelemetryManager.log("Review Operation Finished Successfully for Node ID: ", contentId);

		if (BooleanUtils.isTrue(isAsync)) {
			AsyncContentOperationUtil.makeAsyncOperation(ContentOperations.PUBLISH, contentId, parameterMap);
			TelemetryManager.log("Publish Operation Started Successfully in 'Async Mode' for Node Id: ", contentId);

			response.put(ContentAPIParams.publishStatus.name(),
					"Publish Operation for Content Id '" + contentId + "' Started Successfully!");
		} else {
			TelemetryManager.log("Publish Operation Started Successfully in 'Sync Mode' for Node Id: ", contentId);
			response = pipeline.init(ContentAPIParams.publish.name(), parameterMap);
		}
		return response;
	}

	@Override
	public Response review(String contentId, Node node, boolean isAsync) {
		TelemetryManager.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: ", contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		TelemetryManager.log("Calling the 'Review' Initializer for Node ID: " + contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
