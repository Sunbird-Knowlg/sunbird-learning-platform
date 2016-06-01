package org.ekstep.searchindex.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.util.CompositeSearchConstants;


public class SearchProcessorTest {
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws Exception {
		SearchProcessor processor = new SearchProcessor();
		
		SearchDTO dto =  new SearchDTO();
		List<Map> properties = new ArrayList<Map>();
		
		Map<String, Object> property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN);
		property.put("propertyName", "node_graph_id");
		property.put("values", Arrays.asList(4, 6));
		//properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_GREATER_THAN);
		property.put("propertyName", "node_graph_id");
		property.put("values", Arrays.asList(3.9));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "*");
		property.put("values", Arrays.asList("पाकि"));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL);
		property.put("propertyName", "lemma");
		property.put("values", Arrays.asList("hi"));
		//properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_NOT_LIKE);
		property.put("propertyName", "lemma");
		property.put("values", Arrays.asList("test", "वालों"));
		//properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EXISTS);
		property.put("propertyName", "lemma");
		property.put("values", Arrays.asList("lemma"));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_NOT_EXISTS);
		property.put("propertyName", "lemmaOne");
		property.put("values", Arrays.asList("lemmaOne"));
		properties.add(property);
		
		dto.setProperties(properties);
		dto.setOperation("AND");
		dto.setLimit(10000);
		processor.processSearch(dto, true);
	}
}
