package com.ilimi.framework.common;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.slugs.Slug;

import com.ilimi.graph.common.Identifier;

public class FrameworkIdentifier extends Identifier {
	private static String prefix = "fr_";
	
	public static String getIdentifier(String... vals) {
		String id = prefix;
		if (vals != null) {
			if (null != vals[0] && vals[0].startsWith("fr_"))
				id = "";
			for (String val : vals) {
				if (StringUtils.isNoneBlank(val))
					id += val;
			}
			return Slug.makeSlug(id, true);
		} else {
			return id + getUniqueIdFromTimestamp();
		}
	}
}
