package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.ekstep.common.optimizr.FileType;
import org.ekstep.common.optimizr.FileUtils;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.common.enums.LearningOperations;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

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
@Component("AssetsMimeTypeMgrImpl")
public class AssetsMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/* Logger */
	private static Logger LOGGER = LogManager.getLogger(AssetsMimeTypeMgrImpl.class.getName());

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(Node node, File uploadFile, boolean isAsync) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadFile.getName());

		Response response = new Response();
		try {
			LOGGER.info("Verifying the MimeTypes.");
			Tika tika = new Tika(new MimeTypes());
			String mimeType = tika.detect(uploadFile);
			String nodeMimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
			LOGGER.debug("Uploaded Asset MimeType: " + mimeType);
			if (!StringUtils.equalsIgnoreCase(mimeType, nodeMimeType))
				LOGGER.warn("Uploaded File MimeType is not same as Node (Object) MimeType. [Uploaded MimeType: "
						+ mimeType + " | Node (Object) MimeType: " + nodeMimeType + "]");

			LOGGER.info("Calling Upload Content Node For Node ID: " + node.getIdentifier());
			String[] urlArray = uploadArtifactToAWS(uploadFile, node.getIdentifier());

			LOGGER.info("Updating the Content Node for Node ID: " + node.getIdentifier());
			node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
			node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));

			node.getMetadata().put(ContentAPIParams.status.name(), "Processing");
			LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
			response = updateContentNode(node, urlArray[1]);
			String prevState = (String) node.getMetadata().get(ContentAPIParams.status.name());
			if (!checkError(response)) {
					if ((StringUtils.equalsIgnoreCase(node.getMetadata().get("contentType").toString(), "Asset"))
						&& (StringUtils.equalsIgnoreCase(node.getMetadata().get("mediaType").toString(), "image"))) {
								LOGGER.info("Initiatizing variants map if mimeType is image");

								Map<String, String> variantsMap = new HashMap<String, String>();
								node.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);
								node.getMetadata().put("prevState", prevState);

								LOGGER.info("Generating Telemetry Event. | [Content ID: " + node.getIdentifier()+ "]");
								LogTelemetryEventUtil.logContentLifecycleEvent(node.getIdentifier(), node.getMetadata());
					}
					else{

						LOGGER.info("Updating status to Live for mimeTypes other than image");
						node.getMetadata().put(ContentAPIParams.status.name(), "Live");
					}
			}

			LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
			response = updateContentNode(node, urlArray[1]);

			FileType type = FileUtils.getFileType(uploadFile);
			// Call async image optimiser for configured resolutions if asset
			// type is image
			if (type == FileType.Image) {
				// make async request to image optimiser actor
				Request request = getLearningRequest(LearningActorNames.OPTIMIZER_ACTOR.name(),
						LearningOperations.optimizeImage.name());
				request.put(ContentAPIParams.content_id.name(), node.getIdentifier());
				makeAsyncLearningRequest(request, LOGGER);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model
	 * .Node)
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
