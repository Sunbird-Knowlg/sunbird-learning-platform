package org.sunbird.common.mgr.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.IURLManager;
import org.sunbird.common.util.GoogleDriveUrlUtil;

/**
 * @author amitpriyadarshi
 *
 */
public class GoogleDriveUrlManagerImpl implements IURLManager {

	@Override
	public Map<String, Object> validateURL(String url, String validationCriterion) {
		if(StringUtils.equalsIgnoreCase(validationCriterion, "size")) {
			Long size = GoogleDriveUrlUtil.getSize(url);
			boolean isValidSize = GoogleDriveUrlUtil.isValidSize(size);
			Map<String, Object> fieldMap = new HashMap<>();
			fieldMap.put("value", size);
	        fieldMap.put("valid", isValidSize);
	        return fieldMap;
		}
		else
			throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Passed field is not supported for validation.");
	}

	@Override
	public Map<String, Object> readMetadata(String url) {
		return GoogleDriveUrlUtil.getMetadata(url);
	}

}
