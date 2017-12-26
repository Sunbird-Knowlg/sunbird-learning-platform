package org.ekstep.taxonomy.content.concrete.processor;

import org.ekstep.content.concrete.processor.EmbedControllerProcessor;
import org.ekstep.content.concrete.processor.GlobalizeAssetProcessor;
import org.ekstep.content.concrete.processor.LocalizeAssetProcessor;
import org.ekstep.content.concrete.processor.MissingAssetValidatorProcessor;
import org.ekstep.content.concrete.processor.MissingControllerValidatorProcessor;
import org.ekstep.content.processor.AbstractProcessor;
import org.ekstep.content.processor.ContentPipelineProcessor;

public class PipelineRequestorClient {

	public static AbstractProcessor getPipeline(String operation, String basePath, String contentId) {
		ContentPipelineProcessor contentPipeline = new ContentPipelineProcessor();

		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);

		switch (operation) {
			
		case "globalizeAssetProcessor":
			contentPipeline.registerProcessor(globalizeAssetProcessor);
			break;
			
		case "localizeAssetProcessor":
			contentPipeline.registerProcessor(localizeAssetProcessor);
			break;

		case "embedControllerProcessor":
			contentPipeline.registerProcessor(embedControllerProcessor);
			break;

		case "missingAssetValidatorProcessor":
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			break;

		case "missingCtrlValidatorProcessor":
			contentPipeline.registerProcessor(missingCtrlValidatorProcessor);
			break;
			
		default:
			break;
		}

		return contentPipeline;
	}
}
