package org.ekstep.language.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

public class IndowordnetUtil {

	private ObjectMapper mapper = new ObjectMapper();
	private final String COMMA_SEPARATOR = ",";
	private final String SEMI_COLON_SEPARATOR = ";";
	private final String COLON_SEPARATOR = ":";
	private WordUtil wordUtil = new WordUtil();
	//private EmailService emailService = new EmailService();
	private static Logger LOGGER = LogManager.getLogger(IndowordnetUtil.class.getName());

	@SuppressWarnings({ "unchecked" })
	public void loadWords(String languageGraphId, int batchSize, int maxRecords) throws JsonProcessingException {
		int offset = 0;
		int loop = 0;
		int totalCount = 0;
		long startTime=0l;
		long endTime=0l;
		String language = LanguageMap.getLanguage(languageGraphId);
		if (languageGraphId != null) {
			List<String> errorMessages = new ArrayList<String>();
			Map<String, String> wordLemmaMap = new HashMap<String, String>();
			wordUtil.cacheAllWords(languageGraphId, wordLemmaMap, errorMessages);
			DefinitionDTO wordDefinition = wordUtil.getDefinitionDTO(LanguageParams.Word.name(), languageGraphId);
			DefinitionDTO synsetDefinition = wordUtil.getDefinitionDTO(LanguageParams.Synset.name(), languageGraphId);
			do {
				Session session = HibernateSessionFactory.getSession();
				String languageTableName = getLanguageTableName(language);
				Transaction tx = null;
				try {
					tx = session.beginTransaction();
					Query query = session.createQuery("FROM " + languageTableName + " ORDER BY synset_id");
					query.setFirstResult(offset);
					query.setMaxResults(batchSize);

					startTime=System.currentTimeMillis();
					List<LanguageSynsetData> languageSynsetDataList = query.list();
					endTime=System.currentTimeMillis();
					System.out.println("Getting "+ batchSize+ " records: " + (endTime-startTime));
					if (languageSynsetDataList.isEmpty()) {
						break;
					}
					int count = 0;
					ArrayList<String> nodeIds = new ArrayList<String>();
					for (LanguageSynsetData lSynsetData : languageSynsetDataList) {
						if (totalCount == maxRecords) {
							break;
						}
						count++;
						totalCount++;
						startTime=System.currentTimeMillis();
						SynsetData synsetData = lSynsetData.getSynsetData();
						endTime=System.currentTimeMillis();
						System.out.println("Converting to SynsetData : " + (endTime-startTime));
						
						startTime=System.currentTimeMillis();
						Map<String, Object> wordRequestMap = getWordMap(synsetData, errorMessages);
						endTime=System.currentTimeMillis();
						System.out.println("Getting word map : " + (endTime-startTime));
						errorMessages.addAll(wordUtil.createOrUpdateWord(wordRequestMap, languageGraphId, wordLemmaMap,
								wordDefinition, nodeIds, synsetDefinition));
					}
					asyncUpdate(nodeIds, languageGraphId);
					if (totalCount == maxRecords) {
						break;
					}
					loop++;
					offset = batchSize * loop;
					System.out.println("Loaded " + count + " synsets for language: " + language);
					
				} catch (Exception e) {
					if (tx != null)
						tx.rollback();
					e.printStackTrace();
				} finally {
					HibernateSessionFactory.closeSession();
				}

			} while (true);
			System.out.println("Status Update: Loaded " + totalCount + " synsets for language: " + language);
		}
	}

	private Map<String, Object> getWordMap(SynsetData synsetData, List<String> errorMessages)
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

		// words, gloss, pos and example sentences

		bytesSynset = synsetData.getSynset();
		synsetString = new String(bytesSynset, Charsets.UTF_8);
		String[] words = synsetString.split(COMMA_SEPARATOR);

		wordMap.put(LanguageParams.words.name(), Arrays.asList(words));
		wordMap.put(LanguageParams.indowordnetId.name(), synsetData.getSynset_id());

		bytesGloss = synsetData.getGloss();
		glossString = new String(bytesGloss, Charsets.UTF_8);
		String[] glossArray = glossString.split(SEMI_COLON_SEPARATOR);
		if (glossArray.length == 1) {
			glossArray = glossString.split(COLON_SEPARATOR);
		}
		if (glossArray.length > 0) {
			gloss = glossArray[0];
		}
		if (glossArray.length > 1) {
			exampleSentencesString = glossArray[1];
			exampleSentences = Arrays.asList(exampleSentencesString.split(COMMA_SEPARATOR));
		}

		primaryMeaningMap.put(LanguageParams.gloss.name(), gloss);
		primaryMeaningMap.put(LanguageParams.exampleSentences.name(), exampleSentences);
		primaryMeaningMap.put(LanguageParams.pos.name(), StringUtils.capitalize(
				synsetData.getCategory() != null ? synsetData.getCategory().toLowerCase() : StringUtils.EMPTY));
		primaryMeaningMap.put(LanguageParams.indowordnetId.name(), synsetData.getSynset_id());

		// Relations
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

		// translations
		Map<String, Object> translationsMap = new HashMap<String, Object>();
		for (Map.Entry<String, List<SynsetDataLite>> entry : synsetData.getTranslations().entrySet()) {
			String translatedLanguage = entry.getKey();
			List<SynsetDataLite> translatedDataList = entry.getValue();
			List<String> finalTranslationWords = new ArrayList<String>();

			for (SynsetDataLite translation : translatedDataList) {
				bytesSynset = translation.getSynset();
				synsetString = new String(bytesSynset, Charsets.UTF_8);
				String[] translationWords = synsetString.split(COMMA_SEPARATOR);
				finalTranslationWords.addAll(Arrays.asList(translationWords));
			}

			String translatedLanguageGraphId = LanguageMap.getLanguageGraph(translatedLanguage);
			if (translatedLanguageGraphId == null) {
				errorMessages.add("Graph not found for Language: " + translatedLanguage);
			}
			translationsMap.put(translatedLanguageGraphId, finalTranslationWords);
		}

		primaryMeaningMap.put(LanguageParams.translations.name(), mapper.writeValueAsString(translationsMap));
		wordMap.put(LanguageParams.primaryMeaning.name(), primaryMeaningMap);

		return wordMap;
	}

	public static void main(String[] args) throws JsonProcessingException {
		IndowordnetUtil util = new IndowordnetUtil();
		util.loadWords("tamil", 0, 300);
	}

	private String getLanguageTableName(String language) {
		language = StringUtils.capitalize(language.toLowerCase());
		String tableName = language + IndowordnetConstants.SynsetData.name();
		return tableName;
	}
	
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
