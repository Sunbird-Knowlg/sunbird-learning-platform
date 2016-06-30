package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssetCreatorProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(AssetCreatorProcessor.class.getName());
	
	private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
	
	public AssetCreatorProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Plugin process(Plugin content) {
		try {
			if (null != content) {
				Map<String, String> mediaSrcMap = getNonAssetObjMediaSrcMap(getMedia(content));
				Map<String, String> assetIdMap = createAssetFromSrcMap(mediaSrcMap);
				Manifest manifest = content.getManifest();
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithAssetId(assetIdMap, getMedia(content)));
			}
		} catch(Exception e) {
			LOGGER.error("", e);
		}
		
		return content;
	}
	
	private Map<String, String> createAssetFromSrcMap(Map<String, String> srcMap) {
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
					String assetId = getAssetIdFromResponse(createContentNode(getMinimalAssetNodeMap(assetName, url)));
					if (!StringUtils.isBlank(assetId))
						assetIdMap.put(entry.getKey(), assetId);
				}
			}
		} // return the map of Source_Url --> Asset Id
		return assetIdMap;
	}
	
	private String getAssetIdFromResponse(Response response) {
		String assetId = "";
		if (null != response && response.getResponseCode() == ResponseCode.OK) {
			assetId = (String) response.getResult().get(ContentWorkflowPipelineParams.node_id.name());
		}
		return assetId;
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
			LOGGER.error(ContentErrorMessageConstants.FILE_READ_ERROR, e);
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
}
