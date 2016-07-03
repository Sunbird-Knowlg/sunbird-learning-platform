package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.HttpDownloadUtility;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

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
				Map<String, String> mediaSrcMap = getMediaSrcMap(getMedia(plugin));
				Map<String, String> downloadedAssetsMap = downloadAssets(mediaSrcMap);
				Manifest manifest = plugin.getManifest();
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithUrl(downloadedAssetsMap, getMedia(plugin)));
			}
		} catch(Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [LocalizeAssetProcessor]", e);
		}
		
		return plugin;
	}
	
	private Map<String, String> downloadAssets(Map<String, String> files) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != files && !StringUtils.isBlank(basePath)) {
			LOGGER.info("Starting Asset Download Fanout.");
            ExecutorService pool = Executors.newFixedThreadPool(10);
            List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(files.size());
            for (final Entry<String, String> file : files.entrySet()) {
                tasks.add(new Callable<Map<String, String>>() {
                    public Map<String, String> call() throws Exception {
                    	Map<String, String> downloadMap = new HashMap<String, String>();
                    	if (!StringUtils.isBlank(file.getKey()) && !StringUtils.isBlank(file.getValue())) {
	                    	String downloadPath = basePath;
	                    	if (isWidgetTypeAsset(file.getValue())) 
	                    		downloadPath += File.separator + ContentWorkflowPipelineParams.widgets.name();
	                    	else
	                    		downloadPath += File.separator + ContentWorkflowPipelineParams.assets.name();
	                    	createDirectoryIfNeeded(downloadPath);
	                    	File downloadedFile = HttpDownloadUtility.downloadFile(file.getKey(), downloadPath);
	                    	downloadMap.put(file.getKey(), downloadedFile.getName());
	                    }
                        return downloadMap;
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
		return map;
	}

}
