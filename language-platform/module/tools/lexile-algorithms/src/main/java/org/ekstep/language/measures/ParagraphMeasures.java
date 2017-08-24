package org.ekstep.language.measures;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.cache.GradeComplexityCache;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.LanguageUtil;
import org.ekstep.language.util.WordUtil;
import org.ekstep.language.util.WordnetUtil;

import com.ilimi.graph.dac.model.Node;

// TODO: Auto-generated Javadoc
/**
 * The Class ParagraphMeasures.
 *
 * @author karthik
 */
public class ParagraphMeasures {

	/** The word util. */
	private static WordUtil wordUtil = new WordUtil();

	static {
		SyllableMap.loadSyllables("te");
		SyllableMap.loadSyllables("hi");
		SyllableMap.loadSyllables("ka");
	}

	/**
	 * Analyse texts.
	 *
	 * @param languageId
	 *            the language id
	 * @param texts
	 *            the texts
	 * @return the map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> analyseTexts(String languageId, Map<String, String> texts) {
		try {
			if (!SyllableMap.isLanguageEnabled(languageId) || null == texts || texts.isEmpty())
				return null;

			Map<String, Map<String, Object>> summaryMap = new HashMap<String, Map<String, Object>>();

			Map<String, String> posMap = new HashMap<String, String>();
			Map<String, String> rootWordMap = new HashMap<String, String>();
			Map<String, String> categoryMap = new HashMap<String, String>();
			Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
			Map<String, Integer> storyCountMap = new HashMap<String, Integer>();
			Map<String, ComplexityMeasures> complexityMap = new HashMap<String, ComplexityMeasures>();
			Map<String, Map> wordRowMap = new HashMap<String, Map>();

			for (Entry<String, String> textEntry : texts.entrySet()) {
				String filename = textEntry.getKey();
				String s = textEntry.getValue();
				List<String> words = LanguageUtil.getTokens(s);
				List<String> uniqueWords = words.stream().distinct().collect(Collectors.toList());
				List<Map<String, Object>> wordList = null;
				if (CollectionUtils.isNotEmpty(uniqueWords))
					wordList = wordUtil.indexSearch(languageId, uniqueWords);
				ParagraphComplexity pc = getTextComplexity(languageId, s, wordList);
				System.out.println(filename + ": " + pc.getMeanOrthoComplexity() + ", " + pc.getMeanPhonicComplexity());
				Map<String, Object> summary = new HashMap<String, Object>();
				summary.put("total_orthographic_complexity", pc.getTotalOrthoComplexity());
				summary.put("total_phonologic_complexity", pc.getTotalPhonicComplexity());
				summary.put("avg_orthographic_complexity", pc.getMeanOrthoComplexity());
				summary.put("avg_phonologic_complexity", pc.getMeanPhonicComplexity());
				summary.put("word_count", pc.getWordCount());
				summary.put("syllable_count", pc.getSyllableCount());
				summary.put("averageTotalComplexity", pc.getMeanComplexity());
				List<Map<String, String>> suitableGradeSummary = getSuitableGradeSummaryInfo(languageId,
						pc.getMeanComplexity());
				if (suitableGradeSummary != null)
					summary.put("gradeLevels", suitableGradeSummary);

				Map<String, Integer> wordFrequency = pc.getWordFrequency();
				Map<String, ComplexityMeasures> wordMeasures = pc.getWordMeasures();
				ComplexityMeasuresComparator comparator = new ComplexityMeasuresComparator(wordMeasures);
				Map<String, ComplexityMeasures> sortedMap = new TreeMap<String, ComplexityMeasures>(comparator);
				sortedMap.putAll(wordMeasures);
				complexityMap.putAll(wordMeasures);

				double totalWordComplexity = 0;
				Map<String, Map<String, Object>> wordDetailsMap = new HashMap<String, Map<String, Object>>();
				for (Entry<String, ComplexityMeasures> entry : sortedMap.entrySet()) {
					String pos = posMap.get(entry.getKey());
					String lemma = rootWordMap.get(entry.getKey());
					String category = categoryMap.get(entry.getKey());
					String thresholdLevel = null;
					double oc = entry.getValue().getOrthographic_complexity();
					double phc = entry.getValue().getPhonologic_complexity();
					Double wc = pc.getWordComplexityMap().get(entry.getKey());
					if (StringUtils.isBlank(lemma)) {
						Node node = getNode("variants", entry.getKey(), languageId);
						if (null == node)
							node = getNode("lemma", entry.getKey(), languageId);
						pos = getPOSValue(node, languageId);
						lemma = getLemmaValue(node, languageId);
						thresholdLevel = getThresholdLevelValue(node, languageId);
						category = getCategoryValue(node, languageId);
						wc = getWordComplexityValue(node, languageId);
						if (null == wc)
							wc = (double) 0;
						else
							wc = formatDoubleValue(wc);
						pc.getWordComplexityMap().put(entry.getKey(), wc);
						totalWordComplexity += wc;
						posMap.put(entry.getKey(), pos);
						categoryMap.put(entry.getKey(), category);
						rootWordMap.put(entry.getKey(), lemma);
					}
					Map<String, Object> wordDetailMap = new HashMap<String, Object>();
					wordDetailMap.put("word", entry.getKey());
					wordDetailMap.put("orthographic_complexity", oc + "");
					wordDetailMap.put("phonologic_complexity", phc + "");
					wordDetailMap.put("word_complexity", wc);
					wordDetailMap.put("syllableCount", pc.getSyllableCountMap().get(entry.getKey()) + "");
					wordDetailMap.put("pos", pos);
					wordDetailMap.put("category", category);
					wordDetailMap.put("thresholdLevel", thresholdLevel);
					wordDetailMap.put("total_complexity", oc + phc + ((null == wc) ? 0 : wc.doubleValue()));
					wordDetailsMap.put(entry.getKey(), wordDetailMap);

					Integer count = (null == frequencyMap.get(entry.getKey()) ? 0 : frequencyMap.get(entry.getKey()));
					count += (null == wordFrequency.get(entry.getKey()) ? 1 : wordFrequency.get(entry.getKey()));
					frequencyMap.put(entry.getKey(), count);

					Integer storyCount = (null == storyCountMap.get(entry.getKey()) ? 0
							: storyCountMap.get(entry.getKey()));
					storyCount += 1;
					storyCountMap.put(entry.getKey(), storyCount);
				}
				wordRowMap.put(filename, wordDetailsMap);
				summary.put("total_word_complexity", totalWordComplexity);
				double meanWordComplexity = formatDoubleValue(totalWordComplexity / sortedMap.size());
				summary.put("avg_word_complexity", meanWordComplexity);
				summaryMap.put(textEntry.getKey(), summary);
			}
			Map<String, Object> response = new HashMap<String, Object>();
			response.put("cutoff_complexity", 50);
			response.put("summary", summaryMap);
			response.put("texts", wordRowMap);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Analyse texts CSV.
	 *
	 * @param languageId
	 *            the language id
	 * @param texts
	 *            the texts
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> analyseTextsCSV(String languageId, Map<String, String> texts) {
		try {
			if (!SyllableMap.isLanguageEnabled(languageId) || null == texts || texts.isEmpty())
				return null;
			String[] headerRows = new String[] { "File Name", "Total Orthographic Complexity",
					"Avg. Syllable Orthographic Complexity", "Total Phonological Complexity",
					"Avg. Syllable Phonological Complexity", "Word Count", "Syllable Count" };
			String[] wordRowsHeader = new String[] { "Word", "PoS", "Orthographic Complexity",
					"Phonological Complexity", "Category", "Root Word" };
			String[] totalWordsHeader = new String[] { "Word", "Root Word", "Frequency", "Number of stories", "PoS",
					"Orthographic Complexity", "Phonological Complexity", "Category" };

			Map<String, String> posMap = new HashMap<String, String>();
			Map<String, String> rootWordMap = new HashMap<String, String>();
			Map<String, String> categoryMap = new HashMap<String, String>();
			Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
			Map<String, Integer> storyCountMap = new HashMap<String, Integer>();
			Map<String, ComplexityMeasures> complexityMap = new HashMap<String, ComplexityMeasures>();
			Map<String, List<String[]>> wordRowMap = new HashMap<String, List<String[]>>();
			List<String[]> rows = new ArrayList<String[]>();
			rows.add(headerRows);
			for (Entry<String, String> textEntry : texts.entrySet()) {
				String filename = textEntry.getKey();
				String s = textEntry.getValue();
				List<String> words = LanguageUtil.getTokens(s);
				List<String> uniqueWords = words.stream().distinct().collect(Collectors.toList());
				List<Map<String, Object>> wordList = null;
				if (CollectionUtils.isNotEmpty(uniqueWords))
					wordList = wordUtil.indexSearch(languageId, uniqueWords);
				ParagraphComplexity pc = getTextComplexity(languageId, s, wordList);
				System.out.println(filename + ": " + pc.getMeanOrthoComplexity() + ", " + pc.getMeanPhonicComplexity());
				String[] row = new String[headerRows.length];
				row[0] = filename;
				row[1] = pc.getTotalOrthoComplexity().toString();
				row[2] = pc.getMeanOrthoComplexity().toString();
				row[3] = pc.getTotalPhonicComplexity().toString();
				row[4] = pc.getMeanPhonicComplexity().toString();
				row[5] = "" + pc.getWordCount();
				row[6] = "" + pc.getSyllableCount();

				Map<String, Integer> wordFrequency = pc.getWordFrequency();
				Map<String, ComplexityMeasures> wordMeasures = pc.getWordMeasures();
				ComplexityMeasuresComparator comparator = new ComplexityMeasuresComparator(wordMeasures);
				Map<String, ComplexityMeasures> sortedMap = new TreeMap<String, ComplexityMeasures>(comparator);
				sortedMap.putAll(wordMeasures);
				complexityMap.putAll(wordMeasures);

				List<String[]> wordRows = new ArrayList<String[]>();
				wordRows.add(wordRowsHeader);
				for (Entry<String, ComplexityMeasures> entry : sortedMap.entrySet()) {
					String pos = posMap.get(entry.getKey());
					String lemma = rootWordMap.get(entry.getKey());
					String category = categoryMap.get(entry.getKey());
					if (StringUtils.isBlank(lemma)) {
						Node node = getNode("variants", entry.getKey(), languageId);
						if (null == node)
							node = getNode("lemma", entry.getKey(), languageId);
						pos = getPOSValue(node, languageId);
						lemma = getLemmaValue(node, languageId);
						category = getCategoryValue(node, languageId);
						posMap.put(entry.getKey(), pos);
						categoryMap.put(entry.getKey(), category);
						rootWordMap.put(entry.getKey(), lemma);
					}
					String[] wordRow = new String[wordRowsHeader.length];
					wordRow[0] = entry.getKey();
					wordRow[1] = pos;
					wordRow[2] = entry.getValue().getOrthographic_complexity() + "";
					wordRow[3] = entry.getValue().getPhonologic_complexity() + "";
					wordRow[4] = category;
					wordRow[5] = lemma;
					wordRows.add(wordRow);
					Integer count = (null == frequencyMap.get(entry.getKey()) ? 0 : frequencyMap.get(entry.getKey()));
					count += (null == wordFrequency.get(entry.getKey()) ? 1 : wordFrequency.get(entry.getKey()));
					frequencyMap.put(entry.getKey(), count);

					Integer storyCount = (null == storyCountMap.get(entry.getKey()) ? 0
							: storyCountMap.get(entry.getKey()));
					storyCount += 1;
					storyCountMap.put(entry.getKey(), storyCount);
				}
				wordRowMap.put(filename, wordRows);
				rows.add(row);
			}
			Map<String, Object> response = new HashMap<String, Object>();
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
			StringWriter sWriter = new StringWriter();
			CSVPrinter writer = new CSVPrinter(sWriter, csvFileFormat);
			writer.printRecords(rows);
			response.put("summary", sWriter.toString());
			writer.close();
			sWriter.close();

			Map<String, Object> wordResponse = new HashMap<String, Object>();
			for (Entry<String, List<String[]>> entry : wordRowMap.entrySet()) {
				sWriter = new StringWriter();
				CSVPrinter writer1 = new CSVPrinter(sWriter, csvFileFormat);
				writer1.printRecords(entry.getValue());
				wordResponse.put(entry.getKey(), sWriter.toString());
				writer1.close();
				sWriter.close();
			}
			response.put("groups", wordResponse);

			List<String[]> summaryRows = new ArrayList<String[]>();
			summaryRows.add(totalWordsHeader);
			ComplexityMeasuresComparator comparator = new ComplexityMeasuresComparator(complexityMap);
			Map<String, ComplexityMeasures> summaryMap = new TreeMap<String, ComplexityMeasures>(comparator);
			summaryMap.putAll(complexityMap);
			for (Entry<String, ComplexityMeasures> entry : summaryMap.entrySet()) {
				String[] row = new String[totalWordsHeader.length];
				row[0] = entry.getKey();
				row[1] = rootWordMap.get(entry.getKey());
				row[2] = (null == frequencyMap.get(entry.getKey()) ? "1" : "" + frequencyMap.get(entry.getKey()));
				row[3] = (null == storyCountMap.get(entry.getKey()) ? "1" : "" + storyCountMap.get(entry.getKey()));
				row[4] = posMap.get(entry.getKey());
				row[5] = entry.getValue().getOrthographic_complexity() + "";
				row[6] = entry.getValue().getPhonologic_complexity() + "";
				row[7] = categoryMap.get(entry.getKey());
				summaryRows.add(row);
			}
			sWriter = new StringWriter();
			CSVPrinter writer2 = new CSVPrinter(sWriter, csvFileFormat);
			writer2.printRecords(summaryRows);
			response.put("words", sWriter.toString());
			writer2.close();
			sWriter.close();

			return response;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the node.
	 *
	 * @param prop
	 *            the prop
	 * @param value
	 *            the value
	 * @param languageId
	 *            the language id
	 * @return the node
	 */
	private static Node getNode(String prop, String value, String languageId) {
		return wordUtil.searchWord(languageId, prop, value);
	}

	/**
	 * Gets the lemma value.
	 *
	 * @param node
	 *            the  node
	 * @param languageId
	 *            the language id
	 * @return the lemma value
	 */
	private static String getLemmaValue(Node node, String languageId) {
		String lemma = null;
		
		if (node != null && node.getMetadata() != null)
			lemma = (String) node.getMetadata().get(IWordnetConstants.ATTRIB_LEMMA);

		
		return lemma;
	}

	/**
	 * Gets the threshold level value.
	 *
	 * @param node
	 *            the node
	 * @param languageId
	 *            the language id
	 * @return the threshold level value
	 */
	private static String getThresholdLevelValue(Node node, String languageId) {
		String level = null;
		
		if (node != null && node.getMetadata() != null)
			level = (String) node.getMetadata().get(IWordnetConstants.ATTRIB_THRESHOLD_LEVEL);

		return level;
	}

	/**
	 * Gets the category value.
	 *
	 * @param node
	 *            the  node
	 * @param languageId
	 *            the language id
	 * @return the category value
	 */
	private static String getCategoryValue(Node node, String languageId) {
		String category = null;

		if (node != null && node.getMetadata() != null)
			category = (String) node.getMetadata().get(IWordnetConstants.ATTRIB_CATEGORY);

		return category;
	}

	/**
	 * Gets the word complexity value.
	 *
	 * @param node
	 *            the  node
	 * @param languageId
	 *            the language id
	 * @return the word complexity value
	 */
	private static Double getWordComplexityValue(Node node, String languageId) {

		Double value = null;
		if (node != null && node.getMetadata() != null)
			value = (Double) node.getMetadata().get(IWordnetConstants.ATTRIB_WORD_COMPLEXITY);

		return value;
	}

	/**
	 * Gets the POS value.
	 *
	 * @param node
	 *            the  node
	 * @param languageId
	 *            the language id
	 * @return the POS value
	 */
	private static String getPOSValue(Node node, String languageId) {
		String posValue = null;
		if (node != null && node.getMetadata() != null) {
			String[] pos = (String[]) node.getMetadata().get(IWordnetConstants.ATTRIB_POS);
			if (null != pos && pos.length > 0) {
				for (String str : pos) {
					if (WordnetUtil.isStandardPOS(str)) {
						posValue = str.trim().toLowerCase();
						break;
					}
				}
				if (StringUtils.isBlank(posValue))
					posValue = WordnetUtil.getPosValue(pos[0]);
			}
		}
		return posValue;
	}

	/**
	 * Gets the text complexity.
	 *
	 * @param language
	 *            the language
	 * @param text
	 *            the text
	 * @param wordList
	 *            the word list
	 * @return the text complexity
	 */
	public static ParagraphComplexity getTextComplexity(String language, String text,
			List<Map<String, Object>> wordList) {
		if (!SyllableMap.isLanguageEnabled(language))
			return null;
		if (StringUtils.isNotBlank(text)) {
			List<String> tokens = LanguageUtil.getTokens(text);
			Map<String, Integer> syllableCountMap = new HashMap<String, Integer>();
			Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
			Map<String, WordComplexity> wordComplexities = new HashMap<String, WordComplexity>();
			Map<String, Double> wcMap = new HashMap<String, Double>();
			Map<String, Map<String, Object>> wordDictonary = null;
			if (wordList != null)
				wordDictonary = wordList.stream()
						.collect(Collectors.toMap(s -> (String) s.get(LanguageParams.lemma.name()), s -> s));
			if (null != tokens && !tokens.isEmpty()) {
				for (String word : tokens) {
					WordComplexity wc = WordMeasures.getWordComplexity(language, word);
					wordComplexities.put(word, wc);
					Integer count = null == wordFrequency.get(word) ? 0 : wordFrequency.get(word);
					count += 1;
					wordFrequency.put(word, count);
					syllableCountMap.put(word, wc.getCount());
					if (wordDictonary != null) {
						Map<String, Object> wordNodeMap = wordDictonary.get(word);
						Double wordComplexity = null;
						if (wordNodeMap != null)
							wordComplexity = (Double) wordNodeMap.get(LanguageParams.word_complexity.name());
						wcMap.put(word, wordComplexity);
					}
				}
			}
			ParagraphComplexity pc = new ParagraphComplexity();
			pc.setText(text);
			pc.setWordFrequency(wordFrequency);
			pc.setWordComplexityMap(wcMap);
			pc.setSyllableCountMap(syllableCountMap);
			computeMeans(pc, wordComplexities, wcMap);
			return pc;
		} else {
			return null;
		}
	}

	/**
	 * Gets the suitable grade summary info.
	 *
	 * @param languageId
	 *            the language id
	 * @param value
	 *            the value
	 * @return the suitable grade summary info
	 */
	public static List<Map<String, String>> getSuitableGradeSummaryInfo(String languageId, Double value) {
		List<com.ilimi.graph.dac.model.Node> suitableGrade = GradeComplexityCache.getInstance()
				.getSuitableGrades(languageId, value);
		List<Map<String, String>> suitableGradeSummary = new ArrayList<Map<String, String>>();
		if (suitableGrade != null) {
			for (com.ilimi.graph.dac.model.Node sg : suitableGrade) {
				Map<String, String> gradeInfo = new HashMap<>();
				gradeInfo.put("grade", (String) sg.getMetadata().get("gradeLevel"));
				gradeInfo.put("languageLevel", (String) sg.getMetadata().get("languageLevel"));
				suitableGradeSummary.add(gradeInfo);
			}
			return suitableGradeSummary;
		}
		return null;
	}

	/**
	 * Compute means.
	 *
	 * @param pc
	 *            the pc
	 * @param wordComplexities
	 *            the word complexities
	 * @param wcMap
	 *            the wc map
	 */
	private static void computeMeans(ParagraphComplexity pc, Map<String, WordComplexity> wordComplexities,
			Map<String, Double> wcMap) {
		int count = 0;
		double orthoComplexity = 0;
		double phonicComplexity = 0;
		double wordComplexity = 0;
		Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();
		for (Entry<String, WordComplexity> entry : wordComplexities.entrySet()) {
			WordComplexity wc = entry.getValue();
			orthoComplexity += wc.getOrthoComplexity();
			phonicComplexity += wc.getPhonicComplexity();
			double wcValue = (null == wcMap.get(entry.getKey()) ? 0 : wcMap.get(entry.getKey()).doubleValue());
			wordComplexity += wcValue;
			count += wc.getCount();
			wordMeasures.put(entry.getKey(), new ComplexityMeasures(wc.getOrthoComplexity(), wc.getPhonicComplexity()));
		}
		pc.setWordCount(wordComplexities.size());
		pc.setSyllableCount(count);
		pc.setWordMeasures(wordMeasures);
		pc.setWordComplexityMap(wcMap);
		pc.setTotalOrthoComplexity(formatDoubleValue(orthoComplexity));
		pc.setTotalPhonicComplexity(formatDoubleValue(phonicComplexity));
		pc.setTotalWordComplexity(formatDoubleValue(wordComplexity));
		pc.setMeanOrthoComplexity(formatDoubleValue(orthoComplexity / count));
		pc.setMeanPhonicComplexity(formatDoubleValue(phonicComplexity / count));
		pc.setMeanWordComplexity(formatDoubleValue(wordComplexity / count));
		double totalComplexity = orthoComplexity + phonicComplexity;
		pc.setMeanComplexity(formatDoubleValue(totalComplexity / pc.getWordCount()));
	}

	/**
	 * get text complexity with metrics .
	 *
	 * @param languageId
	 *            the language id
	 * @param text
	 *            the text
	 * @param mapper
	 *            the mapper
	 * @return the themes
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> getTextcomplexityWithMetrics(String languageId, String text,
			ObjectMapper mapper) {

		List<String> words = LanguageUtil.getTokens(text);
		List<String> uniqueWords = words.stream().distinct().collect(Collectors.toList());
		List<Map<String, Object>> wordList = null;
		if (CollectionUtils.isNotEmpty(uniqueWords))
			wordList = wordUtil.indexSearch(languageId, uniqueWords);

		ParagraphComplexity pc = getTextComplexity(languageId, text, wordList);

		Map<String, Object> result = new HashMap<>();

		if (pc != null) {
			result = mapper.convertValue(pc, Map.class);
			List<Map<String, String>> suitableGradeSummary = ParagraphMeasures.getSuitableGradeSummaryInfo(languageId,
					pc.getMeanComplexity());
			if (suitableGradeSummary != null)
				result.put("gradeLevels", suitableGradeSummary);
		}
		
		result.put("totalWordCount", words.size());
		result.put("wordCount", uniqueWords.size());
		
		if (wordList != null) {
			List<String> nonThresholdVocWords = new ArrayList<String>();
			Map<String, String> wordPosMap = new HashMap<>();
			updateThemes(result, wordList);
			updatePOSMetrics(result, wordList, mapper, wordPosMap);
			updateThresholdVocabularyMetrics(result, wordList, nonThresholdVocWords);
			updateTopFive(result, words, wordPosMap, nonThresholdVocWords);
		}

		return result;
	}

	/**
	 * Update top five.
	 *
	 * @param result
	 *            the result
	 * @param words
	 *            the words
	 * @param wordPosMap
	 *            the word pos map
	 * @param nonThresholdVocWords
	 *            the non threshold voc words
	 */
	private static void updateTopFive(Map<String, Object> result, List<String> words, Map<String, String> wordPosMap,
			List<String> nonThresholdVocWords) {

		Map<String, List<String>> wordsGroupedByPos = wordPosMap.entrySet().stream().collect(
				Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

		Map<String, Long> wordCountMap = words.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

		Map<String, Long> wordCountSortedMap = wordCountMap.entrySet().stream()
				.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		Map<String, Object> top5 = new HashMap<>();
		
		for(Entry<String, List<String>> posEntry : wordsGroupedByPos.entrySet()){
			//top5.put("noun", getTopFiveOf(wordCountSortedMap, wordsGroupedByPos.get("noun")));
			top5.put(posEntry.getKey(), getTopFiveOf(wordCountSortedMap, posEntry.getValue()));
		}
		top5.put("non-thresholdVocabulary", getTopFiveOf(wordCountSortedMap, nonThresholdVocWords));

		result.put("top5", top5);
	}

	/**
	 * Gets the top five of.
	 *
	 * @param wordCountSortedMap
	 *            the word count sorted map
	 * @param matchWords
	 *            the match words
	 * @return the top five of
	 */
	private static List<String> getTopFiveOf(Map<String, Long> wordCountSortedMap, List<String> matchWords) {
		int count = 0;
		List<String> words = new ArrayList<String>();
		if (matchWords != null && matchWords.size() > 0)
			for (Entry<String, Long> wordEntry : wordCountSortedMap.entrySet()) {
				String word = wordEntry.getKey();
				if (matchWords.contains(word)) {
					words.add(word);
					count++;
					if (count == 5) {
						break;
					}
				}
			}

		return words;

	}

	/**
	 * Update threshold vocabulary metrics.
	 *
	 * @param result
	 *            the result
	 * @param wordList
	 *            the word list
	 * @param nonThresholdVocWords
	 *            the non threshold voc words
	 */
	private static void updateThresholdVocabularyMetrics(Map<String, Object> result, List<Map<String, Object>> wordList,
			List<String> nonThresholdVocWords) {

		Integer thresholdVocWordCount = 0;
		Integer nonthresholdVocWordCount = 0;

		if (wordList != null) {
			for (Map<String, Object> word : wordList) {
				Object thresholdLevel = word.get(LanguageParams.thresholdLevel.name());
				Object grade = word.get(LanguageParams.grade.name());

				if (grade != null || thresholdLevel != null) {
					thresholdVocWordCount++;
				} else {
					nonthresholdVocWordCount++;
					nonThresholdVocWords.add(word.get(LanguageParams.lemma.name()).toString());
				}
			}

			result.put("thresholdVocabulary", getThresholdVocMap(thresholdVocWordCount, wordList.size()));
			result.put("nonThresholdVocabulary", getThresholdVocMap(nonthresholdVocWordCount, wordList.size()));
		}

	}

	/**
	 * Gets the threshold voc map.
	 *
	 * @param count
	 *            the count
	 * @param wordListSize
	 *            the word list size
	 * @return the threshold voc map
	 */
	private static Map<String, Object> getThresholdVocMap(int count, int wordListSize) {
		Double thresholdPercentage = 0.0;
		thresholdPercentage += count;
		thresholdPercentage = (thresholdPercentage / wordListSize) * 100;
		Map<String, Object> thresholdVocMap = new HashMap<>();
		thresholdVocMap.put("wordCount", count);
		thresholdVocMap.put("%OfWords", formatDoubleValue(thresholdPercentage));
		return thresholdVocMap;
	}

	/**
	 * Update POS metrics.
	 *
	 * @param result
	 *            the result
	 * @param wordList
	 *            the word list
	 * @param mapper
	 *            the mapper
	 * @param wordPosMap
	 *            the word pos map
	 */
	private static void updatePOSMetrics(Map<String, Object> result, List<Map<String, Object>> wordList,
			ObjectMapper mapper, Map<String, String> wordPosMap) {

		Map<String, Integer> posMetrics = new HashMap<>();

		if (wordList != null) {
			for (Map<String, Object> word : wordList) {
				Object pos = word.get(LanguageParams.pos.name());
				List<String> posList = wordUtil.getList(mapper, pos, null);
				if (CollectionUtils.isNotEmpty(posList)) {
					posMetrics.merge(posList.get(0), 1, (v, vv) -> ++v);
					wordPosMap.put(word.get(LanguageParams.lemma.name()).toString(), posList.get(0));
				}
			}
		}

		if (posMetrics != null && posMetrics.size() > 0) {
			Map<String, Double> actualPOSMetric = new HashMap<>();
			for (Entry<String, Integer> posEntry : posMetrics.entrySet()) {
				Double percentage = 0.0;
				percentage += posEntry.getValue();
				percentage = (percentage / wordList.size()) * 100;
				actualPOSMetric.put(posEntry.getKey(), formatDoubleValue(percentage));
			}
			result.put("partsOfSpeech", actualPOSMetric);
		}
	}

	/**
	 * update the themes.
	 *
	 * @param result
	 *            result
	 * @param wordList
	 *            the words
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void updateThemes(Map<String, Object> result, List<Map<String, Object>> wordList) {

		Map<String, Integer> themes = new HashMap<>();

		if (wordList != null) {
			for (Map<String, Object> word : wordList) {
				List<String> tags = (List<String>) word.get(LanguageParams.tags.name());
				for (String tag : tags) {
					themes.merge(tag, 1, (v, vv) -> ++v);
				}
			}
		}

		if (themes != null && themes.size() > 0)
			result.put("themes", themes);
	}

	/**
	 * Format double value.
	 *
	 * @param d
	 *            the d
	 * @return the double
	 */
	private static Double formatDoubleValue(Double d) {
		if (null != d) {
			try {
				BigDecimal bd = new BigDecimal(d);
				bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
				return bd.doubleValue();
			} catch (Exception e) {
			}
		}
		return d;
	}
}

@SuppressWarnings("rawtypes")
class ComplexityMeasuresComparator implements Comparator {
	private Map map;

	public ComplexityMeasuresComparator(Map map) {
		this.map = map;
	}

	@Override
	public int compare(Object o1, Object o2) {
		ComplexityMeasures c1 = (ComplexityMeasures) map.get(o1);
		ComplexityMeasures c2 = (ComplexityMeasures) map.get(o2);
		Double complexity1 = c1.getOrthographic_complexity() + c1.getPhonologic_complexity();
		Double complexity2 = c2.getOrthographic_complexity() + c2.getPhonologic_complexity();
		return complexity2.compareTo(complexity1);
	}

}
