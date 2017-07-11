package org.ekstep.graph.service.request.validator.cache;

import java.util.HashMap;
import java.util.Map;

public class ValidationKeyValueCahce {

	private static Map<String, String> cache = null;
	
	static{
		cache = new HashMap<String, String>();
	}
}
