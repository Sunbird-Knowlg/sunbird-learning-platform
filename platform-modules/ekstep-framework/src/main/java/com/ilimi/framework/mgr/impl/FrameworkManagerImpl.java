package com.ilimi.framework.mgr.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.framework.enums.FrameworkEnum;
import com.ilimi.framework.mgr.IFrameworkManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class <code>FrameworkManagerImpl</code> is the implementation of
 * <code>IFrameworkManager</code> for all the operation including CRUD operation
 * and High Level Operations.
 * 
 * 
 * @author gauraw
 *
 */
@Component
public class FrameworkManagerImpl extends BaseFrameworkManager implements IFrameworkManager {

	private static final String FRAMEWORK_OBJECT_TYPE = "Framework";


	/*
	 * create framework
	 * 
	 * @param Map request
	 * 
	 */
	@Override
	public Response createFramework(Map<String, Object> request) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_FRMAEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return create(request, FRAMEWORK_OBJECT_TYPE, null);
	}

	/*
	 * Read framework by Id
	 * 
	 * @param graphId
	 * 
	 * @param frameworkId
	 * 
	 */
	@Override
	public Response readFramework(String graphId, String frameworkId) throws Exception {
		return read(frameworkId, FRAMEWORK_OBJECT_TYPE, FrameworkEnum.framework.name());
	}

	/*
	 * Update Framework Details
	 * 
	 * @param frameworkId
	 * 
	 * @param Map<String,Object> map
	 * 
	 */
	@Override
	public Response updateFramework(String frameworkId, String channelId, Map<String, Object> map) throws Exception {
		Response getNodeResponse = getDataNode(GRAPH_ID, frameworkId);
		if (checkError(getNodeResponse))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : "+frameworkId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		String owner = (String) graphNode.getMetadata().get("owner");
		if (!(channelId.equalsIgnoreCase(owner))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Owner Information Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}
		return update(frameworkId, FRAMEWORK_OBJECT_TYPE, map);

	}

	/*
	 * Read list of all framework based on criteria
	 * 
	 * @param Map<String,Object> map
	 * 
	 */
	@Override
	public Response listFramework(Map<String, Object> map) throws Exception {
		if (map == null)
			return ERROR("ERR_INVALID_SEARCH_REQUEST", "Invalid Search Request", ResponseCode.CLIENT_ERROR);

		return search(map, FRAMEWORK_OBJECT_TYPE, "frameworks", null);

	}

	/*
	 * Retire Framework - will update the status From "Live" to "Retire"
	 * 
	 * @param frameworkId
	 * 
	 */

	@Override
	public Response retireFramework(String frameworkId, String channelId) throws Exception {
		Response getNodeResponse = getDataNode(GRAPH_ID, frameworkId);
		if (checkError(getNodeResponse))
			throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
					"Framework Not Found With Id : "+frameworkId);
		Node frameworkNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		
		String owner = (String) frameworkNode.getMetadata().get("owner");

		if (!(channelId.equalsIgnoreCase(owner))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Owner Information Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}
		
		return retire(frameworkId, FRAMEWORK_OBJECT_TYPE);

	}

}