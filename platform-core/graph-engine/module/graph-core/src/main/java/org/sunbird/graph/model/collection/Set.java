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
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.cache.mgr.impl.SetCacheManager;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

public class Set extends AbstractCollection {

	public static final String SET_OBJECT_TYPE_KEY = "SET_OBJECT_TYPE_KEY";
	public static final String SET_CRITERIA_KEY = "SET_CRITERIA_KEY";
	public static final String SET_CRITERIA_QUERY_KEY = "SET_CRITERIA_QUERY_KEY";
	public static final String SET_TYPE_KEY = "SET_TYPE";
	private SearchCriteria criteria;
	private String setObjectType;
	private String setCriteria;
	private String setType = SET_TYPES.MATERIALISED_SET.name();
	private List<String> memberIds;
	private List<Relation> inRelations;
	private List<Relation> outRelations;
	private ObjectMapper mapper = new ObjectMapper();

	public static enum SET_TYPES {
		MATERIALISED_SET;
	}

	public Set(BaseGraphManager manager, String graphId, String id, String setObjectType,
			Map<String, Object> metadata) {
		super(manager, graphId, id, metadata);
		this.setObjectType = setObjectType;
		this.setType = SET_TYPES.MATERIALISED_SET.name();
	}

	public Set(BaseGraphManager manager, String graphId, String id, Map<String, Object> metadata) {
		super(manager, graphId, id, metadata);
		this.setType = SET_TYPES.MATERIALISED_SET.name();
	}

	public Set(BaseGraphManager manager, String graphId, String id, String setObjectType, String memberObjectType,
			Map<String, Object> metadata, List<String> memberIds) {
		super(manager, graphId, id, metadata);
		this.memberIds = memberIds;
		this.setObjectType = setObjectType;
		this.memberObjectType = memberObjectType;
		this.setType = SET_TYPES.MATERIALISED_SET.name();
	}

	public Set(BaseGraphManager manager, String graphId, Node node) {
		super(manager, graphId, node.getIdentifier(), node.getMetadata());
		fromNode(node);
	}

	public Node getSetObject(Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.node_id.name(), getNodeId());
		Response res = searchMgr.getNodeByUniqueId(request);
		Node node = null;
		if (!manager.checkError(res)) {
			node = (Node) res.get(GraphDACParams.node.name());
			if (!StringUtils.equalsIgnoreCase(SystemNodeTypes.SET.name(), node.getNodeType())) {
				// fromNode(node);
				node = null;
			}
		}
		return node;
	}

	@Override
	public Node toNode() {
		Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
		Map<String, Object> metadata = getMetadata();
		if (metadata == null) {
			metadata = new HashMap<String, Object>();
		}
		metadata.put(SET_TYPE_KEY, getSetType());
		if (null != criteria) {
			metadata.put(SET_OBJECT_TYPE_KEY, criteria.getObjectType());
			metadata.put(SET_CRITERIA_QUERY_KEY, criteria.getQuery());
			metadata.put(SET_CRITERIA_KEY, getSetCriteria());
		} else {
			if (StringUtils.isNotBlank(this.memberObjectType))
				metadata.put(SET_OBJECT_TYPE_KEY, this.memberObjectType);
		}
		if (!metadata.isEmpty())
			node.setMetadata(metadata);
		return node;
	}

	@Override
	public void create(Request request) {
		createSet(request);
	}

	public void updateMembership(final Request req) {
		Node setNode = getSetObject(req);

		if (null == setNode) {
			manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SET_NOT_FOUND.name(), "Set not found",
					ResponseCode.RESOURCE_NOT_FOUND, getParent());
		} else {
			if (null != memberIds && memberIds.size() > 0) {
				updateMembershipFromCache(memberIds, setNode);
			} else {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SET_UPDATE_MEMBERSHIP_ERROR.name(),
						"Update membership - invalid Set type.", ResponseCode.CLIENT_ERROR, getParent());
			}
		}
	}

	private void updateMembershipFromCache(final List<String> memberIds, Node node) {
		Request request = new Request();
		request.getContext().get(GraphDACParams.graph_id.name());
		String setId = node.getIdentifier();
		List<String> existingMembers = SetCacheManager.getSetMembers(graphId, setId);

		if (null != existingMembers) {
			List<String> removeIds = new ArrayList<String>();
			List<String> addIds = new ArrayList<String>();
			if (null != memberIds) {
				for (String member : memberIds) {
					if (existingMembers.contains(member)) {
						existingMembers.remove(member);
					} else {
						addIds.add(member);
					}
				}
				removeIds.addAll(existingMembers);
			}
			Request req = new Request();
			req.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			if (removeIds.size() > 0) {
				for (String removeId : removeIds) {
					Response removeResp = removeMemberFromSet(req, getNodeId(), removeId);
					manager.returnResponseOnFailure(Futures.successful(removeResp), getParent());
				}
			}
			if (addIds.size() > 0) {
				Object addResp = addMembersToSet(req, getNodeId(), addIds);
				manager.returnResponseOnFailure(Futures.successful(addResp), getParent());
			}
			Request updateReq = new Request(req);
			updateReq.put(GraphDACParams.node.name(), toNode());
			Response response = nodeMgr.updateNode(updateReq);
			updateRelations(req, node);
			manager.returnResponse(Futures.successful(response), getParent());
		} else {
			manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_GET_SET_MEMBERS_INVALID_SET_ID.name(),
					"Failed to get set members", ResponseCode.RESOURCE_NOT_FOUND, getParent());
		}
	}

	@Override
	public void addMember(final Request req) {
		final String setId = (String) req.get(GraphDACParams.collection_id.name());
		final String memberId = (String) req.get(GraphDACParams.member_id.name());
		if (!manager.validateRequired(setId, memberId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				final ExecutionContext ec = manager.getContext().dispatcher();
				Node set = getNodeObject(req, ec, setId);

				Map<String, Object> metadata = set.getMetadata();
				if (null == metadata) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
							"Invalid Set", ResponseCode.CLIENT_ERROR, getParent());
				} else {
					Node member = getNodeObject(req, ec, memberId);

					if (null == member) {
						manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
								"Member with identifier: " + memberId + " does not exist.", ResponseCode.CLIENT_ERROR,
								getParent());
					} else {
						Future<Object> addResp = addMemberToSet(req, setId, memberId);
						manager.returnResponse(addResp, getParent());
					}

				}

			} catch (Exception e) {
				manager.handleException(e, getParent());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addMembers(final Request req) {
		final String setId = (String) req.get(GraphDACParams.collection_id.name());
		final List<String> members = (List<String>) req.get(GraphDACParams.members.name());
		if (!manager.validateRequired(setId, members)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				final ExecutionContext ec = manager.getContext().dispatcher();
				Node set = getNodeObject(req, ec, setId);

				Map<String, Object> metadata = set.getMetadata();
				if (null == metadata) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
							"Invalid Set", ResponseCode.CLIENT_ERROR, getParent());
				} else {
					Boolean member = false;
					if (members.size() == 1)
						member = checkMemberNode(req, members.get(0), ec);
					else
						member = checkMemberNodes(req, members, ec);

					if (!member) {
						manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
								"Member cannot be added to criteria sets", ResponseCode.CLIENT_ERROR, getParent());
					} else {
						Object response = addMembersToSet(req, setId, members);
						manager.returnResponse(Futures.successful(response), getParent());
					}

				}
			} catch (Exception e) {
				manager.handleException(e, getParent());
			}
		}
	}

	@Override
	public void removeMember(final Request req) {
		try {
			final String setId = (String) req.get(GraphDACParams.collection_id.name());
			final String memberId = (String) req.get(GraphDACParams.member_id.name());
			if (!manager.validateRequired(setId, memberId)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_SET_MEMBER_MISSING_REQ_PARAMS.name(),
						"Required parameters are missing...");
			} else {
				final ExecutionContext ec = manager.getContext().dispatcher();
				Node set = getNodeObject(req, ec, setId);

				Map<String, Object> metadata = set.getMetadata();
				if (null == metadata) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
							"Invalid Set", ResponseCode.CLIENT_ERROR, getParent());
				} else {
					Response response = removeMemberFromSet(req, setId, memberId);
					manager.returnResponse(Futures.successful(response), getParent());
				}
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeMembers(final Request req) {
		try {
			final String setId = (String) req.get(GraphDACParams.collection_id.name());
			final List<String> members = (List<String>) req.get(GraphDACParams.members.name());
			if (!manager.validateRequired(setId, members)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_SET_MEMBER_MISSING_REQ_PARAMS.name(),
						"Required parameters are missing...");
			} else {
				final ExecutionContext ec = manager.getContext().dispatcher();
				Node set = getNodeObject(req, ec, setId);

				Map<String, Object> metadata = set.getMetadata();
				if (null == metadata) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS.name(),
							"Invalid Set", ResponseCode.CLIENT_ERROR, getParent());
				} else {
					Response responseFinal = null;
					for (String memberId : members) {
						responseFinal = removeMemberFromSet(req, setId, memberId);
					}
					manager.returnResponse(Futures.successful(responseFinal), getParent());

				}
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	@Override
	public void getMembers(Request req) {
		try {
			String setId = (String) req.get(GraphDACParams.collection_id.name());
			if (!manager.validateRequired(setId)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_SET_MEMBERS_INVALID_SET_ID.name(),
						"Required parameters are missing...");
			} else {
				req.getContext().get(GraphDACParams.graph_id.name());
				List<String> members = SetCacheManager.getSetMembers(graphId, setId);
				if (null != members) {
					manager.OK(GraphDACParams.members.name(), members, getParent());
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_GET_SET_MEMBERS_INVALID_SET_ID.name(),
							"Failed to get set members", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				}
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	@Override
	public void isMember(Request req) {
		try {
			String setId = (String) req.get(GraphDACParams.collection_id.name());
			String memberId = (String) req.get(GraphDACParams.member_id.name());
			if (!manager.validateRequired(setId, memberId)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IS_SET_MEMBER_INVALID_SET_ID.name(),
						"Required parameters are missing...");
			} else {
				req.getContext().get(GraphDACParams.graph_id.name());
				Boolean isMember = SetCacheManager.isSetMember(graphId, setId, memberId);
				if (isMember) {
					manager.OK(GraphDACParams.member.name(), memberId, getParent());
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_IS_SET_MEMBER_INVALID_SET_ID.name(),
							"Failed to get validate set member", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				}
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	@Override
	public void delete(Request req) {
		try {
			String setId = (String) req.get(GraphDACParams.collection_id.name());
			if (!manager.validateRequired(setId)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_SET_INVALID_SET_ID.name(),
						"Required parameters are missing...");
			} else {
				req.getContext().get(GraphDACParams.graph_id.name());
				SetCacheManager.dropSet(graphId, setId);

				Request dacRequest = new Request(req);
				dacRequest.put(GraphDACParams.collection_id.name(), setId);
				Future<Object> response = Futures.successful(graphMgr.deleteCollection(dacRequest));

				manager.returnResponse(response, getParent());
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	public void getNode(Request req) {
		try {
			if (!manager.validateRequired(this.getNodeId())) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_COLLECTION_MISSING_REQ_PARAMS.name(),
						"Required parameters are missing...");
			} else {
				Request request = new Request(req);
				request.put(GraphDACParams.node_id.name(), this.getNodeId());
				Response response = searchMgr.getNodeByUniqueId(request);
				manager.returnResponse(Futures.successful(response), getParent());
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	@Override
	public void getCardinality(Request req) {
		try {
			// String setId = (String)
			// req.get(GraphDACParams.collection_id.name());
			if (!manager.validateRequired(this.getNodeId())) {
				throw new ClientException(
						GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY_MISSING_REQ_PARAMS.name(),
						"Required parameters are missing...");
			} else {
				req.getContext().get(GraphDACParams.graph_id.name());
				Long cardinality = SetCacheManager.getSetCardinality(graphId, this.getNodeId());
				if (null != cardinality) {
					manager.OK(GraphDACParams.cardinality.name(), cardinality, getParent());
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY_MISSING_REQ_PARAMS.name(),
							"Failed to get cardinality", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				}
			}
		} catch (Exception e) {
			manager.handleException(e, getParent());
		}
	}

	@Override
	public String getSystemNodeType() {
		return SystemNodeTypes.SET.name();
	}

	public String getSetCriteria() {
		return setCriteria;
	}

	public List<Relation> getInRelations() {
		return inRelations;
	}

	public void setInRelations(List<Relation> inRelations) {
		this.inRelations = inRelations;
	}

	public List<Relation> getOutRelations() {
		return outRelations;
	}

	public void setOutRelations(List<Relation> outRelations) {
		this.outRelations = outRelations;
	}

	private void createSet(final Request req) {
		try {
			final ExecutionContext ec = manager.getContext().dispatcher();
			if (null != memberIds && memberIds.size() > 0) {
				Boolean valid = checkMemberNodes(req, memberIds, ec);
				if (valid) {
					createSetNode(req, ec);
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_INVALID_MEMBER_IDS.name(),
							"Member Ids are invalid", ResponseCode.CLIENT_ERROR, getParent());
				}
			} else {
				createSetNode(req, ec);
			}
		} catch (Exception e) {
			manager.ERROR(e, getParent());
		}
	}

	public void createSetNode(final Request req, final ExecutionContext ec) {
		Request request = new Request(req);
		request.put(GraphDACParams.node.name(), toNode());

		Response response = nodeMgr.addNode(request);

		if (manager.checkError(response)) {
			manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_UNKNOWN_ERROR.name(),
					manager.getErrorMessage(response), response.getResponseCode(), getParent());
		} else {
			String setId = (String) response.get(GraphDACParams.node_id.name());
			setNodeId(setId);
			if (null != memberIds && memberIds.size() > 0) {
				Request dacRequest = new Request(req);
				dacRequest.put(GraphDACParams.collection_id.name(), setId);
				dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
				dacRequest.put(GraphDACParams.members.name(), memberIds);
				graphMgr.createCollection(dacRequest);
			}
			req.getContext().get(GraphDACParams.graph_id.name());
			SetCacheManager.createSet(graphId, setId, memberIds);
			Node node = getSetObject(req);

			if (null != node) {
				updateRelations(req, node);
			}

			manager.OK(GraphDACParams.set_id.name(), setId, getParent());
		}

	}

	/**
	 * This method updates the relations, other than membership relation, for the
	 * Set node. The list of incoming and outgoing relations of the input Set object
	 * are created in this method. This method takes care of deleting any
	 * pre-existing relations of the Set node if they are not available in the input
	 * Set object.
	 * 
	 * Membership relation with the member nodes will not be created by this method.
	 * 
	 * @param req
	 *            the Request object with the request context
	 * @param node
	 *            the Set node object
	 */
	private void updateRelations(Request req, Node node) {
		Map<String, List<String>> delOutRels = getRelationsToDelete(node.getOutRelations(), getOutRelations(), true);
		deleteRelations(req, delOutRels, true);
		Map<String, List<String>> delInRels = getRelationsToDelete(node.getInRelations(), getInRelations(), false);
		deleteRelations(req, delInRels, false);
		createRelations(req, getOutRelations(), true);
		createRelations(req, getInRelations(), false);
	}

	/**
	 * Creates the specified list of incoming/outgoing relations of the Set.
	 * 
	 * @param req
	 *            the Request object with the request context
	 * @param rels
	 *            list of relations to be created
	 * @param out
	 *            boolean, to specify whether to create outgoing or incoming
	 *            relations
	 */
	private void createRelations(Request req, List<Relation> rels, boolean out) {
		if (null != rels && rels.size() > 0) {
			Map<String, List<String>> newRels = new HashMap<String, List<String>>();
			for (Relation rel : rels) {
				String type = rel.getRelationType();
				// set members should be added by explicitly sending as a
				// separate member. membership relations within out/in relations
				// will be ignored.
				if (!StringUtils.equalsIgnoreCase(RelationTypes.SET_MEMBERSHIP.relationName(), type)) {
					List<String> ids = newRels.get(rel);
					if (null == ids)
						ids = new ArrayList<String>();
					ids.add(out ? rel.getEndNodeId() : rel.getStartNodeId());
					newRels.put(type, ids);
				}
			}
			Request request = new Request(req);
			TelemetryManager.log("Creating " + (out ? "outgoing" : "incoming") + " relations | count: "+newRels.size());
			for (Entry<String, List<String>> entry : newRels.entrySet()) {
				if (out) {
					request.put(GraphDACParams.start_node_id.name(), getNodeId());
					request.put(GraphDACParams.relation_type.name(), entry.getKey());
					request.put(GraphDACParams.end_node_id.name(), entry.getValue());
					graphMgr.addOutgoingRelations(request);
				} else {
					request.put(GraphDACParams.start_node_id.name(), entry.getValue());
					request.put(GraphDACParams.relation_type.name(), entry.getKey());
					request.put(GraphDACParams.end_node_id.name(), getNodeId());
					graphMgr.addIncomingRelations(request);
				}
			}
		}
	}

	/**
	 * Deletes the specified list of incoming/outgoing relations of the Set.
	 * 
	 * @param req
	 *            the Request object with the request context
	 * @param delRels
	 *            list of relations to be deleted
	 * @param out
	 *            boolean, to specify whether to delete outgoing or incoming
	 *            relations
	 */
	private void deleteRelations(Request req, Map<String, List<String>> delRels, boolean out) {
		if (null != delRels && delRels.size() > 0) {
			TelemetryManager.log("Deleting " + (out ? "outgoing" : "incoming") + " relations | count: "+delRels.size());
			Request request = new Request(req);
			for (Entry<String, List<String>> entry : delRels.entrySet()) {
				if (out) {
					request.put(GraphDACParams.start_node_id.name(), getNodeId());
					request.put(GraphDACParams.relation_type.name(), entry.getKey());
					request.put(GraphDACParams.end_node_id.name(), entry.getValue());
					graphMgr.deleteOutgoingRelations(request);
				} else {
					request.put(GraphDACParams.start_node_id.name(), entry.getValue());
					request.put(GraphDACParams.relation_type.name(), entry.getKey());
					request.put(GraphDACParams.end_node_id.name(), getNodeId());
					graphMgr.deleteIncomingRelations(request);
				}
			}
		}
	}

	/**
	 * This method compares the current relations for the node in the database with
	 * the list of new relations to be created and returns the list of relations
	 * that need to be deleted.
	 * 
	 * @param dbRels
	 *            list of relations in the database
	 * @param rels
	 *            list of new relations to be created
	 * @param out
	 *            boolean, to specify whether to compare outgoing or incoming
	 *            relations
	 * @return the list of relations to be deleted
	 */
	private Map<String, List<String>> getRelationsToDelete(List<Relation> dbRels, List<Relation> rels, boolean out) {
		List<String> relList = new ArrayList<String>();
		if (null != rels && rels.size() > 0) {
			for (Relation rel : rels) {
				String relKey = rel.getStartNodeId() + rel.getRelationType() + rel.getEndNodeId();
				relList.add(relKey);
			}
		}
		Map<String, List<String>> delRels = new HashMap<String, List<String>>();
		if (null != dbRels && dbRels.size() > 0) {
			for (Relation dbRel : dbRels) {
				String relKey = dbRel.getStartNodeId() + dbRel.getRelationType() + dbRel.getEndNodeId();
				String type = dbRel.getRelationType();
				// do not delete any membership relations
				if (!relList.contains(relKey)
						&& !StringUtils.equalsIgnoreCase(RelationTypes.SET_MEMBERSHIP.relationName(), type)) {
					List<String> ids = delRels.get(type);
					if (null == ids)
						ids = new ArrayList<String>();
					ids.add(out ? dbRel.getEndNodeId() : dbRel.getStartNodeId());
					delRels.put(type, ids);
				}
			}
		}
		return delRels;
	}

	@Override
	public String getFunctionalObjectType() {
		return this.setObjectType;
	}

	public String getSetType() {
		return this.setType;
	}

	private Future<Object> addMemberToSet(Request req, String setId, String memberId) {
		req.getContext().get(GraphDACParams.graph_id.name());
		SetCacheManager.addSetMember(graphId, setId, memberId);

		Request dacRequest = new Request(req);
		dacRequest.setOperation("addRelation");
		dacRequest.put(GraphDACParams.start_node_id.name(), setId);
		dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
		dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
		Future<Object> dacResponse = Futures.successful(graphMgr.addRelation(dacRequest));
		return dacResponse;
	}

	private Object addMembersToSet(Request req, String setId, List<String> memberIds) {
		List<Response> response = new ArrayList<Response>();
		req.getContext().get(GraphDACParams.graph_id.name());
		SetCacheManager.addSetMembers(graphId, setId, memberIds);

		for (String memberId : memberIds) {
			Request dacRequest = new Request(req);
			dacRequest.put(GraphDACParams.start_node_id.name(), setId);
			dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
			dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
			Response dacResponse = graphMgr.addRelation(dacRequest);
			response.add(dacResponse);
		}
		return mergeResponses(response);
	}

	private Response removeMemberFromSet(Request req, String setId, String memberId) {
		Request dacRequest = new Request(req);
		dacRequest.put(GraphDACParams.start_node_id.name(), setId);
		dacRequest.put(GraphDACParams.relation_type.name(), new String(RelationTypes.SET_MEMBERSHIP.relationName()));
		dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
		Response dacResponse = graphMgr.deleteRelation(dacRequest);

		// get from redis cache
		req.getContext().get(GraphDACParams.graph_id.name());
		SetCacheManager.dropSet(graphId, setId);
		return dacResponse;
	}

	private void fromNode(Node node) {
		this.metadata = node.getMetadata();
		this.setObjectType = node.getObjectType();
		Map<String, Object> metadata = node.getMetadata();
		if (null != metadata) {
			this.setType = (String) metadata.get(SET_TYPE_KEY);
			this.setCriteria = (String) metadata.get(SET_CRITERIA_KEY);
			this.memberObjectType = (String) metadata.get(SET_OBJECT_TYPE_KEY);
			if (StringUtils.isNotBlank(setCriteria)) {
				try {
					SearchCriteria sc = mapper.readValue(setCriteria, SearchCriteria.class);
					this.criteria = sc;
				} catch (Exception e) {
				}
			}
		}
		this.inRelations = node.getInRelations();
		this.outRelations = node.getOutRelations();
	}

	/**
	 * @param response
	 * @return
	 */
	private Object mergeResponses(List<Response> response) {
		Object res = null;
		if (null != response) {
			for (Object obj : response) {
				res = obj;
			}
		}

		return res;
	}

}
