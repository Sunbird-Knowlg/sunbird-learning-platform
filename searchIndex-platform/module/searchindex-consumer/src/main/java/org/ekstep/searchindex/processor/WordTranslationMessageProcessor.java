package org.ekstep.searchindex.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.searchindex.util.WordTranslationConstants;

import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;


public class WordTranslationMessageProcessor implements IMessageProcessor {

	private static ILogger LOGGER = PlatformLogManager.getLogger();
	private ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private ObjectMapper mapper = new ObjectMapper();

	public WordTranslationMessageProcessor() {
		super();
	}

	public void processMessage(String messageData) {
		try {
			Map<String, Object> message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
			});
			processMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.log("Exception" + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("eid") != null && StringUtils.equalsIgnoreCase((String)message.get("eid"),"BE_WORD_LIFECYCLE")) {
			if(message.get("edata") != null){
				Map<String, Object> edata = (Map<String, Object>)(message.get("edata"));
				if(edata!=null)
				{
					Map<String, Object> data = (Map<String, Object>)(edata.get("eks"));
					if(data.get("id") != null && data.get("state") != null && data.get("lemma") != null){
						String id = (String) data.get("id");
						String state = (String) data.get("state");
						createTranslationIndex();
						//String prevState = (String) data.get("prevState");
						String lemma = (String) data.get("lemma");
						String languageId = (String) data.get("languageId");
						Map<String,String> finalDocumentMap = getIndexDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, 
								CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, id, lemma, languageId);
						LOGGER.log("Adding/Updating translation index document", languageId);
						if(finalDocumentMap!=null && finalDocumentMap.size()>0)
						{
							addOrUpdateIndex(id, finalDocumentMap);
						}
						
					}
				}
			}
		}
	}

	private void addOrUpdateIndex(String uniqueId,Map<String, String> indexesMap) throws Exception {
		LOGGER.log("Translation index created: Identifier: " , uniqueId);
		if(indexesMap!=null && indexesMap.size()>0){
			LOGGER.log("Translation index size " + indexesMap.size());
			elasticSearchUtil.bulkIndexWithIndexId(WordTranslationConstants.TRANSLATION_INDEX,
					WordTranslationConstants.TRANSLATION_INDEX_TYPE, indexesMap);
		}
	}


	private void createTranslationIndex() throws IOException {
		String settings = "{ \"settings\": {   \"index\": {     \"index\": \""+WordTranslationConstants.TRANSLATION_INDEX+"\",     \"type\": \""+WordTranslationConstants.TRANSLATION_INDEX_TYPE+"\",     \"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   } }}";
		String mappings = "{ \""+WordTranslationConstants.TRANSLATION_INDEX_TYPE+"\" : {    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"cs_index_analyzer\",            \"search_analyzer\": \"cs_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"cs_index_analyzer\",        \"search_analyzer\": \"cs_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }}";
		elasticSearchUtil.addIndex(WordTranslationConstants.TRANSLATION_INDEX,
				WordTranslationConstants.TRANSLATION_INDEX_TYPE, settings, mappings);
	}
	
	private Map<String,String> getIndexDocument(String index,String type, String uniqueId,String lemma, String languageId) throws IOException {
		Map<String,String> finalDocumentMap = new HashMap<String,String>();
		try
		{
		List<String> synsetIds = getSynsetForWord(index, type, uniqueId);
		if(synsetIds!=null && synsetIds.size()>0)
		{
			finalDocumentMap =  getTranslationSetFromComposite(synsetIds,index,type,uniqueId,lemma);
		}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return finalDocumentMap;

	}
	
	private List<String> getSynsetForWord(String index,String type, String uniqueId)
	{
		List<String> synsetIds = null;
		try
		{
			Map<String, Object> indexDocument = new HashMap<String, Object>();
			String documentJson = elasticSearchUtil.getDocumentAsStringById(index,type, uniqueId);
			if (documentJson != null && !documentJson.isEmpty()) {
				LOGGER.log("Document exists for " , uniqueId);
				indexDocument = mapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
				});
				Object synsets = indexDocument.get("synonyms");
				if(synsets!=null)
				{
					synsetIds = (List<String>) synsets;
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return synsetIds;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> getTranslationSetFromComposite(List<String> synsetIds, String index, String type,String id, String lemma)
	{
		Map<String, String> indexesMap = new HashMap<String, String>();
		try
		{
			Map<String, Object> matchCriterias = new HashMap<String, Object>();
			Map<String, Object> finalDocument = new HashMap<String, Object>();
			Map<String,List<Map<String,String>>> languageId_words = new HashMap<String,List<Map<String,String>>>();
			matchCriterias.put("synsets", synsetIds);
			List<Map> documentObjectList = elasticSearchUtil.textSearchReturningId(matchCriterias , index, type);
			if(documentObjectList!=null && documentObjectList.size()>0){
				for(Map translationSet: documentObjectList)
				{
					Map<String, Object> indexDocument = translationSet;
					Map<String,List<String>> synset_ids = new HashMap<String,List<String>>();
					for(String synsetId : (List<String>)indexDocument.get("synsets"))
					{
						String synsetIdContent[] = synsetId.split(":");
						if(synsetIdContent.length==1)
						{
							synsetIdContent = synsetId.split("_");
						}
						if(synsetIdContent.length>1)
						{
							String language = synsetIdContent[0];
							List<String> synsetListForLanguage = new ArrayList<String>();
							if(synset_ids.containsKey(language))
							{
								synsetListForLanguage = synset_ids.get(language);
							}
							synsetListForLanguage.add(synsetId);
							synset_ids.put(synsetIdContent[0], synsetListForLanguage);
						}
						languageId_words = getWordsForSynset(synsetId, languageId_words);
					}

					finalDocument.put("translation_set_id", (String)indexDocument.get("identifier"));
					finalDocument.put("synset_ids", synset_ids);
					for(Map.Entry<String,List<Map<String,String>>> entry : languageId_words.entrySet())
					{
						finalDocument.put(entry.getKey(), entry.getValue());
					}
					String document = mapper.writeValueAsString(finalDocument);
					indexesMap.put((String)finalDocument.get("translation_set_id"), document);
				}
			}
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return indexesMap;
	}
	
	private Map<String,List<Map<String,String>>> getWordsForSynset(String id, Map<String,List<Map<String,String>>> languageId_words)
	{
		try
		{
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		String documentJson = elasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, 
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, id);
		if (documentJson != null && !documentJson.isEmpty()) {
			LOGGER.log("Document exists for " , id);
			indexDocument = mapper.readValue(documentJson, new TypeReference<Map<String, Object>>() {
			});
			Object words = indexDocument.get("synonyms");
			if(words!=null)
			{
				List<String> wordsIds = (List<String>) words;
				for(String wordId : wordsIds)
				{
					String wordDocument = elasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, 
							CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, wordId);
					if (wordDocument != null && !wordDocument.isEmpty()) {
						Map<String, Object> indexWordDocument = mapper.readValue(wordDocument, new TypeReference<Map<String, Object>>() {
						});

						String status = (String)indexWordDocument.get("status");
						String graphId = (String)indexWordDocument.get("graph_id");
						if(status.equalsIgnoreCase("Live"))
						{
							String identifier = (String)indexWordDocument.get("identifier");
							String lemma = (String)indexWordDocument.get("lemma");
							Map<String,String> wordMap = new HashMap<String,String>();
							wordMap.put("id", identifier);
							wordMap.put("lemma", lemma);
							List<Map<String,String>> wordList = new ArrayList<Map<String,String>>();
							if(languageId_words.containsKey(graphId))
							{
								wordList = languageId_words.get(graphId);
								boolean flag = false;
								for(Map<String,String> word : wordList)
								{
									if(word.get("id").equalsIgnoreCase(identifier))
									{
										flag = true;
										break;
									}
								}
								if(flag)
									continue;
							}
							wordList.add(wordMap);
							languageId_words.put(graphId, wordList);
						}
				}
				}
				
			}
		}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return languageId_words;
	}
}
