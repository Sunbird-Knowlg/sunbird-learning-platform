package com.ilimi.graph.model.node;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.Property;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.exception.GraphRelationErrorCodes;
import com.ilimi.graph.model.IRelation;
import com.ilimi.graph.model.relation.RelationHandler;

public class DataNode extends AbstractNode {

    private String objectType;
    private List<Relation> inRelations;
    private List<Relation> outRelations;

    public DataNode(BaseGraphManager manager, String graphId, String nodeId, String objectType, Map<String, Object> metadata) {
        super(manager, graphId, nodeId, metadata);
        this.objectType = objectType;
    }

    public DataNode(BaseGraphManager manager, String graphId, Node node) {
        super(manager, graphId, node.getIdentifier(), node.getMetadata());
        this.objectType = node.getObjectType();
        this.inRelations = node.getInRelations();
        this.outRelations = node.getOutRelations();
    }

    @Override
    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
        node.setMetadata(this.metadata);
        node.setInRelations(inRelations);
        node.setOutRelations(outRelations);
        return node;
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.DATA_NODE.name();
    }

    @Override
    public String getFunctionalObjectType() {
        return this.objectType;
    }

    @Override
    public void removeProperty(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
            request.setOperation("removePropertyValue");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    @Override
    public void setProperty(Request req) {
        Property property = (Property) req.get(GraphDACParams.METADATA.name());
        if (!manager.validateRequired(property)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            checkMetadata(property.getPropertyName(), property.getPropertyValue());
            try {
                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                request.setOperation("updatePropertyValue");
                request.copyRequestValueObjects(req.getRequest());
                Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                manager.returnResponse(response, getParent());
            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    @Override
    public void create(final Request req) {
        try {
            checkMetadata(metadata);
            Future<Map<String, List<String>>> aggregate = validateNode(req);
            aggregate.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                @Override
                public void onSuccess(Map<String, List<String>> messages) throws Throwable {
                    List<StringValue> errMessages = getErrorMessages(messages);
                    if (null == errMessages || errMessages.isEmpty()) {
                        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                        Request request = new Request(req);
                        request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                        request.setOperation("addNode");
                        request.put(GraphDACParams.NODE.name(), toNode());
                        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                        validateNodeRelations(toNode(), response, request);
                    } else {
                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_ERROR.name(), "Node validation failed",
                                ResponseCode.CLIENT_ERROR, GraphDACParams.MESSAGES.name(),
                                new BaseValueObjectList<StringValue>(errMessages), getParent());
                    }
                }
            }, manager.getContext().dispatcher());
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    @Override
    public void updateMetadata(final Request req) {
        final Node node = (Node) req.get(GraphDACParams.NODE.name());
        if (!manager.validateRequired(node)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), "Required parameters are missing...");
        } else {
            try {
                checkMetadata(metadata);
                final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
                request.setOperation("getNodeByUniqueId");
                request.put(GraphDACParams.NODE_ID.name(), new StringValue(getNodeId()));
                Future<Object> nodeFuture = Patterns.ask(dacRouter, request, timeout);
                nodeFuture.onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                        boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), "Failed to get data node");
                        if (valid) {
                            Response res = (Response) arg1;
                            Node dbNode = (Node) res.get(GraphDACParams.NODE.name());
                            if (null == dbNode || StringUtils.isNotBlank(dbNode.getNodeType())
                                    || !StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), dbNode.getNodeType())) {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), "Failed to get data node",
                                        ResponseCode.RESOURCE_NOT_FOUND, getParent());
                            } else {
                                if (null == metadata)
                                    metadata = new HashMap<String, Object>();
                                Map<String, Object> dbMetadata = dbNode.getMetadata();
                                if (null != dbMetadata && !dbMetadata.isEmpty()) {
                                    metadata.putAll(dbMetadata);
                                }
                                Future<Map<String, List<String>>> aggregate = validateNode(req);
                                aggregate.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                                    @Override
                                    public void onSuccess(Map<String, List<String>> messages) throws Throwable {
                                        List<StringValue> errMessages = getErrorMessages(messages);
                                        if (null == errMessages || errMessages.isEmpty()) {
                                            Request request = new Request(req);
                                            request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                                            request.setOperation("updateNode");
                                            request.put(GraphDACParams.NODE.name(), toNode());
                                            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                                            validateNodeRelations(node, response, request);
                                        } else {
                                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(),
                                                    "Metadata validation failed", ResponseCode.CLIENT_ERROR,
                                                    GraphDACParams.MESSAGES.name(), new BaseValueObjectList<StringValue>(errMessages),
                                                    getParent());
                                        }
                                    }
                                }, manager.getContext().dispatcher());
                            }
                        }
                    }
                }, manager.getContext().dispatcher());
            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    private void validateNodeRelations(final Node node, final Future<Object> response, final Request request) {
        response.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                        GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), "Failed to update node");
                if (valid) {
                    validateRelations(node.getInRelations(), request);
                    validateRelations(node.getOutRelations(), request);
                    manager.OK(getParent());
                }
            }
        }, manager.getContext().dispatcher());
    }

    private void validateRelations(List<Relation> relations, final Request request) {
        if (null != relations) {
            for (final Relation rel : relations) {
                try {
                    IRelation relation = RelationHandler.getRelation(manager, graphId, rel.getStartNodeId(), rel.getRelationType(),
                            getNodeId());
                    Future<Map<String, List<String>>> aggregate = relation.validateRelation(request);
                    aggregate.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                        @Override
                        public void onSuccess(Map<String, List<String>> messageMap) throws Throwable {
                            List<StringValue> errMessages = getErrorMessages(messageMap);
                            if (null != errMessages && !errMessages.isEmpty()) {
                                deleteRelation(rel, request);
                            }
                        }
                    }, manager.getContext().dispatcher());
                } catch (Exception e) {
                    deleteRelation(rel, request);
                }
            }
        }
    }

    private void deleteRelation(Relation rel, final Request req) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
        request.setOperation("deleteRelation");
        request.put(GraphDACParams.START_NODE_ID.name(), new StringValue(rel.getStartNodeId()));
        request.put(GraphDACParams.RELATION_TYPE.name(), new StringValue(rel.getRelationType()));
        request.put(GraphDACParams.END_NODE_ID.name(), new StringValue(rel.getEndNodeId()));
        dacRouter.tell(request, getParent());
    }

    @Override
    public Future<Map<String, List<String>>> validateNode(Request req) {
        try {
            final ExecutionContext ec = manager.context().dispatcher();
            final List<String> messages = new ArrayList<String>();
            if (StringUtils.isBlank(objectType)) {
                messages.add("Object type not set for node: " + getNodeId());
                Future<List<String>> message = Futures.successful(messages);
                return getMessageMap(message, ec);
            } else {
                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
                request.setOperation("getNodeByUniqueId");
                request.put(GraphDACParams.NODE_ID.name(), new StringValue(SystemNodeTypes.DEFINITION_NODE.name() + "_" + this.objectType));
                Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                Future<List<String>> props = response.map(new Mapper<Object, List<String>>() {
                    @Override
                    public List<String> apply(Object parameter) {
                        if (null != parameter && parameter instanceof Response) {
                            Response res = (Response) parameter;
                            Node node = (Node) res.get(GraphDACParams.NODE.name());
                            if (null != node) {
                                DefinitionNode definitionNode = new DefinitionNode(getManager(), node);
                                validateMetadata(definitionNode.getIndexedMetadata(), messages);
                                validateMetadata(definitionNode.getNonIndexedMetadata(), messages);
                                List<RelationDefinition> inRelDefs = definitionNode.getInRelations();
                                validateRelations(inRelDefs, "incoming", messages);
                                List<RelationDefinition> outRelDefs = definitionNode.getOutRelations();
                                validateRelations(outRelDefs, "outgoing", messages);
                            } else {
                                messages.add("Definition node not found for Object Type: " + objectType);
                            }
                        } else {
                            messages.add("Definition node not found for Object Type: " + objectType);
                        }
                        return messages;
                    }
                }, manager.getContext().dispatcher());
                return getMessageMap(props, ec);
            }
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
        }
    }

    public List<String> validateNode(Map<String, Node> defNodesMap) {
        try {
            List<String> messages = new ArrayList<String>();
            if (StringUtils.isBlank(objectType)) {
                messages.add("Object type not set for node: " + getNodeId());
            } else {
                if (null == defNodesMap)
                    defNodesMap = new HashMap<String, Node>();
                Node defNode = defNodesMap.get(objectType);
                if (null == defNode) {
                    messages.add("Definition node not found for Object Type: " + objectType);
                } else {
                    DefinitionNode definitionNode = new DefinitionNode(getManager(), defNode);
                    validateMetadata(definitionNode.getIndexedMetadata(), messages);
                    validateMetadata(definitionNode.getNonIndexedMetadata(), messages);
                    List<RelationDefinition> inRelDefs = definitionNode.getInRelations();
                    validateRelations(inRelDefs, "incoming", messages);
                    List<RelationDefinition> outRelDefs = definitionNode.getOutRelations();
                    validateRelations(outRelDefs, "outgoing", messages);
                }
            }
            return messages;
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
        }
    }

    private void validateRelations(List<RelationDefinition> relDefs, String direction, List<String> messages) {
        List<RelationDefinition> requiredRels = new ArrayList<RelationDefinition>();
        if (null != relDefs && !relDefs.isEmpty()) {
            for (RelationDefinition def : relDefs) {
                if (def.isRequired()) {
                    requiredRels.add(def);
                }
            }
        }
        List<Relation> rels = null;
        if (StringUtils.equals("incoming", direction)) {
            rels = getInRelations();
        } else {
            rels = getOutRelations();
        }
        if (null != rels && !rels.isEmpty()) {
            for (Relation rel : rels) {
                if (!RelationTypes.isValidRelationType(rel.getRelationType()))
                    messages.add("Relation " + rel.getRelationType() + " is not supported");
            }
        }
        if (!requiredRels.isEmpty()) {
            if (null == rels || rels.isEmpty()) {
                messages.add("Required " + direction + " relations are missing");
            } else {
                for (RelationDefinition def : requiredRels) {
                    boolean found = false;
                    for (Relation rel : rels) {
                        if (StringUtils.equals(def.getRelationName(), rel.getRelationType())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        messages.add("Required " + direction + " incoming relation " + def.getRelationName() + " is missing");
                    }
                }
            }
        }
    }

    private void validateMetadata(List<MetadataDefinition> defs, List<String> messages) {
        if (null != defs && !defs.isEmpty()) {
            for (MetadataDefinition def : defs) {
                String propName = def.getPropertyName();
                if (StringUtils.isNotBlank(propName)) {
                    Object value = getPropertyValue(propName);
                    if (def.isRequired() && null == value) {
                        messages.add("Required Metadata " + propName + " not set");
                    } else {
                        checkDataType(value, def, messages);
                    }
                }
            }
        }
    }

    private Object getPropertyValue(String propName) {
        if (null != metadata && !metadata.isEmpty() && StringUtils.isNotBlank(propName)) {
            return metadata.get(propName);
        }
        return null;
    }

    private void checkDataType(Object value, MetadataDefinition def, List<String> messages) {
        if (null != value) {
            String propName = def.getPropertyName();
            String dataType = def.getDataType();
            List<Object> range = def.getRange();
            if (StringUtils.equalsIgnoreCase("string", dataType) && !(value instanceof String)) {
                messages.add("Metadata " + propName + " should be a String value");
            } else if (StringUtils.equalsIgnoreCase("boolean", dataType) && !(value instanceof Boolean)) {
                messages.add("Metadata " + propName + " should be a Boolean value");
            } else if (StringUtils.equalsIgnoreCase("number", dataType) && !(value instanceof Number)) {
                messages.add("Metadata " + propName + " should be a Numeric value");
            } else if (StringUtils.equalsIgnoreCase("select", dataType)) {
                if (null == range || !range.contains(value))
                    messages.add("Metadata " + propName + " should be one of: " + range);
            } else if (StringUtils.equalsIgnoreCase("multi-select", dataType)) {
                if (null == range) {
                    messages.add("Metadata " + propName + " should be one of: " + range);
                } else {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i++) {
                        if (!range.contains(Array.get(value, i))) {
                            messages.add("Metadata " + propName + " should be one of: " + range);
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<Relation> getInRelations() {
        return inRelations;
    }

    public void setInRelations(List<Relation> inRelations) {
        this.inRelations = inRelations;
    }

    public List<Relation> getOutRelations() {
        return outRelations;
    }

    public void setOutRelations(List<Relation> outRelations) {
        this.outRelations = outRelations;
    }

}
