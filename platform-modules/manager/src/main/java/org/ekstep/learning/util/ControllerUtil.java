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
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

// TODO: Auto-generated Javadoc
/**
 * The Class ControllerUtil, provides controller utility functionality for all
 * learning actors.
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
	 * Gets the node.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param contentId
	 *            the content id
	 * @return the node
	 */
	public Node getNode(String taxonomyId, String contentId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), contentId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return null;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

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

	/**
	 * Gets the definition.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param objectType
	 *            the object type
	 * @return the definition
	 */
	public DefinitionDTO getDefinition(String taxonomyId, String objectType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}
	
}
