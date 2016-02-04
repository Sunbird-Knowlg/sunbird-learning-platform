package org.ekstep.language.actor;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import org.ekstep.language.model.WordInfoBean;
import org.ekstep.language.parser.SSFParser;
import org.ekstep.language.util.Constants;
import org.ekstep.language.util.ElasticSearchUtil;
import org.ekstep.language.util.WordUtil;

import akka.actor.ActorRef;

import com.ilimi.common.dto.Request;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;

public class IndexesActor extends LanguageBaseActor {

	private static Logger LOGGER = LogManager.getLogger(IndexesActor.class
			.getName());
	
	private int DEFAULT_LIMIT = 10000;

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
		if (msg instanceof Request) {
			Request request = (Request) msg;
			String languageId = (String) request.getContext().get(
					LanguageParams.language_id.name());
			String operation = request.getOperation();
			try {
				if (StringUtils.equalsIgnoreCase(
						LanguageOperations.loadCitations.name(), operation)) {
					String filePathOnServer = (String) request
							.get(LanguageParams.file_path.name());
					String sourceType = (String) request
							.get(LanguageParams.source_type.name());
					String grade = (String) request.get(LanguageParams.grade
							.name());
					String source = (String) request.get(LanguageParams.source
							.name());
					boolean skipCitations = request.get(LanguageParams.skipCitations
							.name()) != null ? (boolean) request.get(LanguageParams.skipCitations
							.name()):false;
					SSFParser.parseSsfFiles(filePathOnServer, sourceType,
							source, grade, skipCitations, languageId);
					OK(getSender());
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.citationsCount.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					List<String> groupByList = (List<String>) request
							.get(LanguageParams.groupBy.name());

					Map<String, Object> groupByWordMap = new HashMap<String, Object>();
					groupByWordMap.put("groupByParent",
							LanguageParams.word.name());
					groupByWordMap.put("groupByChildList", groupByList);

					List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
					groupByFinalList.add(groupByWordMap);

					getCitationsCount(words, languageId, groupByFinalList);
					OK(getSender());
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.citations.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					Object sourceType = request.get(LanguageParams.source_type
							.name()) != null ? request
							.get(LanguageParams.source_type.name()) : null;
					Object grade = request.get(LanguageParams.grade.name()) != null ? request
							.get(LanguageParams.grade.name()) : null;
					Object pos = request.get(LanguageParams.pos.name()) != null ? request
							.get(LanguageParams.pos.name()) : null;
					Object fileName = request.get(LanguageParams.file_name
							.name()) != null ? request.get(LanguageParams.pos
							.name()) : null;
					String fromDate = (String) (request
							.get(LanguageParams.from_date.name()) != null ? request
							.get(LanguageParams.from_date.name()) : null);
					String toDate = (String) (request
							.get(LanguageParams.to_date.name()) != null ? request
							.get(LanguageParams.to_date.name()) : null);
					
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);

					List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
					if (request.get(LanguageParams.groupBy.name()) != null) {
						List<String> groupByList = (List<String>) request
								.get(LanguageParams.groupBy.name());
						Map<String, Object> groupByWordMap = new HashMap<String, Object>();
						groupByWordMap.put("groupByParent",
								LanguageParams.word.name());
						groupByWordMap.put("groupByChildList", groupByList);
						groupByFinalList.add(groupByWordMap);
					}

					getCitations(words, groupByFinalList, sourceType, grade, pos, fileName,
							fromDate, toDate, languageId, limit);
					OK(getSender());
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.getRootWords.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
					getRootWords(words, languageId, limit);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.rootWordInfo.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : null);
					getRootWordInfo(words, languageId, limit);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.getWordId.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
					getWordIds(words, languageId, limit);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.getIndexInfo.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					List<String> groupByList = (List<String>) request
							.get(LanguageParams.groupBy.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
					Map<String, Object> groupByWordMap = new HashMap<String, Object>();
					groupByWordMap.put("groupByParent",
							LanguageParams.word.name());
					groupByWordMap.put("groupByChildList", groupByList);

					List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
					groupByFinalList.add(groupByWordMap);

					getIndexInfo(words, groupByFinalList, languageId, limit);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.addWordIndex.name(), operation)) {
					List<Map<String, String>> words = (List<Map<String, String>>) request
							.get(LanguageParams.words.name());
					addWordIndex(words, languageId);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.getWordMetrics.name(), operation)) {
					getWordMetrics(languageId);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.addCitationIndex.name(), operation)) {
					List<Map<String, String>> Citations = (List<Map<String, String>>) request
							.get(LanguageParams.citations.name());
					addCitations(Citations, languageId);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.wordWildCard.name(), operation)) {
					String wordWildCard = (String) request
							.get(LanguageParams.word.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
					wordWildCard(wordWildCard, languageId, limit);
				} else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.morphologicalVariants.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : null);
					getMorphologicalVariants(words, languageId, limit);
				}  else if (StringUtils.equalsIgnoreCase(
						LanguageOperations.wordInfo.name(), operation)) {
					List<String> words = (List<String>) request
							.get(LanguageParams.words.name());
					int limit = (int) (request.get(LanguageParams.limit.name()) != null ? request
							.get(LanguageParams.limit.name()) : null);
					getWordInfo(words, languageId, limit);
				} else {
					LOGGER.info("Unsupported operation: " + operation);
					unhandled(msg);
				}
			} catch (Exception e) {
				handleException(e, getSender());
			}
		} else {
			LOGGER.info("Unsupported operation!");
			unhandled(msg);
		}
	}

	@SuppressWarnings("unchecked")
	private void getMorphologicalVariants(List<String> words,
			String languageId, int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		ArrayList<String> rootWords = new ArrayList<String>();
		Map<String, Object> rootWordsMap = getRootWordsMap(words, languageId, limit, util);
		for(Map.Entry<String, Object> entry : rootWordsMap.entrySet()){
			Map<String, Object> wordMap = (Map<String, Object>) entry.getValue();
			String rootWord = (String) wordMap.get("rootWord");
			rootWords.add(rootWord);
		}
		Map<String, ArrayList<String>> variantsMap = getVariants(rootWords, util, languageId);
		OK(LanguageParams.morphological_variants.name(), variantsMap, getSender());
	}

	
	
	private Map<String, ArrayList<String>> getVariants(
			ArrayList<String> rootWords, ElasticSearchUtil util, String languageId) throws IOException {
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "rootWord";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, rootWords);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				searchCriteria, indexName, Constants.WORD_INDEX_TYPE);
		Map<String, ArrayList<String>> variantsMap = new HashMap<String, ArrayList<String>>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			String word = wordIndex.getWord();
			ArrayList<String> rootWordList = (ArrayList<String>) variantsMap.get(wordIndex.getRootWord());
			if(rootWordList == null){
				rootWordList =  new ArrayList<String>();
			}
			rootWordList.add(word);
			variantsMap.put(wordIndex.getRootWord(), rootWordList);
		}
		return variantsMap;
	}

	private void wordWildCard(String wordWildCard, String languageId, int limit) throws Exception {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		List<Object> words = util.wildCardSearch(WordIndexBean.class,
				textKeyWord,wordWildCard, indexName, Constants.WORD_INDEX_TYPE);
		OK(LanguageParams.words.name(), words, getSender());		
	}

	@SuppressWarnings("rawtypes")
	private void addCitations(List<Map<String, String>> citations,
			String languageId) throws Exception {
		WordUtil wordUtil = new WordUtil();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, List> indexes = new HashMap<String, List>();
		ArrayList<CitationBean> citationBeanList = new ArrayList<CitationBean>();
		for (Map<String, String> map : citations) {
			String jsonString = mapper.writeValueAsString(map);
			CitationBean citation = mapper.readValue(jsonString,
					CitationBean.class);
			citationBeanList.add(citation);
		}

		ArrayList<String> errorList = wordUtil
				.validateCitationsList(citationBeanList);
		if (!errorList.isEmpty()) {
			ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Required parameters are missing",
					ResponseCode.SERVER_ERROR, getSender());
		} else {
			indexes.put(Constants.CITATION_INDEX_COMMON_NAME, citationBeanList);
			wordUtil.addIndexesToElasticSearch(indexes,
					languageId);
			OK(getSender());
		}
	}

	private void getWordMetrics(String languageId) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
				+ languageId;
		String distinctKey = "rootWord";
		List<Map<String, Object>> groupByList = new ArrayList<Map<String, Object>>();
		Map<String, Object> sourceType = new HashMap<String, Object>();
		sourceType.put("groupBy", "sourceType");
		sourceType.put("distinctKey", distinctKey);
		groupByList.add(sourceType);
		Map<String, Object> source = new HashMap<String, Object>();
		source.put("groupBy", "source");
		source.put("distinctKey", distinctKey);
		groupByList.add(source);
		Map<String, Object> pos = new HashMap<String, Object>();
		pos.put("groupBy", "pos");
		pos.put("distinctKey", distinctKey);
		groupByList.add(pos);
		Map<String, Object> grade = new HashMap<String, Object>();
		grade.put("groupBy", "grade");
		grade.put("distinctKey", distinctKey);
		groupByList.add(grade);
		/*Map<String, Object> fileName = new HashMap<String, Object>();
		fileName.put("groupBy", "fileName");
		fileName.put("distinctKey", distinctKey);
		groupByList.add(fileName);
		Map<String, Object> date = new HashMap<String, Object>();
		date.put("groupBy", "date");
		date.put("distinctKey", distinctKey);
		groupByList.add(date);*/

		Map<String, Object> wordMetrics = (Map<String, Object>) util
				.getDistinctCountOfSearch(null, citationIndexName,
						Constants.CITATION_INDEX_TYPE, groupByList);
		OK(LanguageParams.word_metrics.name(), wordMetrics, getSender());

	}

	private void addWordIndex(List<Map<String, String>> words, String languageId)
			throws JsonGenerationException, JsonMappingException, Exception {
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
				+ languageId;
		ElasticSearchUtil util = new ElasticSearchUtil();
		WordUtil wordUtil = new WordUtil();
		Map<String, String> wordIndexes = new HashMap<String, String>();
		for (Map<String, String> wordMap : words) {
			String word = wordMap.get("word");
			String rootWord = wordMap.get("rootWord");
			if(rootWord == null){
				rootWord = word;
			}
			String id = wordMap.get("id");
			wordIndexes.put(word, wordUtil.getWordIndex(word, rootWord, id,
					new ObjectMapper()));
		}
		
		util.bulkIndexWithIndexId(wordIndexName,
				Constants.WORD_INDEX_TYPE, wordIndexes);
		OK(getSender());
	}

	private void getRootWords(List<String> words, String languageId, int limit)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		Map<String, Object> rootWordsMap = getRootWordsMap(words, languageId, limit, util);
		OK(LanguageParams.root_words.name(), rootWordsMap, getSender());
	}

	private Map<String, Object> getRootWordsMap(List<String> words, String languageId, int limit, ElasticSearchUtil util)
			throws IOException {
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				searchCriteria, indexName, Constants.WORD_INDEX_TYPE);
		Map<String, Object> rootWordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("rootWord", wordIndex.getRootWord());
			rootWordsMap.put(wordIndex.getWord(), wordMap);
		}
		return rootWordsMap;
	}
	
	private void getRootWordInfo(List<String> words, String languageId,
			int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INFO_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "rootWord";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordInfoIndexes = util.textSearch(WordInfoBean.class,
				searchCriteria, indexName, Constants.WORD_INFO_INDEX_TYPE);
		Map<String, ArrayList<WordInfoBean>> rootWordsMap = new HashMap<String, ArrayList<WordInfoBean>>();
		for (Object wordIndexTemp : wordInfoIndexes) {
			WordInfoBean wordInfo = (WordInfoBean) wordIndexTemp;
			ArrayList<WordInfoBean> rootWordList = (ArrayList<WordInfoBean>) rootWordsMap.get(wordInfo.getRootWord());
			if(rootWordList == null){
				rootWordList =  new ArrayList<WordInfoBean>();
			}
			rootWordList.add(wordInfo);
			rootWordsMap.put(wordInfo.getRootWord(), rootWordList);
		}
		OK(LanguageParams.root_word_info.name(), rootWordsMap, getSender());
		
	}
	
	private void getWordInfo(List<String> words, String languageId,
			int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INFO_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordInfoIndexes = util.textSearch(WordInfoBean.class,
				searchCriteria, indexName, Constants.WORD_INFO_INDEX_TYPE);
		Map<String, Object> wordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordInfoIndexes) {
			WordInfoBean wordInfo = (WordInfoBean) wordIndexTemp;
			wordsMap.put(wordInfo.getRootWord(), wordInfo);
		}
		OK(LanguageParams.word_info.name(), wordsMap, getSender());
		
	}
	
	private void getWordIds(List<String> words, String languageId, int limit)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				searchCriteria, indexName, Constants.WORD_INDEX_TYPE);
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
	private void getIndexInfo(List<String> words,
			List<Map<String, Object>> groupByFinalList, String languageId, int limit)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
				+ languageId;
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
				+ languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		// ************
		Map<String, Object> countMap = util.getCountOfSearch(null,
				searchCriteria, citationIndexName,
				Constants.CITATION_INDEX_TYPE, groupByFinalList);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				searchCriteria, wordIndexName, Constants.WORD_INDEX_TYPE);
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
			wordMap.put("citations", citationWordMap);
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		OK(LanguageParams.index_info.name(), wordIdsMap, getSender());
	}

	@SuppressWarnings("unchecked")
	private void getCitationsCount(List<String> words, String languageId,
			List<Map<String, Object>> groupByList) throws IOException {

		ElasticSearchUtil util = new ElasticSearchUtil();
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
				+ languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		Map<String, Object> wordMap = (Map<String, Object>) util
				.getCountOfSearch(CitationBean.class, searchCriteria,
						citationIndexName, Constants.CITATION_INDEX_TYPE,
						groupByList).get(LanguageParams.word.name());
		OK(LanguageParams.citation_count.name(), wordMap, getSender());
	}

	private void getCitations(List<String> words, List<Map<String, Object>> groupByList, Object sourceType,
			Object grade, Object pos, Object fileName, String fromDate,
			String toDate, String languageId, int limit) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		WordUtil wordUtil = new WordUtil();
		String indexName = Constants.CITATION_INDEX_COMMON_NAME + "_"
				+ languageId;
		String textKeyWord = "word";
		Map<String, Object> textFiltersMap = new HashMap<String, Object>();
		if (sourceType != null) {
			textFiltersMap.put("sourceType",
					wordUtil.getList(mapper, sourceType, null));
		}
		if (grade != null) {
			textFiltersMap.put("grade", wordUtil.getList(mapper, grade, null));
		}
		if (pos != null) {
			textFiltersMap.put("pos", wordUtil.getList(mapper, pos, null));
		}
		if (fileName != null) {
			textFiltersMap.put("fileName",
					wordUtil.getList(mapper, fileName, null));
		}

		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> citations = util.textFiltersSearch(CitationBean.class,
				searchCriteria, textFiltersMap, indexName,
				Constants.CITATION_INDEX_TYPE);
		Map<String, ArrayList<CitationBean>> citationsList = new HashMap<String, ArrayList<CitationBean>>();
		for(Object citationObj : citations){
			CitationBean citationBean = (CitationBean) citationObj;
			ArrayList<CitationBean> citationBeanList = citationsList.get(citationBean.getWord());
			if(citationBeanList == null){
				citationBeanList = new ArrayList<CitationBean>();
			}
			citationBeanList.add(citationBean);
			citationsList.put(citationBean.getWord(), citationBeanList);
		}
		
		OK(LanguageParams.citations.name(), citationsList, getSender());
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}