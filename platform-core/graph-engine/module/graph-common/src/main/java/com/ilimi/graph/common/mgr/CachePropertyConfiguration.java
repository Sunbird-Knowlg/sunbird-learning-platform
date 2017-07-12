package com.ilimi.graph.common.mgr;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CachePropertyConfiguration {
	
	
	public static List<String> getProperties() {
		String key = "platform.data.node.cacheable.properties";
		String value = Configuration.getProperty(key);
		List<String> result = null;
		if (StringUtils.isNotBlank(value))
			result = Arrays.asList(value.replaceAll("\\s", "").split(","));
		return result;
	}

}
