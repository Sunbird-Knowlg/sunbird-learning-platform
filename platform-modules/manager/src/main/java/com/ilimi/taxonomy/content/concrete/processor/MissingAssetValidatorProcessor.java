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

public class MissingAssetValidatorProcessor extends AbstractProcessor {

	private static Logger LOGGER = LogManager.getLogger(MissingAssetValidatorProcessor.class.getName());

	public MissingAssetValidatorProcessor(String basePath, String contentId) {
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
			if (null != plugin)
				validateMissingAssets(plugin);
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [MissingAssetValidatorProcessor]", e);
		}
		return plugin;
	}

	private void validateMissingAssets(Plugin plugin) {
		if (null != plugin) {
			Manifest manifest = plugin.getManifest();
			if (null != manifest) {
				List<Media> medias = manifest.getMedias();
				LOGGER.info("Validating Assets.");
				for (Media media : medias) {
					if (isWidgetTypeAsset(media.getType())
							&& !new File(basePath + File.separator + ContentWorkflowPipelineParams.widgets.name()
									+ File.separator + media.getSrc()).exists())
						throw new ClientException(ContentErrorCodeConstants.MISSING_ASSETS.name(),
								ContentErrorMessageConstants.MISSING_ASSETS_ERROR + " | [Asset Id '" + media.getId()
										+ "' is missing.]");
					else if (!new File(basePath + File.separator + ContentWorkflowPipelineParams.assets.name()
							+ File.separator + media.getSrc()).exists())
						throw new ClientException(ContentErrorCodeConstants.MISSING_ASSETS.name(),
								ContentErrorMessageConstants.MISSING_ASSETS_ERROR + " | [Asset Id '" + media.getId()
										+ "' is missing.]");
				}
			}
		}
	}

}
