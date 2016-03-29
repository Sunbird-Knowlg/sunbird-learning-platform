package org.ekstep.language.measures;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;
import org.ekstep.language.util.LanguageUtil;

public class ParagraphMeasures {

    public static ParagraphComplexity getTextComplexity(String language, String text) {
        if (!SyllableMap.isLanguageEnabled(language))
            return null;
        if (StringUtils.isNotBlank(text)) {
            List<String> tokens = LanguageUtil.getTokens(text);
            Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();
            if (null != tokens && !tokens.isEmpty()) {
                for (String word : tokens) {
                    WordComplexity wc = WordMeasures.getWordComplexity(language, word);
                    wordMeasures.put(word, new ComplexityMeasures(wc.getOrthoComplexity(), wc.getPhonicComplexity()));
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
        Map<String, ComplexityMeasures> wordMeasures = pc.getWordMeasures();
        for (Entry<String, ComplexityMeasures> entry : wordMeasures.entrySet()) {
            orthoComplexity += entry.getValue().getOrthographic_complexity();
            phonicComplexity += entry.getValue().getPhonologic_complexity();
        }
        pc.setTotalOrthoComplexity(formatDoubleValue(orthoComplexity));
        pc.setTotalPhonicComplexity(formatDoubleValue(phonicComplexity));
        pc.setMeanOrthoComplexity(formatDoubleValue(orthoComplexity / count));
        pc.setMeanPhonicComplexity(formatDoubleValue(phonicComplexity / count));
    }

    private static Double formatDoubleValue(Double d) {
        if (null != d) {
            BigDecimal bd = new BigDecimal(d);
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();
        }
        return d;
    }
}
