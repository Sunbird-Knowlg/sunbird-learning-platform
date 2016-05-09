package com.ilimi.taxonomy.content.concrete.processor;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class LocalizeAssetProcessor extends AbstractProcessor {
	
	private static Logger LOGGER = LogManager.getLogger(LocalizeAssetProcessor.class.getName());
	
	public LocalizeAssetProcessor(String basePath, String contentId) {
		this.basePath = basePath;
		this.contentId = contentId;
	}

	@Override
	protected Content process(Content content) {
		try {
			if (null != content) {
				Map<String, String> mediaSrcMap = getMediaSrcMap(getMedia(content));
				Map<String, String> downloadedAssetsMap = downloadAssets(mediaSrcMap, this.basePath);
				Manifest manifest = content.getManifest();
				if (null != manifest)
					manifest.setMedias(getUpdatedMediaWithUrl(downloadedAssetsMap, getMedia(content)));
			}
		} catch(Exception e) {
			LOGGER.error("", e);
		}
		
		return content;
	}

}
