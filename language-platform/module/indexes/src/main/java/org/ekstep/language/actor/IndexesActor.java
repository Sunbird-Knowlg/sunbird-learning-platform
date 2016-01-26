package org.ekstep.language.actor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordIndexBean;
import org.ekstep.language.parser.Constants;
import org.ekstep.language.parser.SSFParser;
import org.ekstep.language.util.ElasticSearchUtil;
import org.ekstep.language.util.WordUtil;

import akka.actor.ActorRef;

import com.ilimi.common.dto.Request;

public class IndexesActor extends LanguageBaseActor {

	private static Logger LOGGER = LogManager.getLogger(IndexesActor.class
			.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
		if (msg instanceof Request) {
			Request request = (Request) msg;
			String languageId = (String) request.getContext().get(
					LanguageParams.language_id.name());
			String operation = request.getOperation();
			if (StringUtils.equalsIgnoreCase(
					LanguageOperations.loadCitations.name(), operation)) {
				String filePathOnServer = (String) request
						.get(LanguageParams.file_path.name());
				String sourceType = (String) request
						.get(LanguageParams.source_type.name());
				String grade = (String) request
						.get(LanguageParams.grade.name());
				SSFParser.parseSsfFilesFolder(filePathOnServer, sourceType,
						grade, languageId);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(
					LanguageOperations.citationsCount.name(), operation)) {
				List<String> words = (List<String>) request
						.get(LanguageParams.words.name());
				List<Map<String, Object>> groupbyList = (List<Map<String, Object>>) request
						.get(LanguageParams.groupByList.name());
				String sourceType = request.get(LanguageParams.source_type
						.name()) != null ? (String) request
						.get(LanguageParams.source_type.name()) : null;
				String grade = request.get(LanguageParams.grade.name()) != null ? (String) request
						.get(LanguageParams.grade.name()) : null;
				String pos = request.get(LanguageParams.pos.name()) != null ? (String) request
						.get(LanguageParams.pos.name()) : null;
				String fromDate = request.get(LanguageParams.from_date.name()) != null ? (String) request
						.get(LanguageParams.from_date.name()) : null;
				String toDate = request.get(LanguageParams.to_date.name()) != null ? (String) request
						.get(LanguageParams.to_date.name()) : null;
				getCitationsCount(words, sourceType, grade, pos, fromDate,
						toDate, languageId, groupbyList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(
					LanguageOperations.getRootWords.name(), operation)) {
				List<String> words = (List<String>) request
						.get(LanguageParams.words.name());
				getRootWords(words, languageId);
			} else if (StringUtils.equalsIgnoreCase(
					LanguageOperations.getWordId.name(), operation)) {
				List<String> words = (List<String>) request
						.get(LanguageParams.words.name());
				getWordIds(words, languageId);
			} else if (StringUtils.equalsIgnoreCase(
					LanguageOperations.getIndexInfo.name(), operation)) {
				List<String> words = (List<String>) request
						.get(LanguageParams.words.name());
				getIndexInfo(words, languageId);
			} else if (StringUtils.equalsIgnoreCase(
					LanguageOperations.addWordIndex.name(), operation)) {
				List<Map<String, String>> words = (List<Map<String, String>>) request
						.get(LanguageParams.words.name());
				addWordIndex(words, languageId);
			} else {
				LOGGER.info("Unsupported operation: " + operation);
				unhandled(msg);
			}
		} else {
			LOGGER.info("Unsupported operation!");
			unhandled(msg);
		}
	}

	private void addWordIndex(List<Map<String, String>> words, String languageId)
			throws JsonGenerationException, JsonMappingException, IOException {
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
				+ languageId;
		ElasticSearchUtil util = new ElasticSearchUtil();
		WordUtil wordUtil = new WordUtil();
		ArrayList<String> wordIndexes = new ArrayList<String>();
		for (Map<String, String> wordMap : words) {
			String word = wordMap.get("word");
			String id = wordMap.get("id");
			wordIndexes.add(wordUtil.getWordIndex(word, word, id,
					new ObjectMapper()));
		}
		util.bulkIndexWithAutoGenerateIndexId(wordIndexName,
				Constants.WORD_INDEX_TYPE, wordIndexes);
	}

	private void getRootWords(List<String> words, String languageId)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> textFilters = new HashMap<String, Object>();
		textFilters.put(textKeyWord, words);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				textFilters, indexName, Constants.WORD_INDEX_TYPE);
		Map<String, Object> rootWordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("rootWord", wordIndex.getRootWord());
			rootWordsMap.put(wordIndex.getWord(), wordMap);
		}
		OK(LanguageParams.root_words.name(), rootWordsMap, getSender());
	}

	private void getWordIds(List<String> words, String languageId)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> textFilters = new HashMap<String, Object>();
		textFilters.put(textKeyWord, words);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				textFilters, indexName, Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		OK(LanguageParams.word_ids.name(), wordIdsMap, getSender());
	}

	@SuppressWarnings("unchecked")
	private void getIndexInfo(List<String> words, String languageId)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
				+ languageId;
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
				+ languageId;
		String textKeyWord = "word";
		Map<String, Object> textFilters = new HashMap<String, Object>();
		textFilters.put(textKeyWord, words);
		// ************
		Map<String, Object> groupByMap = new HashMap<String, Object>();
		groupByMap.put("groupByParent", "word");
		List<Map<String, Object>> groupByList = new ArrayList<Map<String, Object>>();
		groupByList.add(groupByMap);
		Map<String, Object> countMap = util.getCountOfSearch(null, textFilters,
				citationIndexName, Constants.CITATION_INDEX_TYPE, groupByList);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				textFilters, wordIndexName, Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();
		Map<String, Object> citiationCountsByWord = (Map<String, Object>) countMap
				.get("word");
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordMap.put("rootWord", wordIndex.getRootWord());
			Map<String, Object> citationWordMap = (Map<String, Object>) citiationCountsByWord
					.get(wordIndex.getWord());
			wordMap.put("citations", citationWordMap.get("count"));
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		OK(LanguageParams.index_info.name(), wordIdsMap, getSender());
	}

	private void getCitationsCount(List<String> words, String sourceType,
			String grade, String pos, String fromDate, String toDate,
			String languageId, List<Map<String, Object>> groupByList)
			throws IOException {

		ElasticSearchUtil util = new ElasticSearchUtil();
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
				+ languageId;
		String textKeyWord = "word";
		Map<String, Object> textFilters = new HashMap<String, Object>();
		textFilters.put(textKeyWord, words);
		if (groupByList == null) {
			OK(LanguageParams.citation_count.name(), util.textSearch(CitationBean.class, textFilters, citationIndexName,
					Constants.CITATION_INDEX_TYPE), getSender());
		} else {
			OK(LanguageParams.citation_count.name(), util.getCountOfSearch(CitationBean.class, textFilters, citationIndexName,
					Constants.CITATION_INDEX_TYPE, groupByList), getSender());
		}
		
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}