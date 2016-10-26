package org.ekstep.learning.actor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.optimizr.image.ImageResolutionUtil;
import org.ekstep.learning.common.enums.LearningErrorCodes;
import org.ekstep.learning.common.enums.LearningOperations;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

import akka.actor.ActorRef;

// TODO: Auto-generated Javadoc
/**
 * The Class OptimizerActor, provides akka actor functionality to optimiseImage
 * operation for different resolutions
 *
 * @author karthik
 */
public class OptimizerActor extends BaseGraphManager {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(OptimizerActor.class.getName());

	/** The ekstep optimizr. */
	private Optimizr ekstepOptimizr = new Optimizr();

	/** The controller util. */
	private ControllerUtil controllerUtil = new ControllerUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
		Request request = (Request) msg;
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LearningOperations.optimiseImage.name(), operation)) {
				File uploadedFile = (File) request.get(ContentAPIParams.uploadFile.name());
				Node node = (Node) request.get(ContentAPIParams.node.name());
				String folder = (String) request.get(ContentAPIParams.folder.name());
				Map<String, Object> resolutions = (Map<String, Object>) request
						.get(ContentAPIParams.resolutions.name());
				optimiseImage(node, uploadedFile, folder, resolutions);
				OK(sender());
			} else {
				LOGGER.info("Unsupported operation: " + operation);
				throw new ClientException(LearningErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			LOGGER.error("Error in optimizer actor", e);
			handleException(e, getSender());
		}
	}

	/**
	 * Optimise image.
	 *
	 * @param node
	 *            the node
	 * @param uploadedFile
	 *            the uploaded file
	 * @param folder
	 *            the folder
	 * @param resolutions
	 *            the resolutions
	 * @throws Exception
	 *             the exception
	 */
	private void optimiseImage(Node node, File uploadedFile, String folder, Map<String, Object> resolutions)
			throws Exception {

		try {

			String originalURL = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
			LOGGER.info(
					"optimiseImage | originalURL=" + originalURL + " | uploadedFile=" + uploadedFile.getAbsolutePath());

			Map<String, String> resolutionMap = new HashMap<String, String>();

			for (Map.Entry<String, Object> entry : resolutions.entrySet()) {
				String resolution = entry.getKey();
				Map<String, Object> resolutionValueMap = (Map<String, Object>) entry.getValue();
				List<Integer> dimension = (List<Integer>) resolutionValueMap.get("dimensions");
				int dpi = (int) resolutionValueMap.get("dpi");

				if (dimension == null || dimension.size() != 2)
					throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
							"Image Resolution is not configured for content optimization");

				if (ImageResolutionUtil.isImageOptimizable(uploadedFile, dimension.get(0), dimension.get(1))) {
					double targetResolution = ImageResolutionUtil.getOptimalDPI(uploadedFile, dpi);
					File optimisedMedFile = ekstepOptimizr.optimizeImage(uploadedFile, targetResolution,
							dimension.get(0), dimension.get(1), resolution);
					String[] optimisedURLArray = controllerUtil.uploadToAWS(optimisedMedFile, folder);
					resolutionMap.put(resolution, optimisedURLArray[1]);

					if (null != optimisedMedFile && optimisedMedFile.exists()) {
						try {
							LOGGER.info("Cleanup - Deleting optimised File");
							optimisedMedFile.delete();
						} catch (Exception e) {
							LOGGER.error("Something Went Wrong While Deleting the optimised File.", e);
						}
					}
				} else {
					resolutionMap.put(resolution, originalURL);
				}

			}

			if (null != uploadedFile && uploadedFile.exists()) {
				try {
					LOGGER.info("Cleanup - Deleting Uploaded File");
					uploadedFile.delete();
				} catch (Exception e) {
					LOGGER.error("Something Went Wrong While Deleting the Uploaded File.", e);
				}
			}

			node.getMetadata().put(ContentAPIParams.resolutions.name(), resolutionMap);

		} catch (Exception e) {
			LOGGER.error("Something Went Wrong While optimising image ", e);
			throw e;
		} finally {
			node.getMetadata().put(ContentAPIParams.status.name(), "Draft");
			controllerUtil.updateNode(node);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.common.mgr.BaseGraphManager#invokeMethod(com.ilimi.common
	 * .dto.Request, akka.actor.ActorRef)
	 */
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {

	}

}
