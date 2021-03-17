package org.sunbird.common.mgr.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.IURLManager;
import org.sunbird.common.util.YouTubeUrlUtil;

/**
 * @author amitpriyadarshi
 *
 */
public class YoutubeUrlManagerImpl implements IURLManager {

	@Override
	public Map<String, Object> validateURL(String url, String validationCriterion) {
		if(StringUtils.equalsIgnoreCase(validationCriterion, "license")) {
			String license = YouTubeUrlUtil.getLicense(url);
			boolean isValidLicense = YouTubeUrlUtil.isValidLicense(license);
			Map<String, Object> fieldMap = new HashMap<>();
			fieldMap.put("value", license);
	        fieldMap.put("valid", isValidLicense);
	        return fieldMap;
		}
		else
			throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Passed field is not supported for validation.");
	}

	@Override
	public Map<String, Object> readMetadata(String url) {
		String licenseType = YouTubeUrlUtil.getLicense(url);
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("license", licenseType);
		return metadata;
	}

}
