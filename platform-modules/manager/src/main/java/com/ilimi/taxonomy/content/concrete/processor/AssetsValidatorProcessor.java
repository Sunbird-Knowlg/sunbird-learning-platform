package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssetsValidatorProcessor extends AbstractProcessor {

	private static Logger LOGGER = LogManager.getLogger(AssetsValidatorProcessor.class.getName());

	public AssetsValidatorProcessor(String basePath, String contentId) {
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
				validateAssets(plugin);
			}
		} catch(Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [AssetsValidatorProcessor]", e);
		}
		return plugin;
	}
	
	private void validateAssets(Plugin plugin){
		if (null != plugin) {
			Manifest manifest = plugin.getManifest();
			if (null != manifest) {
				List<Media> medias = manifest.getMedias();
				for (Media media: medias) {
					LOGGER.info("Validating Asset Id: " + media.getId());
					validateAsset(media);
				}
			}
		}
	}

	private boolean validateAsset(Media media) {
		boolean isValid = false;
		if (null != media) {
			File file = new File(getAssetPath(media.getType(), media.getSrc()));
			if (file.exists()) {
				if (!isValidAssetMimeType(file))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MIME_TYPE.name(), 
							ContentErrorMessageConstants.INVALID_ASSET_MIMETYPE + " | [Asset " + file.getName() + " has Invalid Mime-Type.]");
				if (!isValidAssetSize(file))
					throw new ClientException(ContentErrorCodeConstants.FILE_SIZE_EXCEEDS_LIMIT.name(), 
							ContentErrorMessageConstants.ASSET_FILE_SIZE_LIMIT_EXCEEDS + " | [Asset " + file.getName() + " is Bigger in Size.]");
				isValid = true;
				LOGGER.info("Asset Id {0} is Valid.", media.getId());
			}
		}
		return isValid;
	}

	private boolean isValidAssetMimeType(File file) {
		boolean isValidMimeType = false;
		if (file.exists()) {
			LOGGER.info("Validating Asset File {0} for Mime-Type.", file.getName());

		}
		return isValidMimeType;
	}

	private boolean isValidAssetSize(File file) {
		boolean isValidMimeType = false;
		if (file.exists()) {
			LOGGER.info("Validating Asset File {0} for Size.", file.getName());

		}
		return isValidMimeType;
	}

	private String getAssetPath(String type, String src) {
		String path = "";
		if (!StringUtils.isBlank(type) && !StringUtils.isBlank(src)) {
			LOGGER.info("Fetching Asset Path.");
			if (isWidgetTypeAsset(type))
				path = basePath + File.separator + ContentWorkflowPipelineParams.widgets.name() + File.separator + src;
			else
				path = basePath + File.separator + ContentWorkflowPipelineParams.assets.name() + File.separator + src;
		}
		return path;
	}

}
