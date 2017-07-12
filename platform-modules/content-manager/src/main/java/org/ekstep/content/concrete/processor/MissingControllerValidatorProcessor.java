package org.ekstep.content.concrete.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Controller;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.processor.AbstractProcessor;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;

/**
 * The Class MissingControllerValidatorProcessor.
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
 * @see MissingAssetValidatorProcessor
 */
public class MissingControllerValidatorProcessor extends AbstractProcessor {

	/** The logger. */
	

	/**
	 * Instantiates a new missing controller validator processor.
	 *
	 * @param basePath the base path
	 * @param contentId the content id
	 */
	public MissingControllerValidatorProcessor(String basePath, String contentId) {
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
				validateMissingControllers(plugin);
		} catch (ClientException ce) {
			throw ce;
		} catch (Exception e) {
			throw new ServerException(ContentErrorCodeConstants.PROCESSOR_ERROR.name(), 
					ContentErrorMessageConstants.PROCESSOR_ERROR + " | [MissingAssetValidatorProcessor]", e);
		}
		return plugin;
	}
	
	/**
	 * Validate missing controllers.
	 *
	 * @param plugin the plugin
	 */
	private void validateMissingControllers(Plugin plugin) {
		if (null != plugin) {
			List<Controller> controllers = plugin.getControllers();
			if (null != controllers && !controllers.isEmpty()) {
				List<String> controllerIds = new ArrayList<String>();
				PlatformLogger.log("Validating Contollers.");
				for (Controller controller : controllers) {
					if (controllerIds.contains(controller.getId()))
						throw new ClientException(ContentErrorCodeConstants.DUPLICATE_CONTROLLER_ID.name(),
								ContentErrorMessageConstants.DUPLICATE_CONTROLLER_ID_ERROR + " | [Controller Id '" + controller.getId()
										+ "' is used more than once in the ECML.]");
					else
						controllerIds.add(controller.getId());
					if (StringUtils.isBlank(controller.getcData())) {
						String type = (String) controller.getData().get(ContentWorkflowPipelineParams.type.name());
						if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.data.name(), type)
								&& !new File(basePath + File.separator + ContentWorkflowPipelineParams.data.name()
										+ File.separator + controller.getId() + ".json").exists())
							throw new ClientException(ContentErrorCodeConstants.MISSING_CONTROLLER_FILE.name(),
									ContentErrorMessageConstants.MISSING_CONTROLLER_FILES_ERROR + " | [Controller Id '" + controller.getId()
											+ "' is missing.]");
						else if (StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), type)
									&& !new File(basePath + File.separator + ContentWorkflowPipelineParams.items.name()
									+ File.separator + controller.getId() + ".json").exists())
							throw new ClientException(ContentErrorCodeConstants.MISSING_CONTROLLER_FILE.name(),
									ContentErrorMessageConstants.MISSING_CONTROLLER_FILES_ERROR + " | [Controller Id '" + controller.getId()
											+ "' is missing.]");
					}
				}
			}
		}
	}

}
