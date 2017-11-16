package com.ilimi.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.mgr.IGraphDACNodeMgr;
import com.ilimi.graph.dac.mgr.IGraphDACSearchMgr;
import com.ilimi.graph.dac.mgr.impl.GraphDACNodeMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACSearchMgrImpl;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.exception.GraphRelationErrorCodes;

import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;


public class ProxyNode extends AbstractNode {

    private String objectType;
    private String graphId;
    private String identifier;

	private static IGraphDACNodeMgr nodeMgr = new GraphDACNodeMgrImpl();
	private static IGraphDACSearchMgr searchMgr = new GraphDACSearchMgrImpl();
    
    public ProxyNode(BaseGraphManager manager, String graphId, String nodeId, String objectType,
            Map<String, Object> metadata,String identifier) {
        super(manager, graphId, nodeId, metadata);
        this.objectType = objectType;
        this.graphId = graphId;
        this.identifier = identifier;
    }
    
    public ProxyNode(BaseGraphManager manager, String graphId, Node node) {
        super(manager, graphId, node.getIdentifier(), node.getMetadata());
        this.objectType = node.getObjectType();
        this.graphId = graphId;
        this.identifier = node.getIdentifier();
    }

    @Override
    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
        node.setMetadata(this.metadata);
        return node;
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.PROXY_NODE.name();
    }

    @Override
    public String getFunctionalObjectType() {
        return this.objectType;
    }

    public Future<Node> getNodeObject(Request req) {
        Request request = new Request(req);
        request.put(GraphDACParams.node_id.name(), getNodeId());
        request.put(GraphDACParams.get_tags.name(), true);
		Future<Object> response = Futures.successful(searchMgr.getNodeByUniqueId(request));
        Future<Node> message = response.map(new Mapper<Object, Node>() {
            @Override
            public Node apply(Object parameter) {
                if (null != parameter && parameter instanceof Response) {
                    Response res = (Response) parameter;
                    Node node = (Node) res.get(GraphDACParams.node.name());
                    return node;
                }
                return null;
            }
        }, manager.getContext().dispatcher());
        return message;
    }

    
    @Override
    public void removeProperty(Request req) {
        try {
            Request request = new Request(req);
            request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(nodeMgr.removePropertyValue(request));
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    @Override
    public void setProperty(Request req) {
        Property property = (Property) req.get(GraphDACParams.metadata.name());
        if (!manager.validateRequired(property)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            checkMetadata(property.getPropertyName(), property.getPropertyValue());
            try {
                Request request = new Request(req);
                request.copyRequestValueObjects(req.getRequest());
				Future<Object> response = Futures.successful(nodeMgr.updatePropertyValue(request));
                manager.returnResponse(response, getParent());
            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    public Future<String> createNode(final Request req) {
        Request request = new Request(req);
        request.put(GraphDACParams.node.name(), toNode());
		Future<Object> response = Futures.successful(nodeMgr.addNode(request));
        Future<String> message = response.map(new Mapper<Object, String>() {
            @Override
            public String apply(Object parameter) {
                if (parameter instanceof Response) {
                    Response res = (Response) parameter;
                    if (manager.checkError(res)) {
                        return manager.getErrorMessage(res);
                    } else {
                        String identifier = (String) res.get(GraphDACParams.node_id.name());
                        if (manager.validateRequired(identifier)) {
                            setNodeId(identifier);
                        } else {
                            return "Error creating node in the graph";
                        }
                    }
                } else {
                    return "Error creating node in the graph";
                }
                return null;
            }
        }, manager.getContext().dispatcher());
        return message;
    }

    public Future<String> updateNode(Request req) {
        try {
            checkMetadata(metadata);
            Request request = new Request(req);
            request.setOperation("updateNode");
            request.put(GraphDACParams.node.name(), toNode());
			Future<Object> response = Futures.successful(nodeMgr.updateNode(request));
            Future<String> message = response.map(new Mapper<Object, String>() {
                @Override
                public String apply(Object parameter) {
                    if (parameter instanceof Response) {
                        Response res = (Response) parameter;
                        if (manager.checkError(res)) {
                            return manager.getErrorMessage(res);
                        }
                    } else {
                        return "Error updating node";
                    }
                    return null;
                }
            }, manager.getContext().dispatcher());
            return message;
        } catch (Exception e) {
            return Futures.successful(e.getMessage());
        }
    }

    public Future<Map<String, List<String>>> validateNode(Request req) {
    	
    	try {
    		final ExecutionContext ec = manager.context().dispatcher();
            final List<String> messages = new ArrayList<String>();
    		if(StringUtils.isBlank(graphId) || StringUtils.isBlank(objectType) || StringUtils.isBlank(identifier))
    		{
    			messages.add("GraphId or Object type  or identifier not set for node: " + getNodeId());
    			Future<List<String>> message = Futures.successful(messages);
                return getMessageMap(message, ec);
    		}else
    		{
    			Map<String, List<String>> map = new HashMap<String, List<String>>();
            	map.put(getNodeId(), messages);
            	return Futures.successful(map);
    		}
    	} catch (Exception e) {
    		throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
    	}
    }


}
