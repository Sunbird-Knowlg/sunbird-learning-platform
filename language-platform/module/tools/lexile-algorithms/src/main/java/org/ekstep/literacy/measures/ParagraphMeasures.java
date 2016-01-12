package org.ekstep.literacy.measures;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.ekstep.literacy.entity.ParagraphComplexity;
import org.ekstep.literacy.entity.WordComplexity;
import org.ekstep.literacy.meta.OrthographicVectors;
import org.ekstep.literacy.meta.PhonologicVectors;

public class ParagraphMeasures {

	public static void main(String[] args) {
		try {
			String path = "/Users/rayulu/work/EkStep/language_model/wordnets/wiktionary/meta/words";
			OrthographicVectors.load(path);
			PhonologicVectors.load(path);

			String text = "గువ్వకు జరమమ్మా రాతిరి బువ్వే తినలేదు ముక్కుకి ముక్కెర కావాలంటది ముక్కు చూపుతూ నడవాలంటది  గువ్వకు జరమమ్మా రాతిరి బువ్వే తినలేదు నడుముకు ఒడ్డాణం కావాలంటది నడుము తిప్పుతూ నడవాలంటది గువ్వకు జరమమ్మా రాతిరి బువ్వే తినలేదు కాళ్ళకి గజ్జెలు కావాలంటది కాళ్ళు తిప్పుతూ తిరగాలంటది గువ్వకు జరమమ్మా రాతిరి బువ్వే తినలేదు";
			ParagraphComplexity pc = getParagraphMeasures(text);
			if (null != pc) {
				System.out.println("Word Count: " + pc.getWordMeasures().size());
				System.out.println(
						"Mean Complexity: (" + pc.getMeanOrthoComplexity() + ", " + pc.getMeanPhonicComplexity() + ")");
				System.out.println("Total Complexity: (" + pc.getTotalOrthoComplexity() + ", "
						+ pc.getTotalPhonicComplexity() + ")");
				for (WordComplexity wc : pc.getWordMeasures()) {
					System.out.print(
							wc.getWord() + " (" + wc.getOrthoComplexity() + ", " + wc.getPhonicComplexity() + "), ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ParagraphComplexity getParagraphMeasures(String text) {
		if (null != text && text.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(text);
			List<WordComplexity> wordMeasures = new ArrayList<WordComplexity>();
			if (null != st) {
				while (st.hasMoreTokens()) {
					String word = st.nextToken().trim();
					WordComplexity wc = WordMeasures.getWordComplexity(word);
					wordMeasures.add(wc);
				}
			}
			ParagraphComplexity pc = new ParagraphComplexity();
			pc.setText(text);
			pc.setWordMeasures(wordMeasures);
			computeMeans(pc);
			return pc;
		} else {
			return null;
		}
	}

	private static void computeMeans(ParagraphComplexity pc) {
		int count = pc.getWordMeasures().size();
		double orthoComplexity = 0;
		double phonicComplexity = 0;
		for (WordComplexity wc : pc.getWordMeasures()) {
			orthoComplexity += wc.getOrthoComplexity();
			phonicComplexity += wc.getPhonicComplexity();
		}
		pc.setTotalOrthoComplexity(orthoComplexity);
		pc.setTotalPhonicComplexity(phonicComplexity);
		pc.setMeanOrthoComplexity(orthoComplexity / count);
		pc.setMeanPhonicComplexity(phonicComplexity / count);
	}
}
