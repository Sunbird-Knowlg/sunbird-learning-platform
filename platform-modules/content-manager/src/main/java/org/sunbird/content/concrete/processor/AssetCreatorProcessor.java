package org.sunbird.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.common.ContentConfigurationConstants;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;


/**
 * The Class AssetCreatorProcessor.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 */
public class AssetCreatorProcessor extends AbstractProcessor {

	/** The logger. */
	

	/** The Constant URL_REGEX is the Pattern to verify the Web Address. */
	private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

	/**
	 * Instantiates a new asset creator processor and sets the base
	 * path and current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public AssetCreatorProcessor(String basePath, String contentId) {
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
	 * org.sunbird.taxonomy.content.processor.AbstractProcessor#process(org.sunbird.
	 * taxonomy.content.entity.Plugin)
	 */
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
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(),
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [AssetCreatorProcessor]", e);
		}

		return content;
	}

	/**
	 * Creates the asset from src map.
	 *
	 * @param srcMap
	 *            the src map
	 * @return the map
	 */
	private Map<String, String> createAssetFromSrcMap(Map<String, String> srcMap) {
		Map<String, String> assetIdMap = new HashMap<String, String>();
		if (null != srcMap) {
			for (Entry<String, String> entry : srcMap.entrySet()) {
				String assetName = getFileNameWithoutExtensionFromPathString(entry.getValue());
				if (!StringUtils.isBlank(assetName)) {
					String url = "";
					if (isWebAddress(entry.getKey()))
						url = entry.getKey();
					else if (isWidgetTypeAsset(entry.getValue()))
						url = basePath + File.separator + ContentWorkflowPipelineParams.widgets.name() + File.separator
								+ entry.getKey();
					url = basePath + File.separator + ContentWorkflowPipelineParams.assets.name() + File.separator
							+ entry.getKey();
					String assetId = getAssetIdFromResponse(createContentNode(getMinimalAssetNodeMap(assetName, url)));
					if (!StringUtils.isBlank(assetId))
						assetIdMap.put(entry.getKey(), assetId);
				}
			}
		} // return the map of Source_Url --> Asset Id
		return assetIdMap;
	}

	/**
	 * Gets the asset id from response.
	 *
	 * @param response
	 *            the response
	 * @return the asset id from response
	 */
	private String getAssetIdFromResponse(Response response) {
		String assetId = "";
		if (null != response && response.getResponseCode() == ResponseCode.OK) {
			assetId = (String) response.getResult().get(ContentWorkflowPipelineParams.node_id.name());
		}
		return assetId;
	}

	/**
	 * Gets the file name without extension from path string.
	 *
	 * @param path
	 *            the path
	 * @return the file name without extension from path string
	 */
	private String getFileNameWithoutExtensionFromPathString(String path) {
		String fileName = "";
		if (!StringUtils.isBlank(path)) {
			if (!StringUtils.contains(path, ContentConfigurationConstants.URL_PATH_SEPERATOR))
				fileName = path;
			else
				fileName = StringUtils.substringAfterLast(path, ContentConfigurationConstants.URL_PATH_SEPERATOR);
			if (StringUtils.contains(fileName, ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR))
				fileName = StringUtils.substringBeforeLast(fileName,
						ContentConfigurationConstants.FILENAME_EXTENSION_SEPERATOR);
		}
		return fileName;
	}

	/**
	 * Checks if the given Url is web address.
	 *
	 * @param url
	 *            the url
	 * @return true, if is valid is web address
	 */
	private boolean isWebAddress(String url) {
		boolean isWebAdd = false;
		Pattern p = Pattern.compile(URL_REGEX);
		Matcher m = p.matcher(url);
		if (m.find())
			isWebAdd = true;
		return isWebAdd;
	}

	/**
	 * Gets the minimal asset node map.
	 *
	 * @param assetName
	 *            the asset name
	 * @param url
	 *            the url
	 * @return the minimal asset node map
	 */
	private Map<String, Object> getMinimalAssetNodeMap(String assetName, String url) {
		Map<String, Object> minimalAssetNodeMap = new HashMap<String, Object>();
		if (!StringUtils.isBlank(assetName)) {
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.name.name(), assetName);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.code.name(),
					getContentCode(assetName, ContentWorkflowPipelineParams.Asset.name()));
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.owner.name(),
					ContentConfigurationConstants.DEFAULT_CONTENT_OWNER);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Live.name());
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.body.name(),
					ContentConfigurationConstants.DEFAULT_CONTENT_BODY);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.contentType.name(),
					ContentWorkflowPipelineParams.Asset.name());
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.downloadUrl.name(), isWebAddress(url) ? url : "");
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.pkgVersion.name(),
					ContentConfigurationConstants.DEFAULT_CONTENT_PACKAGE_VERSION);
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.mediaType.name(), getMediaType(new File(url)));
			minimalAssetNodeMap.put(ContentWorkflowPipelineParams.mimeType.name(), getMimeType(new File(url)));
		}
		return minimalAssetNodeMap;
	}

	/**
	 * Gets the content code.
	 *
	 * @param contentName
	 *            the content name
	 * @param contentType
	 *            the content type
	 * @return the content code
	 */
	private String getContentCode(String contentName, String contentType) {
		String contentCode = "";
		if (!StringUtils.isBlank(contentName) && !StringUtils.isBlank(contentType))
			contentCode = ContentConfigurationConstants.DEFAULT_CONTENT_CODE_PREFIX + contentType + contentName;
		return contentCode;
	}

	/**
	 * Gets the mime type.
	 *
	 * @param file
	 *            the file
	 * @return the mime type
	 */
	private String getMimeType(File file) {
		String mimeType = "";
		try {
			if (file.exists()) {
				Tika tika = new Tika();
				mimeType = tika.detect(file);
			}
		} catch (IOException e) {
			TelemetryManager.error(ContentErrorMessageConstants.FILE_READ_ERROR + " : " + file.getName(), e);
		}
		return mimeType;
	}

	/**
	 * Gets the media type.
	 *
	 * @param file
	 *            the file
	 * @return the media type
	 */
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
