package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;

import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;

public class AssetsValidatorProcessor extends AbstractProcessor {

	@Override
	protected Content process(Content content) {
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
