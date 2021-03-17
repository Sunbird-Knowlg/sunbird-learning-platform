package org.sunbird.graph.model.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.cache.mgr.impl.SequenceCacheManager;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.exception.GraphEngineErrorCodes;

import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;

public class Sequence extends AbstractCollection {

    private List<String> memberIds;


    public Sequence(BaseGraphManager manager, String graphId, String id) {
        super(manager, graphId, id, null); // TODO: Will add metadata if required.
    }

    public Sequence(BaseGraphManager manager, String graphId, String id, List<String> memberIds) {
        super(manager, graphId, id, null); // TODO: Will add metadata if required.
        this.memberIds = memberIds;
    }

    @Override
    public void create(final Request req) {
        try {
            final ExecutionContext ec = manager.getContext().dispatcher();
            if (null != memberIds && memberIds.size() > 0) {
				Boolean validMembers = checkMemberNodes(req, memberIds, ec);
				if (validMembers) {
					createSequenceObject(req, ec);
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_INVALID_MEMBERIDS.name(),
							"Member Ids are invalid", ResponseCode.CLIENT_ERROR, getParent());
				}
            } else {
                createSequenceObject(req, ec);
            }
        } catch (Exception e) {
            throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_UNKNOWN_ERROR.name(), e.getMessage(), e);
        }
    }

    @Override
    public void addMember(final Request req) {
        final String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
        final String memberId = (String) req.get(GraphDACParams.member_id.name());
        if (!manager.validateRequired(sequenceId, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_SEQUENCE_MEMBER_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
            	req.getContext().get(GraphDACParams.graph_id.name());
         		Long index = SequenceCacheManager.addSequenceMember(graphId, sequenceId, null, memberId);
                if(null == index){
                    manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_SEQUENCE_MEMBER_UNKNOWN_ERROR.name(),
                            null, ResponseCode.CLIENT_ERROR, getParent());
                } else {
                    Request dacRequest = new Request(req);
                    dacRequest.put(GraphDACParams.start_node_id.name(), sequenceId);
                    dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName());
                    dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
                    dacRequest.put(GraphDACParams.metadata.name(), map);
					manager.returnResponse(Futures.successful(graphMgr.addRelation(dacRequest)), getParent());
                }
            } catch (Exception e) {
                manager.handleException(e, getParent());
            }
        }
    }

    @Override
    public void removeMember(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            String memberId = (String) req.get(GraphDACParams.member_id.name());
            if (!manager.validateRequired(sequenceId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_SEQUENCE_MEMBER_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
            	req.getContext().get(GraphDACParams.graph_id.name());
         		SequenceCacheManager.removeSequenceMember(graphId, sequenceId, memberId);
                Request dacRequest = new Request(req);
                dacRequest.put(GraphDACParams.start_node_id.name(), sequenceId);
                dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName());
                dacRequest.put(GraphDACParams.end_node_id.name(), memberId);
				graphMgr.deleteRelation(dacRequest);
                manager.OK(GraphDACParams.sequence_id.name(), sequenceId, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void getMembers(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(sequenceId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_SEQUENCE_MEMBERS_INVALID_SEQID.name(),
                        "Required parameters are missing...");
            } else {
            	req.getContext().get(GraphDACParams.graph_id.name());
         		List<String> members = SequenceCacheManager.getSequenceMembers(graphId, sequenceId);
         		manager.OK(GraphDACParams.members.name(), members, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void isMember(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            String memberId = (String) req.get(GraphDACParams.member_id.name());
            if (!manager.validateRequired(sequenceId, memberId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IS_SEQUENCE_MEMBER_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
            	req.getContext().get(GraphDACParams.graph_id.name());
         		Boolean isMember= SequenceCacheManager.isSequenceMember(graphId, sequenceId, memberId);
         		manager.OK(GraphDACParams.is_member.name(), isMember, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void delete(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(sequenceId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_SEQUENCE_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
            	req.getContext().get(GraphDACParams.graph_id.name());
         		SequenceCacheManager.dropSequence(graphId, sequenceId);
                Request dacRequest = new Request(req);
                dacRequest.put(GraphDACParams.collection_id.name(), sequenceId);
				graphMgr.deleteCollection(dacRequest);
                manager.OK(GraphDACParams.sequence_id.name(), sequenceId, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public void getCardinality(Request req) {
        try {
            String sequenceId = (String) req.get(GraphDACParams.collection_id.name());
            if (!manager.validateRequired(sequenceId)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_CARDINALITY_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            } else {
            	req.getContext().get(GraphDACParams.graph_id.name());
         		Long cardinality= SequenceCacheManager.getSequenceCardinality(graphId, sequenceId);
         		manager.OK(GraphDACParams.cardinality.name(), cardinality, getParent());
            }
        } catch (Exception e) {
            manager.handleException(e, getParent());
        }
    }

    @Override
    public String getSystemNodeType() {
        return SystemNodeTypes.SEQUENCE.name();
    }

    private void createSequenceObject(final Request req, final ExecutionContext ec) {
        Request request = new Request(req);
        request.put(GraphDACParams.node.name(), toNode());

		Response res = nodeMgr.addNode(request);

		if (manager.checkError(res)) {
			manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SEQUENCE_UNKNOWN_ERROR.name(),
					manager.getErrorMessage(res), res.getResponseCode(), getParent());
		} else {
			String sequenceId = (String) res.get(GraphDACParams.node_id.name());
			req.getContext().get(GraphDACParams.graph_id.name());
			SequenceCacheManager.createSequence(graphId, sequenceId, memberIds);

			if (null != memberIds && memberIds.size() > 0) {
				Request dacRequest = new Request(req);
				dacRequest.put(GraphDACParams.collection_id.name(), sequenceId);
				dacRequest.put(GraphDACParams.relation_type.name(), RelationTypes.SEQUENCE_MEMBERSHIP.relationName());
				dacRequest.put(GraphDACParams.index.name(), SystemProperties.IL_SEQUENCE_INDEX.name());
				dacRequest.put(GraphDACParams.members.name(), memberIds);
				graphMgr.createCollection(dacRequest);
            }
			manager.OK(GraphDACParams.sequence_id.name(), sequenceId, getParent());
		}
    }

	@Override
	public void removeMembers(Request request) {
		// TODO Auto-generated method stub
		
	}
}
