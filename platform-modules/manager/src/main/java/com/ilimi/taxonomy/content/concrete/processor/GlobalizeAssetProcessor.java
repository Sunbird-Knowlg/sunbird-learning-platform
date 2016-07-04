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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class GlobalizeAssetProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(GlobalizeAssetProcessor.class.getName());
	
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

	@Override
	protected Plugin process(Plugin plugin) {
		try {
			if (null != plugin) {
				List<Media> medias = getMedia(plugin);
				Map<String, String> uploadedAssetsMap = uploadAssets(medias);
				Manifest manifest = plugin.getManifest();
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithUrl(uploadedAssetsMap, medias));
			}
		} catch(InterruptedException | ExecutionException e) {
			LOGGER.error("Globalize Asset Processor Error: failed to upload assets", e);
			throw new ServerException(ContentErrorCodeConstants.ASSET_UPLOAD_ERROR.name(), 
					ContentErrorMessageConstants.ASSET_UPLOAD_ERROR + " | [GlobalizeAssetProcessor]", e);
		} catch (Exception e) {
			LOGGER.error("Globalize Asset Processor Error: server error", e);
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [GlobalizeAssetProcessor]", e);
		}
		
		return plugin;
	}
	
	private Map<String, String> uploadAssets(List<Media> medias) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != medias && StringUtils.isNotBlank(basePath)){
			ExecutorService pool = Executors.newFixedThreadPool(10);
	        List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(medias.size());
	        for (final Media media : medias) {
	        	tasks.add(new Callable<Map<String, String>>() {
	            	public Map<String, String> call() throws Exception {
	                	Map<String, String> uploadMap = new HashMap<String, String>();
	                    if (StringUtils.isNotBlank(media.getId()) && StringUtils.isNotBlank(media.getSrc()) && StringUtils.isNotBlank(media.getType())) {
		                	File uploadFile;
		                    if (isWidgetTypeAsset(media.getType())) 
		                    	uploadFile = new File(basePath + File.separator + 
		                    		ContentWorkflowPipelineParams.widgets.name() + File.separator + media.getSrc());
		                    else
		                    	uploadFile = new File(basePath + File.separator + 
		                    		ContentWorkflowPipelineParams.assets.name() + File.separator + media.getSrc());
		                    String[] uploadedFileUrl;
		                    if (uploadFile.exists()) {
			                	uploadedFileUrl = AWSUploader.uploadFile(ContentConfigurationConstants.BUCKET_NAME, 
			                		ContentConfigurationConstants.FOLDER_NAME + "/" + Slug.makeSlug(contentId, true), uploadFile);
			                	if (null != uploadedFileUrl && uploadedFileUrl.length > 1)
			                        uploadMap.put(media.getId(), uploadedFileUrl[ContentConfigurationConstants.AWS_UPLOAD_RESULT_URL_INDEX]);
		                    }
	                    }
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
		return map;
	}
}
