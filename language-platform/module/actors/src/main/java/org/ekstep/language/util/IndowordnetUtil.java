package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.Util.HibernateSessionFactory;
import org.ekstep.language.Util.IndowordnetConstants;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.model.LanguageSynsetData;
import org.ekstep.language.model.SynsetData;
import org.ekstep.language.model.SynsetDataLite;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.WordUtil;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.ilimi.common.dto.Request;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.model.node.DefinitionDTO;

import akka.actor.ActorRef;

/**
 * The Class IndowordnetUtil provides utilities to load words form a indoword
 * net DB into the platform.
 * 
 * @author Amarnath
 * 
 */
public class IndowordnetUtil {

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/** The comma separator. */
	private final String COMMA_SEPARATOR = ",";

	/** The semi colon separator. */
	private final String SEMI_COLON_SEPARATOR = ";";

	/** The colon separator. */
	private final String COLON_SEPARATOR = ":";

	/** The double quotes. */
	private final String DOUBLE_QUOTES = "\"";

	/** The word util. */
	private WordUtil wordUtil = new WordUtil();

	/** The logger. */
	// private EmailService emailService = new EmailService();
	private static Logger LOGGER = LogManager.getLogger(IndowordnetUtil.class.getName());

	/**
	 * Queries words from the Indowordnet DB for a given language, processes the
	 * words, its relations, transaltions, etc and loads them into the Graph DB.
	 *
	 * @param languageGraphId
	 *            the language graph id
	 * @param batchSize
	 *            the batch size
	 * @param maxRecords
	 *            the max records
	 * @param initialOffset
	 *            the initial offset
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	@SuppressWarnings({ "unchecked" })
	public void loadWords(String languageGraphId, int batchSize, int maxRecords, int initialOffset)
			throws JsonProcessingException {
		int offset = initialOffset;
		int loop = 0;
		int totalCount = 0;
		long startTime = 0l;
		long endTime = 0l;
		String language = LanguageMap.getLanguage(languageGraphId);
		if (languageGraphId != null) {
			List<String> errorMessages = new ArrayList<String>();
			Map<String, String> wordLemmaMap = new HashMap<String, String>();
			wordUtil.cacheAllWords(languageGraphId, wordLemmaMap, errorMessages);
			DefinitionDTO wordDefinition = wordUtil.getDefinitionDTO(LanguageParams.Word.name(), languageGraphId);
			DefinitionDTO synsetDefinition = wordUtil.getDefinitionDTO(LanguageParams.Synset.name(), languageGraphId);
			long totalStartTime = System.currentTimeMillis();

			// process wortd in batches
			do {
				long batchStartTime = System.currentTimeMillis();
				Session session = HibernateSessionFactory.getSession();
				String languageTableName = getLanguageTableName(language);
				Transaction tx = null;
				try {
					tx = session.beginTransaction();

					// get records from the indowWordNet DB
					Query query = session.createQuery("FROM " + languageTableName + " ORDER BY synset_id");
					query.setFirstResult(offset);
					query.setMaxResults(batchSize);

					startTime = System.currentTimeMillis();
					List<LanguageSynsetData> languageSynsetDataList = query.list();
					endTime = System.currentTimeMillis();
					System.out.println("Getting " + batchSize + " records: " + (endTime - startTime));
					if (languageSynsetDataList.isEmpty()) {
						break;
					}
					int count = 0;
					ArrayList<String> nodeIds = new ArrayList<String>();
					for (LanguageSynsetData lSynsetData : languageSynsetDataList) {
						try {
							if (totalCount == maxRecords) {
								break;
							}
							count++;
							totalCount++;
							SynsetData synsetData = lSynsetData.getSynsetData();
							// get Word object
							Map<String, Object> wordRequestMap = getWordMap(synsetData, errorMessages, languageGraphId);
							long synsetStartTime = System.currentTimeMillis();
							int englishTranslationId = 0;
							if(synsetData!=null && StringUtils.equalsIgnoreCase(languageGraphId, "en")){
								englishTranslationId = synsetData.getSynset_id();

							}
							// Create/update word in the Graph
							errorMessages.addAll(wordUtil.createOrUpdateWord(wordRequestMap, languageGraphId,
									wordLemmaMap, wordDefinition, nodeIds, synsetDefinition, englishTranslationId));
							long synsetEndTime = System.currentTimeMillis();
							System.out.println(
									"Time taken for importing one synset record: " + (synsetEndTime - synsetStartTime));
						} catch (Exception e) {
							LOGGER.error(e.getMessage(), e);
							e.printStackTrace();
							errorMessages.add(e.getMessage());
						}
					}

					// enrich the words
					asyncUpdate(nodeIds, languageGraphId);
					System.out.println("Loaded " + count + " synsets for language: " + language);
					long batchEndTime = System.currentTimeMillis();
					System.out.println("Time taken for one batch: " + (batchEndTime - batchStartTime));
					if (totalCount == maxRecords) {
						break;
					}
					loop++;
					offset = batchSize * loop + initialOffset;
				} catch (Exception e) {
					if (tx != null)
						tx.rollback();
					LOGGER.error(e.getMessage(), e);
					e.printStackTrace();
					errorMessages.add(e.getMessage());
				} finally {
					HibernateSessionFactory.closeSession();
				}

			} while (true);
			long totalEndTime = System.currentTimeMillis();
			System.out.println("Status Update: Loaded " + totalCount + " synsets for language: " + language);
			System.out.println("Total time taken for import: " + (totalEndTime - totalStartTime));
			if (!errorMessages.isEmpty()) {
				System.out.println("Error Messages for Indowordnet import ********************************* ");
				for (String errorMessage : errorMessages) {
					System.out.println(errorMessage);
				}
			}
		}
	}

	/**
	 * Forms the word object as a map.
	 *
	 * @param synsetData
	 *            the synset data
	 * @param errorMessages
	 *            the error messages
	 * @return the word map
	 * @throws JsonProcessingException
	 *             the json processing exception
	 */
	private Map<String, Object> getWordMap(SynsetData synsetData, List<String> errorMessages, String languageGraphId)
			throws JsonProcessingException {
		byte[] bytesSynset = null;
		byte[] bytesGloss = null;
		String synsetString = null;
		String glossString = null;
		String gloss = null;
		String exampleSentencesString = null;
		List<String> exampleSentences = null;
		Map<String, Object> wordMap = new HashMap<String, Object>();
		Map<String, Object> primaryMeaningMap = new HashMap<String, Object>();
		boolean setFlag = false;

		// words, gloss, pos and example sentences

		// get the list of words from the synset field
		bytesSynset = synsetData.getSynset();
		synsetString = new String(bytesSynset, Charsets.UTF_8);
		String[] words = synsetString.split(COMMA_SEPARATOR);

		wordMap.put(LanguageParams.words.name(), Arrays.asList(words));
		if(StringUtils.equalsIgnoreCase(languageGraphId, "en"))
		{
			if(synsetData.getEnglish_synset_id()!=0){
				wordMap.put(LanguageParams.indowordnetId.name(), synsetData.getEnglish_synset_id());
				primaryMeaningMap.put(LanguageParams.indowordnetId.name(), synsetData.getEnglish_synset_id());
				setFlag = true;
			}
		}if(!setFlag)
		{
			wordMap.put(LanguageParams.indowordnetId.name(), synsetData.getSynset_id());
			primaryMeaningMap.put(LanguageParams.indowordnetId.name(), synsetData.getSynset_id());
		}
		

		bytesGloss = synsetData.getGloss();
		glossString = new String(bytesGloss, Charsets.UTF_8);
		int indexOfQuote = glossString.indexOf(DOUBLE_QUOTES);
		if (indexOfQuote > 0) {
			gloss = glossString.substring(0, indexOfQuote - 1);
			gloss = gloss.replaceAll(SEMI_COLON_SEPARATOR, "");
			gloss = gloss.replaceAll(COLON_SEPARATOR, "");
			exampleSentencesString = glossString.substring(indexOfQuote + 1, glossString.length() - 1);
			exampleSentencesString = exampleSentencesString.replaceAll("\"", "");
			exampleSentences = Arrays.asList(exampleSentencesString.split(COMMA_SEPARATOR));
		} else {
			gloss = glossString;
		}

		// From the primary meaning object as a map
		primaryMeaningMap.put(LanguageParams.gloss.name(), gloss);
		primaryMeaningMap.put(LanguageParams.exampleSentences.name(), exampleSentences);
		primaryMeaningMap.put(LanguageParams.pos.name(), StringUtils.capitalize(
				synsetData.getCategory() != null ? synsetData.getCategory().toLowerCase() : StringUtils.EMPTY));
		

		// Process and create the relations of the word
		for (Map.Entry<String, List<SynsetDataLite>> entry : synsetData.getRelations().entrySet()) {
			String relationName = entry.getKey();
			List<SynsetDataLite> relationDataList = entry.getValue();
			List<Map<String, String>> relationsList = new ArrayList<>();

			for (SynsetDataLite relation : relationDataList) {
				bytesSynset = relation.getSynset();
				synsetString = new String(bytesSynset, Charsets.UTF_8);
				String[] relationWords = synsetString.split(COMMA_SEPARATOR);

				for (String relationWord : relationWords) {
					Map<String, String> lemmaMap = new HashMap<String, String>();
					lemmaMap.put(LanguageParams.lemma.name(), relationWord);
					relationsList.add(lemmaMap);
				}
			}
			primaryMeaningMap.put(relationName, relationsList);
		}
		wordMap.put(LanguageParams.primaryMeaning.name(), primaryMeaningMap);

		return wordMap;
	}

	/**
	 * Gets the language table name.
	 *
	 * @param language
	 *            the language
	 * @return the language table name
	 */
	private String getLanguageTableName(String language) {
		language = StringUtils.capitalize(language.toLowerCase());
		String tableName = language + IndowordnetConstants.SynsetData.name();
		return tableName;
	}

	/**
	 * Async update to enrich the words.
	 *
	 * @param nodeIds
	 *            the node ids
	 * @param languageId
	 *            the language id
	 */
	private void asyncUpdate(List<String> nodeIds, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.node_ids.name(), nodeIds);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
		request.setOperation(LanguageOperations.enrichWords.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);
		makeAsyncRequest(request, LOGGER);
	}

	/**
	 * Makes the request asynchronously.
	 *
	 * @param request
	 *            the request
	 * @param logger
	 *            the logger
	 */
	public void makeAsyncRequest(Request request, Logger logger) {
		ActorRef router = LanguageRequestRouterPool.getRequestRouter();
		try {
			router.tell(request, router);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}
}
