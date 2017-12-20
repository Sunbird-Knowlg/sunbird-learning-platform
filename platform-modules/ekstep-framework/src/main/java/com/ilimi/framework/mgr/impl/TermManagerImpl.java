/**
 * 
 */
package com.ilimi.framework.mgr.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.framework.enums.TermEnum;
import com.ilimi.framework.mgr.ITermManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

/**
 * @author pradyumna
 *
 */
@Component
public class TermManagerImpl extends BaseFrameworkManager implements ITermManager {

	private static final String TERM_OBJECT_TYPE = "Term";

	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.ITermManager#createTerm(java.lang.String, java.util.Map)
	 */
	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.ITermManager#createTerm(java.lang.String, java.util.Map)
	 */
	@Override
	public Response createTerm(String scopeId, String categoryId, Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		String label = (String) request.get(TermEnum.label.name());
		if (StringUtils.isBlank(label))
			return ERROR("ERR_TERM_LABEL_REQUIRED", "Unique Label is required for Term", ResponseCode.CLIENT_ERROR);

		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
			validateMasterTerm(categoryId, label);
		} else {
			validateCategoryId(categoryId);
		}

		String id = generateIdentifier(categoryId, label);

		if (null != id)
			request.put(TermEnum.identifier.name(), id);
		else
			throw new ServerException("ERR_SERVER_ERROR", "Unable to create TermId", ResponseCode.SERVER_ERROR);

		setRelations(categoryId, request);
		return create(request, TERM_OBJECT_TYPE);
	}


	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#readTerm(java.lang.String, java.lang.String)
	 */
	@Override
	public Response readTerm(String scopeId, String termId, String categoryId) {
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
		} else {
			validateCategoryId(categoryId);
		}
		termId = generateIdentifier(categoryId, termId);
		if (validateScopeNode(termId, categoryId)) {
			return read(termId, TERM_OBJECT_TYPE, TermEnum.term.name());
		} else {
			throw new ClientException("ERR_CATEGORY_NOT_FOUND", "Category/CategoryInstance is not related Term");
		}

	}

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#updateCategory(java.lang.String, java.util.Map)
	 */
	@Override
	public Response updateTerm(String scopeId, String categoryId, String termId, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (map.containsKey(TermEnum.label.name()))
			return ERROR("ERR_SERVER_ERROR", "Term Label cannot be updated", ResponseCode.SERVER_ERROR);
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
		} else {
			validateCategoryId(categoryId);
		}
		termId = generateIdentifier(categoryId, termId);
		if (validateScopeNode(termId, categoryId)) {
			return update(termId, TERM_OBJECT_TYPE, map);
		} else {
			throw new ClientException("ERR_CATEGORY_NOT_FOUND", "Category/CategoryInstance is not related Term");
		}

	}

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#searchTerms(java.lang.String, java.util.Map)
	 */
	@Override
	public Response searchTerms(String scopeId, String categoryId, Map<String, Object> map) {
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
		} else {
			validateCategoryId(categoryId);
		}
		return search(map, TERM_OBJECT_TYPE, "terms", categoryId);
	}

	/* (non-Javadoc)
	 * @see com.ilimi.framework.mgr.ITermManager#retireTerm(java.lang.String, java.lang.String)
	 */
	@Override
	public Response retireTerm(String scopeId, String categoryId, String termId) {
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
		} else {
			validateCategoryId(categoryId);
		}
		termId = generateIdentifier(categoryId, termId);
		if (validateScopeNode(termId, categoryId)) {
			return retire(termId, TERM_OBJECT_TYPE);
		} else {
			throw new ClientException("ERR_CATEGORY_NOT_FOUND", "Category/CategoryInstance is not related Term");
		}
	}

	public void validateRequest(String scope, String categoryId) {
		Boolean valid = false;
		if (StringUtils.isNotBlank(scope) && StringUtils.isNotBlank(categoryId)) {
			Response categoryResp = getDataNode(GRAPH_ID, categoryId);
			if (!checkError(categoryResp)) {
				Node node = (Node) categoryResp.get(GraphDACParams.node.name());
				if (StringUtils.equalsIgnoreCase(categoryId, node.getIdentifier())) {
					List<Relation> inRelation = node.getInRelations();
					if (!inRelation.isEmpty()) {
						for (Relation relation : inRelation) {
							if (StringUtils.equalsIgnoreCase(scope, relation.getStartNodeId())) {
								valid = true;
								break;
							}
						}
					}
				}
			} else {
				valid = false;
			}
			if (!valid)
				throw new ClientException("ERR_INVALID_CATEGORY_ID", "Required fields missing...");
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Required fields missing...");
		}

	}

	private void validateCategoryId(String categoryId) {
		if (StringUtils.isNotBlank(categoryId)) {
			Response categoryResp = getDataNode(GRAPH_ID, categoryId);
			if (!checkError(categoryResp)) {
				Node node = (Node) categoryResp.get(GraphDACParams.node.name());
				if (!StringUtils.equalsIgnoreCase(categoryId, node.getIdentifier())) {
					throw new ClientException("ERR_INVALID_CATEGORY_ID", "Required fields missing...");
				}
			}
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Required fields missing...");
		}
	}
	
	/**
	 * Validate Term against Master Term (Term under Master Category)
	 * If term in the request exist in Master Category,then term will be valid else invalid.
	 * 
	 * @author gauraw
	 * 
	 * */
	public void validateMasterTerm(String categoryId, String termLabel) {
		if (StringUtils.isNotBlank(termLabel)) {
			String temp[]=categoryId.split("_");
			String termId=generateIdentifier(temp[temp.length-1],termLabel);
			Response termResp = getDataNode(GRAPH_ID, termId);
			if (!checkError(termResp)) {
				Node node = (Node) termResp.get(GraphDACParams.node.name());
				if (!StringUtils.equalsIgnoreCase(termId, node.getIdentifier())) {
					throw new ClientException("ERR_INVALID_TERM_ID", "Ivalid Term Id.");
				}
			}
		} else {
			throw new ClientException("ERR_INVALID_TERM_ID", "Ivalid Term Id.");
		}

	}

}