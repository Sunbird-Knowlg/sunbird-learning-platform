package org.ekstep.searchindex.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.optimizr.Optimizr;
import org.ekstep.common.optimizr.image.ImageResolutionUtil;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.DefinitionDTO;

// TODO: Auto-generated Javadoc
/**
 * The Class OptimizerActor, provides akka actor functionality to optimiseImage
 * operation for different resolutions.
 *
 * @author karthik
 */
public class OptimizerUtil {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(OptimizerUtil.class.getName());

	/** The ekstep optimizr. */
	private Optimizr ekstepOptimizr = new Optimizr();

	/** The controller util. */
	public ControllerUtil controllerUtil = new ControllerUtil();

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";
	
	private static final String s3Content = "s3.content.folder";
    private static final String s3Artifact = "s3.artifact.folder";

    Map<String, String> variantsMap = new HashMap<String, String>();
	/**
	 * Optimise image.
	 *
	 * @param contentId
	 *            the content id
	 * @param folder
	 *            the folder
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public  Map<String, String> optimiseImage(String contentId) throws Exception {

		// get content definition to get configured resolution
		DefinitionDTO contentDefinition = controllerUtil.getDefinition("domain", "Content");
		String variantsStr = (String) contentDefinition.getMetadata().get(ContentAPIParams.variants.name());
		Map<String, Object> variants = mapper.readValue(variantsStr, Map.class);

		if (variants != null && variants.size() > 0) {
			try {
				Node node = controllerUtil.getNode("domain", contentId);
				if (node == null)
					throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
							"content is null, contentId=" + contentId);

				String originalURL = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());

				String tempFolder = tempFileLocation + File.separator + System.currentTimeMillis() + "_temp";
				File originalFile = HttpDownloadUtility.downloadFile(originalURL, tempFolder);
				LOGGER.info("optimiseImage | originalURL=" + originalURL + " | uploadedFile="
						+ originalFile.getAbsolutePath());

				// run for each resolution
				for (Map.Entry<String, Object> entry : variants.entrySet()) {
					String resolution = entry.getKey();
					Map<String, Object> variantValueMap = (Map<String, Object>) entry.getValue();
					List<Integer> dimension = (List<Integer>) variantValueMap.get("dimensions");
					int dpi = (int) variantValueMap.get("dpi");

					if (dimension == null || dimension.size() != 2)
						throw new ClientException(ContentErrorCodes.ERR_CONTENT_OPTIMIZE.name(),
								"Image Resolution/variants is not configured for content optimization");

					if (ImageResolutionUtil.isImageOptimizable(originalFile, dimension.get(0), dimension.get(1))) {
						double targetResolution = ImageResolutionUtil.getOptimalDPI(originalFile, dpi);
						File optimisedFile = ekstepOptimizr.optimizeImage(originalFile, targetResolution,
								dimension.get(0), dimension.get(1), resolution);
						String[] optimisedURLArray = uploadToAWS(optimisedFile, contentId);
						variantsMap.put(resolution, optimisedURLArray[1]);

						if (null != optimisedFile && optimisedFile.exists()) {
							try {
								LOGGER.info("Cleanup - Deleting optimised File");
								optimisedFile.delete();
							} catch (Exception e) {
								LOGGER.error("Something Went Wrong While Deleting the optimised File.", e);
							}
						}
					} else {
						variantsMap.put(resolution, originalURL);
					}

				}

				if (null != originalFile && originalFile.exists()) {
					try {
						LOGGER.info("Cleanup - Deleting Uploaded File");
						originalFile.delete();
					} catch (Exception e) {
						LOGGER.error("Something Went Wrong While Deleting the Uploaded File.", e);
					}
				}
				// delete folder created for downloading asset file
				delete(new File(tempFolder));

			} catch (Exception e) {
				LOGGER.error("Something Went Wrong While optimising image ", e);
				throw e;
			}

		}
		return variantsMap;
	}

	/**
	 * Delete.
	 *
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
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
	public String[] uploadToAWS(File uploadedFile, String identifier) {
		String[] urlArray = new String[] {};
		try {
			String folder = S3PropertyReader.getProperty(s3Content);
        	folder = folder + "/" + Slug.makeSlug(identifier, true) + "/" + S3PropertyReader.getProperty(s3Artifact);
			urlArray = AWSUploader.uploadFile(folder, uploadedFile);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
					"Error wihile uploading the File.", e);
		}
		return urlArray;
	}
}
