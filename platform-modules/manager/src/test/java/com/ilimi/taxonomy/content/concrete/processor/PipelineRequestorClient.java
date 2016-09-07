package com.ilimi.taxonomy.content.concrete.processor;

import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.processor.ContentPipelineProcessor;

public class PipelineRequestorClient {

	public static AbstractProcessor getPipeline(String operation, String basePath, String contentId) {
		ContentPipelineProcessor contentPipeline = new ContentPipelineProcessor();

		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
		AbstractProcessor assetsValidatorProcessor = new AssetsValidatorProcessor(basePath, contentId);
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

		case "assetsValidatorProcessor":
			contentPipeline.registerProcessor(assetsValidatorProcessor);
			break;	

		default:
			break;
		}

		return contentPipeline;
	}
}
