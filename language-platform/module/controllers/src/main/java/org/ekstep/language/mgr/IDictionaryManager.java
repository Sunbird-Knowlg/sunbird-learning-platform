package org.ekstep.language.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IDictionaryManager {

    Response create(String languageId, String objectType, Request request);

    Response update(String languageId, String id, String objectType, Request request);

    Response find(String languageId, String id, String[] fields);

    Response findAll(String languageId, String objectType, String[] fields, Integer limit);

    Response deleteRelation(String languageId, String objectType, String objectId1, String relation, String objectId2);
    
    Response list(String languageId, String objectType, Request request);

}
