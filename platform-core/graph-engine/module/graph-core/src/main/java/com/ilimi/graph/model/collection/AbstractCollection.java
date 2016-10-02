package com.ilimi.graph.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.ICollection;

public abstract class AbstractCollection extends AbstractDomainObject implements ICollection {

    private String id;
    protected Map<String, Object> metadata;
    protected String memberObjectType;

    public AbstractCollection(BaseGraphManager manager, String graphId, String id, Map<String, Object> metadata) {
        super(manager, graphId);
        this.id = id;
        this.metadata = metadata;
    }
    
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        checkMetadata(this.metadata);
    }
    
    public String getMemberObjectType() {
        return memberObjectType;
    }

    @Override
    public String getNodeId() {
        return this.id;
    }

    protected void setNodeId(String id) {
        this.id = id;
    }

    @Override
    public void getProperty(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "getProperty is not supported on collections");
    }

    @Override
    public void removeProperty(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "removeProperty is not supported on collections");
    }

    @Override
    public void setProperty(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "setProperty is not supported on collections");
    }

    @Override
    public void updateMetadata(Request request) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "updateMetadata is not supported on collections");
    }

    @Override
    public void delete(Request request) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMembers(Request request) {

    }

    @Override
    public void create(Request request) {

    }

    @Override
    public void getCardinality(Request request) {
        // TODO Auto-generated method stub
    }

    @Override
    public Node toNode() {
        Node node = new Node(this.id, getSystemNodeType(), getFunctionalObjectType());
        return node;
    }

    @Override
    public String getFunctionalObjectType() {
        return null;
    }

    @Override
    public Future<Map<String, List<String>>> validateNode(Request request) {
        Future<List<String>> metadataValidation = Futures.successful(null);
        return getMessageMap(metadataValidation, manager.getContext().dispatcher());
    }

    protected Future<Map<String, List<String>>> getMessageMap(Future<List<String>> aggregate, ExecutionContext ec) {
        Future<Map<String, List<String>>> messageMap = aggregate.map(new Mapper<List<String>, Map<String, List<String>>>() {
            @Override
            public Map<String, List<String>> apply(List<String> parameter) {
                Map<String, List<String>> map = new HashMap<String, List<String>>();
                List<String> messages = new ArrayList<String>();
                if (null != parameter && !parameter.isEmpty()) {
                    messages.addAll(parameter);
                }
                map.put(getNodeId(), messages);
                return map;
            }
        }, ec);
        return messageMap;
    }

    @SuppressWarnings("unchecked")
    protected Future<Boolean> checkMemberNodes(Request req, final List<String> memberIds, final ExecutionContext ec) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("getNodesByUniqueIds");
        request.put(GraphDACParams.node_ids.name(), memberIds);
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        Future<Boolean> validMembers = dacFuture.map(new Mapper<Object, Boolean>() {
            @Override
            public Boolean apply(Object parameter) {
                if (parameter instanceof Response) {
                    Response ar = (Response) parameter;
                    List<Node> nodes = (List<Node>) ar.get(GraphDACParams.node_list.name());
                    if (manager.validateRequired(nodes)) {
                        if (memberIds.size() == nodes.size()) {
                            for (Node node : nodes) {
                                if (!StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), node.getNodeType()) && !StringUtils.equals(SystemNodeTypes.PROXY_NODE.name(), node.getNodeType()))
                                    return false;
                                if (StringUtils.isNotBlank(getMemberObjectType())) {
                                    if (!StringUtils.equals(getMemberObjectType(), node.getObjectType()))
                                        return false;
                                }
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        }, ec);
        return validMembers;
    }

    protected Future<Node> getNodeObject(Request req, ExecutionContext ec, String setId) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = getRequestObject(req, GraphDACManagers.DAC_SEARCH_MANAGER, "getNodeByUniqueId", GraphDACParams.node_id.name(),
                setId);
        Future<Object> dacFuture = Patterns.ask(dacRouter, request, timeout);
        Future<Node> nodeFuture = dacFuture.map(new Mapper<Object, Node>() {
            @Override
            public Node apply(Object parameter) {
                if (null != parameter && parameter instanceof Response) {
                    Response res = (Response) parameter;
                    Node node = (Node) res.get(GraphDACParams.node.name());
                    return node;
                }
                return null;
            }
        }, ec);
        return nodeFuture;
    }
    
    protected void checkMetadata(Map<String, Object> metadata) {
        if (null != metadata && metadata.size() > 0) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                checkMetadata(entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected void checkMetadata(String key, Object value) {
        if (SystemProperties.isSystemProperty(key)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), key + " is a reserved system property");
        }
        if (null != value) {
            ObjectMapper mapper = new ObjectMapper();
            if (value instanceof Map) {
                try {
                    value = new String(mapper.writeValueAsString(value));
                    if(null != metadata) 
                        metadata.put(key, value);
                } catch (Exception e) {
                    throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_JSON.name(), "Invalid JSON for property:"+key, e);
                }
            } else if (value instanceof List) {
                List list = (List) value;
                Object[] array = getArray(key, list);
                if (null == array) {
                    try {
                        value = new String(mapper.writeValueAsString(list));
                        if(null != metadata) 
                            metadata.put(key, value);
                    } catch (Exception e) {
                        throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_JSON.name(), "Invalid JSON for property:"+key, e);
                    }
                } else {
                    value = array;
                    if (null != metadata)
                        metadata.put(key, array);
                }
            } else if (!(value instanceof String) && !(value instanceof String[]) && !(value instanceof Double)
                    && !(value instanceof double[]) && !(value instanceof Float) && !(value instanceof float[]) && !(value instanceof Long)
                    && !(value instanceof long[]) && !(value instanceof Integer) && !(value instanceof int[])
                    && !(value instanceof Boolean) && !(value instanceof boolean[])) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: "
                        + key);
            }
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object[] getArray(String key, List list) {
        Object[] array = null;
        try {
            if (null != list && !list.isEmpty()) {
                Object obj = list.get(0);
                if (obj instanceof String) {
                    array = list.toArray(new String[list.size()]);
                } else if (obj instanceof Double) {
                    array = list.toArray(new Double[list.size()]);
                } else if (obj instanceof Float) {
                    array = list.toArray(new Float[list.size()]);
                } else if (obj instanceof Long) {
                    array = list.toArray(new Long[list.size()]);
                } else if (obj instanceof Integer) {
                    array = list.toArray(new Integer[list.size()]);
                } else if (obj instanceof Boolean) {
                    array = list.toArray(new Boolean[list.size()]);
                } else if( obj instanceof Map) {
                    array = null;
                } else {
                    throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: " + key);
                }
            }
        } catch (Exception e) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: " + key);
        }
        return array;
    }

}
