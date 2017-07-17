package org.ekstep.jobs.samza.util;

import org.codehaus.jackson.map.ObjectMapper;

public class JSONUtils {
	
	private static ObjectMapper mapper;
	
	static {
		mapper = new ObjectMapper();
		//mapper.configure(Deserialization.FAIL_ON_UNKNOWN_PROPERTIES, false);
	    //mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
	}
	
	public static String serialize(Object object) throws Exception {
		return mapper.writeValueAsString(object);
	}
	
}