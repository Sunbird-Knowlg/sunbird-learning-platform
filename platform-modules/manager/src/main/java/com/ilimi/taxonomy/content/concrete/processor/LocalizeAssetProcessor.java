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

public class LocalizeAssetProcessor extends AbstractProcessor {

	private static Logger LOGGER = LogManager.getLogger(LocalizeAssetProcessor.class.getName());

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

	@SuppressWarnings("unchecked")
	private Map<String, String> processAssetsDownload(List<Media> medias) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			Map<String, Object> downloadResultMap = downloadAssets(medias);
			Map<String, String> successMap = (Map<String, String>) downloadResultMap
					.get(ContentWorkflowPipelineParams.success.name());
			if (null != successMap && !successMap.isEmpty())
				map.putAll(successMap);
			List<Media> skippedMedia = (List<Media>) downloadResultMap
					.get(ContentWorkflowPipelineParams.skipped.name());
			if (null != skippedMedia && !skippedMedia.isEmpty()) {
				LOGGER.info("Fetching the Retry Count From Configuration.");
				String retryCount = PropertiesUtil
						.getProperty(ContentWorkflowPipelineParams.RETRY_ASSET_DOWNLOAD_COUNT.name());
				if (!StringUtils.isBlank(retryCount)) {
					int retryCnt = NumberUtils.createInteger(retryCount);
					LOGGER.info("Starting the Retry For Count: " + retryCnt);
					for (int i = 0; i < retryCnt; i++) {
						LOGGER.info("Retrying Asset Download For " + i + 1 + " times");
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

	private Map<String, Object> downloadAssets(List<Media> medias) throws InterruptedException, ExecutionException {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != medias && !StringUtils.isBlank(basePath)) {
			LOGGER.info("Starting Asset Download Fanout.");
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
			map.put(ContentWorkflowPipelineParams.success.name(), successfulMediaDownloads);
			map.put(ContentWorkflowPipelineParams.skipped.name(), skippedMediaDownloads);
		}
		return map;
	}

}
