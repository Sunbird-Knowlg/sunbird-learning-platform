package org.sunbird.graph.model.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.telemetry.logger.TelemetryManager;

/**
 * 
 * @author mahesh
 *
 */

public class CategoryCache {

	public static List<Object> getTerms(String framework, String category) {
		String key = getKey(framework, category);
		return RedisStoreUtil.getList(key);
	}

	@SuppressWarnings("unchecked")
	public static void setFramework(String id, Map<String, Object> framework) {
		try {
			if (null != framework && !framework.isEmpty()) {
				List<Map<String, Object>> categories = (List<Map<String, Object>>) framework.get("categories");
				setFramework(id, categories);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private static String getKey(String framework, String category) {
		return "cat_" + framework + category;
	}

	private static void setFramework(String framework, List<Map<String, Object>> categories) {
		if (null != categories && !categories.isEmpty()) {
			for (Map<String, Object> category : categories) {
				String catName = (String) category.get("code");
				List<Object> terms = getTerms(category, "terms");
				if (!terms.isEmpty()) {
					String key = getKey(framework, catName);
					TelemetryManager.info("Setting framework category cache with key: " + key);
					RedisStoreUtil.saveList(key, terms);
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
						List<Object> childTerms = getTerms(term, "children");
						if (!childTerms.isEmpty())
							returnTerms.addAll(childTerms);
					}
				}
		}
		return returnTerms;
	}
}