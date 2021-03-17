package org.sunbird.content.processor;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.content.concrete.processor.AssessmentItemCreatorProcessor;
import org.sunbird.content.concrete.processor.AssetCreatorProcessor;
import org.sunbird.content.concrete.processor.AssetsLicenseValidatorProcessor;
import org.sunbird.content.concrete.processor.AssetsValidatorProcessor;
import org.sunbird.content.concrete.processor.BaseConcreteProcessor;
import org.sunbird.content.concrete.processor.EmbedControllerProcessor;
import org.sunbird.content.concrete.processor.GlobalizeAssetProcessor;
import org.sunbird.content.concrete.processor.LocalizeAssetProcessor;
import org.sunbird.content.concrete.processor.MissingAssetValidatorProcessor;
import org.sunbird.content.concrete.processor.MissingControllerValidatorProcessor;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;

/**
 * The Class <code>AbstractProcessor</code> is the base of All the Concrete
 * Processor thus helps to play all Concrete Processor in
 * <code>Pipes and Filter</code> Design Pattern. It can be used in two ways
 * <code>Automatic Chain</code> and <code>Explicit Chain</code>. The Automatic
 * Chain can be initialized by setting <code>setNextProcessor</code> field
 * pointing to instance of next processor. For Explicit initialization
 * <code>ContentPipelineProcessor</code> needs to be used and its register
 * method for registering the individual Processor.
 * 
 * <p>
 * All the Concrete Processor should be designed in a way that they can be
 * registered or chained in any sequence i.e. Each Processor should concentrate
 * on its own atomic task, there should be no dependency on any other processor
 * for its operation.
 *
 * 
 * @author Mohammad Azharuddin
 * 
 * @see AssessmentItemCreatorProcessor
 * @see AssetCreatorProcessor
 * @see AssetsLicenseValidatorProcessor
 * @see AssetsValidatorProcessor
 * @see BaseConcreteProcessor
 * @see EmbedControllerProcessor
 * @see GlobalizeAssetProcessor
 * @see LocalizeAssetProcessor
 * @see MissingAssetValidatorProcessor
 * @see MissingControllerValidatorProcessor
 * @see ContentPipelineProcessor
 * 
 */
public abstract class AbstractProcessor extends BaseConcreteProcessor {

	/**
	 * The base path is the location on disk where all the File Handling and
	 * manipulation will take place.
	 */
	protected String basePath;

	/** The content id is the identifier of Content <code>Node</code> */
	protected String contentId;

	/**
	 * The next processor points to the instance of Next Processor which needs
	 * to be executed.
	 */
	protected AbstractProcessor nextProcessor;

	/**
	 * Sets the next processor.
	 *
	 * @param nextProcessor
	 *            the new next processor
	 */
	public void setNextProcessor(AbstractProcessor nextProcessor) {
		this.nextProcessor = nextProcessor;
		this.isAutomaticChainExecutionEnabled = true;
	}

	/** Set it to <code>true</code> for enabling automatic chain execution. */
	protected boolean isAutomaticChainExecutionEnabled = false;

	/**
	 * Sets the flag if automatic chain execution enabled is set.
	 *
	 * @param isAutomaticChainExecutionEnabled
	 *            the new flag to enable automatic chain execution.
	 */
	public void setIsAutomaticChainExecutionEnabled(boolean isAutomaticChainExecutionEnabled) {
		this.isAutomaticChainExecutionEnabled = isAutomaticChainExecutionEnabled;
	}

	/**
	 * Execute.
	 *
	 * @param content
	 *            the content is the ECRF Object.
	 * @return the processed ECRF Object
	 */
	public Plugin execute(Plugin content) {
		content = process(content);
		if (null != nextProcessor && isAutomaticChainExecutionEnabled == true) {
			content = nextProcessor.execute(content);
		}
		return content;
	}
	
	protected String getSubFolderPath(Media media) {
		String path = "";
		if (null != media.getData() && !media.getData().isEmpty()) {
			Object plugin = media.getData().get(ContentWorkflowPipelineParams.plugin.name());
			Object ver = media.getData().get(ContentWorkflowPipelineParams.ver.name());
			if (null != plugin && StringUtils.isNotBlank(plugin.toString()))
				if (null != ver && StringUtils.isNotBlank(ver.toString()))
					path += plugin + File.separator + ver;
		}
		return path;
	}

	/**
	 * Process is an Abstract Method for which the Concrete Processor of Content
	 * Work-Flow Pipeline needs to provide implementation. All the Logic of
	 * Concrete Processor should start and finish from at this method.
	 *
	 * @param content
	 *            The ECRF Object to Process.
	 * @return The processed ECRF Object
	 */
	abstract protected Plugin process(Plugin content);
}
