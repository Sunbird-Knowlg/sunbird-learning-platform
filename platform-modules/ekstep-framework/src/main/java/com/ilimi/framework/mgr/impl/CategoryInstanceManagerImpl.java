package com.ilimi.framework.mgr.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.framework.enums.CategoryEnum;
import com.ilimi.framework.mgr.ICategoryInstanceManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

/**
 * This is the entry point for all CRUD operations related to category Instance API.
 * 
 * @author Rashmi
 *
 */
@Component
public class CategoryInstanceManagerImpl extends BaseFrameworkManager implements ICategoryInstanceManager {

	private static final String CATEGORY_INSTANCE_OBJECT_TYPE = "CategoryInstance";
	

	private static final String GRAPH_ID = "domain";
	
	@Override
	public Response createCategoryInstance(String identifier, Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get("code") || StringUtils.isBlank((String)request.get("code")))
			return ERROR("ERR_CATEGORY_INSTANCE_CODE_REQUIRED", "Unique code is mandatory for categoryInstance", ResponseCode.CLIENT_ERROR);
		
		String id = generateIdentifier(identifier, (String) request.get("code"));
		if (null != id)
			request.put(CategoryEnum.identifier.name(), id);
		else
			throw new ServerException("ERR_SERVER_ERROR", "Unable to create CategoryInstanceId",
					ResponseCode.SERVER_ERROR);

		List<Relation> inRelations = setRelations(identifier);

		return create(request, CATEGORY_INSTANCE_OBJECT_TYPE, inRelations);
	}

	@Override
	public Response readCategoryInstance(String identifier, String categoryInstanceId) {
		if (validateScopeNode(identifier, categoryInstanceId)) {
			return read(categoryInstanceId, CATEGORY_INSTANCE_OBJECT_TYPE, CategoryEnum.categoryInstance.name());
		} else {
			throw new ClientException(ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/" + ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(), "Given channel/framework is not related to given category");
		}
	}

	@Override
	public Response updateCategoryInstance(String identifier, String categoryInstanceId, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (validateScopeNode(identifier, categoryInstanceId)) {
			return update(categoryInstanceId, CATEGORY_INSTANCE_OBJECT_TYPE, map);
		} else {
			throw new ClientException(ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/" + ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(), "Given channel/framework is not related to given category");
		}
	}

	@Override
	public Response searchCategoryInstance(String identifier, Map<String, Object> map) {
		return search(map, CATEGORY_INSTANCE_OBJECT_TYPE, "categoryInstances", identifier);
	}
	
	@Override
	public Response retireCategoryInstance(String identifier, String categoryInstanceId) {
		if (validateScopeNode(identifier, categoryInstanceId)) {
			return retire(identifier, CATEGORY_INSTANCE_OBJECT_TYPE);
		} else {
			throw new ClientException(
					ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/"
							+ ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(),
					"Given channel/framework is not related to given category");
		}

	}
	
	
	public boolean validateScopeId(String identifier) {
		if (StringUtils.isNotBlank(identifier)) {
			Response response = getDataNode(GRAPH_ID, identifier);
			if (checkError(response)) {
				return false;
			} else {
				Node node = (Node) response.get(GraphDACParams.node.name());
				if (StringUtils.equalsIgnoreCase(identifier, node.getIdentifier())) {
					return true;
				}
			}
		} else {
			throw new ClientException("ERR_INVALID_CHANNEL_ID/ERR_INVALID_FRAMEWORK_ID", "Required fields missing...");
		}
		return false;
	}
}