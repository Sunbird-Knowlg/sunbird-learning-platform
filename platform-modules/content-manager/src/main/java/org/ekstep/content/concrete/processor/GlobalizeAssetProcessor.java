package org.ekstep.content.concrete.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.common.ContentConfigurationConstants;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Manifest;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.processor.AbstractProcessor;

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

	private static final String s3Content = "s3.content.folder";
    private static final String s3Assets = "s3.asset.folder";

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
	 * org.ekstep.taxonomy.content.processor.AbstractProcessor#process(org.ekstep.
	 * taxonomy.content.entity.Plugin)
	 */
	@Override
	protected Plugin process(Plugin plugin) {
		try {
			PlatformLogger.log("ECRF Object (Plugin): ", plugin);
			if (null != plugin) {
				PlatformLogger.log("Starting the Process. | [Content Id '" + contentId + "']");
				List<Media> medias = getMedia(plugin);
				PlatformLogger.log("Total Medias: " + medias.size() + " | [Content Id '" + contentId + "']");
				Map<String, String> uploadedAssetsMap = uploadAssets(medias);
				Manifest manifest = plugin.getManifest();
				PlatformLogger.log("Setting the Medias in Manifest. | [Content Id '" + contentId + "']");
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
	 * @param <code>src</code>
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
		PlatformLogger.log("Medias: ", medias);
		Map<String, String> map = new HashMap<String, String>();
		if (null != medias && StringUtils.isNotBlank(basePath)) {
			PlatformLogger.log("Starting the Fan-out for Upload. | [Content Id '" + contentId + "']");
			ExecutorService pool = Executors.newFixedThreadPool(10);
			List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(medias.size());
			for (final Media media : medias) {
				PlatformLogger.log("Adding All Medias as Task fro Upload. | [Content Id '" + contentId + "']");
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
							PlatformLogger.log("Upload File: | [Content Id '" + contentId + "']");
							String[] uploadedFileUrl;
							if (uploadFile.exists()) {
								String folderName = S3PropertyReader.getProperty(s3Content) + "/"
										+ Slug.makeSlug(contentId, true);
								String path = S3PropertyReader.getProperty(s3Assets);
								/*String folderName = ContentConfigurationConstants.FOLDER_NAME + "/"
										+ Slug.makeSlug(contentId, true);
								String path = getFolderPath(media.getSrc());*/
								PlatformLogger.log("Folder to Upload: " + folderName + "| [Content Id '" + contentId + "']");
								PlatformLogger.log("Path to Upload: " + path + "| [Content Id '" + contentId + "']");
								if (StringUtils.isNotBlank(path))
									folderName = folderName + "/" + path;
								folderName = folderName + "/" + System.currentTimeMillis();
								uploadedFileUrl = AWSUploader.uploadFile(folderName, uploadFile);
								if (null != uploadedFileUrl && uploadedFileUrl.length > 1)
									uploadMap.put(media.getId(),
											uploadedFileUrl[ContentConfigurationConstants.AWS_UPLOAD_RESULT_URL_INDEX]);
							}
						}
						PlatformLogger.log("Download Finished for Media Id: " + media.getId() + " | [Content Id '" + contentId
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
		PlatformLogger.log("Returning the Map of Uploaded Assets. | [Content Id '" + contentId + "']");
		return map;
	}
}
