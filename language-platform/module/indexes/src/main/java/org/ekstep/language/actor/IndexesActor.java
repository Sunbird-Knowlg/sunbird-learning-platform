package org.ekstep.language.actor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.common.util.UnzipUtility;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordIndexBean;
import org.ekstep.language.model.WordInfoBean;
import org.ekstep.language.parser.SSFParser;
import org.ekstep.language.util.Constants;
import org.ekstep.language.util.WordUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

import org.ekstep.common.enums.TaxonomyErrorCodes;

import akka.actor.ActorRef;

/**
 * The Class IndexesActor is an AKKA actor that processes all requests from
 * IndexesController to provide operations on Word Index, Word Info Index and
 * Citation indexes in Elasticsearch.
 * 
 * @author Amarnath
 */
public class IndexesActor extends LanguageBaseActor {

	/** The logger. */
	

	/** The default limit. */
	private int DEFAULT_LIMIT = 10000;

	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/temp/";

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		PlatformLogger.log("Received Command: " , msg);
		Request request = (Request) msg;
		String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LanguageOperations.loadCitations.name(), operation)) {
				InputStream zipStream = (InputStream) request.get(LanguageParams.input_stream.name());
				if (null == zipStream)
					throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(),
							"Input Zip object is emtpy");

				UnzipUtility unzipper = new UnzipUtility();
				String filePathOnServer = tempFileLocation + System.currentTimeMillis() + "_temp";
				try {
					unzipper.unzip(zipStream, filePathOnServer);
					String sourceType = (String) request.get(LanguageParams.source_type.name());
					String grade = (String) request.get(LanguageParams.grade.name());
					String source = (String) request.get(LanguageParams.source.name());
					boolean skipCitations = request.get(LanguageParams.skipCitations.name()) != null
							? (boolean) request.get(LanguageParams.skipCitations.name()) : false;
					dropIndex(languageId);
					SSFParser.parseSsfFiles(filePathOnServer, sourceType, source, grade, skipCitations, languageId);
					OK(getSender());
				} catch (Exception ex) {
					throw ex;
				} finally {
					File zipFileDirectory = new File(filePathOnServer);
					if (!zipFileDirectory.exists()) {
						System.out.println("Directory does not exist.");
					} else {
						try {
							delete(zipFileDirectory);
						} catch (IOException e) {
							PlatformLogger.log("Exception",e.getMessage(), e);
							e.printStackTrace();
						}
					}
				}

			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.citationsCount.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				List<String> groupByList = (List<String>) request.get(LanguageParams.groupBy.name());

				Map<String, Object> groupByWordMap = new HashMap<String, Object>();
				groupByWordMap.put("groupByParent", LanguageParams.word.name());
				groupByWordMap.put("groupByChildList", groupByList);

				List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
				groupByFinalList.add(groupByWordMap);

				getCitationsCount(words, languageId, groupByFinalList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.citations.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				Object sourceType = request.get(LanguageParams.source_type.name()) != null
						? request.get(LanguageParams.source_type.name()) : null;
				Object source = request.get(LanguageParams.source.name()) != null
						? request.get(LanguageParams.source.name()) : null;
				Object grade = request.get(LanguageParams.grade.name()) != null
						? request.get(LanguageParams.grade.name()) : null;
				Object pos = request.get(LanguageParams.pos.name()) != null ? request.get(LanguageParams.pos.name())
						: null;
				Object fileName = request.get(LanguageParams.file_name.name()) != null
						? request.get(LanguageParams.file_name.name()) : null;

				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);

				getCitations(words, sourceType, source, grade, pos, fileName, null, null, languageId, limit);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getRootWords.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				getRootWords(words, languageId, limit);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.rootWordInfo.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				getRootWordInfo(words, languageId, limit);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getWordId.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				getWordIds(words, languageId, limit);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getIndexInfo.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				List<String> groupByList = (List<String>) request.get(LanguageParams.groupBy.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				Map<String, Object> groupByWordMap = new HashMap<String, Object>();
				groupByWordMap.put("groupByParent", LanguageParams.word.name());
				groupByWordMap.put("groupByChildList", groupByList);

				List<Map<String, Object>> groupByFinalList = new ArrayList<Map<String, Object>>();
				groupByFinalList.add(groupByWordMap);

				getIndexInfo(words, groupByFinalList, languageId, limit);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.addWordIndex.name(), operation)) {
				List<Map<String, String>> words = (List<Map<String, String>>) request.get(LanguageParams.words.name());
				addWordIndex(words, languageId);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getWordMetrics.name(), operation)) {
				getWordMetrics(languageId);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.addCitationIndex.name(), operation)) {
				List<Map<String, String>> Citations = (List<Map<String, String>>) request
						.get(LanguageParams.citations.name());
				addCitations(Citations, languageId);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.wordWildCard.name(), operation)) {
				String wordWildCard = (String) request.get(LanguageParams.word.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				wordWildCard(wordWildCard, languageId, limit);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.morphologicalVariants.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				getMorphologicalVariants(words, languageId, limit);
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.wordInfo.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				int limit = (int) (request.get(LanguageParams.limit.name()) != null
						? request.get(LanguageParams.limit.name()) : DEFAULT_LIMIT);
				getWordInfo(words, languageId, limit);
			} else {
				PlatformLogger.log("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			PlatformLogger.log("List | Exception: " , e.getMessage(), e);
			handleException(e, getSender());
		}
	}

	/**
	 * Drop index.
	 *
	 * @param languageId
	 *            the language id
	 */
	private void dropIndex(String languageId) {
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_" + languageId;
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String wordInfoIndexName = Constants.WORD_INFO_INDEX_COMMON_NAME + "_" + languageId;

		try {
			ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
			if (elasticSearchUtil.isIndexExists(citationIndexName))
				elasticSearchUtil.deleteIndex(citationIndexName);
			if (elasticSearchUtil.isIndexExists(wordIndexName))
				elasticSearchUtil.deleteIndex(wordIndexName);
			if (elasticSearchUtil.isIndexExists(wordInfoIndexName))
				elasticSearchUtil.deleteIndex(wordInfoIndexName);
		} catch (Exception e) {
			PlatformLogger.log("IndexesActor, dropIndex | Exception: " , e.getMessage(), e);
		}

	}

	/**
	 * Gets the root words of the given words and Gets the morphological
	 * variants of a given words.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the morphological variants
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getMorphologicalVariants(List<String> words, String languageId, int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		ArrayList<String> rootWords = new ArrayList<String>();
		Map<String, Object> rootWordsMap = getRootWordsMap(words, languageId, limit, util);
		for (Map.Entry<String, Object> entry : rootWordsMap.entrySet()) {
			Map<String, Object> wordMap = (Map<String, Object>) entry.getValue();
			String rootWord = (String) wordMap.get("rootWord");
			rootWords.add(rootWord);
		}
		Map<String, ArrayList<String>> variantsMap = getVariants(rootWords, util, languageId);
		OK(LanguageParams.morphological_variants.name(), variantsMap, getSender());
	}

	/**
	 * Gets the morphological variants of words from its root words.
	 *
	 * @param rootWords
	 *            the root words
	 * @param util
	 *            the util
	 * @param languageId
	 *            the language id
	 * @return the variants
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Map<String, ArrayList<String>> getVariants(ArrayList<String> rootWords, ElasticSearchUtil util,
			String languageId) throws IOException {
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "rootWord";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, rootWords);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, indexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, ArrayList<String>> variantsMap = new HashMap<String, ArrayList<String>>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			String word = wordIndex.getWord();
			ArrayList<String> rootWordList = (ArrayList<String>) variantsMap.get(wordIndex.getRootWord());
			if (rootWordList == null) {
				rootWordList = new ArrayList<String>();
			}
			rootWordList.add(word);
			variantsMap.put(wordIndex.getRootWord(), rootWordList);
		}
		return variantsMap;
	}

	/**
	 * Performs a wild card search for a word on ES.
	 *
	 * @param wordWildCard
	 *            the word wild card
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @throws Exception
	 *             the exception
	 */
	private void wordWildCard(String wordWildCard, String languageId, int limit) throws Exception {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		List<Object> words = util.wildCardSearch(WordIndexBean.class, textKeyWord, wordWildCard, indexName,
				Constants.WORD_INDEX_TYPE);
		OK(LanguageParams.words.name(), words, getSender());
	}

	/**
	 * Adds the citations request to ES.
	 *
	 * @param citations
	 *            the citations
	 * @param languageId
	 *            the language id
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("rawtypes")
	private void addCitations(List<Map<String, String>> citations, String languageId) throws Exception {
		WordUtil wordUtil = new WordUtil();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, List> indexes = new HashMap<String, List>();
		ArrayList<CitationBean> citationBeanList = new ArrayList<CitationBean>();
		for (Map<String, String> map : citations) {
			String jsonString = mapper.writeValueAsString(map);
			CitationBean citation = mapper.readValue(jsonString, CitationBean.class);
			citationBeanList.add(citation);
		}

		ArrayList<String> errorList = wordUtil.validateCitationsList(citationBeanList);
		if (!errorList.isEmpty()) {
			ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "Required parameters are missing", ResponseCode.SERVER_ERROR,
					getSender());
		} else {
			indexes.put(Constants.CITATION_INDEX_COMMON_NAME, citationBeanList);
			wordUtil.addIndexesToElasticSearch(indexes, languageId);
			OK(getSender());
		}
	}

	/**
	 * Gets the word metrics from ES grouped by facets.
	 *
	 * @param languageId
	 *            the language id
	 * @return the word metrics
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void getWordMetrics(String languageId) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_" + languageId;
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

		Map<String, Object> wordMetrics = (Map<String, Object>) util.getDistinctCountOfSearch(null, citationIndexName,
				Constants.CITATION_INDEX_TYPE, groupByList);
		OK(LanguageParams.word_metrics.name(), wordMetrics, getSender());

	}

	/**
	 * Adds the word indexes to ES.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @throws JsonGenerationException
	 *             the json generation exception
	 * @throws JsonMappingException
	 *             the json mapping exception
	 * @throws Exception
	 *             the exception
	 */
	private void addWordIndex(List<Map<String, String>> words, String languageId)
			throws JsonGenerationException, JsonMappingException, Exception {
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		ElasticSearchUtil util = new ElasticSearchUtil();
		WordUtil wordUtil = new WordUtil();
		Map<String, String> wordIndexes = new HashMap<String, String>();
		for (Map<String, String> wordMap : words) {
			String word = wordMap.get("word");
			String rootWord = wordMap.get("rootWord");
			if (rootWord == null) {
				rootWord = word;
			}
			String id = wordMap.get("id");
			wordIndexes.put(word, wordUtil.getWordIndex(word, rootWord, id, new ObjectMapper()));
		}

		util.bulkIndexWithIndexId(wordIndexName, Constants.WORD_INDEX_TYPE, wordIndexes);
		OK(getSender());
	}

	/**
	 * Gets the root words for a given list of words.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the root words
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void getRootWords(List<String> words, String languageId, int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		Map<String, Object> rootWordsMap = getRootWordsMap(words, languageId, limit, util);
		OK(LanguageParams.root_words.name(), rootWordsMap, getSender());
	}

	/**
	 * Gets the root words as a map for a given list of words.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @param util
	 *            the util
	 * @return the root words map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Map<String, Object> getRootWordsMap(List<String> words, String languageId, int limit,
			ElasticSearchUtil util) throws IOException {
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, indexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, Object> rootWordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("rootWord", wordIndex.getRootWord());
			rootWordsMap.put(wordIndex.getWord(), wordMap);
		}
		return rootWordsMap;
	}

	/**
	 * Gets the root word info for the given list of words.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the root word info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getRootWordInfo(List<String> words, String languageId, int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INFO_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "rootWord";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordInfoIndexes = util.textSearch(WordInfoBean.class, searchCriteria, indexName,
				Constants.WORD_INFO_INDEX_TYPE);
		Map<String, ArrayList<Map<String, Object>>> rootWordsMap = new HashMap<String, ArrayList<Map<String, Object>>>();
		for (Object wordIndexTemp : wordInfoIndexes) {
			Map<String, Object> wordInfo = mapper.convertValue(wordIndexTemp, Map.class);
			String rootword = (String) wordInfo.get("rootWord");
			ArrayList<Map<String, Object>> rootWordList = (ArrayList<Map<String, Object>>) rootWordsMap.get(rootword);
			if (rootWordList == null) {
				rootWordList = new ArrayList<Map<String, Object>>();
			}
			rootWordList.add(wordInfo);
			rootWordsMap.put(rootword, rootWordList);
		}
		OK(LanguageParams.root_word_info.name(), rootWordsMap, getSender());

	}

	/**
	 * Gets the word info index document of the words.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the word info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getWordInfo(List<String> words, String languageId, int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INFO_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordInfoIndexes = util.textSearch(WordInfoBean.class, searchCriteria, indexName,
				Constants.WORD_INFO_INDEX_TYPE);
		Map<String, Object> wordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordInfoIndexes) {
			Map<String, Object> infoMap = mapper.convertValue(wordIndexTemp, Map.class);
			wordsMap.put(infoMap.get("word").toString(), infoMap);
		}

		OK(LanguageParams.word_info.name(), writeMapToCSV(wordsMap), getSender());

	}

	/**
	 * Gets the word ids of the given words from ES.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the word ids
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void getWordIds(List<String> words, String languageId, int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, indexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		OK(LanguageParams.word_ids.name(), wordIdsMap, getSender());
	}

	/**
	 * Gets the index info details of a word.
	 *
	 * @param words
	 *            the words
	 * @param groupByFinalList
	 *            the group by final list
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the index info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getIndexInfo(List<String> words, List<Map<String, Object>> groupByFinalList, String languageId,
			int limit) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		// ************
		Map<String, Object> countMap = util.getCountOfSearch(null, searchCriteria, citationIndexName,
				Constants.CITATION_INDEX_TYPE, groupByFinalList);
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, wordIndexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();
		Map<String, Object> citiationCountsByWord = (Map<String, Object>) countMap.get("word");
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordMap.put("rootWord", wordIndex.getRootWord());
			Map<String, Object> citationWordMap = (Map<String, Object>) citiationCountsByWord.get(wordIndex.getWord());
			wordMap.put("citations", citationWordMap);
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		OK(LanguageParams.index_info.name(), wordIdsMap, getSender());
	}

	/**
	 * Gets the citations count grouped by different facets.
	 *
	 * @param words
	 *            the words
	 * @param languageId
	 *            the language id
	 * @param groupByList
	 *            the group by list
	 * @return the citations count
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getCitationsCount(List<String> words, String languageId, List<Map<String, Object>> groupByList)
			throws IOException {

		ElasticSearchUtil util = new ElasticSearchUtil();
		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, words);
		Map<String, Object> wordMap = (Map<String, Object>) util.getCountOfSearch(CitationBean.class, searchCriteria,
				citationIndexName, Constants.CITATION_INDEX_TYPE, groupByList).get(LanguageParams.word.name());
		OK(LanguageParams.citation_count.name(), wordMap, getSender());
	}

	/**
	 * Gets the citations of the words from ES.
	 *
	 * @param words
	 *            the words
	 * @param sourceType
	 *            the source type
	 * @param source
	 *            the source
	 * @param grade
	 *            the grade
	 * @param pos
	 *            the pos
	 * @param fileName
	 *            the file name
	 * @param fromDate
	 *            the from date
	 * @param toDate
	 *            the to date
	 * @param languageId
	 *            the language id
	 * @param limit
	 *            the limit
	 * @return the citations
	 * @throws Exception
	 *             the exception
	 */
	private void getCitations(List<String> words, Object sourceType, Object source, Object grade, Object pos,
			Object fileName, String fromDate, String toDate, String languageId, int limit) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ElasticSearchUtil util = new ElasticSearchUtil(limit);
		WordUtil wordUtil = new WordUtil();
		String indexName = Constants.CITATION_INDEX_COMMON_NAME + "_" + languageId;

		Map<String, Object> textFiltersMap = new HashMap<String, Object>();
		if (sourceType != null) {
			textFiltersMap.put("sourceType", wordUtil.getList(mapper, sourceType, null));
		}
		if (source != null) {
			textFiltersMap.put("source", wordUtil.getList(mapper, source, null));
		}
		if (grade != null) {
			textFiltersMap.put("grade", wordUtil.getList(mapper, grade, null));
		}
		if (pos != null) {
			textFiltersMap.put("pos", wordUtil.getList(mapper, pos, null));
		}
		if (fileName != null) {
			textFiltersMap.put("fileName", wordUtil.getList(mapper, fileName, null));
		}
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put("word", words);
		searchCriteria.put("rootWord", words);
		List<Object> citations = util.textFiltersSearch(CitationBean.class, searchCriteria, textFiltersMap, indexName,
				Constants.CITATION_INDEX_TYPE);
		Map<String, ArrayList<CitationBean>> citationsList = new HashMap<String, ArrayList<CitationBean>>();
		for (Object citationObj : citations) {
			CitationBean citationBean = (CitationBean) citationObj;
			ArrayList<CitationBean> citationBeanList = citationsList.get(citationBean.getWord());
			if (citationBeanList == null) {
				citationBeanList = new ArrayList<CitationBean>();
			}
			citationBeanList.add(citationBean);
			citationsList.put(citationBean.getWord(), citationBeanList);
		}

		OK(LanguageParams.citations.name(), citationsList, getSender());
	}

	/**
	 * Deletes the file.
	 *
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}

	/**
	 * Write map to CSV.
	 *
	 * @param wordsMap
	 *            the words map
	 * @return the string
	 */
	@SuppressWarnings("unchecked")
	private String writeMapToCSV(Map<String, Object> wordsMap) {

		String NEW_LINE = "\n";
		StringBuffer oneLine = new StringBuffer();

		boolean header = true;
		for (Object wordInfo : wordsMap.values()) {
			Map<String, Object> wordInfoMap = (Map<String, Object>) wordInfo;
			if (header) {
				oneLine.append(StringUtils.join(wordInfoMap.keySet(), ','));
				oneLine.append(NEW_LINE);
				header = false;
			}
			oneLine.append(StringUtils.join(wordInfoMap.values(), ','));
			oneLine.append(NEW_LINE);
		}
		return oneLine.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.common.mgr.BaseGraphManager#invokeMethod(org.ekstep.common
	 * .dto.Request, akka.actor.ActorRef)
	 */
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}