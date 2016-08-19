package com.ilimi.graph.dac.mgr.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.factory.GraphServiceFactory;
import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACNodeMgr;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import akka.actor.ActorRef;

public class GraphDACNodeMgrImpl extends BaseGraphManager implements IGraphDACNodeMgr {
	
	private static Logger LOGGER = LogManager.getLogger(GraphDACNodeMgrImpl.class.getName());

	static IGraphDatabaseService service;
	static {
		String databasePolicy = DACConfigurationConstants.ACTIVE_DATABASE_POLICY;

		LOGGER.info("Active Database Policy Id:" + databasePolicy);

		if (StringUtils.isBlank(databasePolicy))
			databasePolicy = DACConfigurationConstants.DEFAULT_DATABASE_POLICY;

		LOGGER.info("Creating Database Connection Using Policy Id:" + databasePolicy);

		service = GraphServiceFactory.getDatabaseService(databasePolicy);
	}

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphDACActorPoolMgr.getMethod(GraphDACManagers.DAC_NODE_MANAGER, methodName);
            if (null == method) {
                throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_INVALID_OPERATION.name(), "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    @Override
    public void upsertNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
        if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Invalid input node");
        else {
            try {
                service.upsertNode(graphId, node, request);
                OK(GraphDACParams.node_id.name(), node.getIdentifier(), getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @Override
    public void addNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
        if (null == node || StringUtils.isBlank(node.getNodeType()))
            throw new ClientException(GraphDACErrorCodes.ERR_CREATE_NODE_MISSING_REQ_PARAMS.name(), "Invalid input node");
        else {
            try {
                service.addNode(graphId, node, request);
                OK(GraphDACParams.node_id.name(), node.getIdentifier(), getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @Override
    public void updateNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        com.ilimi.graph.dac.model.Node node = (com.ilimi.graph.dac.model.Node) request.get(GraphDACParams.node.name());
        if (null == node || StringUtils.isBlank(node.getNodeType()) || StringUtils.isBlank(node.getIdentifier()))
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Invalid input node");
        else {
            try {
                service.updateNode(graphId, node, request);
                OK(GraphDACParams.node_id.name(), node.getIdentifier(), getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void importNodes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<com.ilimi.graph.dac.model.Node> nodes = (List<com.ilimi.graph.dac.model.Node>) request
                .get(GraphDACParams.node_list.name());
        if (!validateRequired(nodes))
            throw new ClientException(GraphDACErrorCodes.ERR_IMPORT_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        else {
            try {
                service.importNodes(graphId, nodes, request);
                OK(getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @Override
    public void updatePropertyValue(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Property property = (Property) request.get(GraphDACParams.metadata.name());
        if (!validateRequired(nodeId, property)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
                service.updatePropertyValue(graphId, nodeId, property, request);
                OK(getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updatePropertyValues(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
        if (!validateRequired(nodeId, metadata)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
                service.updatePropertyValues(graphId, nodeId, metadata, request);
                OK(getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @Override
    public void removePropertyValue(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(nodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
                service.removePropertyValue(graphId, nodeId, key, request);
                OK(getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removePropertyValues(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        List<String> keys = (List<String>) request.get(GraphDACParams.property_keys.name());
        if (!validateRequired(nodeId, keys)) {
            throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
                service.removePropertyValues(graphId, nodeId, keys, request);
                OK(getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

    @Override
    public void deleteNode(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_DELETE_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
                service.deleteNode(graphId, nodeId, request);
                OK(getSender());
            } catch (Exception e) {
                ERROR(e, getSender());
            }
        }
    }

}
