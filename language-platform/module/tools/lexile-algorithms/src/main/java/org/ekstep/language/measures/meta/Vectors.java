package org.ekstep.language.measures.meta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Vectors {

    public static Integer[] getVector(String language, String s, Map<String, Map<String, Integer[]>> vectorMap) {
        if (null != s && s.trim().length() > 0) {
            if (vectorMap.containsKey(language.toLowerCase().trim())) {
                return vectorMap.get(language.toLowerCase().trim()).get(s.toUpperCase());
            }
        }
        return null;
    }

    public static int getIncrement(String language, String s, Map<String, Map<String, Integer>> incrMap) {
        if (null != s && s.trim().length() > 0) {
            if (incrMap.containsKey(language.toLowerCase().trim())) {
                return incrMap.get(language.toLowerCase().trim()).get(s.toUpperCase());
            }
        }
        return 0;
    }

    public static Double[] getWeightage(String language, Map<String, Double[]> weightageMap) {
        return weightageMap.get(language.toLowerCase().trim());
    }

    public static int getVectorCount(String language, Map<String, Double[]> weightageMap) {
        Double[] weightage = weightageMap.get(language.toLowerCase().trim());
        return null == weightage ? 0 : weightage.length;
    }

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

        MetaLoader.loadVectors(language.toLowerCase().trim() + File.separator + vectorFileName, langVectorMap,
                langIncrMap);
        if (null != langVectorMap && !langVectorMap.isEmpty()) {
            Integer[] vector = langVectorMap.values().iterator().next();
            Double[] langWeightage = new Double[vector.length];
            MetaLoader.loadWeightage(
                    File.separator + language.toLowerCase().trim() + File.separator + weightageFileName, langWeightage);
            weightageMap.put(language.toLowerCase().trim(), langWeightage);
        }
    }
}
