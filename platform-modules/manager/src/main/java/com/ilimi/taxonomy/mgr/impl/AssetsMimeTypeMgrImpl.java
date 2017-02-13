package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
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
		node.getMetadata().put(ContentAPIParams.status.name(), "Processing");
		LOGGER.info("Calling 'updateContentNode' for Node ID: " + node.getIdentifier());
		Response response = updateContentNode(node, urlArray[1]);
		String prevState = (String) node.getMetadata().get(ContentAPIParams.status.name());
		String mimeType = (String) node.getMetadata().get("mimeType");
		if (!checkError(response)) {
			LOGGER.info("Checking if mimeType is of image" + mimeType);
			if(StringUtils.startsWith("image", mimeType)){
				
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
		LOGGER.info("updating the node" + node);
		Response resp = updateNode(node);
		return resp;
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
