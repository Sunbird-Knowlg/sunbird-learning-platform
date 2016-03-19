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
		property.put("operation", Constants.SEARCH_OPERATION_GREATER_THAN);
		property.put("propertyName", "node_graph_id");
		property.put("values", Arrays.asList(4, 6));
		//properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", Constants.SEARCH_OPERATION_GREATER_THAN);
		property.put("propertyName", "node_graph_id");
		property.put("values", Arrays.asList(3.9));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", Constants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "lemma");
		property.put("values", Arrays.asList("test", "पाकि"));
		//properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", Constants.SEARCH_OPERATION_NOT_LIKE);
		property.put("propertyName", "lemma");
		property.put("values", Arrays.asList("test"));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", Constants.SEARCH_OPERATION_EXISTS);
		property.put("propertyName", "lemma");
		property.put("values", Arrays.asList("lemma"));
		properties.add(property);
		
		dto.setProperties(properties);
		dto.setOperation("AND");
		
		processor.processSearch(dto);
	}
}
