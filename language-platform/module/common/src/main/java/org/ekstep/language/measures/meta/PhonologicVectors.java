package org.ekstep.language.measures.meta;

import java.util.HashMap;
import java.util.Map;

public class PhonologicVectors {

    private static Map<String, Map<String, Integer[]>> vectorMap = new HashMap<String, Map<String, Integer[]>>();
    private static Map<String, Map<String, Integer>> incrMap = new HashMap<String, Map<String, Integer>>();
    private static Map<String, Double[]> weightageMap = new HashMap<String, Double[]>();

    public static Integer[] getPhonologicVector(String language, String s) {
        return Vectors.getVector(language, s, vectorMap);
    }

    public static int getIncrement(String language, String s) {
        return Vectors.getIncrement(language, s, incrMap);
    }

    public static Double[] getWeightage(String language) {
        return Vectors.getWeightage(language, weightageMap);
    }

    public static int getVectorCount(String language) {
        return Vectors.getVectorCount(language, weightageMap);
    }

    public static void load(String language) throws Exception {
        Vectors.load(language, vectorMap, incrMap, weightageMap, "PhonologicVectors.csv", "PhonologicWeightage.csv");
    }
}
