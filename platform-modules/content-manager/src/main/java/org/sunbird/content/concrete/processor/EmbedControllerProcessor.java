package org.sunbird.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Controller;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;

/**
 * The Class EmbedControllerProcessor.
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 */
public class EmbedControllerProcessor extends AbstractProcessor {

	/** The logger. */
	

	/**
	 * Instantiates a new embed controller processor.
	 *
	 * @param basePath the base path
	 * @param contentId the content id
	 */
	public EmbedControllerProcessor(String basePath, String contentId) {
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
				plugin = embedController(plugin);
		} catch(Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [EmbedControllerProcessor]", e);
		}
		return plugin;
	}

	/**
	 * Embed controller.
	 *
	 * @param plugin the plugin
	 * @return the plugin
	 */
	private Plugin embedController(Plugin plugin) {
		try {
			if (null != plugin) {
				List<Controller> controllers = plugin.getControllers();
				if (null != controllers) {
					for (Controller controller : controllers) {
						if (StringUtils.isBlank(controller.getcData())) {
							TelemetryManager.log("There is No Existing In-Line Controller.");
							Map<String, Object> data = controller.getData();
							if (null != data) {
								String id = (String) data.get(ContentWorkflowPipelineParams.id.name());
								String type = (String) data.get(ContentWorkflowPipelineParams.type.name());
								TelemetryManager.log("Controller Id: " + id + " | type: " + type);
								if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(type)) {
									File file = null;
									if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), type))
										file = new File(
											basePath + File.separator + ContentWorkflowPipelineParams.items.name()
													+ File.separator + id + ".json");
									else if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.data.name(), type))
										file = new File(
												basePath + File.separator + ContentWorkflowPipelineParams.data.name()
														+ File.separator + id + ".json");
									if (null != file && file.exists()) {
										TelemetryManager.log("Reading Controller File: " + file.getPath());
										controller.setcData(FileUtils.readFileToString(file));
									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.CONTROLLER_FILE_READ.name(),
					ContentErrorMessageConstants.CONTROLLER_FILE_READ_ERROR, e);
		}
		return plugin;
	}

}
