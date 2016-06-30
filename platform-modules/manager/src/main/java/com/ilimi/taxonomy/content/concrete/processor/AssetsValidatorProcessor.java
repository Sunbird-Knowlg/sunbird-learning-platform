package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;

import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssetsValidatorProcessor extends AbstractProcessor {

	@Override
	protected Plugin process(Plugin content) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean isValidAssetMimeType(File file) {
		boolean isValidMimeType = false;
		if (file.exists()) {
			
		}
		return isValidMimeType;
	}
	
	private boolean isValidAssetSize(File file) {
		boolean isValidMimeType = false;
		if (file.exists()) {
			
		}
		return isValidMimeType;
	}

}
