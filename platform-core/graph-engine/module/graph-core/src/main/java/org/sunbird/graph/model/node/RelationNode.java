package org.sunbird.graph.model.node;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.model.IRelation;
import org.sunbird.graph.model.relation.HasRelRelation;

import akka.dispatch.Futures;
import scala.concurrent.Future;
import scala.concurrent.Promise;

public class RelationNode extends AbstractIndexNode {

	private String objectType;
	private String name;
	private String relatedType;
	private static final String RELATION_NODE_NAME_KEY = "RELATION_NAME";
	private static final String RELATION_OBJECT_TYPE_KEY = "RELATED_OBJECT_TYPE";

	public RelationNode(BaseGraphManager manager, String graphId, String objectType, String name, String relatedType) {
		super(manager, graphId);
		if (StringUtils.isBlank(objectType) || StringUtils.isBlank(name))
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_NODE.name(), "Invalid Relation Node");
		this.objectType = objectType;
		this.relatedType = relatedType;
		String nodeId = getSystemNodeType() + "_" + objectType + "_" + name;
		if (StringUtils.isNotBlank(relatedType)) {
			nodeId += "_" + relatedType;
		}
		setNodeId(nodeId);
		this.name = name;
	}

	public Future<Map<String, Object>> create(final Request req) {
		final Promise<Map<String, Object>> promise = Futures.promise();
		Future<Map<String, Object>> future = promise.future();

		final String defNodeId = SystemNodeTypes.DEFINITION_NODE.name() + "_" + objectType;

		Response response = getNodeObject(req, getNodeId());
		if (manager.checkError(response)) {
			if (!StringUtils.equals(ResponseCode.RESOURCE_NOT_FOUND.name(), response.getResponseCode().name())) {
				failPromise(promise, GraphEngineErrorCodes.ERR_GRAPH_CREATE_RELATION_NODE_FAILED.name(),
						manager.getErrorMessage(response));
			} else {

				Response getDefNodeFuture = getNodeObject(req, defNodeId);

				if (manager.checkError(getDefNodeFuture)) {
					failPromise(promise, GraphEngineErrorCodes.ERR_GRAPH_CREATE_RELATION_NODE_FAILED.name(),
							manager.getErrorMessage(getDefNodeFuture));
				} else {
					Request request = new Request(req);
					request.put(GraphDACParams.node.name(), toNode());
					Response createFuture = nodeMgr.addNode(request);
					final IRelation rel = new HasRelRelation(getManager(), getGraphId(), defNodeId, getNodeId());
					createIndexNodeRelation(promise, null, createFuture, req, rel,
							GraphEngineErrorCodes.ERR_GRAPH_CREATE_RELATION_NODE_FAILED.name(),
							"Failed to create Relation Node: " + getObjectType() + " - " + getName());
				}
			}
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(GraphDACParams.node_id.name(), getNodeId());
			promise.success(map);
		}

		return future;
	}

	public String getSystemNodeType() {
		return SystemNodeTypes.RELATION_NODE.name();
	}

	public Node toNode() {
		Node node = new Node(getNodeId(), getSystemNodeType(), null);
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(RELATION_NODE_NAME_KEY, getName());
		if (StringUtils.isNotBlank(getRelatedType()))
			metadata.put(RELATION_OBJECT_TYPE_KEY, getRelatedType());
		node.setMetadata(metadata);
		return node;
	}

	public String getObjectType() {
		return objectType;
	}

	public String getName() {
		return name;
	}

	public String getRelatedType() {
		return relatedType;
	}

}