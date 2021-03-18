package org.sunbird.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.cache.exception.GraphCacheErrorCodes;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.mgr.IGraphManager;
import org.sunbird.graph.engine.router.GraphEngineActorPoolMgr;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.model.Graph;
import org.sunbird.graph.model.IRelation;
import org.sunbird.graph.model.cache.DefinitionCache;
import org.sunbird.graph.model.relation.RelationHandler;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;

public class GraphMgrImpl extends BaseGraphManager implements IGraphManager {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphEngineActorPoolMgr.getMethod(GraphEngineManagers.GRAPH_MANAGER, methodName);
            if (null == method) {
                throw new ClientException("ERR_GRAPH_INVALID_OPERATION", "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    @Override
    public void createGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            TelemetryManager.log("Create Graph request: "+ graphId);
            Graph graph = new Graph(this, graphId);
            graph.create(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }
    
    @Override
    public void createUniqueConstraint(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
        	TelemetryManager.log("Create Unique Constraint request: " + graphId);
            Graph graph = new Graph(this, graphId);
            graph.createUniqueConstraint(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }
    
    @Override
    public void createIndex(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
        	TelemetryManager.log("Create Index request: " + graphId);
            Graph graph = new Graph(this, graphId);
            graph.createIndex(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void loadGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            Graph graph = new Graph(this, graphId);
            graph.load(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void validateGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            Graph graph = new Graph(this, graphId);
            graph.validate(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void deleteGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            Graph graph = new Graph(this, graphId);
            graph.delete(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void importGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            Graph graph = new Graph(this, graphId);
            graph.importGraph(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }
    
    @Override
    public void createTaskNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            Graph graph = new Graph(this, graphId);
            graph.createTaskNode(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void exportGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String format = (String) request.get(GraphEngineParams.format.name());
        if (!validateRequired(format)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_INVALID_FORMAT.name(), "Required parameters are missing...");
        } else {
            try {
                Graph graph = new Graph(this, graphId);
                graph.exportGraph(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

	@SuppressWarnings("unchecked")
	@Override
    public void createRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        Map<String, Object> metadata = (Map<String, Object>)  request.get(GraphDACParams.metadata.name());
        // TODO: get metadata
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(), "Required parameters are missing...");
        } else {
            try {
                IRelation relation = RelationHandler.getRelation(this, graphId, startNodeId, relationType, endNodeId, metadata);
                relation.create(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void addOutRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeIds)) {
            throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(), "Required parameters are missing...");
        } else {
            Graph graph = new Graph(this, graphId);
            graph.addOutRelations(request);
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void addInRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeIds, relationType, endNodeId)) {
            throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(), "Required parameters are missing...");
        } else {
            Graph graph = new Graph(this, graphId);
			graph.addInRelations(request);
        }
    }

    @Override
    public void removeRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        // TODO: get metadata
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(), "Required parameters are missing...");
        } else {
            try {
                IRelation relation = RelationHandler.getRelation(this, graphId, startNodeId, relationType, endNodeId, null);
                relation.delete(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    public void deleteCacheNodesProperty(Request request) {
        try {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            String property = (String) request.get(GraphDACParams.property.name());

            if (!validateRequired(graphId, property)) {
                throw new ClientException(GraphCacheErrorCodes.ERR_CACHE_SAVE_PROPERTY_ERROR.name(), "Required parameters are missing...");
            } else {
    			RedisStoreUtil.deleteAllNodeProperty(graphId, property);
            }

            OK(getSender());
        } catch (Exception e) {
            ERROR(e, getSender());
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void bulkUpdateNodes(Request request) {
    	String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
    	List<Node> nodes = (List<Node>) request.get(GraphDACParams.nodes.name());
    	if (null != nodes && !nodes.isEmpty()) {
    		try {
    			Graph graph = new Graph(this, graphId);
                graph.bulkUpdateNodes(request);
    		} catch (Exception e) {
                handleException(e, getSender());
            }
    	} else {
    		OK(getSender());
    	}
    }

    @Override
    public void updateDefinitionCache(Request request) {
        Map<String, Object> req = request.getRequest();
        String graphId = req.getOrDefault(GraphDACParams.graphId.name(), "").toString();
        String objectType = req.getOrDefault(GraphDACParams.objectType.name(), "").toString();
        DefinitionCache.updateDefinitionCache(graphId, objectType);
        OK(getSender());
    }
}
