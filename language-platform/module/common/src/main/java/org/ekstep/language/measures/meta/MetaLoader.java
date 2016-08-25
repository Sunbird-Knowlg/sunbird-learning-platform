package org.ekstep.language.measures.meta;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class MetaLoader {

    public static void loadVectors(String filePath, Map<String, Integer[]> vectorMap, Map<String, Integer> incrMap)
            throws Exception {
    	InputStream is = null;
        BufferedReader br = null;
        try {
        	is = MetaLoader.class.getResourceAsStream(filePath);
        	br = new BufferedReader(new InputStreamReader(is));
            br.readLine();
            String s = null;
            while ((s = br.readLine()) != null) {
                String[] tokens = s.trim().split(",");
                if (null != tokens && tokens.length > 3) {
                    String unicode = tokens[0].trim();
                    Integer[] vector = new Integer[tokens.length - 3];
                    for (int i = 2; i < tokens.length - 1; i++) {
                        vector[i - 2] = Integer.parseInt(tokens[i].trim());
                    }
                    vectorMap.put(unicode.toUpperCase(), vector);
                    Integer incr = Integer.parseInt(tokens[tokens.length - 1].trim());
                    incrMap.put(unicode.toUpperCase(), incr);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }  finally {
        	if (null != br)
           	 br.close();
           	if (null != is)
           		is.close();
           }
    }

    public static void loadWeightage(String filePath, Double[] weightage) throws Exception {
    	InputStream is = null;
        BufferedReader br = null;
        try {
        	is = MetaLoader.class.getResourceAsStream(filePath);
        	br = new BufferedReader(new InputStreamReader(is));
            br.readLine();
            String s = null;
            int index = 0;
            while ((s = br.readLine()) != null) {
                String[] tokens = s.trim().split(",");
                if (null != tokens && tokens.length == 2) {
                    Double weight = Double.parseDouble(tokens[1].trim());
                    weightage[index] = weight;
                    index += 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
        	if (null != br)
        	 br.close();
        	if (null != is)
        		is.close();
        }
    }
}
