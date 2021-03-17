package org.sunbird.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.exception.GraphRelationErrorCodes;

import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;


public class ProxyNode extends AbstractNode {

    private String objectType;
    private String graphId;
    private String identifier;

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

    public Node getNodeObject(Request req) {
        Request request = new Request(req);
        request.put(GraphDACParams.node_id.name(), getNodeId());
        request.put(GraphDACParams.get_tags.name(), true);
        Response response = searchMgr.getNodeByUniqueId(request);
        Node node = (Node) response.get(GraphDACParams.node.name());
        return node;
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

	public String createNode(final Request req) {
        Request request = new Request(req);
        request.put(GraphDACParams.node.name(), toNode());
		Response response = nodeMgr.addNode(request);

		if (manager.checkError(response)) {
			return manager.getErrorMessage(response);
		} else {
			String identifier = (String) response.get(GraphDACParams.node_id.name());
			if (manager.validateRequired(identifier)) {
				setNodeId(identifier);
				return null;
			} else {
				return "Error creating node in the graph";
			}
		}
    }

    public String updateNode(Request req) {
        try {
            checkMetadata(metadata);
            Request request = new Request(req);
            request.put(GraphDACParams.node.name(), toNode());
            Response response = nodeMgr.updateNode(request);
            if (manager.checkError(response)) {
                return manager.getErrorMessage(response);
            }
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

	public Map<String, List<String>> validateNode(Request req) {
	    	try {
	    		final ExecutionContext ec = manager.context().dispatcher();
	            final List<String> messages = new ArrayList<String>();
	    		if(StringUtils.isBlank(graphId) || StringUtils.isBlank(objectType) || StringUtils.isBlank(identifier)) {
	    			messages.add("GraphId or Object type  or identifier not set for node: " + getNodeId());
				return getMessageMap(messages, ec);
	    		} else {
	    			Map<String, List<String>> map = new HashMap<String, List<String>>();
	            	map.put(getNodeId(), messages);
				return map;
	    		}
	    	} catch (Exception e) {
	    		throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
	    	}
    }
}
