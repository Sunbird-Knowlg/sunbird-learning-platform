package com.ilimi.taxonomy.mgr.impl;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

/**
 * The Class APKMimeTypeMgrImpl is a implementation of IMimeTypeManager for
 * Mime-Type as <code>application/vnd.android.package-archive</code> or for APK
 * Content.
 * 
 * @author Azhar
 * 
 * @see IMimeTypeManager
 * @see HTMLMimeTypeMgrImpl
 * @see AssetsMimeTypeMgrImpl
 * @see ECMLMimeTypeMgrImpl
 * @see CollectionMimeTypeMgrImpl
 */
@Component
public class APKMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/* Logger */
	private static Logger LOGGER = LogManager.getLogger(APKMimeTypeMgrImpl.class.getName());

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

		LOGGER.info("Calling Upload Content For Node ID: " + node.getIdentifier());
		return uploadContentArtifact(node, uploadFile);
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

		LOGGER.info("Calling the 'rePublish' for Node ID: " + node.getIdentifier());
		return rePublish(node);
	}

}
