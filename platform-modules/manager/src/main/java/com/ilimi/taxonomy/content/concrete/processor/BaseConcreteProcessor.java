package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.util.AWSUploader;
import com.ilimi.taxonomy.util.HttpDownloadUtility;

public class BaseConcreteProcessor extends BaseManager {
	
	private static Logger LOGGER = LogManager.getLogger(BaseConcreteProcessor.class.getName());
	
	private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
	
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
		if (null != medias) {
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
		if (null != medias) {
			for (Media media: medias) {
				Map<String, String> data = media.getData();
				if (null != data) {
					String src = data.get(ContentWorkflowPipelineParams.src.name());
					String type = data.get(ContentWorkflowPipelineParams.type.name());
					if (!StringUtils.isBlank(src) &&
							!StringUtils.isBlank(type))
						srcMap.put(src, type);
				}
			}
		}
		return srcMap;
	}
	
	public Map<String, String> getNonAssetObjMediaSrcMap(List<Media> medias) {
		Map<String, String> srcMap = new HashMap<String, String>();
		if (null != medias) {
			for (Media media: medias) {
				Map<String, String> data = media.getData();
				if (null != data && data.containsKey(ContentWorkflowPipelineParams.assetId.name())) {
					if (StringUtils.isBlank(data.get(ContentWorkflowPipelineParams.assetId.name()))) {
						String src = data.get(ContentWorkflowPipelineParams.src.name());
						String type = data.get(ContentWorkflowPipelineParams.type.name());
						if (!StringUtils.isBlank(src) &&
								!StringUtils.isBlank(type))
							srcMap.put(src, type);
					}
				}
			}
		}
		return srcMap;
	}
	
	public Map<String, String> uploadAssets(Map<String, String> files, final String basePath) throws InterruptedException, ExecutionException {
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
	
	public Map<String, String> downloadAssets(Map<String, String> files, final String basePath) throws InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<String, String>();
		if (null != files && !StringUtils.isBlank(basePath)){
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
		if (null != urlMap && null != mediaList) {
			for (Media media: mediaList) {
				if (null != media.getData()) {
					String uUrl = urlMap.get(media.getData().get(ContentWorkflowPipelineParams.src.name()));
					if (!StringUtils.isBlank(uUrl)) {
						Map<String, String> map = media.getData();
						map.put(ContentWorkflowPipelineParams.src.name(), uUrl);
						media.setData(map);
					}
				}
			}
		}
		return medias;
	}
	
	public List<File> getControllersFileList(List<Controller> controllers, String type, String basePath) {
		List<File> controllerFileList = new ArrayList<File>();
		if (null != controllers && !StringUtils.isBlank(type) && !StringUtils.isBlank(basePath)) {
			for (Controller controller: controllers) {
				if (null != controller.getData()) {
					if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), 
							controller.getData().get(ContentWorkflowPipelineParams.type.name()))) {
						String controllerId = controller.getData().get(ContentWorkflowPipelineParams.id.name());
						if (!StringUtils.isBlank(controllerId))
							controllerFileList.add(new File(basePath + File.separator + 
									ContentWorkflowPipelineParams.items.name() + File.separator + controllerId + 
									ContentConfigurationConstants.ITEM_CONTROLLER_FILE_EXTENSION));
					}
				}
			}
		}
		return controllerFileList;
	}
	
	public Response createContentNode(Map<String, Object> map, String contentType) {
		Response response = new Response();
		if (null != map && StringUtils.isBlank(contentType)) {
			Node node = getDataNode(map, contentType);
			Request validateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.validateNode.name());
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				response = validateRes;
			} else {
				Request createReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
						ContentWorkflowPipelineParams.createDataNode.name());
				createReq.put(GraphDACParams.node.name(), node);
				response = getResponse(createReq, LOGGER);
			}
		}
		return response;
	}
	
	public Response updateContentNode(Node node, Map<String, Object> map) {
		Response response = new Response();
		if (null != map && null != node) {
			node = updateDataNode(node, map);
			Request validateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
					ContentWorkflowPipelineParams.validateNode.name());
			validateReq.put(GraphDACParams.node.name(), node);
			Response validateRes = getResponse(validateReq, LOGGER);
			if (checkError(validateRes)) {
				response =  validateRes;
			} else {
				Request updateReq = getRequest(ContentConfigurationConstants.GRAPH_ID, GraphEngineManagers.NODE_MANAGER,
						ContentWorkflowPipelineParams.updateDataNode.name());
				updateReq.put(GraphDACParams.node.name(), node);
				updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
				response = getResponse(updateReq, LOGGER);
			}
		}
		return response;
	}
	
	public Map<String, String> createAssetFromSrcMap(Map<String, String> srcMap, String basePath) {
		Map<String, String> assetIdMap = new HashMap<String, String>();
		if (null != srcMap) {
			for (Entry<String, String> entry: srcMap.entrySet()) {
				String assetName = getFileNameWithoutExtensionFromPathString(entry.getValue());
				if (!StringUtils.isBlank(assetName)) {
					String url = "";
					if (isWebAddress(entry.getKey()))
						url = entry.getKey();
					else
						if (isWidgetTypeAsset(entry.getValue()))
							url = basePath + File.separator + ContentWorkflowPipelineParams.widgets.name() + 
							File.separator + entry.getKey();
						url = basePath + File.separator + ContentWorkflowPipelineParams.assets.name() + 
								File.separator + entry.getKey();
					Map<String, Object> assetMap = getMinimalAssetNodeMap(assetName, url); 
				}
			}
		}
		return assetIdMap;
	}
	
	private boolean isWebAddress(String url) {
		boolean isWebAdd = false;
		Pattern p = Pattern.compile(URL_REGEX);
		Matcher m = p.matcher(url);
		if(m.find())
			isWebAdd = true;
		return isWebAdd;
	}
	
	private Map<String, Object> getMinimalAssetNodeMap(String assetName, String url) {
		Map<String, Object> minimalAssetNodeMap = new HashMap<String, Object>();
		if (!StringUtils.isBlank(assetName)) {
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.name.name(), assetName);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.code.name(), getContentCode(assetName, ContentWorkflowPipelineParams.Asset.name()));
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.owner.name(), ContentConfigurationConstants.DEFAULT_CONTENT_OWNER);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Live.name());
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.body.name(), ContentConfigurationConstants.DEFAULT_CONTENT_BODY);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.contentType.name(), ContentWorkflowPipelineParams.Asset.name());
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.downloadUrl.name(), isWebAddress(url) ? url : "");
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.pkgVersion.name(), ContentConfigurationConstants.DEFAULT_CONTENT_PACKAGE_VERSION);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.mediaType.name(), getMediaType(new File(url)));
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.mimeType.name(), getMimeType(new File(url)));
		}
		return minimalAssetNodeMap;
	}
	
	private String getContentCode(String contentName, String contentType) {
		String contentCode = "";
		if (!StringUtils.isBlank(contentName) && !StringUtils.isBlank(contentType))
			contentCode = ContentConfigurationConstants.DEFAULT_CONTENT_CODE_PREFIX + contentType + contentName;
		return contentCode;
	}
	
	private String getMimeType(File file) {
		String mimeType = "";
		try {
			if (file.exists()) {
				Tika tika = new Tika();
				mimeType = tika.detect(file);
			}
		} catch (IOException e) {
			LOGGER.error("Error: ", e);
		}
		return mimeType;
	}
	
	private String getMediaType(File file) {
		String mediaType = "";
		if (file.exists()) {
			String mimeType = getMimeType(file);
			if (StringUtils.contains(mimeType, ContentConfigurationConstants.URL_PATH_SEPERATOR)) {
				String type = StringUtils.substringBefore(mimeType, ContentConfigurationConstants.URL_PATH_SEPERATOR);
				if (!StringUtils.equalsIgnoreCase(type, ContentWorkflowPipelineParams.application.name()))
					mediaType = type;
				mediaType = StringUtils.substringAfter(mimeType, ContentConfigurationConstants.URL_PATH_SEPERATOR);
			}
		}
		return mediaType;
	}
	
	private Node updateDataNode(Node node, Map<String, Object> map) {
		if (null != map && null != node) {
			for (Entry<String, Object> entry: map.entrySet())
				node.getMetadata().put(entry.getKey(), entry.getValue());
		}
		return node;
	}
	
	private Node getDataNode(Map<String, Object> map, String contentType) {
		Node node = new Node();
		if (null != map && !StringUtils.isBlank(contentType)) {
			Map<String, Object> metadata = new HashMap<String, Object>();
			node.setIdentifier((String) map.get(ContentWorkflowPipelineParams.identifier.name()));
			node.setObjectType(ContentWorkflowPipelineParams.Content.name());
			for (Entry<String, Object> entry: map.entrySet())
				metadata.put(entry.getKey(), entry.getValue());
			node.setMetadata(metadata);
		}
		return node;
	}
	
	private String getFileNameWithoutExtensionFromPathString(String path) {
		String fileName = "";
		if (!StringUtils.isBlank(path)) {
		    if (!StringUtils.contains(path, ContentConfigurationConstants.URL_PATH_SEPERATOR))
		        fileName = path;
		    else
		    	fileName = StringUtils.substringAfterLast(path, ContentConfigurationConstants.URL_PATH_SEPERATOR);
		    if (StringUtils.contains(fileName, ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR))
		    	fileName = StringUtils.substringBeforeLast(fileName, ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR);
		}
		return fileName;
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
