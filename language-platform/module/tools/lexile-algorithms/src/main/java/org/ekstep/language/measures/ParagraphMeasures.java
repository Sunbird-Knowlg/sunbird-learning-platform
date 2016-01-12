package org.ekstep.language.measures;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;

public class ParagraphMeasures {

	public static ParagraphComplexity getTextComplexity(String language, String text) {
	    if (!SyllableMap.isLanguageEnabled(language))
            return null;
		if (null != text && text.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(text);
			List<WordComplexity> wordMeasures = new ArrayList<WordComplexity>();
			if (null != st) {
				while (st.hasMoreTokens()) {
					String word = st.nextToken().trim();
					WordComplexity wc = WordMeasures.getWordComplexity(language, word);
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
