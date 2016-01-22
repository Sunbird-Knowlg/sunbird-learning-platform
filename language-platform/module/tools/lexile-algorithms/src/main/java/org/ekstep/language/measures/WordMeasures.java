package org.ekstep.language.measures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.measures.entity.Syllable;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.OrthographicVectors;
import org.ekstep.language.measures.meta.PhonologicVectors;
import org.ekstep.language.measures.meta.SyllableMap;

import com.ilimi.common.exception.ServerException;

public class WordMeasures {
    
    static {
        SyllableMap.loadSyllables("te");
        SyllableMap.loadSyllables("hi");
    }

    public static WordComplexity getWordComplexity(String language, String word) {
        if (!SyllableMap.isLanguageEnabled(language))
            throw new ServerException(LanguageErrorCodes.ERR_UNSUPPORTED_LANGUAGE.name(), "Language not supported");
        List<Syllable> syllables = getSyllables(language, word);
        WordComplexity wc = new WordComplexity();
        wc.setWord(word);
        wc.setCount(syllables.size());
        wc.setOrthoComplexity(0.0);
        wc.setPhonicComplexity(0.0);
        String notation = "";
        String unicode = "";
        Integer[] orthoVector = new Integer[OrthographicVectors.getVectorCount(language)];
        Integer[] phonicVector = new Integer[PhonologicVectors.getVectorCount(language)];
        for (Syllable s : syllables) {
            notation += s.getCode();
            for (String uc : s.getUnicodes()) {
                unicode += ("\\" + uc);
            }
            unicode += " ";
            getSyllableValues(language, s, orthoVector, phonicVector, wc);
        }
        wc.setUnicode(unicode.trim());
        wc.setNotation(notation);
        wc.setOrthoVec(orthoVector);
        wc.setPhonicVec(phonicVector);
        return wc;
    }

    private static void getSyllableValues(String language, Syllable s, Integer[] orthoVector, Integer[] phonicVector,
            WordComplexity wc) {
        List<String> unicodes = s.getUnicodes();
        Integer[] vec1 = null;
        Integer[] vec2 = null;
        Double[] orthoWeights = OrthographicVectors.getWeightage(language);
        Double[] phonicWeights = PhonologicVectors.getWeightage(language);
        Double orthoComplexity = 0.0;
        Double phonicComplexity = 0.0;
        Map<String, Double[]> orthoWeightMap = new HashMap<String, Double[]>();
        Map<String, Double[]> weights = new HashMap<String, Double[]>();
        for (String uc : unicodes) {
            Integer[] v1 = OrthographicVectors.getOrthographicVector(language, uc);
            orthoComplexity += VectorUtil.dotProduct(v1, orthoWeights);
            int orthoIncr = OrthographicVectors.getIncrement(language, uc);
            orthoComplexity += (orthoComplexity * orthoIncr / 100);
            vec1 = VectorUtil.addVector(vec1, v1);
            orthoWeightMap.put(uc, VectorUtil.dotMatrix(v1, orthoWeights, orthoIncr));

            Integer[] v2 = PhonologicVectors.getPhonologicVector(language, uc);
            phonicComplexity += VectorUtil.dotProduct(v2, phonicWeights);
            int phonicIncr = PhonologicVectors.getIncrement(language, uc);
            phonicComplexity += (phonicComplexity * phonicIncr / 100);
            vec2 = VectorUtil.addVector(vec2, v2);
            weights.put(uc, VectorUtil.dotMatrix(v2, phonicWeights, phonicIncr));
        }
        for (int i = 0; i < unicodes.size(); i++) {
            for (int j = i + 1; j < unicodes.size(); j++) {
                Double[] orthoDiffComplexity = VectorUtil.difference(orthoWeightMap.get(unicodes.get(i)),
                        weights.get(orthoWeightMap.get(j)));
                orthoComplexity += VectorUtil.sum(orthoDiffComplexity);

                Double[] diffComplexity = VectorUtil.difference(weights.get(unicodes.get(i)),
                        weights.get(unicodes.get(j)));
                phonicComplexity += VectorUtil.sum(diffComplexity);
            }
        }
        for (int i = 0; i < unicodes.size(); i++) {
            for (int j = i + 1; j < unicodes.size(); j++) {
                Double[] orthoDotComplexity = VectorUtil.dotProduct(orthoWeightMap.get(unicodes.get(i)),
                        orthoWeightMap.get(unicodes.get(j)));
                orthoComplexity += VectorUtil.sum(orthoDotComplexity);

                Double[] dotComplexity = VectorUtil.dotProduct(weights.get(unicodes.get(i)),
                        weights.get(unicodes.get(j)));
                phonicComplexity += VectorUtil.sum(dotComplexity);
            }
        }
        orthoVector = VectorUtil.addVector(orthoVector, vec1);
        phonicVector = VectorUtil.addVector(phonicVector, vec2);
        wc.setOrthoComplexity(wc.getOrthoComplexity() + orthoComplexity);
        wc.setPhonicComplexity(wc.getPhonicComplexity() + phonicComplexity);
    }

    private static List<Syllable> getSyllables(String language, String word) {
        List<Syllable> syllables = new ArrayList<Syllable>();
        if (null != word && word.trim().length() > 0) {
            String code = "";
            List<String> unicodes = new ArrayList<String>();
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                String uc = String.format("%04x", (int) ch);
                String s = SyllableMap.getSyllableType(language, uc);
                if (SyllableMap.CONSONANT_CODE.equalsIgnoreCase(s) || SyllableMap.VOWEL_CODE.equalsIgnoreCase(s)) {
                    if (code.endsWith(SyllableMap.CONSONANT_CODE) || code.endsWith(SyllableMap.VOWEL_CODE)) {
                        if (code.endsWith(SyllableMap.CONSONANT_CODE))
                            code += SyllableMap.VOWEL_SIGN_CODE;
                        unicodes.add(SyllableMap.getDefaultVowel(language));
                        syllables.add(new Syllable(code, unicodes));
                        code = "";
                        unicodes = new ArrayList<String>();
                    }
                    if (code.endsWith(SyllableMap.HALANT_CODE) && SyllableMap.CONSONANT_CODE.equalsIgnoreCase(s)) {
                        unicodes.add(uc + SyllableMap.MODIFIER_SUFFIX);
                        code += s;
                    } else {
                        unicodes.add(uc);
                        code += s;
                    }
                } else if (SyllableMap.VOWEL_SIGN_CODE.equalsIgnoreCase(s)) {
                    code += s;
                    unicodes.add(uc);
                    syllables.add(new Syllable(code, unicodes));
                    code = "";
                    unicodes = new ArrayList<String>();
                } else if (SyllableMap.HALANT_CODE.equalsIgnoreCase(s)) {
                    code += s;
                    unicodes.add(uc);
                } else if (SyllableMap.CLOSE_VOWEL_CODE.equalsIgnoreCase(s)) {
                    if (code.length() == 0 && syllables.size() > 0) {
                        Syllable syllable = syllables.get(syllables.size() - 1);
                        syllable.setInternalCode(syllable.getInternalCode() + s);
                        syllable.setCode(syllable.getCode() + SyllableMap.VOWEL_CODE);
                        syllable.getUnicodes().add(uc);
                    } else {
                        if (code.endsWith(SyllableMap.CONSONANT_CODE))
                            code += s;
                        unicodes.add(uc);
                        syllables.add(new Syllable(code, unicodes));
                        code = "";
                        unicodes = new ArrayList<String>();
                    }
                }
            }
            if (code.endsWith(SyllableMap.CONSONANT_CODE) || code.endsWith(SyllableMap.VOWEL_CODE)) {
                if (code.endsWith(SyllableMap.CONSONANT_CODE)) {
                    code += SyllableMap.VOWEL_SIGN_CODE;
                    unicodes.add(SyllableMap.getDefaultVowel(language));
                }
                syllables.add(new Syllable(code, unicodes));
            } else if (code.endsWith(SyllableMap.HALANT_CODE)) {
                syllables.add(new Syllable(code, unicodes));
            }
        }
        return syllables;
    }

}
