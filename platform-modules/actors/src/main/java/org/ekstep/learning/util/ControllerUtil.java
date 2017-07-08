package org.ekstep.learning.util;

import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

/**
 * The Class ControllerUtil, provides controller utility functionality for all
 * learning actors.
 *
 * @author karthik
 */
public class ControllerUtil extends BaseLearningManager {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Gets the node.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param contentId
	 *            the content id
	 * @return the node
	 */
	public Node getNode(String taxonomyId, String contentId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), contentId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		Response response = copyResponse(getNodeRes);
		if (checkError(response)) {
			return null;
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

	/**
	 * Update node.
	 *
	 * @param node
	 *            the node
	 * @return the response
	 */
	public Response updateNode(Node node) {
		Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
	}

	/**
	 * Gets the definition.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param objectType
	 *            the object type
	 * @return the definition
	 */
	public DefinitionDTO getDefinition(String taxonomyId, String objectType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	/**
	 * Adds out relations.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * 
	 * @param startNodeId
	 *            the startNodeId
	 * 
	 * @param endNodeIds
	 *            the list of endNodeIds
	 * 
	 * @param relationType
	 *            the relationType
	 * 
	 * @return the response
	 */
	public Response addOutRelations(String taxonomyId, String startNodeId, List<String> endNodeIds,
			String relationType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.GRAPH_MANAGER, "addOutRelations",
				GraphDACParams.start_node_id.name(), startNodeId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), endNodeIds);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	/**
	 * Gets the collection members.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param collectionId
	 *            the collectionId
	 * @param collectionType
	 *            the collectionType
	 * 
	 * @return the response
	 */
	public Response getCollectionMembers(String taxonomyId, String collectionId, String collectionType) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getCollectionMembers",
				GraphDACParams.collection_id.name(), collectionId);
		request.put(GraphDACParams.collection_type.name(), collectionType);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	public Response getSet(String taxonomyId, String collectionId) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.COLLECTION_MANAGER, "getSet",
				GraphDACParams.collection_id.name(), collectionId);
		request.put(GraphDACParams.collection_type.name(), taxonomyId);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	/**
	 * Gets Data nodes.
	 *
	 * @param taxonomyId
	 *            the taxonomy id
	 * @param node
	 *            the list of nodes
	 * 
	 * @return the response
	 */
	public Response getDataNodes(String taxonomyId, List<String> nodes) {
		Request request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
				GraphDACParams.node_ids.name(), nodes);
		Response response = getResponse(request, LOGGER);
		if (!checkError(response)) {
			return response;
		}
		return null;
	}

	public Response getHirerachy(String identifier) {
		String url = PropertiesUtil.getProperty("platform-api-url") + "/v2/content/hierarchy/" + identifier;
		Response hirerachyRes = null;
		try {
			String result = HTTPUtil.makeGetRequest(url);
			hirerachyRes = mapper.readValue(result, Response.class);
		} catch (Exception e) {
			LOGGER.log(" Error while getting the Hirerachy of the node " ,e.getMessage(),  e);
		}
		return hirerachyRes;
	}
}
