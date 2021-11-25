package org.sunbird.content.concrete.processor;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.HttpDownloadUtility;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The Class LocalizeAssetProcessor is a Content Workflow pipeline Processor
 * Which is responsible for downloading of Asset Items for the Storage Space to
 * the local storage.
 * 
 * It also has the capability of retry download in case of failure for the
 * particular amount of retry count.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 * 
 */
public class LocalizeAssetProcessor extends AbstractProcessor {

	private String pluginMediaBaseURL;
	private String contentMediaBaseURL;


	/**
	 * Instantiates a new localize asset processor and sets the base path andS
	 * current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public LocalizeAssetProcessor(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;

		this.pluginMediaBaseURL = S3PropertyReader.getProperty("plugin.media.base.url");
		// TODO need to throw exception if this property not exist. Plan in release-1.10.0
		this.contentMediaBaseURL = Platform.config.hasPath("content.media.base.url") ? Platform.config.getString("content.media.base.url") : this.pluginMediaBaseURL;

		
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
				List<Media> medias = getMedia(plugin);
				Map<String, String> downloadedAssetsMap = processAssetsDownload(medias);
				Manifest manifest = plugin.getManifest();
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithUrl(downloadedAssetsMap, getMedia(plugin)));
			}
		} catch (ClientException e) {
			throw e;
		} catch (ServerException e) {
			throw e;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(),
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [LocalizeAssetProcessor]", e);
		}

		return plugin;
	}

	/**
	 * <code>processAssetsDownload</code> is the method responsible for start
	 * the Asset Download, It also handles the retry mechanism of download where
	 * the retry count is coming from the configuration.
	 *
	 * @param medias
	 *            the medias is <code>list</code> of <code>medias</code> from
	 *            <code>ECRF Object</code>.
	 * @return the map of <code>asset Id</code> and asset <code>file name</code>
	 *         .
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> processAssetsDownload(List<Media> medias) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			TelemetryManager.info("Total Medias to Download: for [Content Id '" + contentId + "']: " +  medias.size());

			Map<String, Object> downloadResultMap = downloadAssets(medias);
			TelemetryManager.log("Downloaded Result Map After the Firts Try: "+
					downloadResultMap + " | [Content Id '" + contentId + "']");

			Map<String, String> successMap = (Map<String, String>) downloadResultMap
					.get(ContentWorkflowPipelineParams.success.name());
			TelemetryManager.info("Successful Media Downloads: " + successMap + " | [Content Id '" + contentId + "']");
			if (null != successMap && !successMap.isEmpty())
				map.putAll(successMap);

			List<Media> skippedMedia = (List<Media>) downloadResultMap
					.get(ContentWorkflowPipelineParams.skipped.name());
			TelemetryManager.info("Skipped Media Downloads: " + skippedMedia + " | [Content Id '" + contentId + "']");
			if (null != skippedMedia && !skippedMedia.isEmpty()) {
				TelemetryManager.info("Fetching the Retry Count From Configuration. | [Content Id '" + contentId + "']");
				int retryCnt = Platform.config.getInt(ContentWorkflowPipelineParams.RETRY_ASSET_DOWNLOAD_COUNT.name());
				TelemetryManager.info("Starting the Retry For Count: " + retryCnt + " | [Content Id '" + contentId + "']");
				for (int i = 0; i < retryCnt; i++) {
					TelemetryManager.log(
							"Retrying Asset Download For " + i + 1 + " times" + " | [Content Id '" + contentId + "']");
					if (null != skippedMedia && !skippedMedia.isEmpty()) {
						Map<String, Object> result = downloadAssets(skippedMedia);
						Map<String, String> successfulDownloads = (Map<String, String>) result
								.get(ContentWorkflowPipelineParams.success.name());
						skippedMedia = (List<Media>) downloadResultMap
								.get(ContentWorkflowPipelineParams.skipped.name());
						if (null != successfulDownloads && !successfulDownloads.isEmpty())
							map.putAll(successfulDownloads);
					}
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_CONC_OP_ERROR.name(),
					ContentErrorMessageConstants.ASSET_CONCURRENT_DOWNLOAD_ERROR, e);
		}
		return map;
	}

	/**
	 * <code>downloadAssets</code> is the Utility method which tries to download
	 * the given <code>medias</code>.
	 *
	 * @param medias
	 *            the medias is a <code>list</code> of <code>Medias</code> from
	 *            <code>ECRF Object</code>.
	 * @return the map of downloaded and skipped medias map.
	 * @throws InterruptedException
	 *             the interrupted exception is thrown when the concurrent
	 *             execution is interrupted.
	 * @throws ExecutionException
	 *             the execution exception is thrown when there is an issue in
	 *             running the Current Future Task.
	 */
	private Map<String, Object> downloadAssets(List<Media> medias) throws InterruptedException, ExecutionException {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != medias && !StringUtils.isBlank(basePath)) {
			TelemetryManager.info("Starting Asset Download Fanout. | [Content Id '" + contentId + "']: "+ contentId);
			final List<Media> skippedMediaDownloads = new ArrayList<Media>();
			final Map<String, String> successfulMediaDownloads = new HashMap<String, String>();
			ExecutorService pool = Executors.newFixedThreadPool(10);
			List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(medias.size());
			for (final Media media : medias) {
				if (StringUtils.equals(ContentWorkflowPipelineParams.youtube.name(), media.getType()))
					continue;
				tasks.add(new Callable<Map<String, String>>() {
					public Map<String, String> call() throws Exception {
						Map<String, String> downloadMap = new HashMap<String, String>();
						if (!StringUtils.isBlank(media.getSrc()) && !StringUtils.isBlank(media.getType())) {
							String downloadPath = basePath;
							if (isWidgetTypeAsset(media.getType()))
								downloadPath += File.separator + ContentWorkflowPipelineParams.widgets.name();
							else
								downloadPath += File.separator + ContentWorkflowPipelineParams.assets.name();

							String subFolder = "";
							if (!media.getSrc().startsWith("http")) {
								File f = new File(media.getSrc());
								subFolder = f.getParent();
								if (f.exists()) {
									f.delete();
								}
								subFolder = StringUtils.stripStart(subFolder, File.separator);
							}
							if (StringUtils.isNotBlank(subFolder))
								downloadPath += File.separator + subFolder;
							createDirectoryIfNeeded(downloadPath);
							TelemetryManager.info("Downloading file : " + getDownloadUrl(media.getSrc() + " -TO- " + downloadPath
									+ " | [Content Id '" + contentId + "']");
							File downloadedFile = HttpDownloadUtility.downloadFile(getDownloadUrl(media.getSrc()),
									downloadPath);
							TelemetryManager.info("Downloaded file : " + media.getSrc() + " - " + downloadedFile
									+ " | [Content Id '" + contentId + "']");
							if (null == downloadedFile)
								skippedMediaDownloads.add(media);
							else {
								if (StringUtils.isNotBlank(subFolder))
									downloadMap.put(media.getId(),
											subFolder + File.separator + downloadedFile.getName());
								else
									downloadMap.put(media.getId(), downloadedFile.getName());
							}
						}
						return downloadMap;
					}
				});
			}
			List<Future<Map<String, String>>> results = pool.invokeAll(tasks);
			for (Future<Map<String, String>> downloadMap : results) {
				Map<String, String> m = downloadMap.get();
				if (null != m)
					successfulMediaDownloads.putAll(m);
			}
			pool.shutdown();
			TelemetryManager.info("Successful Media Download Count for | [Content Id '" + contentId + "']"+
					successfulMediaDownloads.size());
			TelemetryManager.info("Skipped Media Download Count: | [Content Id '" + contentId + "']" +
					skippedMediaDownloads.size());
			map.put(ContentWorkflowPipelineParams.success.name(), successfulMediaDownloads);
			map.put(ContentWorkflowPipelineParams.skipped.name(), skippedMediaDownloads);
		}
		TelemetryManager.info("Returning the Map of Successful and Skipped Media. | [Content Id '" + contentId + "']", map);
		return map;
	}

	private String getDownloadUrl(String src) {
		if (StringUtils.isNotBlank(src)) {
			if (!src.startsWith("http")) {
				String prefix = "";
				if (src.contains("content-plugins/")) {
					prefix = pluginMediaBaseURL;
				} else {
					prefix = this.contentMediaBaseURL;
				}
				src = prefix + src;
			}
		}
		TelemetryManager.log("Returning src url: "+ src);
		return src;
	}

}
