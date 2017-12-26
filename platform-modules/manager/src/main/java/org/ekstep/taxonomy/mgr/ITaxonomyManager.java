package org.ekstep.taxonomy.mgr;

import java.io.InputStream;
import java.util.List;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;

public interface ITaxonomyManager {

    Response create(String id, InputStream stream);
    
    Response export(String id, Request req);

    Response delete(String id);

    Response updateDefinition(String id, String json);

    Response findAllDefinitions(String id);

    Response findDefinition(String id, String objectType);

    Response deleteDefinition(String id, String objectType);
    
    Response createIndex(String id, List<String> keys, Boolean unique);
    
    Response getSubGraph(String graphId, String id, Integer depth, List<String> relations);
    
	Response findAllByObjectType(String graphId, String objectType);

}
