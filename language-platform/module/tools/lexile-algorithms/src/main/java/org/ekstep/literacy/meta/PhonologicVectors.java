package org.ekstep.literacy.meta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PhonologicVectors {

	private static Map<String, Integer[]> vectorMap = new HashMap<String, Integer[]>();
	private static Map<String, Integer> incrMap = new HashMap<String, Integer>();
	private static Double[] weightage = null;

	public static Integer[] getPhonologicVector(String s) {
		if (null != s && s.trim().length() > 0) {
			if (vectorMap.containsKey(s.toUpperCase())) {
				return vectorMap.get(s.toUpperCase());
			}
		}
		return null;
	}

	public static int getIncrement(String s) {
		if (null != s && s.trim().length() > 0) {
			if (incrMap.containsKey(s.toUpperCase())) {
				return incrMap.get(s.toUpperCase());
			}
		}
		return 0;
	}
	
	public static Double[] getWeightage() {
		return weightage;
	}
	
	public static int getVectorCount() {
		return null == weightage ? 0 : weightage.length;
	}

	public static void load(String path) throws Exception {
		MetaLoader.loadVectors(path + File.separator + "PhonologicVectors.csv", vectorMap, incrMap);
		if (null != vectorMap && !vectorMap.isEmpty()) {
			Integer[] vector = vectorMap.values().iterator().next();
			weightage = new Double[vector.length];
			MetaLoader.loadWeightage(path + File.separator + "PhonologicWeightage.csv", weightage);
		}
	}
}
