package org.sunbird.content.concrete.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;

/**
 * The Class MissingAssetValidatorProcessor.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingControllerValidatorProcessor
 * 
 */
public class MissingAssetValidatorProcessor extends AbstractProcessor {

	/**
	 * Instantiates a new missing asset validator processor.
	 *
	 * @param basePath the base path
	 * @param contentId the content id
	 */
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

	/* (non-Javadoc)
	 * @see org.sunbird.taxonomy.content.processor.AbstractProcessor#process(org.sunbird.taxonomy.content.entity.Plugin)
	 */
	@Override
	protected Plugin process(Plugin plugin) {
		try {
			if (null != plugin)
				validateMissingAssets(plugin);
		} catch (ClientException ce) {
			throw ce;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [MissingAssetValidatorProcessor]", e);
		}
		return plugin;
	}
	
	/**
	 * Validate missing assets.
	 *
	 * @param plugin the plugin
	 */
	private void validateMissingAssets(Plugin plugin) {
		if (null != plugin) {
			Manifest manifest = plugin.getManifest();
			if (null != manifest) {
				List<Media> medias = manifest.getMedias();
				List<String> mediaIds = new ArrayList<String>();
				TelemetryManager.log("Validating Assets.");
				for (Media media : medias) {
					if (mediaIds.contains(getMediaId(media)))
						throw new ClientException(ContentErrorCodeConstants.DUPLICATE_ASSET_ID.name(),
								ContentErrorMessageConstants.DUPLICATE_ASSET_ID_ERROR + " | [Asset Id '" + media.getId()
										+ "' is used more than once in the manifest.]");
					else
						mediaIds.add(getMediaId(media));

					if (StringUtils.equals(ContentWorkflowPipelineParams.youtube.name(), media.getType()))
						continue;
					if (isWidgetTypeAsset(media.getType())
							&& !new File(basePath + File.separator + ContentWorkflowPipelineParams.widgets.name()
									+ File.separator + media.getSrc()).exists())
						throw new ClientException(ContentErrorCodeConstants.MISSING_ASSETS.name(),
								ContentErrorMessageConstants.MISSING_ASSETS_ERROR + " | [Asset Id '" + media.getId()
										+ "' is missing.]");
					else if (!isWidgetTypeAsset(media.getType()) 
							&& !new File(basePath + File.separator + ContentWorkflowPipelineParams.assets.name()
									+ File.separator + media.getSrc()).exists())
						throw new ClientException(ContentErrorCodeConstants.MISSING_ASSETS.name(),
								ContentErrorMessageConstants.MISSING_ASSETS_ERROR + " | [Asset Id '" + media.getId()
										+ "' is missing.]");
				}
			}
		}
	}
	
	private String getMediaId(Media media) {
		String id = media.getId();
		if (null != media.getData() && !media.getData().isEmpty()) {
			Object plugin = media.getData().get(ContentWorkflowPipelineParams.plugin.name());
			Object ver = media.getData().get(ContentWorkflowPipelineParams.ver.name());
			if (null != plugin && StringUtils.isNotBlank(plugin.toString()))
				if (null != ver && StringUtils.isNotBlank(ver.toString()))
					id += "_" + plugin + "_" + ver;
		}
		return id;
	}
}
