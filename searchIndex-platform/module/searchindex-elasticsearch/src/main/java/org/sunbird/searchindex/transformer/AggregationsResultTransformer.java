package org.sunbird.searchindex.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationsResultTransformer implements IESResultTransformer{
	
	
	@SuppressWarnings("unchecked")
	public Object getTransformedObject(Object obj){
		Map<String, Object>  aggObj = (Map<String, Object>) obj;
		List<Map<String, Object>> transformedObj = new ArrayList<Map<String, Object>>();
		for(Map.Entry<String, Object> entry: aggObj.entrySet()){
			Map<String, Object> facetMap = new HashMap<String, Object>();
			String facetName = entry.getKey();
			facetMap.put("name", facetName);
			Map<String, Object> aggKeyMap = (Map<String, Object>) entry.getValue();
			List<Map<String, Object>> facetKeys = new ArrayList<Map<String, Object>>();
			for(Map.Entry<String, Object> aggKeyEntry: aggKeyMap.entrySet()){
				Map<String, Object> facetKeyMap = new HashMap<String, Object>();
				String facetKeyName = aggKeyEntry.getKey();
				facetKeyMap.put("name", facetKeyName);
				Map<String, Object> facetKeyCountMap = (Map<String, Object>) aggKeyEntry.getValue();
				long count = (long) facetKeyCountMap.get("count");
				facetKeyMap.put("count", count);
				facetKeys.add(facetKeyMap);
			}
			facetMap.put("values", facetKeys);
			transformedObj.add(facetMap);
		}
		return transformedObj;
	}
}
