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
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class DocumentMimeTypeManager is a implementation of IMimeTypeManager for
 * Mime-Type as <code>application/pdf</code>, <code>application/msword</code>
 * for Content creation.
 * 
 * @author Rashmi
 * 
 * @see IMimeTypeManager
 */
public class DocumentMimeTypeManager extends BaseMimeTypeManager implements IMimeTypeManager {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(DocumentMimeTypeManager.class.getName());

	/** The default temp location */
	private static final String tempFileLocation = "/data/contentBundle/";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(Node node, File uploadedFile, boolean isAsync) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadedFile.getName());

		LOGGER.info("Calling Upload Content For Node ID: " + node.getIdentifier());
		return uploadContentArtifact(node, uploadedFile);
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
		Response response = new Response();
		LOGGER.debug("Node: ", node);
		if (null != node.getMetadata().get("artifactUrl")) {
			String url = (String) node.getMetadata().get("artifactUrl");
			if (!StringUtils.startsWith(url, "https://ekstep-public.s3-ap-southeast-1")) {
				downloadDocument(node);
			}
		}
		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline for Node Id: " + node.getIdentifier());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#review(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response review(Node node, boolean isAsync) {
		LOGGER.debug("Node: ", node);

		LOGGER.info("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + node.getIdentifier());
		InitializePipeline pipeline = new InitializePipeline(getBasePath(node.getIdentifier()), node.getIdentifier());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);

		LOGGER.info("Calling the 'Review' Initializer for Node ID: " + node.getIdentifier());
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

	/**
	 * The method uploadArtifactsToS3 is used to download the file from
	 * artifactUrl and upload it to s3 and set the s3 url as artifactUrl
	 */
	private Response downloadDocument(Node node) {
		LOGGER.info("Getting artifactUrl from node");
		String artifactUrl = (String) node.getMetadata().get("artifactUrl");

		File file = HttpDownloadUtility.downloadFile(artifactUrl, tempFileLocation);
		LOGGER.info("Downloading artifactUrl file to local system" + file);

		if (null != file) {
			LOGGER.info("Calling uploadContentArtifact method to upload file to s3");
			Response response = uploadContentArtifact(node, file);
			if (!(checkError(response))) {
				LOGGER.info("returning response" + response);
				return response;
			}
		}
		return null;
	}

	/**
	 * The method uploadContentArtifact uploads the content artifacts to s3 and
	 * set the s3 url as artifactUrl
	 * 
	 * @param node
	 * @param uploadedFile
	 * @return
	 */
	public Response uploadContentArtifact(Node node, File uploadedFile) {
		try {
			Response response = new Response();
			LOGGER.info("Verifying the MimeTypes.");
			Tika tika = new Tika(new MimeTypes());
			String mimeType = tika.detect(uploadedFile);
			String nodeMimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
			LOGGER.debug("Uploaded  MimeType: " + mimeType);
			if (!StringUtils.equalsIgnoreCase(mimeType, nodeMimeType))
				LOGGER.warn("Uploaded File MimeType is not same as Node (Object) MimeType. [Uploaded MimeType: "
						+ mimeType + " | Node (Object) MimeType: " + nodeMimeType + "]");

			LOGGER.info("Calling Upload Content Node For Node ID: " + node.getIdentifier());
			String[] urlArray = uploadArtifactToAWS(uploadedFile, node.getIdentifier());

			LOGGER.info("Updating the Content Node for Node ID: " + node.getIdentifier());
			node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
			node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
			node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));
			response = updateContentNode(node, urlArray[1]);

			LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
			response = updateContentNode(node, urlArray[1]);
			if (!checkError(response)) {
				return response;
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
					"Error! Something went Wrong While Uploading the file. | [Node Id: " + node.getIdentifier() + "]");
		}
		return null;
	}
}
