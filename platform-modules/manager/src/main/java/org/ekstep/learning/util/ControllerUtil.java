package org.ekstep.learning.util;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.AWSUploader;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

// TODO: Auto-generated Javadoc
/**
 * The Class ControllerUtil, provides controller utility functionality for all
 * learning actors
 *
 * @author karthik
 */
public class ControllerUtil extends BaseLearningManager {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ControllerUtil.class.getName());

	/** The Constant bucketName. */
	private static final String bucketName = "ekstep-public";

	/** The Constant folderName. */
	private static final String folderName = "content";

	/**
	 * Update node.
	 *
	 * @param node
	 *            the node
	 * @return the response
	 */
	public Response updateNode(Node node) {
		Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
	}

	/**
	 * Upload to AWS.
	 *
	 * @param uploadedFile
	 *            the uploaded file
	 * @param folder
	 *            the folder
	 * @return the string[]
	 */
	public String[] uploadToAWS(File uploadedFile, String folder) {
		String[] urlArray = new String[] {};
		try {
			if (StringUtils.isBlank(folder))
				folder = folderName;
			urlArray = AWSUploader.uploadFile(bucketName, folder, uploadedFile);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
					"Error wihile uploading the File.", e);
		}
		return urlArray;
	}

}
