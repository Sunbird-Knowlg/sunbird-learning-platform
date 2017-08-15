package org.ekstep.content.mimetype.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentOperations;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.AsyncContentOperationUtil;
import org.ekstep.content.validator.ContentValidator;
import org.ekstep.learning.common.enums.ContentAPIParams;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.
	 * Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, Node node, File uploadedFile, boolean isAsync) {
		PlatformLogger.log("Uploaded File: " + uploadedFile.getName());
		PlatformLogger.log("Calling Upload Content For Node ID: " + node.getIdentifier());
		String mimeType = (String)node.getMetadata().get("mimeType");
		File file = null;
		if(StringUtils.equalsIgnoreCase(mimeType, "application/epub")){
			if (StringUtils.endsWith(uploadedFile.getName(), ".epub")) {
				file = new File("index.epub");
				if(!file.exists()){
					try {
						FileUtils.moveFile(uploadedFile,file);
						return uploadContentArtifact(contentId, node, file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					return uploadContentArtifact(contentId, node, file);
				}
			}
		}
		return uploadContentArtifact(contentId, node, uploadedFile);
	}
	
	@Override
	public Response upload(Node node, String fileUrl) {
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), fileUrl);
		return updateContentNode(node.getIdentifier(), node, fileUrl);
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
		Response response = new Response();
		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline for Node Id: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), false);

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

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#review(com.ilimi.graph.dac.model.
	 *      Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response review(String contentId, Node node, boolean isAsync) {

		PlatformLogger.log("Preparing the Parameter Map for Initializing the Pipeline For Node ID: " + contentId);
		InitializePipeline pipeline = new InitializePipeline(getBasePath(contentId), contentId);
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ContentAPIParams.node.name(), node);
		parameterMap.put(ContentAPIParams.ecmlType.name(), true);

		PlatformLogger.log("Calling the 'Review' Initializer for Node ID: " , contentId);
		return pipeline.init(ContentAPIParams.review.name(), parameterMap);
	}

	/**
	 * The method uploadContentArtifact uploads the content artifacts to s3 and
	 * set the s3 url as artifactUrl
	 * 
	 * @param node
	 * @param uploadedFile
	 * @return
	 */
	public Response uploadContentArtifact(String contentId, Node node, File uploadedFile) {
		try {
			Response response = new Response();
			PlatformLogger.log("Verifying the MimeTypes.");
			String mimeType = (String) node.getMetadata().get("mimeType");
			ContentValidator validator = new ContentValidator();
			if(validator.exceptionChecks(mimeType, uploadedFile)){
				
				PlatformLogger.log("Calling Upload Content Node For Node ID: " , contentId);
				String[] urlArray = uploadArtifactToAWS(uploadedFile, contentId);
	
				node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
				node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
				node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));
	
				PlatformLogger.log("Calling 'updateContentNode' for Node ID: " , contentId);
				response = updateContentNode(contentId, node, urlArray[1]);
				if (!checkError(response)) {
					return response;
				}
			}
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
