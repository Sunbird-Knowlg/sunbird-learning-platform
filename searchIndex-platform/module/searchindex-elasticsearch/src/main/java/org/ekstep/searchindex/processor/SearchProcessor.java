package org.ekstep.searchindex.processor;

import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.util.Constants;

public class SearchProcessor {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processSearch(SearchDTO searchDTO){
		List<Map> properties = searchDTO.getProperties();
		for(Map<String, Object> property: properties){
			List propertyNames = (List) property.get("propertyNames");
			String operation = (String) property.get("operation");
			switch(operation){
			case Constants.SEARCH_OPERATION_LESS_THAN:{
				
			}
				
			}
		}
	}
}
