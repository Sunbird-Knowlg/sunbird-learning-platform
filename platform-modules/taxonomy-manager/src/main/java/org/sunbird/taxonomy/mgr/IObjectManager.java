package org.sunbird.taxonomy.mgr;

import org.sunbird.common.dto.Response;

import java.util.Map;

public interface IObjectManager {

    Response create(String objectType, Map<String, Object> request) throws Exception;

    Response update(String objectType, String id , Map<String, Object> request) throws Exception;

    Response read(String objectType, String id);

    Response delete(String objectType, String id);
}
