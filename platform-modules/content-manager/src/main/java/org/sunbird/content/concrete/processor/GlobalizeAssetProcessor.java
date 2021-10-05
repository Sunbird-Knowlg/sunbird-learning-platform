package org.sunbird.content.concrete.processor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Slug;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The Class GlobalizeAssetProcessor.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 */
public class GlobalizeAssetProcessor extends AbstractProcessor {

	private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
    private static final String ASSETS_FOLDER = "cloud_storage.asset.folder";

	/**
	 * Instantiates a new <code>GlobalizeAssetProcessor</code> and sets the base
	 * path and current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public GlobalizeAssetProcessor(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.taxonomy.content.processor.AbstractProcessor#process(org.sunbird.
	 * taxonomy.content.entity.Plugin)
	 */
	@Override
	protected Plugin process(Plugin plugin) {
		try {
			if (null != plugin) {
				TelemetryManager.log("Starting the Process. | [Content Id '" + contentId + "']");
				List<Media> medias = getMedia(plugin);
				TelemetryManager.log("Total Medias: " + medias.size() + " | [Content Id '" + contentId + "']");
				Map<String, String> uploadedAssetsMap = uploadAssets(medias);
				Manifest manifest = plugin.getManifest();
				TelemetryManager.log("Setting the Medias in Manifest. | [Content Id '" + contentId + "']");
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithUrl(uploadedAssetsMap, medias));
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new ServerException(ContentErrorCodeConstants.ASSET_UPLOAD_ERROR.name(),
					ContentErrorMessageConstants.ASSET_UPLOAD_ERROR + " | [GlobalizeAssetProcessor]", e);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(),
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [GlobalizeAssetProcessor]", e);
		}

		return plugin;
	}

	/**
	 * Gets the folder path of <code>src</code>.
	 *
	 * @param src
	 *            the <code>src</code> attribute.
	 * @return the folder path.
	 */
	@SuppressWarnings("unused")
	private String getFolderPath(String src) {
		String path = "";
		if (StringUtils.isNotBlank(src)) {
			path = FilenameUtils.getPathNoEndSeparator(src);
		}
		return path;
	}

	/**
	 * <code>uploadAssets</code> is a method which uploads all the
	 * <code>assets</code> to the storage space in concurrent way.
	 *
	 * @param medias
	 *            the medias is a <code>list</code> of <code>ECRF Media</code>
	 *            Section.
	 * @return the <code>map</code> of uploaded medias and updated
	 *         <code>src</code> attributes
	 * @throws <code>InterruptedException</code>
	 *             when the Concurrent Upload Process is stopped.
	 * @throws <code>ExecutionException</code>
	 *             thrown by erroneous condition while executing the threads.
	 */
	private Map<String, String> uploadAssets(List<Media> medias) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != medias && StringUtils.isNotBlank(basePath)) {
			TelemetryManager.log("Starting the Fan-out for Upload. | [Content Id '" + contentId + "']");
			ExecutorService pool = Executors.newFixedThreadPool(10);
			List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(medias.size());
			for (final Media media : medias) {
				TelemetryManager.log("Adding All Medias as Task fro Upload. | [Content Id '" + contentId + "']");
				tasks.add(new Callable<Map<String, String>>() {
					public Map<String, String> call() throws Exception {
						Map<String, String> uploadMap = new HashMap<String, String>();
						if (StringUtils.isNotBlank(media.getId()) && StringUtils.isNotBlank(media.getSrc())
								&& StringUtils.isNotBlank(media.getType())) {
							File uploadFile;
							if (isWidgetTypeAsset(media.getType()))
								uploadFile = new File(
										basePath + File.separator + ContentWorkflowPipelineParams.widgets.name()
												+ File.separator + media.getSrc());
							else
								uploadFile = new File(
										basePath + File.separator + ContentWorkflowPipelineParams.assets.name()
												+ File.separator + media.getSrc());
							TelemetryManager.log("Upload File: | [Content Id '" + contentId + "']");
							String[] uploadedFileUrl;
							if (uploadFile.exists()) {
								String folderName = S3PropertyReader.getProperty(CONTENT_FOLDER) + "/"
										+ Slug.makeSlug(contentId, true);
								String path = S3PropertyReader.getProperty(ASSETS_FOLDER);
								/*String folderName = ContentConfigurationConstants.FOLDER_NAME + "/"
										+ Slug.makeSlug(contentId, true);
								String path = getFolderPath(media.getSrc());*/
								TelemetryManager.log("Folder to Upload: " + folderName + "| [Content Id '" + contentId + "']");
								TelemetryManager.log("Path to Upload: " + path + "| [Content Id '" + contentId + "']");
								if (StringUtils.isNotBlank(path))
									folderName = folderName + "/" + path;
								folderName = folderName + "/" + System.currentTimeMillis();
								uploadedFileUrl = CloudStore.uploadFile(folderName, uploadFile, true);
								if (null != uploadedFileUrl && uploadedFileUrl.length > 1)
									uploadMap.put(media.getId(),
											uploadedFileUrl[ContentConfigurationConstants.AWS_UPLOAD_RESULT_URL_INDEX]);
							}
						}
						TelemetryManager.log("Download Finished for Media Id: " + media.getId() + " | [Content Id '" + contentId
								+ "']");
						return uploadMap;
					}
				});
			}
			List<Future<Map<String, String>>> results = pool.invokeAll(tasks);
			for (Future<Map<String, String>> uMap : results) {
				Map<String, String> m = uMap.get();
				if (null != m)
					map.putAll(m);
			}
			pool.shutdown();
		}
		TelemetryManager.log("Returning the Map of Uploaded Assets. | [Content Id '" + contentId + "']");
		return map;
	}
}
