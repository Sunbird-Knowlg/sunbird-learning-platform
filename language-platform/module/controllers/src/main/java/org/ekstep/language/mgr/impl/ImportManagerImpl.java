package org.ekstep.language.mgr.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.LanguageSourceTypeMap;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IImportManager;
import org.ekstep.language.models.SynsetModel;
import org.ekstep.language.models.WordModel;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Component
public class ImportManagerImpl extends BaseLanguageManager implements IImportManager {
	
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
        Request request = getLanguageRequest(languageId, LanguageActorNames.IMPORT_ACTOR.name(), "importData");
        request.put(LanguageParams.format.name(), LanguageParams.CSVInputStream);
        request.put(LanguageParams.input_stream.name(), stream);
        LOGGER.info("List | Request: " + request);
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
	public Response enrich(String languageId, InputStream synsetStream, InputStream wordStream) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
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
	        
	        List<String> lstLemma = getWordLemmaList(lstWord);
	        if (lstLemma.size() > 0) {
	        	// Make a Call to Indexer for Adding word to Index and DB.
	        	
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
	private void callAddCitationToIndex() {
		
	}
	
	private List<String> getWordLemmaList(List<WordModel> lstWord) {
		List<String> lstLemma = new ArrayList<String>();
		for (WordModel word : lstWord) {
			if (!lstLemma.contains(word.getWordLemma()) && null != word.getWordLemma() && word.getWordLemma() != "")
				lstLemma.add(word.getWordLemma().trim());
		}
		return lstLemma;
	}
}