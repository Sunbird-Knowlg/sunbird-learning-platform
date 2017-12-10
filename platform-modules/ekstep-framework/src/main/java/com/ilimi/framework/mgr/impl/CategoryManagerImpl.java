package com.ilimi.framework.mgr.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.framework.enums.CategoryEnum;
import com.ilimi.framework.mgr.ICategoryManager;

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
			return ERROR("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		String code = (String) request.get("code");
		if (null == code || StringUtils.isBlank(code))
			return ERROR("ERR_CATEGORY_CODE_REQUIRED", "Unique code is mandatory for category",
					ResponseCode.CLIENT_ERROR);
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
			return ERROR("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return update(categoryId, CATEGORY_OBJECT_TYPE, map);

	}

	@Override
	public Response searchCategory(Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CATEGORY_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return search(map, CATEGORY_OBJECT_TYPE, "categories", null);
	}

	@Override
	public Response retireCategory(String categoryId) {
		return retire(categoryId, CATEGORY_OBJECT_TYPE);
	}

}