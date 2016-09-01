package org.ekstep.language.measures.meta;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class Vectors is to load orthographic and phonologic vectors from file
 * into its meta class and to lookup
 *
 * @author rayulu
 */
public class Vectors {

	/**
	 * Gets the vector.
	 *
	 * @param language
	 *            the language
	 * @param s
	 *            the s
	 * @param vectorMap
	 *            the vector map
	 * @return the vector
	 */
	public static Integer[] getVector(String language, String s, Map<String, Map<String, Integer[]>> vectorMap) {
		if (null != s && s.trim().length() > 0) {
			if (vectorMap.containsKey(language.toLowerCase().trim())) {
				return vectorMap.get(language.toLowerCase().trim()).get(s.toUpperCase());
			}
		}
		return null;
	}

	/**
	 * Gets the increment.
	 *
	 * @param language
	 *            the language
	 * @param s
	 *            the s
	 * @param incrMap
	 *            the incr map
	 * @return the increment
	 */
	public static int getIncrement(String language, String s, Map<String, Map<String, Integer>> incrMap) {
		try {
			if (StringUtils.isNotBlank(s)) {
				if (incrMap.containsKey(language.toLowerCase().trim())) {
					return incrMap.get(language.toLowerCase().trim()).get(s.toUpperCase());
				}
			}
		} catch (Exception e) {
		}
		return 0;
	}

	/**
	 * Gets the weightage.
	 *
	 * @param language
	 *            the language
	 * @param weightageMap
	 *            the weightage map
	 * @return the weightage
	 */
	public static Double[] getWeightage(String language, Map<String, Double[]> weightageMap) {
		return weightageMap.get(language.toLowerCase().trim());
	}

	/**
	 * Gets the vector count.
	 *
	 * @param language
	 *            the language
	 * @param weightageMap
	 *            the weightage map
	 * @return the vector count
	 */
	public static int getVectorCount(String language, Map<String, Double[]> weightageMap) {
		Double[] weightage = weightageMap.get(language.toLowerCase().trim());
		return null == weightage ? 0 : weightage.length;
	}

	/**
	 * Load.
	 *
	 * @param language
	 *            the language
	 * @param vectorMap
	 *            the vector map
	 * @param incrMap
	 *            the incr map
	 * @param weightageMap
	 *            the weightage map
	 * @param vectorFileName
	 *            the vector file name
	 * @param weightageFileName
	 *            the weightage file name
	 * @throws Exception
	 *             the exception
	 */
	public static void load(String language, Map<String, Map<String, Integer[]>> vectorMap,
			Map<String, Map<String, Integer>> incrMap, Map<String, Double[]> weightageMap, String vectorFileName,
			String weightageFileName) throws Exception {
		Map<String, Integer[]> langVectorMap = vectorMap.get(language.toLowerCase().trim());
		if (null == langVectorMap) {
			langVectorMap = new HashMap<String, Integer[]>();
			vectorMap.put(language.toLowerCase().trim(), langVectorMap);
		}
		Map<String, Integer> langIncrMap = incrMap.get(language.toLowerCase().trim());
		if (null == langIncrMap) {
			langIncrMap = new HashMap<String, Integer>();
			incrMap.put(language.toLowerCase().trim(), langIncrMap);
		}

		MetaLoader.loadVectors("/" + language.toLowerCase().trim() + "/" + vectorFileName, langVectorMap, langIncrMap);
		if (null != langVectorMap && !langVectorMap.isEmpty()) {
			Integer[] vector = langVectorMap.values().iterator().next();
			Double[] langWeightage = new Double[vector.length];
			MetaLoader.loadWeightage("/" + language.toLowerCase().trim() + "/" + weightageFileName, langWeightage);
			weightageMap.put(language.toLowerCase().trim(), langWeightage);
		}
	}
}
