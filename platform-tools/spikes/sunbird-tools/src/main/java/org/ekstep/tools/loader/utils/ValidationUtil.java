/**
 * 
 */
package org.ekstep.tools.loader.utils;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public class ValidationUtil {

	public static boolean validateCreateContent(JsonObject content) {
		
		return (StringUtils.isNotBlank(JsonUtil.getFromObject(content, "mimeType"))
				&& StringUtils.isNotBlank(JsonUtil.getFromObject(content, "createdBy")));
		
		/*boolean flag = false;

		if (StringUtils.isNotBlank(content.get("mimeType").getAsString())
				&& StringUtils.isNotBlank(content.get("createdBy").getAsString())) {
			flag = true;
		}
			return flag;*/
	}

	public static boolean valiateUpdateContent(JsonObject content) {
		
		return (StringUtils.isNotBlank(JsonUtil.getFromObject(content, "content_id")));
	}

	public static boolean valiateCreateConcept(JsonObject concept) {

		return (StringUtils.isNotBlank(JsonUtil.getFromObject(concept, "code"))
				&& StringUtils.isNotBlank(JsonUtil.getFromObject(concept, "framework"))
				&& StringUtils.isNotBlank(JsonUtil.getFromObject(concept, "parent_code")));
	}

}
