package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.optimizr.FileType;
import org.ekstep.common.optimizr.FileUtils;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.common.enums.LearningOperations;
import org.springframework.stereotype.Component;

import com.google.common.io.Files;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
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
	public Response upload(Node node, File uploadFile, String folder) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadFile.getName());
		LOGGER.debug("Uploading to Folder: " + folder);

		LOGGER.info("Calling Upload Content Node For Node ID: " + node.getIdentifier());
		String[] urlArray = uploadToAWS(uploadFile, folder);

		LOGGER.info("Updating the Content Node for Node ID: " + node.getIdentifier());
		node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
		node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
		node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));
		node.getMetadata().put(ContentAPIParams.status.name(), "Live");
		Map<String, String> resolutionMap = new HashMap<String, String>();
		node.getMetadata().put(ContentAPIParams.resolutions.name(), resolutionMap);
		
		LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
		Response response = updateContentNode(node, urlArray[1]);
		
		FileType type = FileUtils.getFileType(uploadFile);
		// Call async image optimiser for configured resolutions if asset type is image
		if(type == FileType.Image){
			//make async request to image optimiser actor
			Request request = getLearningRequest(LearningActorNames.OPTIMIZER_ACTOR.name(), LearningOperations.optimiseImage.name());
			request.put(ContentAPIParams.content_id.name(), node.getIdentifier());
			makeAsyncLearningRequest(request, LOGGER);
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
	public Response publish(Node node) {
		LOGGER.debug("Node: ", node);

		LOGGER.info("Updating the Content Node (Making the 'status' property as 'Live')  for Node ID: "
				+ node.getIdentifier());
		node.getMetadata().put("status", "Live");

		LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
		return updateContentNode(node, null);
	}

}
