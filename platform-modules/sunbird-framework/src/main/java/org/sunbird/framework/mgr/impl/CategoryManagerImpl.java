package org.sunbird.framework.mgr.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.framework.enums.CategoryEnum;
import org.sunbird.framework.mgr.ICategoryManager;
import org.springframework.stereotype.Component;

/**
 * This is the entry point for all CRUD operations related to category API.
 * 
 * @author Rashmi
 *
 */
@Component
public class CategoryManagerImpl extends BaseFrameworkManager implements ICategoryManager {

	private static final String CATEGORY_OBJECT_TYPE = "Category";

	@Override
	public Response createCategory(Map<String, Object> request) {
		if (null == request)
			throw new ClientException("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request");
		String code = (String) request.get("code");
		if (null == code || StringUtils.isBlank(code))
			throw new ClientException("ERR_CATEGORY_CODE_REQUIRED", "Unique code is mandatory for category");
		request.put("identifier", code);
		return create(request, CATEGORY_OBJECT_TYPE);
	}
	
	@Override
	public Response readCategory(String categoryId) {
		return read(categoryId, CATEGORY_OBJECT_TYPE, CategoryEnum.category.name());
	}

	@Override
	public Response updateCategory(String categoryId, Map<String, Object> map) {
		if (null == map)
			throw new ClientException("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request");
		if (map.containsKey("code"))
			throw new ClientException("ERR_CATEGORY_UPDATE", "code updation is not allowed.");
		return update(categoryId, CATEGORY_OBJECT_TYPE, map);

	}

	@Override
	public Response searchCategory(Map<String, Object> map) {
		if (null == map)
			throw new ClientException("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request");
		return search(map, CATEGORY_OBJECT_TYPE, "categories", null);
	}

	@Override
	public Response retireCategory(String categoryId) {
		return retire(categoryId, CATEGORY_OBJECT_TYPE);
	}

}