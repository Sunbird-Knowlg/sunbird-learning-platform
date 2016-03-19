package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.util.Constants;


public class SearchProcessorTest {
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException {
		SearchProcessor processor = new SearchProcessor();
		
		SearchDTO dto =  new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		
		Map<String, Object> property = new HashMap<String, Object>();
		property.put("operation", Constants.SEARCH_OPERATION_STARTS_WITH);
		property.put("propertyNames", Arrays.asList("lemma"));
		property.put("value", "hi");
		
		properties.add(property);
		
		dto.setProperties(properties);
		dto.setOperation("AND");
		
		processor.processSearch(dto);
	}
}
