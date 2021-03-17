package org.sunbird.content.client;

import org.sunbird.content.concrete.processor.AssetsLicenseValidatorProcessor;
import org.sunbird.content.concrete.processor.AssetsValidatorProcessor;
import org.sunbird.content.concrete.processor.EmbedControllerProcessor;
import org.sunbird.content.concrete.processor.GlobalizeAssetProcessor;
import org.sunbird.content.concrete.processor.LocalizeAssetProcessor;
import org.sunbird.content.concrete.processor.MissingAssetValidatorProcessor;
import org.sunbird.content.concrete.processor.MissingControllerValidatorProcessor;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.content.processor.ContentPipelineProcessor;
import org.sunbird.telemetry.logger.TelemetryManager;

public class PipelineRequestorClient {

	public static AbstractProcessor getPipeline(String operation, String basePath, String contentId) {
		ContentPipelineProcessor contentPipeline = new ContentPipelineProcessor();
		
		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
		AbstractProcessor assetsValidatorProcessor = new AssetsValidatorProcessor(basePath, contentId);
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
		AbstractProcessor assetLicenseValidatorProcessor = new AssetsLicenseValidatorProcessor(basePath, contentId);
		
		switch (operation) {
		case "compress":
		case "COMPRESS":
			TelemetryManager.log("Registering the Processors for 'COMPRESS' Operation.");
			contentPipeline.registerProcessor(localizeAssetProcessor);
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			break;
			
		case "extract":
		case "EXTRACT":
			TelemetryManager.log("Registering the Processors for 'EXTRACT' Operation.");
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			contentPipeline.registerProcessor(assetsValidatorProcessor);
			contentPipeline.registerProcessor(missingCtrlValidatorProcessor);
			contentPipeline.registerProcessor(globalizeAssetProcessor);
			contentPipeline.registerProcessor(embedControllerProcessor);
			break;
			
		case "validate":
		case "VALIDATE":
			TelemetryManager.log("Registering the Processors for 'VALIDATE' Operation.");
			contentPipeline.registerProcessor(localizeAssetProcessor);
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			contentPipeline.registerProcessor(assetsValidatorProcessor);
			contentPipeline.registerProcessor(missingCtrlValidatorProcessor);
			contentPipeline.registerProcessor(assetLicenseValidatorProcessor);
			break;

		default:
			TelemetryManager.log("Invalid Pipeline Operation.");
			break;
		}
		
		return contentPipeline;
	}
	
//	public static AbstractProcessor getPipelineChain(String operation, String basePath, String contentId) {
//		AbstractProcessor head = null;
//
//		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
//		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
//		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
//		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
//		AbstractProcessor assetsValidatorProcessor = new AssetsValidatorProcessor(basePath, contentId);
//		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
//
//		switch (operation) {
//		case "compress":
//		case "COMPRESS":
//			TelemetryManager.log("Initialising the Processor's Chain for 'COMPRESS' Operation.");
//			localizeAssetProcessor.setNextProcessor(embedControllerProcessor);
//			embedControllerProcessor.setNextProcessor(missingAssetValidatorProcessor);
//			head = localizeAssetProcessor;
//			break;
//
//		case "extract":
//		case "EXTRACT":
//			TelemetryManager.log("Initialising the Processor's Chain for 'EXTRACT' Operation.");
//			missingAssetValidatorProcessor.setNextProcessor(assetsValidatorProcessor);
//			assetsValidatorProcessor.setNextProcessor(missingCtrlValidatorProcessor);
//			missingCtrlValidatorProcessor.setNextProcessor(globalizeAssetProcessor);
//			globalizeAssetProcessor.setNextProcessor(embedControllerProcessor);
//			head = missingAssetValidatorProcessor;
//			break;
//
//		case "validate":
//		case "VALIDATE":
//			TelemetryManager.log("Initialising the Processor's Chain for 'VALIDATE' Operation.");
//			missingAssetValidatorProcessor.setNextProcessor(assetsValidatorProcessor);
//			assetsValidatorProcessor.setNextProcessor(missingCtrlValidatorProcessor);
//			head = missingAssetValidatorProcessor;
//			break;
//
//		default:
//			TelemetryManager.log("Invalid Pipeline Operation.");
//			break;
//		}
//
//		return head;
//	}

}
