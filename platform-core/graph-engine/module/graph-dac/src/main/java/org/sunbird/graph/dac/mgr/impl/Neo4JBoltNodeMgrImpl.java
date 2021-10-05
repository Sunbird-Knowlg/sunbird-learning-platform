package org.sunbird.graph.dac.mgr.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseDACMgr;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.exception.GraphDACErrorCodes;
import org.sunbird.graph.dac.mgr.IGraphDACNodeMgr;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.operation.Neo4JBoltNodeOperations;

/**
 * The Class GraphDACNodeMgrImpl.
 * 
 * @author Mohammad Azharuddin
 */
public class Neo4JBoltNodeMgrImpl extends BaseDACMgr implements IGraphDACNodeMgr {


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#upsertNode(org.sunbird.common.dto.
	 * Request)
	 */
	@Override
	public Response upsertNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		org.sunbird.graph.dac.model.Node node = (org.sunbird.graph.dac.model.Node) request.get(GraphDACParams.node.name());
		if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Invalid input node");
		else {
			try {
				org.sunbird.graph.dac.model.Node upsertedNode = Neo4JBoltNodeOperations.upsertNode(graphId, node,
						request);
				
				// Creating Map for Response Values
				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.put(GraphDACParams.node_id.name(), upsertedNode.getIdentifier());
				if (null != upsertedNode && null != upsertedNode.getMetadata()) {
					responseMap.put(GraphDACParams.versionKey.name(),
							upsertedNode.getMetadata().get(GraphDACParams.versionKey.name()));

					// Checking if Stale Data has been updated in Node
					if (null != upsertedNode.getMetadata().get(GraphDACParams.NODE_UPDATE_STATUS.name())
							&& StringUtils
									.equalsIgnoreCase(
											((String) upsertedNode.getMetadata()
													.get(GraphDACParams.NODE_UPDATE_STATUS.name())),
											GraphDACParams.STALE_DATA_UPDATED.name()))
						throw new ClientException(DACErrorCodeConstants.STALE_DATA.name(),
								DACErrorMessageConstants.STALE_DATA_UPDATED_WARNING + " | [Node Id: "
										+ node.getIdentifier() + " and Graph Id: " + graphId + "]");
				}

				return OK(responseMap);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#addNode(org.sunbird.common.dto.
	 * Request)
	 */
	@Override
	public Response addNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		org.sunbird.graph.dac.model.Node node = (org.sunbird.graph.dac.model.Node) request.get(GraphDACParams.node.name());
		if (null == node || StringUtils.isBlank(node.getNodeType()))
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(),
					"Invalid input node");
		else {
			try {
				org.sunbird.graph.dac.model.Node addedNode = Neo4JBoltNodeOperations.addNode(graphId, node, request);
				
				// Creating Map for Response Values
				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.put(GraphDACParams.node_id.name(), addedNode.getIdentifier());
				if (null != addedNode && null != addedNode.getMetadata())
					responseMap.put(GraphDACParams.versionKey.name(),
							addedNode.getMetadata().get(GraphDACParams.versionKey.name()));
				
				return OK(responseMap);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#updateNode(org.sunbird.common.dto.
	 * Request)
	 */
	@Override
	public Response updateNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		org.sunbird.graph.dac.model.Node node = (org.sunbird.graph.dac.model.Node) request.get(GraphDACParams.node.name());
		if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Invalid input node");
		else {
			try {
				org.sunbird.graph.dac.model.Node updatedNode = Neo4JBoltNodeOperations.updateNode(graphId, node,
						request);
				
				// Creating Map for Response Values
				Map<String, Object> responseMap = new HashMap<String, Object>();
				responseMap.put(GraphDACParams.node_id.name(), updatedNode.getIdentifier());
				if (null != updatedNode && null != updatedNode.getMetadata()) {
					responseMap.put(GraphDACParams.versionKey.name(),
							updatedNode.getMetadata().get(GraphDACParams.versionKey.name()));

					// Checking if Stale Data has been updated in Node
					if (null != updatedNode.getMetadata().get(GraphDACParams.NODE_UPDATE_STATUS.name())
							&& StringUtils
									.equalsIgnoreCase(
											((String) updatedNode.getMetadata()
													.get(GraphDACParams.NODE_UPDATE_STATUS.name())),
											GraphDACParams.STALE_DATA_UPDATED.name()))
						throw new ClientException(DACErrorCodeConstants.STALE_DATA.name(),
								DACErrorMessageConstants.STALE_DATA_UPDATED_WARNING + " | [Node Id: "
										+ node.getIdentifier() + " and Graph Id: " + graphId + "]");
				}
				
				return OK(responseMap);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#importNodes(org.sunbird.common.dto
	 * .Request)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Response importNodes(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<org.sunbird.graph.dac.model.Node> nodes = (List<org.sunbird.graph.dac.model.Node>) request
				.get(GraphDACParams.node_list.name());
		if (!validateRequired(nodes))
			throw new ClientException(GraphDACErrorCodes.ERR_IMPORT_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		else {
			try {
				Neo4JBoltNodeOperations.importNodes(graphId, nodes, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#updatePropertyValue(org.sunbird.
	 * common.dto.Request)
	 */
	@Override
	public Response updatePropertyValue(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		Property property = (Property) request.get(GraphDACParams.metadata.name());
		if (!validateRequired(nodeId, property)) {
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			try {
				Neo4JBoltNodeOperations.updatePropertyValue(graphId, nodeId, property, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#updatePropertyValues(org.sunbird.
	 * common.dto.Request)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response updatePropertyValues(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
		if (!validateRequired(nodeId, metadata)) {
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			try {
				Neo4JBoltNodeOperations.updatePropertyValues(graphId, nodeId, metadata, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#removePropertyValue(org.sunbird.
	 * common.dto.Request)
	 */
	@Override
	public Response removePropertyValue(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		String key = (String) request.get(GraphDACParams.property_key.name());
		if (!validateRequired(nodeId, key)) {
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			try {
				Neo4JBoltNodeOperations.removePropertyValue(graphId, nodeId, key, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#removePropertyValues(org.sunbird.
	 * common.dto.Request)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response removePropertyValues(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		List<String> keys = (List<String>) request.get(GraphDACParams.property_keys.name());
		if (!validateRequired(nodeId, keys)) {
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			try {
				Neo4JBoltNodeOperations.removePropertyValues(graphId, nodeId, keys, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#deleteNode(org.sunbird.common.dto.
	 * Request)
	 */
	@Override
	public Response deleteNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		if (!validateRequired(nodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			try {
				Neo4JBoltNodeOperations.deleteNode(graphId, nodeId, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.graph.dac.mgr.IGraphDACNodeMgr#upsertRootNode(org.sunbird.common.
	 * dto.Request)
	 */
	@Override
	public Response upsertRootNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		try {
			Neo4JBoltNodeOperations.upsertRootNode(graphId, request);
			return OK();
		} catch (Exception e) {
			return ERROR(e);
		}
	}

}
