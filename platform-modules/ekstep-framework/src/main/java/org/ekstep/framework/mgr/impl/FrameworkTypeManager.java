/**
 * 
 */
package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.framework.mgr.IFrameworkTypeManager;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.springframework.stereotype.Component;

/**
 * @author mahesh
 *
 */

@Component
public class FrameworkTypeManager extends BaseFrameworkManager implements IFrameworkTypeManager {

	private static final String FRAMEWORKTYPE_OBJECT_TYPE = "FrameworkType";
	private static final String DEFAULT_ERR_CODE = "ERR_INVALID_FRMAEWORKTYPE_OBJECT";
	private static Map<String, Object> fwTypes = new HashMap<String, Object>();
	

	/**
	 * To create frameworktype.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Response create(Map<String, Object> request) throws Exception {
		if (null == request)
			throw new ClientException(DEFAULT_ERR_CODE, "Invalid Request", ResponseCode.CLIENT_ERROR);

		String code = (String) request.get("code");
		if (StringUtils.isBlank(code))
			throw new ClientException(DEFAULT_ERR_CODE, "Unique code is mandatory for framework type",
					ResponseCode.CLIENT_ERROR);

		request.put("identifier", code);
		Response response = create(request, FRAMEWORKTYPE_OBJECT_TYPE);
		return response;
	}

	public Response update(String id, Map<String, Object> map) throws Exception {
		return null;
	}

	/**
	 * To list all the frameworktypes.
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Response list(Map<String, Object> map, boolean updateCache) throws Exception {
		if (updateCache) {
			Request request = getRequest(GRAPH_ID, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType",
					GraphDACParams.object_type.name(), FRAMEWORKTYPE_OBJECT_TYPE);
			Response graphRes = getResponse(request);
			if (checkError(graphRes)) {
				return graphRes;
			} else {
				DefinitionDTO definition = getDefinition(GRAPH_ID, FRAMEWORKTYPE_OBJECT_TYPE);
				List<Node> nodes = (List<Node>) graphRes.get(GraphDACParams.node_list.name());
				
				for (Node node : nodes) {
					Map<String, Object> fwType = ConvertGraphNode.convertGraphNode(node, GRAPH_ID, definition, null);
					String id = (String) fwType.get("identifier");
					fwTypes.put(id, fwType);
				}
			}
		}
		Response response = new Response();
		response.setParams(getSucessStatus());
		response.put("frameworktypes", fwTypes.values());
		return response;
	}

	@Override
	public Map<String, Object> getAll() {
		return fwTypes;
	}

}
