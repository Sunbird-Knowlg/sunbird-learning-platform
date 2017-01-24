package com.ilimi.taxonomy.content.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.taxonomy.content.concrete.processor.AssetsValidatorProcessor;
import com.ilimi.taxonomy.content.concrete.processor.EmbedControllerProcessor;
import com.ilimi.taxonomy.content.concrete.processor.GlobalizeAssetProcessor;
import com.ilimi.taxonomy.content.concrete.processor.LocalizeAssetProcessor;
import com.ilimi.taxonomy.content.concrete.processor.MissingAssetValidatorProcessor;
import com.ilimi.taxonomy.content.concrete.processor.MissingControllerValidatorProcessor;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.processor.ContentPipelineProcessor;

public class PipelineRequestorClient {
	
	private static Logger LOGGER = LogManager.getLogger(PipelineRequestorClient.class.getName());
	
	public static AbstractProcessor getPipeline(String operation, String basePath, String contentId) {
		ContentPipelineProcessor contentPipeline = new ContentPipelineProcessor();
		
		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
		AbstractProcessor assetsValidatorProcessor = new AssetsValidatorProcessor(basePath, contentId);
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
		
		switch (operation) {
		case "compress":
			LOGGER.info("Registering the Processors for 'COMPRESS' Operation.");
			contentPipeline.registerProcessor(localizeAssetProcessor);
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			break;
			
		case "extract":
			LOGGER.info("Registering the Processors for 'EXTRACT' Operation.");
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			contentPipeline.registerProcessor(assetsValidatorProcessor);
			contentPipeline.registerProcessor(missingCtrlValidatorProcessor);
			contentPipeline.registerProcessor(globalizeAssetProcessor);
			contentPipeline.registerProcessor(embedControllerProcessor);
			break;
			
		case "validate":
			LOGGER.info("Registering the Processors for 'VALIDATE' Operation.");
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			contentPipeline.registerProcessor(assetsValidatorProcessor);
			contentPipeline.registerProcessor(missingCtrlValidatorProcessor);
			break;

		default:
			break;
		}
		
		return contentPipeline;
	}
	
	public static AbstractProcessor getPipelineChain(String operation, String basePath, String contentId) {
		AbstractProcessor head = null;
		
		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor missingCtrlValidatorProcessor = new MissingControllerValidatorProcessor(basePath, contentId);
		AbstractProcessor assetsValidatorProcessor = new AssetsValidatorProcessor(basePath, contentId);
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
		
		switch (operation) {
		case "compress":
			LOGGER.info("Initialising the Processor's Chain for 'COMPRESS' Operation.");
			localizeAssetProcessor.setNextProcessor(embedControllerProcessor);
			embedControllerProcessor.setNextProcessor(missingAssetValidatorProcessor);
			head = localizeAssetProcessor;
			break;
			
		case "extract":
			LOGGER.info("Initialising the Processor's Chain for 'EXTRACT' Operation.");
			missingAssetValidatorProcessor.setNextProcessor(assetsValidatorProcessor);
			assetsValidatorProcessor.setNextProcessor(missingCtrlValidatorProcessor);
			missingCtrlValidatorProcessor.setNextProcessor(globalizeAssetProcessor);
			globalizeAssetProcessor.setNextProcessor(embedControllerProcessor);
			head = missingAssetValidatorProcessor;
			break;
			
		case "validate":
			LOGGER.info("Initialising the Processor's Chain for 'VALIDATE' Operation.");
			missingAssetValidatorProcessor.setNextProcessor(assetsValidatorProcessor);
			assetsValidatorProcessor.setNextProcessor(missingCtrlValidatorProcessor);
			head = missingAssetValidatorProcessor;
			break;

		default:
			LOGGER.warn("Invalid Pipeline Operation.");
			break;
		}
		
		return head;
	}

}
