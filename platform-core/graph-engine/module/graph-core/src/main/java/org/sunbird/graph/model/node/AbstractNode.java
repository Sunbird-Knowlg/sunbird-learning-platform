package org.sunbird.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.model.AbstractDomainObject;
import org.sunbird.graph.model.INode;

import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

/**
 * The Class AbstractNode.
 * 
 * @author Mohammad Azharuddin
 */
public abstract class AbstractNode extends AbstractDomainObject implements INode {

    /** The node id. */
    private String nodeId;
    
    /** The version key. */
    private String versionKey;
    
    /** The metadata. */
    protected Map<String, Object> metadata;

    /**
     * Instantiates a new abstract node.
     *
     * @param manager the manager
     * @param graphId the graph id
     * @param nodeId the node id
     * @param metadata the metadata
     */
	protected AbstractNode(BaseGraphManager manager, String graphId, String nodeId, Map<String, Object> metadata) {
        super(manager, graphId);
        if (null == manager || StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Node");
        }
        this.nodeId = nodeId;
        this.metadata = metadata;
        if (this.metadata != null) {
            this.metadata.remove("sYS_INTERNAL_LAST_UPDATED_ON");
        }
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.IPropertyContainer#create(org.sunbird.common.dto.Request)
     */
    @Override
    public void create(final Request req) {
        try {
            checkMetadata(metadata);
			Map<String, List<String>> messages = validateNode(req);
			List<String> errMessages = getErrorMessages(messages);
			if (null == errMessages || errMessages.isEmpty()) {
				Request request = new Request(req);
				request.put(GraphDACParams.node.name(), toNode());
				Future<Object> response = Futures.successful(nodeMgr.addNode(request));
				manager.returnResponse(response, getParent());
			} else {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_VALIDATION_FAILED.name(),
						"Node validation failed", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(),
						errMessages, getParent());
			}
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.IPropertyContainer#getProperty(org.sunbird.common.dto.Request)
     */
    @Override
    public void getProperty(Request req) {
        final String key = (String) req.get(GraphDACParams.property_key.name());
        if (!manager.validateRequired(key)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_NODE_PROPERTY_INVALID_KEY.name(),
                    "Get Property: Required Properties are missing");
        } else {
            try {
                Request request = new Request(req);
                request.put(GraphDACParams.node_id.name(), this.nodeId);
                request.put(GraphDACParams.property_key.name(), key);
				Future<Object> response = Futures.successful(searchMgr.getNodeProperty(request));
                manager.returnResponse(response, getParent());
            } catch (Exception e) {
                manager.ERROR(e, getParent());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.IPropertyContainer#removeProperty(org.sunbird.common.dto.Request)
     */
    @Override
    public void removeProperty(Request req) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "Remove Property is not supported on this node");
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.IPropertyContainer#setProperty(org.sunbird.common.dto.Request)
     */
    @Override
    public void setProperty(Request req) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "Set Property is not supported on this node");
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.INode#updateMetadata(org.sunbird.common.dto.Request)
     */
    @Override
    public void updateMetadata(Request req) {
        throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_UNSUPPORTED_OPERATION.name(),
                "Update Metadata is not supported on this node");
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.IPropertyContainer#delete(org.sunbird.common.dto.Request)
     */
    @Override
    public void delete(Request req) {
        try {
            Request request = new Request(req);
            request.put(GraphDACParams.node_id.name(), getNodeId());
			Future<Object> response = Futures.successful(nodeMgr.deleteNode(request));
            manager.returnResponse(response, getParent());
        } catch (Exception e) {
            manager.ERROR(e, getParent());
        }
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.INode#toNode()
     */
    @Override
    public Node toNode() {
        Node node = new Node(this.nodeId, getSystemNodeType(), getFunctionalObjectType());
        node.setMetadata(this.metadata);
        return node;
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.INode#validateNode(org.sunbird.common.dto.Request)
     */
    @Override
    public Map<String, List<String>> validateNode(Request request) {
        return getMessageMap(null, manager.getContext().dispatcher());
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        checkMetadata(this.metadata);
    }

    /* (non-Javadoc)
     * @see org.sunbird.graph.model.INode#getNodeId()
     */
    public String getNodeId() {
        return this.nodeId;
    }

    /**
     * Sets the node id.
     *
     * @param nodeId the new node id
     */
    protected void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    /**
     * Gets the version key.
     *
     * @return the version key
     */
    public String getVersionKey() {
		return versionKey;
	}

	/**
	 * Sets the version key.
	 *
	 * @param versionKey the new version key
	 */
	protected void setVersionKey(String versionKey) {
		this.versionKey = versionKey;
	}

    /**
     * Gets the message map.
     *
     * @param aggregate the aggregate
     * @param ec the ec
     * @return the message map
     */
	protected Map<String, List<String>> getMessageMap(List<String> aggregate, ExecutionContext ec) {
    		Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> messages = new ArrayList<String>();
        if (null != aggregate && !aggregate.isEmpty()) {
            messages.addAll(aggregate);
        }
        map.put(getNodeId(), messages);
        return map;
    }

    /**
     * Gets the error messages.
     *
     * @param messages the messages
     * @return the error messages
     */
    protected List<String> getErrorMessages(Iterable<List<String>> messages) {
        List<String> errMessages = new ArrayList<String>();
        if (null != messages) {
            for (List<String> list : messages) {
                if (null != list && !list.isEmpty()) {
                    errMessages.addAll(list);
                }
            }
        }
        return errMessages;
    }

    /**
     * Check metadata.
     *
     * @param metadata the metadata
     */
    protected void checkMetadata(Map<String, Object> metadata) {
        if (null != metadata && metadata.size() > 0) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                checkMetadata(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Check metadata.
     *
     * @param key the key
     * @param value the value
     */
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
                    if (list.isEmpty()) {
                        value = null;
                        if(null != metadata) 
                            metadata.put(key, value);
                    } else {
                        try {
                            value = new String(mapper.writeValueAsString(list));
                            if(null != metadata) 
                                metadata.put(key, value);
                        } catch (Exception e) {
                            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_JSON.name(), "Invalid value for the property: "+key, e);
                        }
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

    /**
     * Gets the array.
     *
     * @param key the key
     * @param list the list
     * @return the array
     */
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

    /**
     * Convert list to array.
     *
     * @param list the list
     * @return the string[]
     */
    protected String[] convertListToArray(List<String> list) {
        if (null != list && !list.isEmpty()) {
            String[] array = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }
            return array;
        }
        return null;
    }
}
