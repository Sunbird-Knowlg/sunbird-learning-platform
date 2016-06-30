package com.ilimi.taxonomy.content.concrete.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class EmbedControllerProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(EmbedControllerProcessor.class.getName());
	
	public EmbedControllerProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Plugin process(Plugin content) {
		// TODO Auto-generated method stub
		return null;
	}

}
