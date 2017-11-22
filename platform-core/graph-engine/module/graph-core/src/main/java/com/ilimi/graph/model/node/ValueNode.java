package com.ilimi.graph.model.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.mgr.IGraphDACNodeMgr;
import com.ilimi.graph.dac.mgr.IGraphDACSearchMgr;
import com.ilimi.graph.dac.mgr.impl.GraphDACNodeMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACSearchMgrImpl;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.IRelation;
import com.ilimi.graph.model.relation.HasTagRelation;
import com.ilimi.graph.model.relation.HasValueRelation;

import akka.dispatch.Futures;
import scala.concurrent.Future;
import scala.concurrent.Promise;

public class ValueNode extends AbstractIndexNode {

    private String nodeId;
    private String objectType;
    private String name;
    private Object value;
    private String relatedType;
    private String valueNodeType;
    private String sourceNodeId;
    private String relationType = HasValueRelation.RELATION_NAME;
    public static final String VALUE_NODE_VALUE_KEY = "VALUE_NODE_VALUE";
    public static final String VALUE_NODE_TYPE_KEY = "VALUE_NODE_TYPE";
	private static IGraphDACNodeMgr nodeMgr = new GraphDACNodeMgrImpl();
	private static IGraphDACSearchMgr searchMgr = new GraphDACSearchMgrImpl();

    public ValueNode(BaseGraphManager manager, String graphId, String objectType, String name, Object value) {
        super(manager, graphId);
        if (StringUtils.isBlank(objectType) || StringUtils.isBlank(name) || null == value)
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Value Node");
        this.objectType = objectType;
        this.name = name;
        this.value = value;
        this.valueNodeType = SystemNodeTypes.METADATA_NODE.name();
        this.sourceNodeId = SystemNodeTypes.METADATA_NODE.name() + "_" + objectType + "_" + name;
    }

    public ValueNode(BaseGraphManager manager, String graphId, String objectType, String name, String relatedType, String value) {
        super(manager, graphId);
        if (StringUtils.isBlank(objectType) || StringUtils.isBlank(name) || null == value)
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Value Node");
        this.objectType = objectType;
        this.name = name;
        this.value = value;
        this.relatedType = relatedType;
        this.valueNodeType = SystemNodeTypes.RELATION_NODE.name();
        this.sourceNodeId = SystemNodeTypes.RELATION_NODE.name() + "_" + objectType + "_" + name;
        if (StringUtils.isNotBlank(relatedType))
            this.sourceNodeId += "_" + relatedType;
    }

    public ValueNode(BaseGraphManager manager, String graphId, String objectType, String value) {
        super(manager, graphId);
        if (StringUtils.isBlank(objectType) || null == value)
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Value Node");
        this.objectType = objectType;
        this.value = value;
        this.relationType = HasTagRelation.RELATION_NAME;
        this.sourceNodeId = SystemNodeTypes.DEFINITION_NODE.name() + "_" + objectType;
    }

    public Future<Map<String, Object>> create(final Request req) {
        final Promise<Map<String, Object>> promise = Futures.promise();
        Future<Map<String, Object>> future = promise.future();
		Response res = getNodeObject(req, searchMgr, getSourceNodeId());

		if (manager.checkError(res)) {
			failPromise(promise, GraphEngineErrorCodes.ERR_GRAPH_CREATE_VALUE_NODE_FAILED.name(),
					manager.getErrorMessage(res));
		} else {
			Node sourceNode = (Node) res.get(GraphDACParams.node.name());
			List<Relation> rels = sourceNode.getOutRelations();
			boolean found = false;
			if (null != rels && rels.size() > 0) {
				for (Relation rel : rels) {
					if (StringUtils.equalsIgnoreCase(getRelationType(), rel.getRelationType())) {
						Object endNodeValue = rel.getEndNodeMetadata().get(VALUE_NODE_VALUE_KEY);
						if (getValue() == endNodeValue) {
							setNodeId(rel.getEndNodeId());
							found = true;
							break;
                        }
                    }
				}
			}
			if (!found) {
				Request request = new Request(req);
				request.put(GraphDACParams.node.name(), toNode());
				Response addRes = nodeMgr.addNode(request);

				if (manager.checkError(addRes)) {
					failPromise(promise, GraphEngineErrorCodes.ERR_GRAPH_CREATE_VALUE_NODE_FAILED.name(),
							manager.getErrorMessage(addRes));
				} else {
					String valueNodeId = (String) addRes.get(GraphDACParams.node_id.name());
					setNodeId(valueNodeId);
					IRelation rel = new HasValueRelation(getManager(), getGraphId(), getSourceNodeId(), getNodeId());
					rel.createRelation(req);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(GraphDACParams.node_id.name(), getNodeId());
					promise.success(map);

				}
			} else {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(GraphDACParams.node_id.name(), getNodeId());
				promise.success(map);
            }
		}
        return future;
    }

    public String getSystemNodeType() {
        return SystemNodeTypes.VALUE_NODE.name();
    }

    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), null);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(VALUE_NODE_VALUE_KEY, value);
        metadata.put(VALUE_NODE_TYPE_KEY, valueNodeType);
        node.setMetadata(metadata);
        return node;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public String getValueNodeType() {
        return valueNodeType;
    }

    public String getRelationType() {
        return relationType;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }
}
