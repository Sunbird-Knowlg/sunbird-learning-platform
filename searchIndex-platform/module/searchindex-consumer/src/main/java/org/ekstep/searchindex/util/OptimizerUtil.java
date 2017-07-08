package org.ekstep.searchindex.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.DefinitionDTO;

// TODO: Auto-generated Javadoc
/**
 * The Class OptimizerUtil functionality to optimiseImage
 * operation for different resolutions.
 *
 * @author karthik
 */
public class OptimizerUtil {
	/** The logger. */
	
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The ekstep optimizr. */
	private static Optimizr ekstepOptimizr = new Optimizr();

	/** The controller util. */
	public static ControllerUtil controllerUtil = new ControllerUtil();

	/** The mapper. */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";
	
	private static final String s3Content = "s3.content.folder";
    
	private static final String s3Artifact = "s3.artifact.folder";

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
	public static Map<String, String> optimiseImage(String contentId) throws Exception {
		 Map<String, String> variantsMap = new HashMap<String, String>();
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
				LOGGER.log("optimiseImage | originalURL=" + originalURL + " | uploadedFile="
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
								LOGGER.log("Cleanup - Deleting optimised File");
								optimisedFile.delete();
							} catch (Exception e) {
								LOGGER.log("Something Went Wrong While Deleting the optimised File.", e.getMessage(), e);
							}
						}
					} else {
						variantsMap.put(resolution, originalURL);
					}

				}

				if (null != originalFile && originalFile.exists()) {
					try {
						LOGGER.log("Cleanup - Deleting Uploaded File");
						originalFile.delete();
					} catch (Exception e) {
						LOGGER.log("Something Went Wrong While Deleting the Uploaded File.", e.getMessage(), e);
					}
				}
				// delete folder created for downloading asset file
				delete(new File(tempFolder));

			} catch (Exception e) {
				LOGGER.log("Something Went Wrong While optimising image ", e.getMessage(), e);
				throw e;
			}

		}
		LOGGER.log("updated variants map" + variantsMap);
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
	private static void delete(File file) throws IOException {
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
	public static String[] uploadToAWS(File uploadedFile, String identifier) {
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
