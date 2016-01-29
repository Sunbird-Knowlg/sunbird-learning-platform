package org.ekstep.language.mgr.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.LanguageSourceTypeMap;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IImportManager;
import org.ekstep.language.models.DictionaryObject;
import org.ekstep.language.models.SynsetModel;
import org.ekstep.language.models.WordModel;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Component
public class ImportManagerImpl extends BaseLanguageManager implements IImportManager {
	
	private static final String CSV_SEPARATOR = ",";
	
	private static Logger LOGGER = LogManager.getLogger(ITaxonomyManager.class.getName());

	@Override
	public Response importData(String languageId, String sourceId, InputStream stream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(sourceId) || !LanguageSourceTypeMap.containsLanguage(sourceId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_SOURCE_TYPE.name(), "Invalid Source Id");
        if (null == stream)
            throw new ClientException(LanguageErrorCodes.ERR_SOURCE_EMPTY_INPUT_STREAM.name(),
                    "Source object is emtpy");
        LOGGER.info("Import : " + stream);
        Request request = getLanguageRequest(languageId, LanguageActorNames.IMPORT_ACTOR.name(), LanguageOperations.importData.name());
        request.put(LanguageParams.format.name(), LanguageParams.CSVInputStream);
        request.put(LanguageParams.input_stream.name(), stream);
        LOGGER.info("Import | Request: " + request);
        Response importRes = getLanguageResponse(request, LOGGER);
        if (checkError(importRes)) {
            return importRes;
        } else {
            Response response = copyResponse(importRes);
            // TODO: Return the Response for now its '200' or 400 series 
            return response;
        }
	}

	@Override
	public Response enrich(String languageId, String sourceId, InputStream synsetStream, InputStream wordStream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(sourceId) || !LanguageSourceTypeMap.containsLanguage(sourceId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Source Id");
        if (null == synsetStream)
            throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(),
                    "Synset object is emtpy");
        if (null == wordStream)
            throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(),
                    "Word object is emtpy");
        LOGGER.info("Enrich | Synset : " + synsetStream);
        LOGGER.info("Enrich | Word : " + wordStream);
      
        // Indices : Note- Change the value of inedx if there is change in CSV File structure
        final int IDX_WORD_IDENTIFIER = 0;
        final int IDX_WORD_LEMMA = 1;
        final int IDX_SYNSET_IDENTIFIER = 0;
        final int IDX_SYNSET_WORD_MEMBER = 1;
        final int IDX_SYNSET_ANTONYM_SYNSET_ID = 2;
        final int IDX_SYNSET_HYPONYM_SYNSET_ID = 3;
        final int IDX_SYNSET_MERONYM_SYNSET_ID = 4;
        final int IDX_SYNSET_HOLONYM_SYNSET_ID = 5;
        final int IDX_SYNSET_HYPERNYM_SYNSET_ID = 6;
        final int IDX_SYNSET_MEANING = 7;
        final int IDX_SYNSET_USAGE = 8;
        final int IDX_SYNSET_POS = 9;
        
        Reader reader = null;
        BufferedReader br = null; 
        DictionaryObject dictionaryObject = new DictionaryObject();
        List<WordModel> lstEnrichedWord = new ArrayList<WordModel>();
        List<SynsetModel> lstEnrichedSynset = new ArrayList<SynsetModel>();
        List<WordModel> lstWord = new ArrayList<WordModel>();
        List<SynsetModel> lstSynset = new ArrayList<SynsetModel>();
        String line = "";
        String[] objectDetails = null;
        String CSV_SPLIT_BY = ",";
        
        try {
	        // For Word
	        reader = new InputStreamReader(wordStream);
	        br = new BufferedReader(reader);
	        while ((line = br.readLine()) != null) {
				try {
					WordModel word = new WordModel();
					objectDetails = line.split(CSV_SPLIT_BY);
					word.setIdentifier(objectDetails[IDX_WORD_IDENTIFIER]);
					word.setWordLemma(objectDetails[IDX_WORD_LEMMA]);
					lstWord.add(word);
				} catch(ArrayIndexOutOfBoundsException e) {
					continue;
				}	
			}
	        
	        // Cleanup 
	        line = "";
	        if (null != reader) reader.close();
	        if (null != br) br.close();
	        
	        // For Synset
	        reader = new InputStreamReader(synsetStream);
	        br = new BufferedReader(reader);
	        while ((line = br.readLine()) != null) {
				try {
					SynsetModel synset = new SynsetModel();
					objectDetails = line.split(CSV_SPLIT_BY);
					synset.setIdentifier(objectDetails[IDX_SYNSET_IDENTIFIER]);
					synset.setWordMember(objectDetails[IDX_SYNSET_WORD_MEMBER]);
					synset.setAntonymSynsetId(objectDetails[IDX_SYNSET_ANTONYM_SYNSET_ID]);
					synset.setHyponymSynsetId(objectDetails[IDX_SYNSET_HYPONYM_SYNSET_ID]);
					synset.setMeronymSynsetId(objectDetails[IDX_SYNSET_MERONYM_SYNSET_ID]);
					synset.setHolonymSynsetId(objectDetails[IDX_SYNSET_HOLONYM_SYNSET_ID]);
					synset.setHypernymSynsetId(objectDetails[IDX_SYNSET_HYPERNYM_SYNSET_ID]);
					synset.setMeaning(objectDetails[IDX_SYNSET_MEANING]);
					synset.setUsage(objectDetails[IDX_SYNSET_USAGE]);
					synset.setPartOfSpeech(objectDetails[IDX_SYNSET_POS]);
					lstSynset.add(synset);
				} catch(ArrayIndexOutOfBoundsException e) {
					continue;
				}	
			}
	        if (lstWord.size() > 0) {
		        callAddCitationToIndex(languageId,sourceId, lstWord);
		        dictionaryObject = addCitattionCountInfoInWordList(languageId, lstWord, lstSynset);
		        if (null != dictionaryObject) {
		        	lstEnrichedWord = dictionaryObject.getLstWord();
		        	lstEnrichedSynset = dictionaryObject.getLstSynset();
		        }
		        if (null != lstEnrichedWord) {
		        	
		        }
	        }
        } catch(IOException e) {
        	e.printStackTrace();
        } finally {
        	line = "";
        	if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        	if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        return null; 
	}
	
	@SuppressWarnings("unused")
	private void callAddCitationToIndex(String languageId, String sourceId, List<WordModel> lstWord) {
		if (!StringUtils.isBlank(languageId) && LanguageMap.containsLanguage(languageId) && null != lstWord) {
			LOGGER.info("Enrich - callAddCitationToIndex :- Word List : " + lstWord + ", Language Id : " + languageId);
	        Request request = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(), LanguageOperations.addCitationIndex.name());
	        request.put(LanguageParams.citations.name(), getWordMapList(sourceId, lstWord));
	        LOGGER.info("List | Request: " + request);
	        Response addCitationRes = getLanguageResponse(request, LOGGER);
	        if (checkError(addCitationRes)) {
	            System.out.println("Enrich - callAddCitationToIndex : Error");
	        } else {
	            Response response = copyResponse(addCitationRes);
	            // TODO: Return the Response for now its '200' or 400 series 
	            System.out.println("Enrich - callAddCitationToIndex : Success");
	        }
		}
	}
	
	private List<Map<String, String>> getWordMapList(String sourceId, List<WordModel> lstWord) {
		List<Map<String, String>> lstMap = new ArrayList<Map<String, String>>();
		for (WordModel word : lstWord) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(LanguageParams.word.name(), word.getWordLemma());
			map.put(LanguageParams.date.name(), DateTime.now().toString());
			map.put(LanguageParams.source_type.name(), sourceId);
			map.put(LanguageParams.source.name(), LanguageSourceTypeMap.getLanguage(sourceId));
			lstMap.add(map);
		}
		return lstMap;
	}
	
	private Response callGetIndexInfo(String languageId, List<WordModel> lstWord) {
		if (lstWord.size() > 0) {
			List<String> lstLemma = getWordLemmaList(lstWord);
			if (lstLemma.size() > 0) {
				Request request = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(), LanguageOperations.getIndexInfo.name());
				request.put(LanguageParams.words.name(), lstLemma);
				Response getIndexInfoRes = getLanguageResponse(request, LOGGER);
	            return getIndexInfoRes;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private DictionaryObject addCitattionCountInfoInWordList(String languageId, List<WordModel> lstWord, List<SynsetModel> lstSynset) {
		if (lstWord.size() > 0) {
			Response getIndexInfoResponse = callGetIndexInfo(languageId, lstWord);
			if (checkError(getIndexInfoResponse)) {
	            return null;
	        } else {
	            Response response = copyResponse(getIndexInfoResponse);
	            DictionaryObject dictionaryObject = new DictionaryObject();
	            Map<String, String> replacedWordIdMap = new HashMap<String, String>();
	            Map<String, Object> map = (Map<String, Object>) response.get(LanguageParams.index_info.name());
	            for (String key : map.keySet()) {
	            	for (WordModel word : lstWord) {
	            		try {
		            		if (StringUtils.equalsIgnoreCase(word.getWordLemma().trim(), key.trim())) {
		            			// Record the changed/updated word identifier which needs to be replaced in Synset List as well.
		            			if (!StringUtils.equalsIgnoreCase(word.getIdentifier(), map.get(LanguageParams.identifier.name()).toString())) {
		            				replacedWordIdMap.put(word.getIdentifier().trim(), map.get(LanguageParams.identifier.name()).toString());
		            			}
		            			Map<String, Object> citationMap = (Map<String, Object>) map.get(LanguageParams.citations.name());
		            			Map<String, Integer> citationBySourceType = (Map<String, Integer>) citationMap.get(LanguageParams.source_type.name());
		            			Map<String, Integer> citationBySource = (Map<String, Integer>) citationMap.get(LanguageParams.source.name());
		            			Map<String, Integer> citationByPOS = (Map<String, Integer>) citationMap.get(LanguageParams.pos.name());
		            			Map<String, Integer> citationByGrad = (Map<String, Integer>) citationMap.get(LanguageParams.grad.name());
		            			word.setWordLemma(map.get(LanguageParams.root_word.name()).toString().trim());
		            			word.setIdentifier(map.get(LanguageParams.identifier.name()).toString());
		            			word.setTotalCitation(Integer.parseInt(citationMap.get(LanguageParams.total.name()).toString()));
		            			word.setCitationBySourceType(citationBySourceType);
		            			word.setCitationBySource(citationBySource);
		            			word.setCitationByPOS(citationByPOS);
		            			word.setCitationByGrad(citationByGrad);
		            			break;
		            		}
	            		} catch(Exception e) {
	            			e.printStackTrace();
	            			continue; 
	            		}
	            	}
	            }
	            // Remove duplicate words from Word List
	            Map<String, WordModel> uniqueWordMap = new HashMap<String, WordModel>();
	            for (WordModel word : lstWord) {
	            	if (!uniqueWordMap.containsKey(word.getIdentifier().trim())) {
	            		uniqueWordMap.put(word.getIdentifier(), word);
	            	}
	            }
	            lstWord.clear();
	            for (Entry<String, WordModel> entry : uniqueWordMap.entrySet()) {
	            	lstWord.add(entry.getValue());
	            }
	            
	            // Replace new Word Ids with existing one in Synset List.
	            if (lstSynset.size() > 0) {
		            for (SynsetModel synset : lstSynset) {
		            	String[] lstMemberWordId = null;
		            	String memberWordId = synset.getWordMember();
		            	if (!StringUtils.isBlank(memberWordId)) {
		            		lstMemberWordId = memberWordId.split(CSV_SEPARATOR);
		            		for (String wordId : lstMemberWordId) {
		            			if (replacedWordIdMap.containsKey(wordId.trim())) {
		            				wordId = replacedWordIdMap.get(wordId).trim();
		            			}
		            		}
		            		if (null != lstMemberWordId) {
			            		String updatedMemberWordId = StringUtils.join(lstMemberWordId, CSV_SEPARATOR);
			            		if (!StringUtils.equalsIgnoreCase(memberWordId, updatedMemberWordId))
			            			synset.setWordMember(updatedMemberWordId);
		            		}
		            	}
		            }
	            }
	            dictionaryObject.setLstWord(lstWord);
	            dictionaryObject.setLstSynset(lstSynset);
	            return dictionaryObject;
	        }
		}
		return null;
	}
	
	private List<String> getWordLemmaList(List<WordModel> lstWord) {
		List<String> lstLemma = new ArrayList<String>();
		for (WordModel word : lstWord) {
			if (!lstLemma.contains(word.getWordLemma()) && !StringUtils.isBlank(word.getWordLemma()))
				lstLemma.add(word.getWordLemma().trim());
		}
		return lstLemma;
	}
}