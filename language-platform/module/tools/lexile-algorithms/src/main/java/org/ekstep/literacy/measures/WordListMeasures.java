package org.ekstep.literacy.measures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ekstep.literacy.entity.WordComplexity;
import org.ekstep.literacy.meta.OrthographicVectors;
import org.ekstep.literacy.meta.PhonologicVectors;

public class WordListMeasures {

	public static void main(String[] args) {
		String path = System.getProperty("user.dir");
		//path = "/Users/rayulu/work/EkStep/language_model/wordnets/wiktionary/meta/words";
		boolean files = true;
		List<String> words = new ArrayList<String>();
		if (null != args && args.length > 0) {
			if (args[0].equalsIgnoreCase("file")) {
				files = true;
				if (args.length > 1) {
					path = args[1].trim();
				}
			} else {
				for (int i = 1; i < args.length; i++) {
					words.add(args[i].trim());
				}
			}
		} else {
			System.out.println("Invalid arguments... Exiting... ");
			//System.exit(0);
		}
		if (files) {
			processFile(path);
		} else {
			if (words.isEmpty()) {
				System.out.println("Using default word list...");
				words.add("తలుపవరో");
				words.add("స్పర్ధె");
				words.add("స్పర్ధెయల్లి");
				words.add("ఎందాదరు");
				words.add("అష్టరల్లే");
				words.add("ఏర్పాటు");
				words.add("ఘోషిసిదవు");
				words.add("మాడికొండవు");
				words.add("అపాయదింద");
				words.add("నిద్రె");
				words.add("బయసితు");
				words.add("మత్తు");
				words.add("మాడుత్తేనె");
				words.add("మోరె");
				words.add("స్నేహితరు");
				words.add("హళెయదాద");
				words.add("ఇలియొందు");
				words.add("కొల్లబేడ");
				words.add("మాడుత్తేనె");
			}
			processWords(path, words);
		}
	}

	public static void processFile(String path) {
		try {
			OrthographicVectors.load(path);
			PhonologicVectors.load(path);
			File f = new File(path + File.separator + "words.txt");
			BufferedReader br = new BufferedReader(new FileReader(f));

			File f2 = new File(path + File.separator + "rts.txt");
			BufferedReader br2 = new BufferedReader(new FileReader(f2));

			String outputFile = "output_" + System.currentTimeMillis() + ".csv";
			String outputFile2 = "orthoGroups_" + System.currentTimeMillis() + ".csv";
			String outputFile3 = "phonicGroups_" + System.currentTimeMillis() + ".csv";
			FileWriter fw = new FileWriter(new File(path + File.separator + outputFile));
			BufferedWriter bw = new BufferedWriter(fw);
			FileWriter fw2 = new FileWriter(new File(path + File.separator + outputFile2));
			BufferedWriter bw2 = new BufferedWriter(fw2);
			FileWriter fw3 = new FileWriter(new File(path + File.separator + outputFile3));
			BufferedWriter bw3 = new BufferedWriter(fw3);

			Map<Double, List<WordComplexity>> map = new TreeMap<Double, List<WordComplexity>>();
			Map<Integer, List<WordComplexity>> phonicMap = new TreeMap<Integer, List<WordComplexity>>();
			bw.write(
					"word,notation,syllable_count,unicodes,ortho_complexity,phonic_complexity,ortho_vector,phonic_vector\n");
			DecimalFormat df = new DecimalFormat("##.0");
			String s = null;
			while ((s = br.readLine()) != null) {
				if (null != s && s.trim().length() > 0) {
					WordComplexity wc = WordMeasures.getWordComplexity(s.trim());
					String rts = br2.readLine();
					wc.setRts(rts);
					bw.write(wc.getWord() + "(" + wc.getRts() + ")" + "," + wc.getNotation() + "," + wc.getCount() + ","
							+ wc.getUnicode() + "," + wc.getOrthoComplexity() + "," + wc.getPhonicComplexity() + ","
							+ VectorUtil.getBinaryString(wc.getOrthoVec()) + ","
							+ VectorUtil.getBinaryString(wc.getPhonicVec()) + "\n");
					Double oc = round(df, wc.getOrthoComplexity());
					List<WordComplexity> list = map.get(oc);
					if (null == list) {
						list = new ArrayList<WordComplexity>();
						map.put(oc, list);
					}
					list.add(wc);

					int j = (int) (wc.getPhonicComplexity() / 15);
					List<WordComplexity> pList = phonicMap.get(j);
					if (null == pList) {
						pList = new ArrayList<WordComplexity>();
						phonicMap.put(j, pList);
					}
					pList.add(wc);
				}
			}
			System.out.println("Output written to " + path + File.separator + outputFile);
			writeOrthographicGroups(map, bw2);
			writePhonologicGroups(phonicMap, bw3);
			br.close();
			br2.close();
			bw.close();
			bw2.close();
			bw3.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Double round(DecimalFormat df, Double d) {
		return Double.parseDouble(df.format(d));
	}

	private static void writeOrthographicGroups(Map<Double, List<WordComplexity>> map, BufferedWriter bw2)
			throws Exception {
		int index = 0;
		for (Double score : map.keySet()) {
			bw2.write(score + "," + score + "(Syllable Count)");
			if (index < map.keySet().size() - 1)
				bw2.write(",");
			index += 1;
		}
		bw2.write("\n");
		for (List<WordComplexity> list : map.values()) {
			Collections.sort(list, WordComplexity.wordComparator);
		}
		for (int i = 0; i < 5000; i++) {
			int k = 0;
			for (Double score : map.keySet()) {
				List<WordComplexity> list = map.get(score);
				if (list.size() > i) {
					bw2.write(list.get(i).getWord() + "(" + list.get(i).getRts() + ")" + "," + list.get(i).getCount());
				} else {
					bw2.write(",");
				}
				if (k < map.keySet().size() - 1)
					bw2.write(",");
				k += 1;
			}
			bw2.write("\n");
		}
		System.out.println("Orthographic groups written");
	}

	private static void writePhonologicGroups(Map<Integer, List<WordComplexity>> phonicMap, BufferedWriter bw3)
			throws Exception {
		for (Integer i : phonicMap.keySet()) {
			String val = (i * 15) + " - " + ((i + 1) * 15);
			bw3.write(val + "," + val + "(Syllable Count)" + "," + val + "(Score)" + ",");
		}
		bw3.write("\n");
		for (List<WordComplexity> list : phonicMap.values()) {
			Collections.sort(list, WordComplexity.phonicComplexityComparator);
		}
		for (int i = 0; i < 5000; i++) {
			for (Integer score : phonicMap.keySet()) {
				List<WordComplexity> list = phonicMap.get(score);
				if (list.size() > i) {
					bw3.write(list.get(i).getWord() + "(" + list.get(i).getRts() + ")" + "," + list.get(i).getCount()
							+ "," + list.get(i).getPhonicComplexity() + ",");
				} else {
					bw3.write(",");
				}
			}
			bw3.write("\n");
		}
		System.out.println("Phonologic groups written");
	}

	public static void processWords(String path, List<String> words) {
		try {
			OrthographicVectors.load(path);
			PhonologicVectors.load(path);
			for (String word : words) {
				WordComplexity wc = WordMeasures.getWordComplexity(word);
				/*System.out.println(wc.getWord() + "," + wc.getNotation() + "," + wc.getCount() + "," + wc.getUnicode()
						+ "," + wc.getOrthoComplexity() + "," + wc.getPhonicComplexity() + ","
						+ VectorUtil.getBinaryString(wc.getOrthoVec()) + ","
						+ VectorUtil.getBinaryString(wc.getPhonicVec()));*/
				System.out.println(
						wc.getWord() + "," + wc.getOrthoComplexity() + "," + wc.getPhonicComplexity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
