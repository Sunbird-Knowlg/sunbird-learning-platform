package com.ilimi.taxonomy.content.concrete.processor;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.HttpDownloadUtility;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.util.PropertiesUtil;

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

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LocalizeAssetProcessor.class.getName());

	/**
	 * Instantiates a new localize asset processor and sets the base path and
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.content.processor.AbstractProcessor#process(com.ilimi.
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
		LOGGER.debug("Medias to Download: ", medias);
		Map<String, String> map = new HashMap<String, String>();
		try {
			LOGGER.info("Total Medias to Download: " + medias != null ? medias.size()
					: "0" + " | [Content Id '" + contentId + "']");

			Map<String, Object> downloadResultMap = downloadAssets(medias);
			LOGGER.info("Downloaded Result Map After the Firts Try: ",
					downloadResultMap + " | [Content Id '" + contentId + "']");

			Map<String, String> successMap = (Map<String, String>) downloadResultMap
					.get(ContentWorkflowPipelineParams.success.name());
			LOGGER.info("Successful Media Downloads: " + successMap + " | [Content Id '" + contentId + "']");
			if (null != successMap && !successMap.isEmpty())
				map.putAll(successMap);

			List<Media> skippedMedia = (List<Media>) downloadResultMap
					.get(ContentWorkflowPipelineParams.skipped.name());
			LOGGER.info("Skipped Media Downloads: " + skippedMedia + " | [Content Id '" + contentId + "']");
			if (null != skippedMedia && !skippedMedia.isEmpty()) {
				LOGGER.info("Fetching the Retry Count From Configuration. | [Content Id '" + contentId + "']");
				String retryCount = PropertiesUtil
						.getProperty(ContentWorkflowPipelineParams.RETRY_ASSET_DOWNLOAD_COUNT.name());
				if (!StringUtils.isBlank(retryCount)) {
					int retryCnt = NumberUtils.createInteger(retryCount);
					LOGGER.info("Starting the Retry For Count: " + retryCnt + " | [Content Id '" + contentId + "']");
					for (int i = 0; i < retryCnt; i++) {
						LOGGER.info("Retrying Asset Download For " + i + 1 + " times" + " | [Content Id '" + contentId
								+ "']");
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
			LOGGER.info("Starting Asset Download Fanout. | [Content Id '" + contentId + "']");
			final List<Media> skippedMediaDownloads = new ArrayList<Media>();
			final Map<String, String> successfulMediaDownloads = new HashMap<String, String>();
			ExecutorService pool = Executors.newFixedThreadPool(10);
			List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(medias.size());
			for (final Media media : medias) {
				tasks.add(new Callable<Map<String, String>>() {
					public Map<String, String> call() throws Exception {
						Map<String, String> downloadMap = new HashMap<String, String>();
						if (!StringUtils.isBlank(media.getSrc()) && !StringUtils.isBlank(media.getType())) {
							String downloadPath = basePath;
							if (isWidgetTypeAsset(media.getType()))
								downloadPath += File.separator + ContentWorkflowPipelineParams.widgets.name();
							else
								downloadPath += File.separator + ContentWorkflowPipelineParams.assets.name();
							createDirectoryIfNeeded(downloadPath);
							File downloadedFile = HttpDownloadUtility.downloadFile(media.getSrc(), downloadPath);
							LOGGER.info("Downloaded file : " + media.getSrc() + " - " + downloadedFile
									+ " | [Content Id '" + contentId + "']");
							if (null == downloadedFile)
								skippedMediaDownloads.add(media);
							else
								downloadMap.put(media.getId(), downloadedFile.getName());
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
			LOGGER.info("Successful Media Download Count: " + successfulMediaDownloads.size() + " | [Content Id '"
					+ contentId + "']");
			LOGGER.info("Skipped Media Download Count: " + skippedMediaDownloads.size() + " | [Content Id '" + contentId
					+ "']");
			map.put(ContentWorkflowPipelineParams.success.name(), successfulMediaDownloads);
			map.put(ContentWorkflowPipelineParams.skipped.name(), skippedMediaDownloads);
		}
		LOGGER.info("Returning the Map of Successful and Skipped Media. | [Content Id '" + contentId + "']");
		return map;
	}

}
