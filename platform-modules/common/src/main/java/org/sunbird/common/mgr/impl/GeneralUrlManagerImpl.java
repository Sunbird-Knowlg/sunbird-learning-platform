package org.sunbird.common.mgr.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.IURLManager;
import org.sunbird.common.util.HTTPUrlUtil;

/**
 * @author amitpriyadarshi
 *
 */
public class GeneralUrlManagerImpl implements IURLManager {

	@Override
	public Map<String, Object> validateURL(String url, String validationCriterion) {
		if(StringUtils.equalsIgnoreCase(validationCriterion, "size")) {
			Map<String, Object> metadata = HTTPUrlUtil.getMetadata(url);
			Long size = metadata.get("size")==null?0:(Long)metadata.get("size");
			boolean isValidSize = HTTPUrlUtil.isValidSize(size);
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
		return HTTPUrlUtil.getMetadata(url);
	}

}
