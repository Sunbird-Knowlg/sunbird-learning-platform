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

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.util.AWSUploader;
import com.ilimi.taxonomy.util.HttpDownloadUtility;

public class BaseConcreteProcessor {
	
	public Response updateNode(String contentId, Map<String, Object> fields) {
		Response response = new Response();
		return response; 
	}
	
	public List<Media> getMedia(Content content) {
		List<Media> medias = new ArrayList<Media>();
		if (null != content) {
			medias = content.getManifest().getMedias();
		}
		return medias;
	}
	
	public List<String> getMediaSrcList(List<Media> medias) {
		List<String> mediaSrcList = new ArrayList<String>();
		if (null != medias && medias.size() > 0) {
			for (Media media: medias) {
				String src = media.getData().get(ContentWorkflowPipelineParams.src.name());
				if (!StringUtils.isBlank(src))
					mediaSrcList.add(src);
			}
		}
		return mediaSrcList;
	}
	
	public Map<String, String> getMediaSrcMap(List<Media> medias) {
		Map<String, String> srcMap = new HashMap<String, String>();
		if (null != medias && medias.size() > 0) {
			for (Media media: medias) {
				String src = media.getData().get(ContentWorkflowPipelineParams.src.name());
				String type = media.getData().get(ContentWorkflowPipelineParams.type.name());
				if (!StringUtils.isBlank(src) &&
						!StringUtils.isBlank(type))
					srcMap.put(src, type);
			}
		}
		return srcMap;
	}
	
	public Map<String, String> uploadAssets(Map<String, String> files, final String basePath) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != files && files.size() > 0 && !StringUtils.isBlank(basePath)){
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
		                    				ContentWorkflowPipelineParams.widgets.name() + 
		                    				file.getKey());
		                    	else
		                    		uploadFile = new File(basePath + File.separator + 
		                    				ContentWorkflowPipelineParams.assets.name() + 
		                    				file.getKey());
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
	
	public Map<String, String> downloadAssets(Map<String, String> files, final String basePath) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != files && files.size() > 0 && !StringUtils.isBlank(basePath)){
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
	
	public List<Media> getUpdatedMediaWithUrl(Map<String, String> urlMap, List<Media> mediaList) {
		List<Media> medias = new ArrayList<Media>();
		if (null != urlMap && null != mediaList && mediaList.size() > 0) {
			for (Media media: mediaList) {
				if (null != media.getData()) {
					String url =  media.getData().get(ContentWorkflowPipelineParams.src.name());
					if (!StringUtils.isBlank(url)) {
						String uUrl = urlMap.get(url);
						if (!StringUtils.isBlank(uUrl)) {
							Map<String, String> map = media.getData();
							map.put(ContentWorkflowPipelineParams.src.name(), uUrl);
							media.setData(map);
						}
					}
				}
			}
		}
		return medias;
	}
	
	private boolean isWidgetTypeAsset(String assetType) {
		return StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.js.name()) ||
				StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.css.name()) ||
				StringUtils.equalsIgnoreCase(assetType, ContentWorkflowPipelineParams.plugin.name());
	}
	
	private void createDirectoryIfNeeded(String directoryName) {
        File theDir = new File(directoryName);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            theDir.mkdir();
        }
    }
	
}
