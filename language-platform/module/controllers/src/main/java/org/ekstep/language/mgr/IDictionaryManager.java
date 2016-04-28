package org.ekstep.language.mgr;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IDictionaryManager {
    
    Response upload(File uploadedFile);

    Response create(String languageId, String objectType, Request request);

    Response update(String languageId, String id, String objectType, Request request);

    Response find(String languageId, String id, String[] fields);

    Response findAll(String languageId, String objectType, String[] fields, Integer limit);

    Response deleteRelation(String languageId, String objectType, String objectId1, String relation, String objectId2);
    
    Response list(String languageId, String objectType, Request request);

	Response addRelation(String languageId, String objectType, String objectId1, String relation, String objectId2);

	Response relatedObjects(String languageId, String objectType, String objectId, String relation, String[] fields,
			String[] relations);
	
	Response translation(String languageId, String[] words, String[] languages);

	Response importWordSynset(String languageId, InputStream inputStream) throws Exception;
    
    void findWordsCSV(String languageId, String objectType, InputStream is, OutputStream out);
	
	Response createWordV2(String languageId, String objectType, Request request);
	
	Response updateWordV2(String languageId, String id, String objectType, Request request);

	Response findV2(String languageId, String id, String[] fields);

	Response findAllV2(String languageId, String objectType, String[] fields, Integer limit);

	Response listV2(String languageId, String name, Request request);
    
    Response loadEnglishWordsArpabetsMap(InputStream in);
    
    Response getSyllables(String languageID, String word);
    
    Response getArpabets(String languageID, String word);
    
    Response getPhoneticSpellingByLanguage(String languageID, String word);
    
    Response getSimilarSoundWords(String languageId, String word);

}
