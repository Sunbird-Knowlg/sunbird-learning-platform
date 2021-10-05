package org.sunbird.framework.mgr.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.framework.enums.CategoryEnum;
import org.sunbird.framework.mgr.ICategoryInstanceManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.common.enums.ContentErrorCodes;
import org.springframework.stereotype.Component;

/**
 * This is the entry point for all CRUD operations related to category Instance
 * API.
 * 
 * @author Rashmi
 *
 */
@Component
public class CategoryInstanceManagerImpl extends BaseFrameworkManager implements ICategoryInstanceManager
{

	private static final String CATEGORY_INSTANCE_OBJECT_TYPE = "CategoryInstance";

	private static final String GRAPH_ID = "domain";

	@Override
	public Response createCategoryInstance(String scopeId, Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get("code") || StringUtils.isBlank((String) request.get("code")))
			return ERROR("ERR_CATEGORY_INSTANCE_CODE_REQUIRED", "Unique code is mandatory for categoryInstance",
					ResponseCode.CLIENT_ERROR);
		validateCategoryNode((String)request.get("code"));
		String categoryId = generateIdentifier(scopeId, (String) request.get("code"));
		if (null != categoryId)
			request.put(CategoryEnum.identifier.name(), categoryId);
		setRelations(scopeId, request);
		Response response = create(request, CATEGORY_INSTANCE_OBJECT_TYPE);
		return response;
	}

	@Override
	public Response readCategoryInstance(String scopeId, String categoryCode) {
		String categoryId = generateIdentifier(scopeId, categoryCode);
		if (validateScopeNode(categoryId, scopeId))
			return read(categoryId, CATEGORY_INSTANCE_OBJECT_TYPE, CategoryEnum.category.name());
		else
			throw new ClientException(
					ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/"
							+ ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(),
					"Given channel/framework is not related to given category");
	}

	@Override
	public Response updateCategoryInstance(String scopeId, String categoryCode, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		String categoryId = generateIdentifier(scopeId, categoryCode);
		if (validateScopeNode(categoryId, scopeId))
			return update(categoryId, CATEGORY_INSTANCE_OBJECT_TYPE, map);
		else
			throw new ClientException(
					ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/"
							+ ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(),
					"Given channel/framework is not related to given category");
	}

	@Override
	public Response searchCategoryInstance(String categoryId, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return search(map, CATEGORY_INSTANCE_OBJECT_TYPE, "categories", categoryId);
	}

	@Override
	public Response retireCategoryInstance(String scopeId, String categoryCode) {
		String newCategoryInstanceId = generateIdentifier(scopeId, categoryCode);
		if (validateScopeNode(newCategoryInstanceId, scopeId))
			return retire(newCategoryInstanceId, CATEGORY_INSTANCE_OBJECT_TYPE);
		else
			throw new ClientException(
					ContentErrorCodes.ERR_CHANNEL_NOT_FOUND.name() + "/"
							+ ContentErrorCodes.ERR_FRAMEWORK_NOT_FOUND.name(),
					"Given channel/framework is not related to given category");
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
		}
		return false;
	}
	
	private void validateCategoryNode(String code) {
		Response response = getDataNode(GRAPH_ID, code);
		if(checkError(response)) 
			throw new ClientException(ContentErrorCodes.ERR_CATEGORY_NOT_FOUND.name() + "/"
					+ ContentErrorCodes.ERR_CATEGORY_NOT_FOUND.name(),
			"Given category does not belong to master category data");
	}
}