package org.sunbird.graph.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.model.AbstractDomainObject;
import org.sunbird.graph.model.ICollection;

import scala.concurrent.ExecutionContext;

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
	public Map<String, List<String>> validateNode(Request request) {
		return getMessageMap(null, manager.getContext().dispatcher());
    }

	protected Map<String, List<String>> getMessageMap(List<String> aggregate, ExecutionContext ec) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> messages = new ArrayList<String>();
		if (null != aggregate && !aggregate.isEmpty()) {
			messages.addAll(aggregate);
		}
		map.put(getNodeId(), messages);
		return map;
    }

	protected Boolean checkMemberNode(Request req, final String memberId, final ExecutionContext ec) {
        Request request = new Request(req);
        request.put(GraphDACParams.node_id.name(), memberId);

		Response ar = searchMgr.getNodeByUniqueId(request);
		Node node = (Node) ar.get(GraphDACParams.node.name());
		if (manager.validateRequired(node)) {
			if (!StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())
					&& !StringUtils.equals(SystemNodeTypes.PROXY_NODE.name(), node.getNodeType()))
                return false;
			if (StringUtils.isNotBlank(getMemberObjectType())) {
				if (!StringUtils.equals(getMemberObjectType(), node.getObjectType()))
					return false;
            }
			return true;
		}
		return false;
    }
    
	@SuppressWarnings("unchecked")
	protected Boolean checkMemberNodes(Request req, final List<String> memberIds, final ExecutionContext ec) {
        Request request = new Request(req);
        request.put(GraphDACParams.node_ids.name(), memberIds);
		Response ar = searchMgr.getNodesByUniqueIds(request);
		List<Node> nodes = (List<Node>) ar.get(GraphDACParams.node_list.name());
		if (manager.validateRequired(nodes)) {
			if (memberIds.size() == nodes.size()) {
				for (Node node : nodes) {
					if (!StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())
							&& !StringUtils.equals(SystemNodeTypes.PROXY_NODE.name(), node.getNodeType()))
						return false;
					if (StringUtils.isNotBlank(getMemberObjectType())) {
						if (!StringUtils.equals(getMemberObjectType(), node.getObjectType()))
							return false;
                    }
                }
				return true;
            }
		}
		return false;
    }

	protected Node getNodeObject(Request req, ExecutionContext ec, String setId) {
		Request request = new Request(req);
		request.put(GraphDACParams.node_id.name(), setId);
		Response res = searchMgr.getNodeByUniqueId(request);
		Node node = null;

		if (!manager.checkError(res)) {
			node = (Node) res.get(GraphDACParams.node.name());
		}
		return node;

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
