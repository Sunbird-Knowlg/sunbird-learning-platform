package com.ilimi.graph.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import scala.Tuple2;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;

import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.Property;
import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.dto.Status.StatusType;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ResponseCode;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.importer.ImportData;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;
import com.ilimi.graph.model.collection.Tag;
import com.ilimi.graph.model.node.DataNode;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.DefinitionNode;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.graph.model.relation.RelationHandler;
import com.ilimi.graph.reader.CSVImportMessageHandler;
import com.ilimi.graph.reader.GraphReader;
import com.ilimi.graph.reader.GraphReaderFactory;
import com.ilimi.graph.reader.JsonGraphReader;
import com.ilimi.graph.writer.GraphWriterFactory;
import com.ilimi.graph.writer.RDFGraphWriter;

public class Graph extends AbstractDomainObject {

    public static final String ERROR_MESSAGES = "ERROR_MESSAGES";

    public Graph(BaseGraphManager manager, String graphId) {
        super(manager, graphId);
    }

    public void create(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
            request.setOperation("createGraph");
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void load(Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            // get all definition nodes
            final Request defNodesReq = new Request(req);
            defNodesReq.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            defNodesReq.setOperation("getNodesByProperty");
            Property defNodeProperty = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name());
            defNodesReq.put(GraphDACParams.METADATA.name(), defNodeProperty);
            Future<Object> defNodesResponse = Patterns.ask(dacRouter, defNodesReq, timeout);
            defNodesResponse.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                    if (null != arg0) {
                        manager.handleException(arg0, getParent());
                    } else {
                        if (arg1 instanceof Response) {
                            Response res = (Response) arg1;
                            BaseValueObjectList<Node> defNodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST.name());
                            if (null != defNodes && null != defNodes.getValueObjectList() && !defNodes.getValueObjectList().isEmpty()) {
                                ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                                for (Node defNode : defNodes.getValueObjectList()) {
                                    DefinitionNode node = new DefinitionNode(manager, defNode);
                                    node.loadToCache(cacheRouter, defNodesReq);
                                }
                                manager.OK(getParent());
                            } else {
                                manager.OK(getParent());
                            }
                        } else {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_LOAD_GRAPH_UNKNOWN_ERROR.name(), "Failed to get definition nodes",
                                    ResponseCode.SERVER_ERROR, getParent());
                        }
                    }
                }
            }, ec);
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void validate(final Request req) {
        Future<Map<String, List<String>>> validationMap = validateGraph(req);
        final ExecutionContext ec = manager.getContext().dispatcher();
        validationMap.onComplete(new OnComplete<Map<String, List<String>>>() {
            @Override
            public void onComplete(Throwable arg0, Map<String, List<String>> map) throws Throwable {
                if (null != arg0) {
                    List<String> messages = new ArrayList<String>();
                    messages.add(arg0.getMessage());
                    Map<String, List<String>> errorMap = new HashMap<String, List<String>>();
                    errorMap.put(ERROR_MESSAGES, messages);
                    manager.OK(GraphDACParams.MESSAGES.name(), new BaseValueObjectMap<List<String>>(errorMap), getParent());
                } else {
                    manager.OK(GraphDACParams.MESSAGES.name(), new BaseValueObjectMap<List<String>>(map), getParent());
                }
            }
        }, ec);
    }

    private Future<Map<String, List<String>>> validateGraph(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();

            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            // get all definition nodes
            Request defNodesReq = new Request(req);
            defNodesReq.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            defNodesReq.setOperation("getNodesByProperty");
            Property defNodeProperty = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name());
            defNodesReq.put(GraphDACParams.METADATA.name(), defNodeProperty);
            Future<Object> defNodesResponse = Patterns.ask(dacRouter, defNodesReq, timeout);

            // get all data nodes
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("getNodesByProperty");
            Property property = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name());
            request.put(GraphDACParams.METADATA.name(), property);
            Future<Object> dataNodesResponse = Patterns.ask(dacRouter, request, timeout);

            // get all relations
            Request relsRequest = new Request(req);
            relsRequest.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            relsRequest.setOperation("getAllRelations");
            Future<Object> relationsResponse = Patterns.ask(dacRouter, relsRequest, timeout);

            // List<Future<List<String>>> validationMessages = new
            // ArrayList<Future<List<String>>>();
            List<Future<Map<String, List<String>>>> validationMessages = new ArrayList<Future<Map<String, List<String>>>>();

            // Promise to get all relation validation messages
            final Promise<Map<String, List<String>>> relationsPromise = Futures.promise();
            Future<Map<String, List<String>>> relationMessages = relationsPromise.future();
            getRelationValidationsFuture(relationsResponse, relationsPromise, ec, relsRequest);
            validationMessages.add(relationMessages);

            // get future of all node validation messages
            final Promise<Map<String, List<String>>> nodesPromise = Futures.promise();
            Future<Map<String, List<String>>> nodeMessages = nodesPromise.future();
            getNodesValidationsFuture(defNodesResponse, dataNodesResponse, nodesPromise, ec, request);
            validationMessages.add(nodeMessages);

            Future<Iterable<Map<String, List<String>>>> validationsFuture = Futures.sequence(validationMessages, ec);
            Future<Map<String, List<String>>> validationMap = validationsFuture.map(
                    new Mapper<Iterable<Map<String, List<String>>>, Map<String, List<String>>>() {
                        @Override
                        public Map<String, List<String>> apply(Iterable<Map<String, List<String>>> parameter) {
                            Map<String, List<String>> errorMap = new HashMap<String, List<String>>();
                            if (null != parameter) {
                                for (Map<String, List<String>> map : parameter) {
                                    if (null != map && !map.isEmpty()) {
                                        for (Entry<String, List<String>> entry : map.entrySet()) {
                                            if (null != entry.getValue() && !entry.getValue().isEmpty()) {
                                                List<String> list = errorMap.get(entry.getKey());
                                                if (null == list) {
                                                    list = new ArrayList<String>();
                                                    errorMap.put(entry.getKey(), list);
                                                }
                                                list.addAll(entry.getValue());
                                            }
                                        }
                                    }
                                }
                            }
                            return errorMap;
                        }
                    }, ec);

            return validationMap;
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_VALIDATE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void delete(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
            request.setOperation("deleteGraph");
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_DELETE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void importGraph(final Request request) {
        try {
            final String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            final StringValue format = (StringValue) request.get(GraphEngineParams.FORMAT.name());
            final InputStreamValue inputStream = (InputStreamValue) request.get(GraphEngineParams.INPUT_STREAM.name());
            if(StringUtils.isBlank(graphId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_INVALID_GRAPH_ID.name(), "GraphId is missing");
            }
            if (!manager.validateRequired(inputStream)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_INVALID_INPUTSTREAM.name(), "Import stream is missing");
            } else {
                // Get byte array.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(inputStream.getInputStream(), baos);
                byte[] bytes = baos.toByteArray();
                inputStream.setInputStream(new ByteArrayInputStream(bytes));
                final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

                final ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                final ExecutionContext ec = manager.getContext().dispatcher();

                // Fetch Definition Nodes
                final Request defNodesReq = new Request(request);
                defNodesReq.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
                defNodesReq.setOperation("getNodesByProperty");
                Property defNodeProperty = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name());
                defNodesReq.put(GraphDACParams.METADATA.name(), defNodeProperty);
                Future<Object> defNodesResponse = Patterns.ask(dacRouter, defNodesReq, timeout);

                // Create Definition Nodes Property Map from Future.
                Future<Map<String, Map<String, MetadataDefinition>>> propDataMapFuture = defNodesResponse.map(
                        new Mapper<Object, Map<String, Map<String, MetadataDefinition>>>() {
                            @Override
                            public Map<String, Map<String, MetadataDefinition>> apply(Object parameter) {
                                Map<String, Map<String, MetadataDefinition>> propertyDataMap = new HashMap<String, Map<String, MetadataDefinition>>();
                                if (parameter instanceof Response) {
                                    Response res = (Response) parameter;
                                    BaseValueObjectList<Node> defNodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST
                                            .name());
                                    List<DefinitionNode> defNodesList = new ArrayList<DefinitionNode>();
                                    for (Node defNode : defNodes.getValueObjectList()) {
                                        DefinitionNode node = new DefinitionNode(manager, defNode);
                                        defNodesList.add(node);
                                    }
                                    propertyDataMap = getPropertyDataMap(defNodesList);
                                }
                                return propertyDataMap;
                            }

                            private Map<String, Map<String, MetadataDefinition>> getPropertyDataMap(List<DefinitionNode> defNodesList) {
                                Map<String, Map<String, MetadataDefinition>> propertyDataMap = new HashMap<String, Map<String, MetadataDefinition>>();
                                for (DefinitionNode node : defNodesList) {
                                    String objectType = node.getFunctionalObjectType();
                                    Map<String, MetadataDefinition> propMap = new HashMap<String, MetadataDefinition>();
                                    List<MetadataDefinition> indexedMeta = node.getIndexedMetadata();
                                    if (indexedMeta != null) {
                                        for (MetadataDefinition propDef : indexedMeta) {
                                            propMap.put(propDef.getTitle(), propDef);
                                        }
                                    }
                                    List<MetadataDefinition> nonIndexedMeta = node.getNonIndexedMetadata();
                                    if (nonIndexedMeta != null) {
                                        for (MetadataDefinition propDef : nonIndexedMeta) {
                                            propMap.put(propDef.getTitle(), propDef);
                                        }
                                    }
                                    propertyDataMap.put(objectType, propMap);
                                }
                                return propertyDataMap;
                            }

                        }, ec);

                // Import inputStream and get outputStream with Validations.
                propDataMapFuture.onComplete(new OnComplete<Map<String, Map<String, MetadataDefinition>>>() {

                    @Override
                    public void onComplete(Throwable arg0, Map<String, Map<String, MetadataDefinition>> propertyDataMap) {
                        if (null != arg0) {
                            manager.ERROR(arg0, getParent());
                        } else {
                            try {
                                // Create ImportData object from inputStream.
                                final ImportData importData = GraphReaderFactory.getObject(getManager(), format.getId(), graphId,
                                        inputStream.getInputStream(), propertyDataMap);
                                request.put(GraphDACParams.IMPORT_INPUT_OBJECT.name(), importData);

                                // Use ImportData object and import Graph.
                                request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
                                request.setOperation("importGraph");
                                Future<Object> importResponse = Patterns.ask(dacRouter, request, timeout);
                                importResponse.onComplete(new OnComplete<Object>() {
                                    @Override
                                    public void onComplete(Throwable throwable, Object arg1) throws Throwable {
                                        Response actorResponse = (Response) arg1;
                                        if (throwable != null) {
                                            manager.ERROR(throwable, getParent());
                                        } else {
                                            Status status = (Status) actorResponse.getStatus();
                                            if (StatusType.ERROR.name().equals(status.getStatus())) {
                                                getParent().tell(actorResponse, manager.getSelf());
                                            } else {
                                                BaseValueObjectMap importMsgBVMap = (BaseValueObjectMap) actorResponse
                                                        .get(GraphDACParams.MESSAGES.name());
                                                final Map<String, List<String>> importMsgMap = importMsgBVMap.getBaseValueMap();

                                                // Create Tag Nodes.
                                                Map<String, List<StringValue>> tagMembersMap = importData.getTagMembersMap();
                                                if (tagMembersMap != null) {
                                                    for (String tagName : tagMembersMap.keySet()) {
                                                        Request tagRequest = new Request(request);
                                                        Tag tag = new Tag(manager, graphId, tagName, null, tagMembersMap.get(tagName));
                                                        tag.createTag(tagRequest);
                                                    }
                                                }

                                                // Validate Graph.
                                                Future<Map<String, List<String>>> validationMap = validateGraph(request);
                                                validationMap.onComplete(new OnComplete<Map<String, List<String>>>() {
                                                    @Override
                                                    public void onComplete(Throwable throwable, Map<String, List<String>> validateMsgMap)
                                                            throws Throwable {
                                                        if (throwable != null) {
                                                            manager.ERROR(throwable, getParent());
                                                        } else {
                                                            for (String rowIdentifier : importMsgMap.keySet()) {
                                                                if (validateMsgMap.containsKey(rowIdentifier)) {
                                                                    List<String> msgs = new ArrayList<String>(importMsgMap
                                                                            .get(rowIdentifier));
                                                                    msgs.addAll(validateMsgMap.get(rowIdentifier));
                                                                    validateMsgMap.put(rowIdentifier, msgs);
                                                                } else {
                                                                    List<String> msgs = new ArrayList<String>(importMsgMap
                                                                            .get(rowIdentifier));
                                                                    validateMsgMap.put(rowIdentifier, msgs);
                                                                }
                                                            }
                                                            CSVImportMessageHandler msgHandler = new CSVImportMessageHandler(
                                                                    byteInputStream);
                                                            OutputStream outputStream = msgHandler.getOutputStream(validateMsgMap);
                                                            manager.OK(GraphEngineParams.OUTPUT_STREAM.name(), new OutputStreamValue(
                                                                    outputStream), getParent());
                                                        }
                                                    }

                                                }, ec);
                                            }
                                        }
                                    }
                                }, ec);
                            } catch (Exception e) {
                                manager.ERROR(e, getParent());
                            }
                        }
                    }
                }, ec);

            }
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void searchNodes(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("searchNodes");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Future<List<Map<String, Object>>> executeQuery(Request req, String query, Map<String, Object> params) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("executeQuery");
            request.put(GraphDACParams.QUERY.name(), new StringValue(query));
            if (null != params && !params.isEmpty())
                request.put(GraphDACParams.PARAMS.name(), new BaseValueObjectMap<Object>(params));
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            Future<List<Map<String, Object>>> future = response.map(new Mapper<Object, List<Map<String, Object>>>() {
                @Override
                public List<Map<String, Object>> apply(Object parameter) {
                    if (null != parameter && parameter instanceof Response) {
                        Response res = (Response) parameter;
                        BaseValueObjectList<BaseValueObjectMap<Object>> resultMap = (BaseValueObjectList<BaseValueObjectMap<Object>>) res
                                .get(GraphDACParams.RESULTS.name());
                        if (null != resultMap && null != resultMap.getValueObjectList() && !resultMap.getValueObjectList().isEmpty()) {
                            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
                            for (BaseValueObjectMap<Object> map : resultMap.getValueObjectList()) {
                                if (null != map && null != map.getBaseValueMap() && !map.getBaseValueMap().isEmpty()) {
                                    Map<String, Object> row = map.getBaseValueMap();
                                    result.add(row);
                                }
                            }
                            return result;
                        }
                    }
                    return null;
                }
            }, manager.getContext().dispatcher());
            return future;
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void getNodesByObjectType(Request req) {
        StringValue objectType = (StringValue) req.get(GraphDACParams.OBJECT_TYPE.name());
        if (!manager.validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_MISSING_REQ_PARAMS.name(),
                    "Object Type is required for GetNodesByObjectType API");
        } else {
            try {
                ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                Request request = new Request(req);
                request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
                request.setOperation("getNodesByProperty");
                request.copyRequestValueObjects(req.getRequest());
                Property property = new Property(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), objectType.getId());
                request.put(GraphDACParams.METADATA.name(), property);
                Future<Object> response = Patterns.ask(dacRouter, request, timeout);
                response.onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                        boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), "Failed to get nodes");
                        if (valid) {
                            Response res = (Response) arg1;
                            BaseValueObjectList<Node> nodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST.name());
                            if (null != nodes && null != nodes.getValueObjectList() && !nodes.getValueObjectList().isEmpty()) {
                                List<Node> nodeList = new ArrayList<Node>();
                                for (Node node : nodes.getValueObjectList()) {
                                    if (null != node && StringUtils.isNotBlank(node.getNodeType())
                                            && StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())) {
                                        nodeList.add(node);
                                    }
                                }
                                manager.OK(GraphDACParams.NODE_LIST.name(), new BaseValueObjectList<Node>(nodeList), getParent());
                            } else {
                                manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(), "Failed to get data nodes",
                                        ResponseCode.RESOURCE_NOT_FOUND, getParent());
                            }
                        }
                    }
                }, manager.getContext().dispatcher());

            } catch (Exception e) {
                throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
            }
        }
    }

    public void getDataNode(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("getNodeByUniqueId");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            response.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                    boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                            GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), "Failed to get data node");
                    if (valid) {
                        Response res = (Response) arg1;
                        Node node = (Node) res.get(GraphDACParams.NODE.name());
                        if (null == node || StringUtils.isBlank(node.getNodeType())
                                || !StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())) {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(), "Failed to get data node",
                                    ResponseCode.RESOURCE_NOT_FOUND, getParent());
                        } else {
                            manager.OK(GraphDACParams.NODE.name(), node, getParent());
                        }
                    }
                }
            }, manager.getContext().dispatcher());

        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void getDataNodes(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("getNodesByUniqueIds");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void getDefinitionNode(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("searchNodes");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            response.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                    boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                            GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), "Failed to get definition node");
                    if (valid) {
                        Response res = (Response) arg1;
                        BaseValueObjectList<Node> nodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST.name());
                        if (null != nodes && null != nodes.getValueObjectList() && !nodes.getValueObjectList().isEmpty()) {
                            Node node = nodes.getValueObjectList().get(0);
                            DefinitionNode defNode = new DefinitionNode(manager, node);
                            DefinitionDTO definition = defNode.getValueObject();
                            manager.OK(GraphDACParams.DEFINITION_NODE.name(), definition, getParent());
                        } else {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(), "Failed to get definition node",
                                    ResponseCode.RESOURCE_NOT_FOUND, getParent());
                        }
                    }
                }
            }, manager.getContext().dispatcher());

        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void getDefinitionNodes(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("searchNodes");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            response.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                    boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                            GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), "Failed to get definition node");
                    if (valid) {
                        Response res = (Response) arg1;
                        BaseValueObjectList<Node> nodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST.name());
                        if (null != nodes && null != nodes.getValueObjectList() && !nodes.getValueObjectList().isEmpty()) {
                            List<DefinitionDTO> definitions = new ArrayList<DefinitionDTO>();
                            for (Node node : nodes.getValueObjectList()) {
                                DefinitionNode defNode = new DefinitionNode(manager, node);
                                DefinitionDTO definition = defNode.getValueObject();
                                definitions.add(definition);
                            }
                            manager.OK(GraphDACParams.DEFINITION_NODES.name(), new BaseValueObjectList<DefinitionDTO>(definitions),
                                    getParent());
                        } else {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(), "Failed to get definition node",
                                    ResponseCode.RESOURCE_NOT_FOUND, getParent());
                        }
                    }
                }
            }, manager.getContext().dispatcher());

        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void getNodesCount(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("getNodesCount");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void traverse(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("traverse");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void getSubGraph(Request req) {
        try {
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            Request request = new Request(req);
            request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            request.setOperation("getSubGraph");
            request.copyRequestValueObjects(req.getRequest());
            Future<Object> response = Patterns.ask(dacRouter, request, timeout);
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    public void importDefinitions(final Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue json = (StringValue) request.get(GraphEngineParams.INPUT_STREAM.name());
        if (!manager.validateRequired(json)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(), "Input JSON is blank");
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                GraphReader graphReader = new JsonGraphReader(manager, mapper, graphId, json.getId());
                if (graphReader.getValidations().size() > 0) {
                    String validations = mapper.writeValueAsString(graphReader.getValidations());
                    throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_VALIDATION_FAILED.name(), validations);
                }
                ImportData inputData = new ImportData(graphReader.getDefinitionNodes(), graphReader.getDataNodes(),
                        graphReader.getRelations(), graphReader.getTagMembersMap());
                final List<Node> nodes = inputData.getDefinitionNodes();
                if (null == nodes || nodes.isEmpty()) {
                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(), "Definition nodes list is empty",
                            ResponseCode.CLIENT_ERROR, getParent());
                } else {
                    final ExecutionContext ec = manager.getContext().dispatcher();
                    Map<String, List<String>> messageMap = new HashMap<String, List<String>>();
                    final List<DefinitionNode> defNodes = new ArrayList<DefinitionNode>();
                    for (Node node : nodes) {
                        node.setGraphId(graphId);
                        DefinitionNode defNode = new DefinitionNode(manager, node);
                        defNodes.add(defNode);
                        List<String> defNodeValidation = defNode.validateDefinitionNode();
                        if (null != defNodeValidation && !defNodeValidation.isEmpty()) {
                            messageMap.put(defNode.getNodeId(), defNodeValidation);
                        }
                    }
                    if (null == messageMap || messageMap.isEmpty()) {
                        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
                        List<Future<Object>> futures = new ArrayList<Future<Object>>();
                        final Request req = new Request(request);
                        req.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
                        req.setOperation("importNodes");
                        req.put(GraphDACParams.NODE_LIST.name(), new BaseValueObjectList<Node>(nodes));
                        Future<Object> response = Patterns.ask(dacRouter, req, timeout);
                        futures.add(response);
                        response.onComplete(new OnComplete<Object>() {
                            @Override
                            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                                boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                                        GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_FAILED_TO_CREATE.name(), "Definition nodes creation error");
                                if (valid) {
                                    ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
                                    for (DefinitionNode defNode : defNodes) {
                                        defNode.loadToCache(cacheRouter, request);
                                    }
                                    manager.OK(getParent());
                                }
                            }
                        }, ec);
                    } else {
                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_VALIDATION_FAILED.name(), "Definition nodes validation error",
                                ResponseCode.CLIENT_ERROR, GraphDACParams.MESSAGES.name(),
                                new BaseValueObjectMap<List<String>>(messageMap), getParent());
                    }
                }
            } catch (Exception e) {
                throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_UNKNOWN_ERROR.name(), e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void exportGraph(final Request request) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
            final StringValue format = (StringValue) request.get(GraphEngineParams.FORMAT.name());

            Request nodesReq = new Request(request);
            nodesReq.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            nodesReq.setOperation("getAllNodes");
            Future<Object> nodesResponse = Patterns.ask(dacRouter, nodesReq, timeout);

            Request relationsReq = new Request(request);
            relationsReq.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
            relationsReq.setOperation("getAllRelations");
            Future<Object> relationsResponse = Patterns.ask(dacRouter, relationsReq, timeout);

            Future<Object> exportFuture = nodesResponse.zip(relationsResponse).map(new Mapper<Tuple2<Object, Object>, Object>() {
                @Override
                public Object apply(Tuple2<Object, Object> zipped) {
                    Response nodesResp = (Response) zipped._1();
                    if (manager.checkError(nodesResp)) {
                        String msg = manager.getErrorMessage(nodesResp);
                        if (StringUtils.isNotBlank(msg)) {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_UNKNOWN_ERROR.name(), msg, nodesResp.getResponseCode(),
                                    getParent());
                        }
                    }
                    Response relationsResp = (Response) zipped._2();
                    if (manager.checkError(nodesResp)) {
                        String msg = manager.getErrorMessage(nodesResp);
                        if (StringUtils.isNotBlank(msg)) {
                            manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_UNKNOWN_ERROR.name(), msg, relationsResp.getResponseCode(),
                                    getParent());
                        }
                    }
                    BaseValueObjectList<Node> nodesBV = (BaseValueObjectList<Node>) nodesResp.get(GraphDACParams.NODE_LIST.name());
                    List<Node> nodes = nodesBV.getValueObjectList();
                    BaseValueObjectList<Relation> relationsBV = (BaseValueObjectList<Relation>) relationsResp.get(GraphDACParams.RELATIONS
                            .name());
                    List<Relation> relations = relationsBV.getValueObjectList();
                    OutputStream outputStream = new ByteArrayOutputStream();
                    try {
                        outputStream = GraphWriterFactory.getData(format.getId(), nodes, relations);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Response response = new Response();
                    Status status = new Status();
                    status.setCode("0");
                    status.setStatus(StatusType.SUCCESS.name());
                    status.setMessage("Operation successful");
                    response.setStatus(status);
                    response.put(GraphEngineParams.OUTPUT_STREAM.name(), new OutputStreamValue(outputStream));
                    return response;
                }

            }, ec);

            manager.returnResponse(exportFuture, getParent());

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void getNodesValidationsFuture(Future<Object> defNodesResponse, final Future<Object> nodesResponse,
            final Promise<Map<String, List<String>>> nodesPromise, final ExecutionContext ec, final Request request) {

        defNodesResponse.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                final Map<String, List<String>> messages = new HashMap<String, List<String>>();
                messages.put(ERROR_MESSAGES, new ArrayList<String>());
                if (null != arg0) {
                    messages.get(ERROR_MESSAGES).add("Error getting Definition Nodes");
                    nodesPromise.success(messages);
                } else {
                    if (arg1 instanceof Response) {
                        Response res = (Response) arg1;
                        if (manager.checkError(res)) {
                            messages.get(ERROR_MESSAGES).add(manager.getErrorMessage(res));
                            nodesPromise.success(messages);
                        } else {
                            BaseValueObjectList<Node> defNodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST.name());
                            final Map<String, Node> defNodeMap = new HashMap<String, Node>();
                            if (null != defNodes && null != defNodes.getValueObjectList() && !defNodes.getValueObjectList().isEmpty()) {
                                for (Node n : defNodes.getValueObjectList()) {
                                    defNodeMap.put(n.getObjectType(), n);
                                }
                            }
                            nodesResponse.onSuccess(new OnSuccess<Object>() {
                                @Override
                                public void onSuccess(Object arg0) throws Throwable {
                                    if (arg0 instanceof Response) {
                                        Response res = (Response) arg0;
                                        if (manager.checkError(res)) {
                                            messages.get(ERROR_MESSAGES).add(manager.getErrorMessage(res));
                                            nodesPromise.success(messages);
                                        } else {
                                            BaseValueObjectList<Node> nodes = (BaseValueObjectList<Node>) res.get(GraphDACParams.NODE_LIST
                                                    .name());
                                            if (null != nodes && null != nodes.getValueObjectList()
                                                    && !nodes.getValueObjectList().isEmpty()) {
                                                for (Node node : nodes.getValueObjectList()) {
                                                    try {
                                                        DataNode datanode = new DataNode(getManager(), getGraphId(), node);
                                                        List<String> validationMsgs = datanode.validateNode(defNodeMap);
                                                        if (null != validationMsgs && !validationMsgs.isEmpty()) {
                                                            List<String> list = messages.get(node.getIdentifier());
                                                            if (null == list) {
                                                                list = new ArrayList<String>();
                                                                messages.put(node.getIdentifier(), list);
                                                            }
                                                            list.addAll(validationMsgs);
                                                        }
                                                    } catch (Exception e) {
                                                        List<String> list = messages.get(node.getIdentifier());
                                                        if (null == list) {
                                                            list = new ArrayList<String>();
                                                            messages.put(node.getIdentifier(), list);
                                                        }
                                                        list.add(e.getMessage());
                                                    }
                                                }
                                                nodesPromise.success(messages);
                                            } else {
                                                nodesPromise.success(messages);
                                            }
                                        }
                                    } else {
                                        messages.get(ERROR_MESSAGES).add("Failed to get nodes");
                                        nodesPromise.success(messages);
                                    }
                                }
                            }, ec);
                        }
                    } else {
                        messages.get(ERROR_MESSAGES).add("Error getting Definition Nodes");
                        nodesPromise.success(messages);
                    }
                }
            }
        }, ec);
    }

    @SuppressWarnings("unchecked")
    private void getRelationValidationsFuture(Future<Object> relationsResponse, final Promise<Map<String, List<String>>> relationsPromise,
            final ExecutionContext ec, final Request request) {
        relationsResponse.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object arg0) throws Throwable {
                final Map<String, List<String>> messages = new HashMap<String, List<String>>();
                messages.put(ERROR_MESSAGES, new ArrayList<String>());
                if (arg0 instanceof Response) {
                    Response res = (Response) arg0;
                    if (manager.checkError(res)) {
                        messages.get(ERROR_MESSAGES).add(manager.getErrorMessage(res));
                    } else {
                        BaseValueObjectList<Relation> rels = (BaseValueObjectList<Relation>) res.get(GraphDACParams.RELATIONS.name());
                        if (null != rels && null != rels.getValueObjectList() && !rels.getValueObjectList().isEmpty()) {
                            List<Future<Map<String, List<String>>>> msgFutures = new ArrayList<Future<Map<String, List<String>>>>();
                            for (final Relation rel : rels.getValueObjectList()) {
                                try {
                                    IRelation iRel = RelationHandler.getRelation(getManager(), rel.getGraphId(), rel.getStartNodeId(),
                                            rel.getRelationType(), rel.getEndNodeId());
                                    Future<Map<String, List<String>>> validationMsgs = iRel.validateRelation(request);
                                    msgFutures.add(validationMsgs);
                                } catch (Exception e) {
                                    List<String> list = messages.get(rel.getStartNodeId());
                                    if (null == list) {
                                        list = new ArrayList<String>();
                                        messages.put(rel.getStartNodeId(), list);
                                    }
                                    list.add(e.getMessage());
                                }
                            }
                            Future<Iterable<Map<String, List<String>>>> relFutures = Futures.sequence(msgFutures, ec);
                            relFutures.onComplete(new OnComplete<Iterable<Map<String, List<String>>>>() {
                                @Override
                                public void onComplete(Throwable arg0, Iterable<Map<String, List<String>>> arg1) throws Throwable {
                                    if (null != arg0) {
                                        relationsPromise.success(messages);
                                    } else {
                                        // add relation validation messages
                                        // to messages list
                                        if (null != arg1) {
                                            for (Map<String, List<String>> map : arg1) {
                                                if (null != map) {
                                                    for (Entry<String, List<String>> entry : map.entrySet()) {
                                                        List<String> list = messages.get(entry.getKey());
                                                        if (null == list) {
                                                            list = new ArrayList<String>();
                                                            messages.put(entry.getKey(), list);
                                                        }
                                                        list.addAll(entry.getValue());
                                                    }
                                                }
                                            }
                                        }
                                        relationsPromise.success(messages);
                                    }
                                }
                            }, ec);
                        } else {
                            relationsPromise.success(messages);
                        }
                    }
                } else {
                    messages.get(ERROR_MESSAGES).add("Failed to get relations");
                    relationsPromise.success(messages);
                }
            }
        }, ec);
    }
    
    public void exportNode(Request req) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("getNodeByUniqueId");
        request.copyRequestValueObjects(req.getRequest());
        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
        response.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = manager.checkResponseObject(arg0, arg1, getParent(),
                        GraphEngineErrorCodes.ERR_GRAPH_EXPORT_NODE_UNKNOWN_ERROR.name(), "Failed to export node.");
                if (valid) {
                    Response res = (Response) arg1;
                    Node node = (Node) res.get(GraphDACParams.NODE.name());
                    if (null == node || StringUtils.isBlank(node.getNodeType())
                            || !StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())) {
                        manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_NODE_NOT_FOUND.name(), "Failed to export node",
                                ResponseCode.RESOURCE_NOT_FOUND, getParent());
                    } else {
                        RDFGraphWriter rdfWriter = new RDFGraphWriter();
                        InputStream is = rdfWriter.getRDF(node);
                        manager.OK(GraphEngineParams.INPUT_STREAM.name(), new InputStreamValue(is), getParent());
                    }
                }
            }
        }, manager.getContext().dispatcher());
    }
}
