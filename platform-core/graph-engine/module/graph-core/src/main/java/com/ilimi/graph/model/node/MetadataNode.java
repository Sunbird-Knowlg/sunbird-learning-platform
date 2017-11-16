package com.ilimi.graph.model.node;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.mgr.IGraphDACNodeMgr;
import com.ilimi.graph.dac.mgr.IGraphDACSearchMgr;
import com.ilimi.graph.dac.mgr.impl.GraphDACNodeMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACSearchMgrImpl;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.IRelation;
import com.ilimi.graph.model.relation.HasMetadataRelation;

import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;

public class MetadataNode extends AbstractIndexNode {

    private String objectType;
    private String name;
    private static final String METADATA_NODE_NAME_KEY = "METADATA_PROPERTY_NAME";
	private static IGraphDACNodeMgr nodeMgr = new GraphDACNodeMgrImpl();
	private static IGraphDACSearchMgr searchMgr = new GraphDACSearchMgrImpl();

    public MetadataNode(BaseGraphManager manager, String graphId, String objectType, String name) {
        super(manager, graphId);
        if (StringUtils.isBlank(objectType) || StringUtils.isBlank(name))
            throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Metadata Node");
        this.objectType = objectType;
        setNodeId(getSystemNodeType() + "_" + objectType + "_" + name);
        this.name = name;
    }

    public Future<Map<String, Object>> create(final Request req) {
        final Promise<Map<String, Object>> promise = Futures.promise();
        Future<Map<String, Object>> future = promise.future();
        final String defNodeId = SystemNodeTypes.DEFINITION_NODE.name() + "_" + objectType;
        final ExecutionContext ec = manager.getContext().dispatcher();
		Future<Object> getFuture = getNodeObject(req, searchMgr, getNodeId());
        getFuture.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                boolean valid = checkIfNodeExists(promise, arg0, arg1, GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name());
                if (valid) {
					Future<Object> getDefNodeFuture = getNodeObject(req, searchMgr, defNodeId);
                    getDefNodeFuture.onComplete(new OnComplete<Object>() {
                        @Override
                        public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                            boolean createNode = validateResponse(promise, arg0, arg1,
                                    GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name(), "Definition Node not found: "
                                            + getObjectType());
                            if (createNode) {
                                Request request = new Request(req);
                                request.put(GraphDACParams.node.name(), toNode());
								Future<Object> createFuture = Futures.successful(nodeMgr.addNode(request));
                                createFuture.onComplete(new OnComplete<Object>() {
                                    @Override
                                    public void onComplete(Throwable arg0, Object arg1) throws Throwable {
                                        IRelation rel = new HasMetadataRelation(getManager(), getGraphId(), defNodeId, getNodeId());
                                        createIndexNodeRelation(promise, arg0, arg1, req, rel,
                                                GraphEngineErrorCodes.ERR_GRAPH_CREATE_METADATA_NODE_FAILED.name(),
                                                "Failed to create Metadata Node: " + getObjectType() + " - " + getName());
                                    }
                                }, ec);
                            }
                        }
                    }, ec);
                }
            }
        }, ec);
        return future;
    }

    public String getSystemNodeType() {
        return SystemNodeTypes.METADATA_NODE.name();
    }

    public Node toNode() {
        Node node = new Node(getNodeId(), getSystemNodeType(), null);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(METADATA_NODE_NAME_KEY, getName());
        node.setMetadata(metadata);
        return node;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getName() {
        return name;
    }

}
