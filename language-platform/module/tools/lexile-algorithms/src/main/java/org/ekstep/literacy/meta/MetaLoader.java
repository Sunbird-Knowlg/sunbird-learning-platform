package org.ekstep.literacy.meta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

public class MetaLoader {

	public static void loadVectors(String filePath, Map<String, Integer[]> vectorMap, Map<String, Integer> incrMap) throws Exception {
		try {
			File f = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(f));
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
		}
	}

	public static void loadWeightage(String filePath, Double[] weightage) throws Exception {
		try {
			File f = new File(filePath);
			BufferedReader br = new BufferedReader(new FileReader(f));
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
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
