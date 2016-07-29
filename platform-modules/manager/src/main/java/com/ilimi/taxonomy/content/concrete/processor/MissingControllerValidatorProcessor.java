package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class MissingControllerValidatorProcessor extends AbstractProcessor {

	private static Logger LOGGER = LogManager.getLogger(MissingControllerValidatorProcessor.class.getName());

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
	
	private void validateMissingControllers(Plugin plugin) {
		if (null != plugin) {
			List<Controller> controllers = plugin.getControllers();
			if (null != controllers && !controllers.isEmpty()) {
				List<String> controllerIds = new ArrayList<String>();
				LOGGER.info("Validating Contollers.");
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
