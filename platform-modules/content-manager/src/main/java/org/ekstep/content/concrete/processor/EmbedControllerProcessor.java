package org.ekstep.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Controller;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.processor.AbstractProcessor;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

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
	private static ILogger LOGGER = PlatformLogManager.getLogger();

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
	 * @see com.ilimi.taxonomy.content.processor.AbstractProcessor#process(com.ilimi.taxonomy.content.entity.Plugin)
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
							LOGGER.log("There is No Existing In-Line Controller.");
							Map<String, Object> data = controller.getData();
							if (null != data) {
								String id = (String) data.get(ContentWorkflowPipelineParams.id.name());
								String type = (String) data.get(ContentWorkflowPipelineParams.type.name());
								LOGGER.log("Controller Id: " + id + " | type: " + type);
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
										LOGGER.log("Reading Controller File: " + file.getPath());
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
