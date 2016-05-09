package com.ilimi.taxonomy.content.concrete.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssessmentItemCreationProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(AssessmentItemCreationProcessor.class.getName());
	
	public AssessmentItemCreationProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Content process(Content content) {
		try {
			
		} catch(Exception e) {
			 LOGGER.error("", e);
		}
		return content;
	}

}
