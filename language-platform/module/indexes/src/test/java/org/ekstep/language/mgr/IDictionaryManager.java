package org.ekstep.language.mgr;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;

public interface IDictionaryManager {

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

}
