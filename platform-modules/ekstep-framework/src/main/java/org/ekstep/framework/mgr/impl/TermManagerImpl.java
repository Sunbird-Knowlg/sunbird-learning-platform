/**
 * 
 */
package org.ekstep.framework.mgr.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.springframework.stereotype.Component;

import org.ekstep.framework.enums.TermEnum;
import org.ekstep.framework.mgr.ITermManager;

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
	@SuppressWarnings("unchecked")
	@Override
	public Response createTerm(String scopeId, String categoryId, Map<String, Object> request) throws Exception{
		if (null == request)
			return ERROR("ERR_INVALID_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		String code = (String) request.get(TermEnum.code.name());
		if (StringUtils.isBlank(code))
			return ERROR("ERR_TERM_LABEL_REQUIRED", "Unique code is required for Term", ResponseCode.CLIENT_ERROR);

		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
			//validateMasterTerm(categoryId, label);  commented to remove term validation with master term
		} else {
			validateCategoryId(categoryId);
		}

		String id = generateIdentifier(categoryId, code);

		if (null != id)
			request.put(TermEnum.identifier.name(), id);
		else
			throw new ServerException("ERR_SERVER_ERROR", "Unable to create TermId", ResponseCode.SERVER_ERROR);

		if(!request.containsKey(TermEnum.parents.name()) || ((List<Object>)request.get(TermEnum.parents.name())).isEmpty())
			setRelations(categoryId, request);
		request.put("category", categoryId);
		Response response = create(request, TERM_OBJECT_TYPE);
		if(response.getResponseCode() == ResponseCode.OK) {
			generateFrameworkHierarchy(id);
		}
		return response;
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
		//if (validateScopeNode(termId, categoryId)) {
			return read(termId, TERM_OBJECT_TYPE, TermEnum.term.name());
		//} else {
		//	throw new ClientException("ERR_CATEGORY_NOT_FOUND", "Category/CategoryInstance is not related Term");
		//}

	}

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#updateCategory(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response updateTerm(String scopeId, String categoryId, String termId, Map<String, Object> request) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (request.containsKey(TermEnum.code.name()))
			return ERROR("ERR_SERVER_ERROR", "Term Code cannot be updated", ResponseCode.SERVER_ERROR);
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
		} else {
			validateCategoryId(categoryId);
		}
		termId = generateIdentifier(categoryId, termId);
		
		if(!request.containsKey(TermEnum.parents.name()) || ((List<Object>)request.get(TermEnum.parents.name())).isEmpty()) {
			setRelations(categoryId, request);
			request.put(TermEnum.parents.name(), null);
		}else {
			Response responseNode = getDataNode(GRAPH_ID, categoryId);
			Node dataNode = (Node) responseNode.get(GraphDACParams.node.name());
			String objectType = dataNode.getObjectType();
			if(StringUtils.equalsIgnoreCase(StringUtils.lowerCase(objectType), TermEnum.categoryinstances.name())) {
				request.put(TermEnum.categoryinstances.name(), null);
			}
		}
		Response response = update(termId, TERM_OBJECT_TYPE, request);
		if(response.getResponseCode() == ResponseCode.OK) {
			generateFrameworkHierarchy(termId);
		}
		return response;
		
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
		return search(map, TERM_OBJECT_TYPE, TermEnum.terms.name(), categoryId);
	}

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.ITermManager#retireTerm(java.lang.String, java.lang.String)
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
			}else {
				throw new ClientException("ERR_INVALID_TERM_ID", "Ivalid Term Id.");
			}
		} else {
			throw new ClientException("ERR_INVALID_TERM_ID", "Ivalid Term Id.");
		}

	}

}