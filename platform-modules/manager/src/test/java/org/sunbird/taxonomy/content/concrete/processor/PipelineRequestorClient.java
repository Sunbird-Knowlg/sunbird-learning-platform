package org.sunbird.taxonomy.content.concrete.processor;

import org.sunbird.content.concrete.processor.AssetsLicenseValidatorProcessor;
import org.sunbird.content.concrete.processor.EmbedControllerProcessor;
import org.sunbird.content.concrete.processor.GlobalizeAssetProcessor;
import org.sunbird.content.concrete.processor.LocalizeAssetProcessor;
import org.sunbird.content.concrete.processor.MissingAssetValidatorProcessor;
import org.sunbird.content.concrete.processor.MissingControllerValidatorProcessor;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.content.processor.ContentPipelineProcessor;

public class PipelineRequestorClient {

	public static AbstractProcessor getPipeline(String operation, String basePath, String contentId) {
		ContentPipelineProcessor contentPipeline = new ContentPipelineProcessor();

		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
		AbstractProcessor assetsLicenseValidatorProcessor = new AssetsLicenseValidatorProcessor(basePath, contentId);

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

		case "assetsLicenseValidatorProcessor":
			contentPipeline.registerProcessor(assetsLicenseValidatorProcessor);
			break;
			
		default:
			break;
		}

		return contentPipeline;
	}
}
