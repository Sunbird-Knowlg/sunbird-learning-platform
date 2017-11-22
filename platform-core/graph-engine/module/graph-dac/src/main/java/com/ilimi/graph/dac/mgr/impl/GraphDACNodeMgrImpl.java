package com.ilimi.graph.dac.mgr.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.service.INeo4JBoltNodeOperations;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.operation.Neo4JBoltNodeOperations;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.GraphDACMgr;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACNodeMgr;

/**
 * The Class GraphDACNodeMgrImpl.
 * 
 * @author Mohammad Azharuddin
 */
public class GraphDACNodeMgrImpl extends GraphDACMgr implements IGraphDACNodeMgr {

	private static INeo4JBoltNodeOperations service = new Neo4JBoltNodeOperations();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#upsertNode(com.ilimi.common.dto.
	 * Request)
	 */
	@Override
	public Response upsertNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
		if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Invalid input node");
		else {
			try {
				com.ilimi.graph.dac.model.Node upsertedNode = service.upsertNode(graphId, node, request);
				
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#addNode(com.ilimi.common.dto.
	 * Request)
	 */
	@Override
	public Response addNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
		if (null == node || StringUtils.isBlank(node.getNodeType()))
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(),
					"Invalid input node");
		else {
			try {
				com.ilimi.graph.dac.model.Node addedNode = service.addNode(graphId, node, request);
				
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#updateNode(com.ilimi.common.dto.
	 * Request)
	 */
	@Override
	public Response updateNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
		if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Invalid input node");
		else {
			try {
				com.ilimi.graph.dac.model.Node updatedNode = service.updateNode(graphId, node, request);
				
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#importNodes(com.ilimi.common.dto
	 * .Request)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Response importNodes(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<com.ilimi.graph.dac.model.Node> nodes = (List<com.ilimi.graph.dac.model.Node>) request
				.get(GraphDACParams.node_list.name());
		if (!validateRequired(nodes))
			throw new ClientException(GraphDACErrorCodes.ERR_IMPORT_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		else {
			try {
				service.importNodes(graphId, nodes, request);
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#updatePropertyValue(com.ilimi.
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
				service.updatePropertyValue(graphId, nodeId, property, request);
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#updatePropertyValues(com.ilimi.
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
				service.updatePropertyValues(graphId, nodeId, metadata, request);
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#removePropertyValue(com.ilimi.
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
				service.removePropertyValue(graphId, nodeId, key, request);
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#removePropertyValues(com.ilimi.
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
				service.removePropertyValues(graphId, nodeId, keys, request);
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#deleteNode(com.ilimi.common.dto.
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
				service.deleteNode(graphId, nodeId, request);
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
	 * com.ilimi.graph.dac.mgr.IGraphDACNodeMgr#upsertRootNode(com.ilimi.common.
	 * dto.Request)
	 */
	@Override
	public Response upsertRootNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		try {
			service.upsertRootNode(graphId, request);
			return OK();
		} catch (Exception e) {
			return ERROR(e);
		}
	}

}
