package com.ilimi.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.mgr.INodeManager;
import com.ilimi.graph.engine.router.GraphEngineActorPoolMgr;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.Graph;
import com.ilimi.graph.model.node.DataNode;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.DefinitionNode;
import com.ilimi.graph.model.node.MetadataDefinition;

public class NodeManagerImpl extends BaseGraphManager implements INodeManager {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphEngineActorPoolMgr.getMethod(GraphEngineManagers.NODE_MANAGER, methodName);
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
    public void saveDefinitionNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        DefinitionDTO definition = (DefinitionDTO) request.get(GraphDACParams.DEFINITION_NODE.name());
        if (!validateRequired(definition) || StringUtils.isBlank(definition.getObjectType())) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            try {
                List<MetadataDefinition> indexedMetadata = new ArrayList<MetadataDefinition>();
                List<MetadataDefinition> nonIndexedMetadata = new ArrayList<MetadataDefinition>();
                if (null != definition.getProperties() && !definition.getProperties().isEmpty()) {
                    for (MetadataDefinition def : definition.getProperties()) {
                        if (def.isIndexed()) {
                            indexedMetadata.add(def);
                        } else {
                            nonIndexedMetadata.add(def);
                        }
                    }
                }
                DefinitionNode node = new DefinitionNode(this, graphId, definition.getObjectType(), indexedMetadata, nonIndexedMetadata,
                        definition.getInRelations(), definition.getOutRelations(), definition.getSystemTags());
                node.create(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void createDataNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        Node node = (Node) request.get(GraphDACParams.NODE.name());
        if (!validateRequired(node)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            try {
                DataNode datanode = new DataNode(this, graphId, node);
                datanode.create(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void updateDataNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        Node node = (Node) request.get(GraphDACParams.NODE.name());
        if (!validateRequired(nodeId, node)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            try {
                node.setIdentifier(nodeId.getId());
                DataNode datanode = new DataNode(this, graphId, node);
                datanode.updateMetadata(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void deleteDataNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            try {
                DataNode node = new DataNode(this, graphId, nodeId.getId(), null, null);
                node.delete(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void deleteDefinition(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            try {
                DefinitionNode node = new DefinitionNode(this, graphId, objectType.getId(), null, null, null, null, null);
                node.delete(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void importDefinitions(final Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        try {
            Graph graph = new Graph(this, graphId);
            graph.importDefinitions(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }
}
