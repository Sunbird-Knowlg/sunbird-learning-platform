package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.dac.model.Node;

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
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync) {
		PlatformLogger.log("Node: ", node.getIdentifier());
		PlatformLogger.log("Uploaded File: " + uploadFile.getName());

		Response response = new Response();
		try {
			PlatformLogger.log("Verifying the MimeTypes.");
			Tika tika = new Tika(new MimeTypes());
			String mimeType = tika.detect(uploadFile);
			String nodeMimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
			PlatformLogger.log("Uploaded Asset MimeType: " , mimeType);
			if (!StringUtils.equalsIgnoreCase(mimeType, nodeMimeType))
				PlatformLogger.log("Uploaded File MimeType is not same as Node (Object) MimeType. [Uploaded MimeType: "
						+ mimeType + " | Node (Object) MimeType: " + nodeMimeType + "]");

			PlatformLogger.log("Calling Upload Content Node For Node ID: " + node.getIdentifier());
			String[] urlArray = uploadArtifactToAWS(uploadFile, node.getIdentifier());

			PlatformLogger.log("Updating the Content Node for Node ID: " , node.getIdentifier());
			node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
			node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));

			// Making all the assets by default 'Live' since we have discontinued the image Processor for time being
//			node.getMetadata().put(ContentAPIParams.status.name(), "Processing");
			node.getMetadata().put(ContentAPIParams.status.name(), ContentAPIParams.Live.name());
			response = updateContentNode(contentId, node, urlArray[1]);
			String prevState = (String) node.getMetadata().get(ContentAPIParams.status.name());
			if (!checkError(response)) {
					if ((StringUtils.equalsIgnoreCase(node.getMetadata().get("contentType").toString(), "Asset"))
						&& (StringUtils.equalsIgnoreCase(node.getMetadata().get("mediaType").toString(), "image"))) {
								Map<String, String> variantsMap = new HashMap<String, String>();
								node.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);
								node.getMetadata().put("prevState", prevState);

								PlatformLogger.log("Generating Telemetry Event. | [Content ID: " , node.getIdentifier());
								LogTelemetryEventUtil.logContentLifecycleEvent(node.getIdentifier(), node.getMetadata());
					}
					else {
						PlatformLogger.log("Updating status to Live for mimeTypes other than image", node.getMetadata().get("contentType").toString());
						node.getMetadata().put(ContentAPIParams.status.name(), "Live");
					}
			}

			PlatformLogger.log("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
			response = updateContentNode(contentId, node, urlArray[1]);

//			FileType type = FileUtils.getFileType(uploadFile);
//			// Call async image optimiser for configured resolutions if asset
//			// type is image
//			if (type == FileType.Image) {
//				// make async request to image optimiser actor
//				Request request = getLearningRequest(LearningActorNames.OPTIMIZER_ACTOR.name(),
//						LearningOperations.optimizeImage.name());
//				request.put(ContentAPIParams.content_id.name(), node.getIdentifier());
//				makeAsyncLearningRequest(request);
//			}


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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model
	 * .Node)
	 */
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
		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " , contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

		PlatformLogger.log("Calling the 'Review' Initializer for Node ID: " + contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

}
