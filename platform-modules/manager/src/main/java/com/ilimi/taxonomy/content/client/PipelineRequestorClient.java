package com.ilimi.taxonomy.content.client;

import com.ilimi.taxonomy.content.concrete.processor.AssessmentItemCreatorProcessor;
import com.ilimi.taxonomy.content.concrete.processor.AssetCreatorProcessor;
import com.ilimi.taxonomy.content.concrete.processor.EmbedControllerProcessor;
import com.ilimi.taxonomy.content.concrete.processor.GlobalizeAssetProcessor;
import com.ilimi.taxonomy.content.concrete.processor.LocalizeAssetProcessor;
import com.ilimi.taxonomy.content.concrete.processor.MissingAssetValidatorProcessor;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.processor.ContentPipelineProcessor;

public class PipelineRequestorClient {
	
	public static AbstractProcessor getPipeline(String operation, String basePath, String contentId) {
		ContentPipelineProcessor contentPipeline = new ContentPipelineProcessor();
		
		AbstractProcessor localizeAssetProcessor = new LocalizeAssetProcessor(basePath, contentId);
		AbstractProcessor embedControllerProcessor = new EmbedControllerProcessor(basePath, contentId);
		AbstractProcessor missingAssetValidatorProcessor = new MissingAssetValidatorProcessor(basePath, contentId);
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
		AbstractProcessor assetCreatorProcessor = new AssetCreatorProcessor(basePath, contentId);
		AbstractProcessor assessmentItemCreatorProcessor = new AssessmentItemCreatorProcessor(basePath, contentId);
		
		switch (operation) {
		case "compress":
			contentPipeline.registerProcessor(localizeAssetProcessor);
			contentPipeline.registerProcessor(embedControllerProcessor);
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			break;
			
		case "extract":
			contentPipeline.registerProcessor(missingAssetValidatorProcessor);
			contentPipeline.registerProcessor(globalizeAssetProcessor);
			contentPipeline.registerProcessor(assetCreatorProcessor);
			contentPipeline.registerProcessor(assessmentItemCreatorProcessor);
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
		AbstractProcessor globalizeAssetProcessor = new GlobalizeAssetProcessor(basePath, contentId);
		AbstractProcessor assetCreatorProcessor = new AssetCreatorProcessor(basePath, contentId);
		AbstractProcessor assessmentItemCreatorProcessor = new AssessmentItemCreatorProcessor(basePath, contentId);
		
		switch (operation) {
		case "compress":
			localizeAssetProcessor.setNextProcessor(embedControllerProcessor);
			embedControllerProcessor.setNextProcessor(missingAssetValidatorProcessor);
			head = localizeAssetProcessor;
			break;
			
		case "extract":
			missingAssetValidatorProcessor.setNextProcessor(globalizeAssetProcessor);
			globalizeAssetProcessor.setNextProcessor(assetCreatorProcessor);
			assetCreatorProcessor.setNextProcessor(assessmentItemCreatorProcessor);
			head = missingAssetValidatorProcessor;
			break;

		default:
			break;
		}
		
		return head;
	}

}
