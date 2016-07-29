package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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

public class EmbedControllerProcessor extends AbstractProcessor {

	private static Logger LOGGER = LogManager.getLogger(EmbedControllerProcessor.class.getName());

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

	private Plugin embedController(Plugin plugin) {
		try {
			if (null != plugin) {
				List<Controller> controllers = plugin.getControllers();
				if (null != controllers) {
					for (Controller controller : controllers) {
						if (StringUtils.isBlank(controller.getcData())) {
							LOGGER.info("There is No Existing In-Line Controller.");
							Map<String, Object> data = controller.getData();
							if (null != data) {
								String id = (String) data.get(ContentWorkflowPipelineParams.id.name());
								String type = (String) data.get(ContentWorkflowPipelineParams.type.name());
								LOGGER.info("Controller Id: " + id + " | type: " + type);
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
										LOGGER.info("Reading Controller File: " + file.getPath());
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
