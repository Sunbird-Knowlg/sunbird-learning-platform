package org.ekstep.language.measures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.language.measures.entity.Syllable;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.OrthographicVectors;
import org.ekstep.language.measures.meta.PhonologicVectors;
import org.ekstep.language.measures.meta.TeluguSyllables;

public class WordMeasures2 {

	public static void main(String[] args) {
		String path = "/Users/rayulu/work/EkStep/language_model/wordnets/wiktionary/meta";
		boolean files = true;
		if (files) {
			processFile(path);
		} else {
			List<String> words = new ArrayList<String>();
			words.add("కమల");
			words.add("క్షమించు");
			words.add("ఆక్షేపించు");
			words.add("వారాంగన");
			words.add("ఝఠాఝుఠం");
			words.add("జ్ఞానాధర్షిని");
			words.add("అద్రిజ");
			words.add("మేస్త్రీ");
			processWords(path, words);
		}
	}

	public static void processFile(String path) {
		try {
			OrthographicVectors.load(path);
			PhonologicVectors.load(path);
			File f = new File(path + File.separator + "words.txt");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String outputFile = "output_" + System.currentTimeMillis() + ".csv";
			FileWriter fw = new FileWriter(
					new File(path + File.separator + outputFile));
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("word,notation,syllable_count,unicodes,ortho_complexity,phonic_complexity,ortho_vector,phonic_vector\n");
			String s = null;
			while ((s = br.readLine()) != null) {
				if (null != s && s.trim().length() > 0) {
					WordComplexity wc = getWordComplexity(s.trim());
					bw.write(wc.getWord() + "," + wc.getNotation() + "," + wc.getCount() + ","
							+ wc.getUnicode() + "," + wc.getOrthoComplexity() + "," + wc.getPhonicComplexity() + ","
							+ VectorUtil.getBinaryString(wc.getOrthoVec()) + ","
							+ VectorUtil.getBinaryString(wc.getPhonicVec()) + "\n");
				}
			}
			System.out.println("Output written to " + path + File.separator + outputFile);
			br.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void processWords(String path, List<String> words) {
		try {
			OrthographicVectors.load(path);
			PhonologicVectors.load(path);
			for (String word : words) {
				WordComplexity wc = getWordComplexity(word);
				System.out.println(wc.getWord() + "," + wc.getNotation() + "," + wc.getCount() + "," + wc.getUnicode() + ","
						+ wc.getOrthoComplexity() + "," + wc.getPhonicComplexity() + ","
						+ VectorUtil.getBinaryString(wc.getOrthoVec()) + ","
						+ VectorUtil.getBinaryString(wc.getPhonicVec()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static WordComplexity getWordComplexity(String word) {
		List<Syllable> syllables = getSyllables(word);
		WordComplexity wc = new WordComplexity();
		wc.setWord(word);
		wc.setCount(syllables.size());
		wc.setOrthoComplexity(0.0);
		wc.setPhonicComplexity(0.0);
		String notation = "";
		String unicode = "";
		Integer[] orthoVector = new Integer[OrthographicVectors.getVectorCount()];
		Integer[] phonicVector = new Integer[PhonologicVectors.getVectorCount()];
		for (Syllable s : syllables) {
			notation += s.getCode();
			for (String uc : s.getUnicodes()) {
				unicode += ("\\" + uc);
			}
			unicode += " ";
			getSyllableValues(s, orthoVector, phonicVector, wc);
		}
		wc.setUnicode(unicode.trim());
		wc.setNotation(notation);
		wc.setOrthoVec(orthoVector);
		wc.setPhonicVec(phonicVector);
		return wc;
	}

	private static void getSyllableValues(Syllable s, Integer[] orthoVector, Integer[] phonicVector,
			WordComplexity wc) {
		List<String> unicodes = s.getUnicodes();
		Integer[] vec1 = null;
		Integer[] vec2 = null;
		Double[] orthoWeights = OrthographicVectors.getWeightage();
		Double[] phonicWeights = PhonologicVectors.getWeightage();
		Double orthoComplexity = 0.0;
		Double phonicComplexity = 0.0;
		Map<String, Double[]> weights = new HashMap<String, Double[]>();
		for (String uc : unicodes) {
			Integer[] v1 = OrthographicVectors.getOrthographicVector(uc);
			orthoComplexity += VectorUtil.dotProduct(v1, orthoWeights);
			int orthoIncr = OrthographicVectors.getIncrement(uc);
			orthoComplexity += (orthoComplexity * orthoIncr/100);
			vec1 = VectorUtil.addVector(vec1, v1);

			Integer[] v2 = PhonologicVectors.getPhonologicVector(uc);
			phonicComplexity += VectorUtil.dotProduct(v2, phonicWeights);
			int phonicIncr = PhonologicVectors.getIncrement(uc);
			phonicComplexity += (phonicComplexity * phonicIncr/100);
			vec2 = VectorUtil.addVector(vec2, v2);
			weights.put(uc, VectorUtil.dotMatrix(v2, phonicWeights));
		}
		for (int i = 0; i < unicodes.size(); i++) {
			for (int j = i + 1; j < unicodes.size(); j++) {
				Double[] diffComplexity = VectorUtil.difference(weights.get(unicodes.get(i)),
						weights.get(unicodes.get(j)));
				phonicComplexity += VectorUtil.sum(diffComplexity);
			}
		}
		orthoVector = VectorUtil.addVector(orthoVector, vec1);
		phonicVector = VectorUtil.addVector(phonicVector, vec2);
		wc.setOrthoComplexity(wc.getOrthoComplexity() + orthoComplexity);
		wc.setPhonicComplexity(wc.getPhonicComplexity() + phonicComplexity);
	}

	private static List<Syllable> getSyllables(String word) {
		List<Syllable> syllables = new ArrayList<Syllable>();
		if (null != word && word.trim().length() > 0) {
			String code = "";
			List<String> unicodes = new ArrayList<String>();
			for (int i = 0; i < word.length(); i++) {
				char ch = word.charAt(i);
				String uc = String.format("%04x", (int) ch);
				String s = TeluguSyllables.getSyllableType(uc);
				if (TeluguSyllables.CONSONANT_CODE.equalsIgnoreCase(s)
						|| TeluguSyllables.VOWEL_CODE.equalsIgnoreCase(s)) {
					if (code.endsWith(TeluguSyllables.CONSONANT_CODE) || code.endsWith(TeluguSyllables.VOWEL_CODE)) {
						code += TeluguSyllables.VOWEL_SIGN_CODE;
						unicodes.add(TeluguSyllables.DEFAULT_VOWEL);
						syllables.add(new Syllable(code, unicodes));
						code = "";
						unicodes = new ArrayList<String>();
					}
					if (code.endsWith(TeluguSyllables.HALANT_CODE)
							&& TeluguSyllables.CONSONANT_CODE.equalsIgnoreCase(s)) {
						unicodes.add(uc + TeluguSyllables.MODIFIER_SUFFIX);
						code += s;
					} else {
						unicodes.add(uc);
						code += s;
					}
				} else if (TeluguSyllables.VOWEL_SIGN_CODE.equalsIgnoreCase(s)) {
					code += s;
					unicodes.add(uc);
					syllables.add(new Syllable(code, unicodes));
					code = "";
					unicodes = new ArrayList<String>();
				} else if (TeluguSyllables.HALANT_CODE.equalsIgnoreCase(s)) {
					code += s;
					unicodes.add(uc);
				} else if (TeluguSyllables.CLOSE_VOWEL_CODE.equalsIgnoreCase(s)) {
					if (code.length() == 0 && syllables.size() > 0) {
						Syllable syllable = syllables.get(syllables.size() - 1);
						syllable.setInternalCode(syllable.getInternalCode() + s);
						syllable.setCode(syllable.getCode() + TeluguSyllables.VOWEL_CODE);
						syllable.getUnicodes().add(uc);
					} else {
						code += s;
						unicodes.add(uc);
						syllables.add(new Syllable(code, unicodes));
						code = "";
						unicodes = new ArrayList<String>();
					}
				}
			}
			if (code.endsWith(TeluguSyllables.CONSONANT_CODE) || code.endsWith(TeluguSyllables.VOWEL_CODE)) {
				code += TeluguSyllables.VOWEL_SIGN_CODE;
				unicodes.add(TeluguSyllables.DEFAULT_VOWEL);
				syllables.add(new Syllable(code, unicodes));
			} else if (code.endsWith(TeluguSyllables.HALANT_CODE)) {
				syllables.add(new Syllable(code, unicodes));
			}
		}
		return syllables;
	}

}
