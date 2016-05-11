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

import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.util.AWSUploader;

public class GlobalizeAssetProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(GlobalizeAssetProcessor.class.getName());
	
	public GlobalizeAssetProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Content process(Content content) {
		try {
			if (null != content) {
				Map<String, String> mediaSrcMap = getMediaSrcMap(getMedia(content));
				Map<String, String> uploadedAssetsMap = uploadAssets(mediaSrcMap);
				Manifest manifest = content.getManifest();
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithUrl(uploadedAssetsMap, getMedia(content)));
			}
		} catch(InterruptedException | ExecutionException e) {
			LOGGER.error("", e);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		
		return content;
	}
	
	private Map<String, String> uploadAssets(Map<String, String> files) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != files && !StringUtils.isBlank(basePath)){
	            ExecutorService pool = Executors.newFixedThreadPool(10);
	            List<Callable<Map<String, String>>> tasks = new ArrayList<Callable<Map<String, String>>>(files.size());
	            for (final Entry<String, String> file : files.entrySet()) {
	                tasks.add(new Callable<Map<String, String>>() {
	                    public Map<String, String> call() throws Exception {
	                    	Map<String, String> uploadMap = new HashMap<String, String>();
	                    	if (!StringUtils.isBlank(file.getKey()) && !StringUtils.isBlank(file.getValue())) {
		                    	File uploadFile;
		                    	if (isWidgetTypeAsset(file.getValue())) 
		                    		uploadFile = new File(basePath + File.separator + 
		                    				ContentWorkflowPipelineParams.widgets.name() + file.getKey());
		                    	else
		                    		uploadFile = new File(basePath + File.separator + 
		                    				ContentWorkflowPipelineParams.assets.name() + file.getKey());
		                    	String[] uploadedFileUrl;
		                    	if (uploadFile.exists()) {
			                        uploadedFileUrl = AWSUploader.uploadFile(ContentConfigurationConstants.BUCKET_NAME, 
			                        		ContentConfigurationConstants.FOLDER_NAME, 
			                        		uploadFile);
			                        if (null != uploadedFileUrl && uploadedFileUrl.length > 0)
			                        	uploadMap.put(file.getKey(), uploadedFileUrl[ContentConfigurationConstants.AWS_UPLOAD_RESULT_URL_INDEX]);
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
