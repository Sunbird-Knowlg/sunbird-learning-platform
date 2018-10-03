package org.ekstep.jobs.samza.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Slug;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.optimizr.FileType;
import org.ekstep.common.optimizr.FileUtils;
import org.ekstep.common.optimizr.image.ImageResolutionUtil;
import org.ekstep.common.optimizr.image.ResizeImagemagickProcessor;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;

/**
 * The Class OptimizerUtil functionality to optimiseImage operation for different resolutions.
 *
 * @author Rashmi N
 */
public class OptimizerUtil {
	/** The logger. */

	static JobLogger LOGGER = new JobLogger(OptimizerUtil.class);

	/** The controller util. */
	public static ControllerUtil controllerUtil = new ControllerUtil();

	/** The mapper. */
	private static ObjectMapper mapper = new ObjectMapper();

	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";

	private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";

	/**
	 * Optimise image.
	 *
	 * @param contentId the content id
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> optimizeImage(String contentId, String tempFileLocation, Node node) throws Exception {

		String originalURL = (String) node.getMetadata().get(ContentAPIParams.downloadUrl.name());
		LOGGER.info("Optimizing image - " + contentId + " | URL:" + originalURL);
		Map<String, String> variantsMap = new HashMap<String, String>();
		// get content definition to get configured resolution
		DefinitionDTO contentDefinition = controllerUtil.getDefinition("domain", "Content");
		String variantsStr = (String) contentDefinition.getMetadata().get(ContentAPIParams.variants.name());
		Map<String, Object> variants = mapper.readValue(variantsStr, Map.class);

		if (variants != null && variants.size() > 0) {

			String tempFolder = tempFileLocation + File.separator + System.currentTimeMillis() + "_temp";
			File originalFile = HttpDownloadUtility.downloadFile(originalURL, tempFolder);

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
					File optimisedFile = optimizeImage(originalFile, targetResolution, dimension.get(0), dimension.get(1), resolution);

					if (null != optimisedFile && optimisedFile.exists()) {
						String[] optimisedURLArray = uploadToAWS(optimisedFile, contentId);
						variantsMap.put(resolution, optimisedURLArray[1]);
						delete(optimisedFile);
					}
				} else {
					variantsMap.put(resolution, originalURL);
				}
			}

			if (null != originalFile && originalFile.exists()) {
				delete(originalFile);
			}
			// delete folder created for downloading asset file
			delete(new File(tempFolder));

		} else {
			LOGGER.info("No variants found for optimization" + contentId);
		}
		return variantsMap;
	}

	private static File optimizeImage(File file, double dpi, int width, int height, String resolution) throws Exception {
		ResizeImagemagickProcessor proc = new ResizeImagemagickProcessor();

		File output = null;
		FileType type = FileUtils.getFileType(file);

		if (proc.isApplicable(type)) {
			try {
				output = proc.process(file, dpi, width, height, resolution);
			} catch (Exception ex) {

			}
		}

		return output;
	}

	/**
	 * Delete.
	 *
	 * @param file the file
	 * @throws IOException Signals that an I/O exception has occurred.
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
	 * @param uploadedFile the uploaded file
	 * @return the string[]
	 */
	public static String[] uploadToAWS(File uploadedFile, String identifier) {
		String[] urlArray = new String[] {};
		try {
			String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
			folder = folder + "/" + Slug.makeSlug(identifier, true) + "/" + S3PropertyReader.getProperty(ARTEFACT_FOLDER);
			urlArray = CloudStore.uploadFile(folder, uploadedFile, true);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(), "Error wihile uploading the File.", e);
		}
		return urlArray;
	}
}
