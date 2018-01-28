package org.ekstep.graph.model.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.telemetry.logger.TelemetryManager;

/**
 * 
 * @author mahesh
 *
 */

public class CategoryCache {

	private static Map<String, List<Object>> categories = new HashMap<>();

	public static List<Object> getTerms(String framework, String category) {
		String key = getKey(framework, category);
		return categories.get(key);
	}

	@SuppressWarnings("unchecked")
	public static void setFramework(String id, Map<String, Object> framework) {
		try {
			if (null != framework && !framework.isEmpty()) {
				List<Map<String, Object>> categories = (List<Map<String, Object>>) framework.get("categories");
				setFramework(id, categories);
			}
		} catch (Exception e) {
			TelemetryManager.error("Error while setting category cache for framework: "+ id, e);
		}
	}
	
	private static String getKey(String framework, String category) {
		return framework + category;
	}

	private static void setFramework(String framework, List<Map<String, Object>> categories) {
		if (null != categories && !categories.isEmpty()) {
			for (Map<String, Object> category : categories) {
				String catName = (String) category.get("code");
				List<Object> terms = getTerms(category, "terms");
				if (!terms.isEmpty()) {
					String key = getKey(framework, catName);
					CategoryCache.categories.put(key, terms);
					Map<String, Object> params = new HashMap<>();
					params.put("framework", framework);
					params.put("category", catName);
					params.put("terms", terms);
					TelemetryManager.info("Update - category cache.", params);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Object> getTerms(Map<String, Object> category, String key) {
		List<Object> returnTerms = new ArrayList<Object>();
		if (null != category && !category.isEmpty()) {
			List<Map<String, Object>> terms = (List<Map<String, Object>>) category.get(key);
			if (null != terms)
				for (Map<String, Object> term : terms) {
					Object termName = term.get("name");
					if (StringUtils.isNotBlank((String) termName)) {
						returnTerms.add(termName);
						List<Object> childTerms = getTerms(category, "children");
						if (!childTerms.isEmpty())
							returnTerms.addAll(childTerms);
					}
				}
		}
		return returnTerms;
	}
}
