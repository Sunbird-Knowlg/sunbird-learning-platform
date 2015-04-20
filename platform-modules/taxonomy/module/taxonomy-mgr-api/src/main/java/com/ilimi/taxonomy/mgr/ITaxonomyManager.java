package com.ilimi.taxonomy.mgr;

import java.io.InputStream;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;

public interface ITaxonomyManager {

    Response findAll();

    Response find(String id, boolean subgraph);

    Response create(String id, InputStream stream);

    Response update(String id, InputStream stream);

    Response delete(String id);

    Response search(String id, Request request);

    Response createDefinition(String id, Request request);

    Response updateDefinition(String id, String objectType, Request request);

    Response findAllDefinitions(String id);

    Response findDefinition(String id, String objectType);

    Response deleteDefinition(String id, String objectType);

}
