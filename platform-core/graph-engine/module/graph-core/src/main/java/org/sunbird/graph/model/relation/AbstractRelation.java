package org.sunbird.graph.model.relation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.model.AbstractDomainObject;
import org.sunbird.graph.model.IRelation;
import org.sunbird.graph.model.cache.DefinitionCache;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.dispatch.Futures;
import scala.concurrent.Future;

public abstract class AbstractRelation extends AbstractDomainObject implements IRelation {

	protected String startNodeId;
	protected String endNodeId;
	protected Map<String, Object> metadata;

	protected AbstractRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId,
			Map<String, Object> metadata) {
		this(manager, graphId, startNodeId, endNodeId);
		this.metadata = metadata;
	}

	protected AbstractRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
		super(manager, graphId);
		if (null == manager || StringUtils.isBlank(graphId) || StringUtils.isBlank(startNodeId)
				|| StringUtils.isBlank(endNodeId)) {
			throw new ClientException(GraphRelationErrorCodes.ERR_INVALID_RELATION.name(), "Invalid Relation");
		}
		this.startNodeId = startNodeId;
		this.endNodeId = endNodeId;
	}

	public void create(final Request req) {
		try {
			Boolean skipValidation = (Boolean) req.get(GraphDACParams.skip_validations.name());
			Map<String, List<String>> messageMap = null;
			if (null == skipValidation || !skipValidation)
				messageMap = validateRelation(req);
			List<String> errMessages = getErrorMessages(messageMap);
			if (null == errMessages || errMessages.isEmpty()) {
				Request request = new Request(req);
				request.put(GraphDACParams.start_node_id.name(), getStartNodeId());
				request.put(GraphDACParams.relation_type.name(), getRelationType());
				request.put(GraphDACParams.end_node_id.name(), getEndNodeId());
				request.put(GraphDACParams.metadata.name(), getMetadata());
				Future<Object> response = Futures.successful(graphMgr.addRelation(request));
				manager.returnResponse(response, getParent());
			} else {
				manager.OK(GraphDACParams.messages.name(), errMessages, getParent());
			}
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_CREATE.name(),
					"Error occured while creating the Relation", e);
		}
	}

	public String createRelation(final Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.start_node_id.name(), getStartNodeId());
		request.put(GraphDACParams.relation_type.name(), getRelationType());
		request.put(GraphDACParams.end_node_id.name(), getEndNodeId());
		request.put(GraphDACParams.metadata.name(), getMetadata());

		Response res = graphMgr.addRelation(request);
		if (manager.checkError(res)) {
			return manager.getErrorMessage(res);
		}
		return null;
	}

	@Override
	public void delete(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(graphMgr.deleteRelation(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_DELETE.name(),
					"Error occured while deleting the relation", e);
		}
	}

	@Override
	public String deleteRelation(Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.start_node_id.name(), getStartNodeId());
		request.put(GraphDACParams.relation_type.name(), getRelationType());
		request.put(GraphDACParams.end_node_id.name(), getEndNodeId());

		Response res = graphMgr.deleteRelation(request);
		if (manager.checkError(res)) {
			return manager.getErrorMessage(res);
		}
		return null;
	}

	@Override
	public void validate(final Request request) {
		try {
			Map<String, List<String>> messageMap = validateRelation(request);

			List<String> errMessages = getErrorMessages(messageMap);
			if (null == errMessages || errMessages.isEmpty()) {
				manager.OK(getParent());
			} else {
				manager.OK(GraphDACParams.messages.name(), errMessages, getParent());
			}
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(),
					"Error Validating the relation", e);
		}
	}

	public Relation toRelation() {
		Relation relation = new Relation(this.startNodeId, getRelationType(), this.endNodeId);
		return relation;
	}

	public String getStartNodeId() {
		return this.startNodeId;
	}

	public String getEndNodeId() {
		return this.endNodeId;
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	public boolean isType(String relationType) {
		return StringUtils.equalsIgnoreCase(getRelationType(), relationType);
	}

	public void getProperty(Request req) {
		try {
			String key = (String) req.get(GraphDACParams.property_key.name());
			Request request = new Request(req);
			request.put(GraphDACParams.start_node_id.name(), this.startNodeId);
			request.put(GraphDACParams.relation_type.name(), getRelationType());
			request.put(GraphDACParams.end_node_id.name(), this.endNodeId);
			request.put(GraphDACParams.property_key.name(), key);
			Future<Object> response = Futures.successful(searchMgr.getRelationProperty(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(),
					"Error in fetching the relation properties", e);
		}
	}

	public void removeProperty(Request req) {
		throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_UNSUPPORTED_OPERATION.name(),
				"Remove Property is not supported on relations");
	}

	public void setProperty(Request req) {
		throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_UNSUPPORTED_OPERATION.name(),
				"Set Property is not supported on relations");
	}

	protected Map<String, List<String>> getMessageMap(Iterable<String> aggregate) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> messages = new ArrayList<String>();
		if (null != aggregate) {
			for (String msg : aggregate) {
				if (StringUtils.isNotBlank(msg))
					messages.add(msg);
			}
		}
		map.put(getStartNodeId(), messages);
		return map;

	}

	protected Node getNode(Request request, String nodeId) {
		try {
			Request newReq = new Request(request);
			newReq.put(GraphDACParams.node_id.name(), nodeId);

			Response res = searchMgr.getNodeByUniqueId(newReq);
			if (!manager.checkError(res)) {
				Node node = (Node) res.get(GraphDACParams.node.name());
				return node;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(),
					"Error occured while validating the relation", e);
		}
	}

	protected String checkCycle(Request req) {
		try {
			Request request = new Request(req);
			request.put(GraphDACParams.start_node_id.name(), this.endNodeId);
			request.put(GraphDACParams.relation_type.name(), getRelationType());
			request.put(GraphDACParams.end_node_id.name(), this.startNodeId);
			Response res = searchMgr.checkCyclicLoop(request);

			if (manager.checkError(res)) {
				return manager.getErrorMessage(res);
			} else {
				Boolean loop = (Boolean) res.get(GraphDACParams.loop.name());
				if (null != loop && loop.booleanValue()) {
					String msg = (String) res.get(GraphDACParams.message.name());
					return msg;
				} else {
					if (StringUtils.equals(startNodeId, endNodeId))
						return "Relation '" + getRelationType() + "' cannot be created between: " + getStartNodeId()
								+ " and " + getEndNodeId();
					else
						return null;
				}
			}

		} catch (Exception e) {
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(),
					"Error occured while validing the relation", e);
		}
	}

	protected String getNodeTypeFuture(String nodeId, Node node, final String[] nodeTypes) {
		if (null == node) {
			return "Node '" + nodeId + "' not Found";
		} else {
			if (Arrays.asList(nodeTypes).contains(node.getNodeType())) {
				return null;
			} else {
				return "Node " + node.getIdentifier() + " is not a " + nodeTypes;
			}
		}
	}

	protected String getNodeTypeFuture(Node nodeFuture) {
		if (null != nodeFuture)
			return nodeFuture.getNodeType();
		else
			return null;

	}

	protected String getObjectTypeFuture(Node node) {
		if (null != node)
			return node.getObjectType();
		else
			return null;

	}

	protected String compareFutures(String future1, final String future2, final String property) {
		if (StringUtils.isNotBlank(future1) && StringUtils.isNotBlank(future2)) {
			if (StringUtils.equals(future1, future2)) {
				return null;
			} else {
				return property + " values do not match";
			}
		} else {
			return property + " cannot be empty";
		}
	}

	protected String validateObjectTypes(String objectType, final String endNodeObjectType, final Request request) {

		if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(endNodeObjectType)) {
			List<String> outRelations = DefinitionCache.getOutRelationObjectTypes(graphId, objectType);
			boolean found = false;
			if (null != outRelations && !outRelations.isEmpty()) {
				for (String outRel : outRelations) {
					if (StringUtils.equals(getRelationType() + ":" + endNodeObjectType, outRel)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				return (getRelationType() + " is not allowed between " + objectType + " and " + endNodeObjectType);
			}
		}
		return null;

	}

}
