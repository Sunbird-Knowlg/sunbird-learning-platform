package org.sunbird.graph.engine.router;

import org.sunbird.graph.engine.mgr.impl.CollectionManagerImpl;
import org.sunbird.graph.engine.mgr.impl.GraphMgrImpl;
import org.sunbird.graph.engine.mgr.impl.NodeManagerImpl;
import org.sunbird.graph.engine.mgr.impl.SearchManagerImpl;

public class GraphEngineManagers {

    public static final String GRAPH_MANAGER = GraphMgrImpl.class.getName();
    public static final String NODE_MANAGER = NodeManagerImpl.class.getName();
    public static final String COLLECTION_MANAGER = CollectionManagerImpl.class.getName();
    public static final String SEARCH_MANAGER = SearchManagerImpl.class.getName();
}
