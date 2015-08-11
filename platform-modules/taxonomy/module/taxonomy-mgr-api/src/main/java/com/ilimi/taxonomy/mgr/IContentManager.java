package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IContentManager {

    Response create(String taxonomyId, String objectType, Request request);
    
    Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit, String[] gfields);

    Response find(String id, String taxonomyId, String objectType, String[] fields);

    Response update(String id, String taxonomyId, String objectType, Request request);

    Response delete(String id, String taxonomyId);

}
