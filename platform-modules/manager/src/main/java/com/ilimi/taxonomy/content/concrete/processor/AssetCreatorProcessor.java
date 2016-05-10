package com.ilimi.taxonomy.content.concrete.processor;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssetCreatorProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(AssetCreatorProcessor.class.getName());
	
	public AssetCreatorProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Content process(Content content) {
		try {
			if (null != content) {
				Map<String, String> mediaSrcMap = getNonAssetObjMediaSrcMap(getMedia(content));
			}
		} catch(Exception e) {
			LOGGER.error("", e);
		}
		
		return content;
	}
}
