package com.ilimi.taxonomy.mgr.impl;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.ContentAPIParams;
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
	public Response upload(Node node, File uploadFile) {
		LOGGER.debug("Node: ", node);
		LOGGER.debug("Uploaded File: " + uploadFile.getName());

		LOGGER.info("Calling Upload Content Node For Node ID: " + node.getIdentifier());
		String[] urlArray = uploadArtifactToAWS(uploadFile, node.getIdentifier());

		LOGGER.info("Updating the Content Node for Node ID: " + node.getIdentifier());
		node.getMetadata().put(ContentAPIParams.s3Key.name(), urlArray[0]);
		node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
		node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
		node.getMetadata().put(ContentAPIParams.size.name(), getS3FileSize(urlArray[0]));

		LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
		return updateContentNode(node, urlArray[1]);
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
