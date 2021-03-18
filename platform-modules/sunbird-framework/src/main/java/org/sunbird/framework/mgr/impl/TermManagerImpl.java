/**
 * 
 */
package org.sunbird.framework.mgr.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.framework.enums.TermEnum;
import org.sunbird.framework.mgr.ITermManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.springframework.stereotype.Component;

/**
 * @author pradyumna
 *
 */
@Component
public class TermManagerImpl extends BaseFrameworkManager implements ITermManager {

	private static final String TERM_OBJECT_TYPE = "Term";
	private static final int TERM_CREATION_LIMIT = Platform.config.hasPath("framework.max_term_creation_limit")
			? Platform.config.getInt("framework.max_term_creation_limit")
			: 200;

	@SuppressWarnings("unchecked")
	@Override
	public Response createTerm(String scopeId, String category, Request req) throws Exception {
		List<Map<String, Object>> requestList = getRequestData(req);
		if (null == req.get(TermEnum.term.name()) || null == requestList || requestList.isEmpty())
			throw new ClientException("ERR_INVALID_TERM_OBJECT", "Invalid Request");

		if (TERM_CREATION_LIMIT < requestList.size())
			throw new ClientException("ERR_INVALID_TERM_REQUEST",
					"No. of request exceeded max limit of " + TERM_CREATION_LIMIT);

		String categoryId = category;
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId, category);
			// validateMasterTerm(categoryId, label); commented to remove term validation
			// with master term
		} else {
			validateCategoryId(categoryId);
		}

		int codeError = 0;
		int serverError = 0;
		String id = null;
		List<String> identifiers = new ArrayList<String>();

		for (Map<String, Object> request : requestList) {
			String code = (String) request.get(TermEnum.code.name());
			if (StringUtils.isNotBlank(code)) {
				id = generateIdentifier(categoryId, code);
				if (null != id)
					request.put(TermEnum.identifier.name(), id);
				else {
					serverError += 1;
					continue;
				}

				if (!request.containsKey(TermEnum.parents.name())
						|| ((List<Object>) request.get(TermEnum.parents.name())).isEmpty())
					setRelations(categoryId, request);
				request.put("category", category);
				Response resp = create(request, TERM_OBJECT_TYPE);
				if (!checkError(resp)) {
					identifiers.add((String) resp.getResult().get("node_id"));
				} else {
					if ((StringUtils.equalsIgnoreCase("CONSTRAINT_VALIDATION_FAILED", resp.getParams().getErr()))) {
						codeError += 1;
					} else {
						serverError += 1;
					}
				}

			} else {
				codeError += 1;
			}
		}
		return createResponse(codeError, serverError, identifiers, requestList.size());
	}


	/* (non-Javadoc)
	 * @see org.sunbird.framework.mgr.ITermManager#readTerm(java.lang.String, java.lang.String)
	 */
	@Override
	public Response readTerm(String scopeId, String termId, String categoryId) {
		String newCategoryId = categoryId;
		if (null != scopeId) {
			newCategoryId = generateIdentifier(scopeId, newCategoryId);
			validateRequest(scopeId, newCategoryId, categoryId);
		} else {
			validateCategoryId(newCategoryId);
		}
		String newTermId = generateIdentifier(newCategoryId, termId);
		//if (validateScopeNode(newTermId, categoryId)) {
			return read(newTermId, TERM_OBJECT_TYPE, TermEnum.term.name());
		//} else {
		//	throw new ClientException("ERR_CATEGORY_NOT_FOUND", "Category/CategoryInstance is not related Term");
		//}

	}

	/* (non-Javadoc)
	 * @see org.sunbird.framework.mgr.ITermManager#updateCategory(java.lang.String, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response updateTerm(String scopeId, String category, String termId, Map<String, Object> request) throws Exception {
		if (null == request || request.isEmpty())
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (request.containsKey(TermEnum.code.name()))
			return ERROR("ERR_CODE_UPDATION_NOT_ALLOWED", "Term Code cannot be updated", ResponseCode.CLIENT_ERROR);
		
		String categoryId = category;
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId, category);
		} else {
			validateCategoryId(categoryId);
		}
		String newTermId = generateIdentifier(categoryId, termId);
		
		if(!request.containsKey(TermEnum.parents.name()) || ((List<Object>)request.get(TermEnum.parents.name())).isEmpty()) {
			setRelations(categoryId, request);
			request.put(TermEnum.parents.name(), null);
		}else {
			Response responseNode = getDataNode(GRAPH_ID, categoryId);
			Node dataNode = (Node) responseNode.get(GraphDACParams.node.name());
			String objectType = dataNode.getObjectType();
			if(StringUtils.equalsIgnoreCase(StringUtils.lowerCase(objectType), TermEnum.categoryinstance.name())) {
				request.put(TermEnum.categories.name(), null);
			}
		}
		request.put("category", category);
		return update(newTermId, TERM_OBJECT_TYPE, request);
	}

	/* (non-Javadoc)
	 * @see org.sunbird.framework.mgr.ITermManager#searchTerms(java.lang.String, java.util.Map)
	 */
	@Override
	public Response searchTerms(String scopeId, String categoryId, Map<String, Object> map) {
		String newCategoryId = categoryId;
		if (null != scopeId) {
			newCategoryId = generateIdentifier(scopeId, newCategoryId);
			validateRequest(scopeId, newCategoryId, categoryId);
		} else {
			validateCategoryId(newCategoryId);
		}
		return search(map, TERM_OBJECT_TYPE, TermEnum.terms.name(), newCategoryId);
	}

	/* (non-Javadoc)
	 * @see org.sunbird.framework.mgr.ITermManager#retireTerm(java.lang.String, java.lang.String)
	 */
	@Override
	public Response retireTerm(String scopeId, String categoryId, String termId) {
		String newCategoryId = categoryId;
		if (null != scopeId) {
			newCategoryId = generateIdentifier(scopeId, newCategoryId);
			validateRequest(scopeId, newCategoryId, categoryId);
		} else {
			validateCategoryId(newCategoryId);
		}
		String newTermId = generateIdentifier(newCategoryId, termId);
		if (validateScopeNode(newTermId, newCategoryId)) {
			return retire(newTermId, TERM_OBJECT_TYPE);
		} else {
			throw new ClientException("ERR_CATEGORY_NOT_FOUND", "Category/CategoryInstance is not related Term");
		}
	}

	public void validateRequest(String scope, String categoryId, String originalCategory) {
		Boolean valid = false;
		if (StringUtils.isNotBlank(scope) && StringUtils.isNotBlank(categoryId)
				&& !StringUtils.equals(categoryId, "_")
				&& !StringUtils.equals(categoryId, scope + "_")) {
			Response categoryResp = getDataNode(GRAPH_ID, categoryId);
			if (!checkError(categoryResp)) {
				Node node = (Node) categoryResp.get(GraphDACParams.node.name());
				if(!StringUtils.equals(originalCategory, (String) node.getMetadata().get("code")))
					throw new ClientException("ERR_INVALID_CATEGORY", "Please provide a valid category");
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
				throw new ClientException("ERR_INVALID_CATEGORY_ID", "Please provide valid category.");
			}
			if (!valid)
				throw new ClientException("ERR_INVALID_CATEGORY_ID", "Please provide valid channel/framework.");
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Please provide required fields. category, channel/framework should not be empty.");
		}

	}

	private void validateCategoryId(String categoryId) {
		if (StringUtils.isNotBlank(categoryId)) {
			Response categoryResp = getDataNode(GRAPH_ID, categoryId);
			if (checkError(categoryResp)) {
				throw new ClientException("ERR_INVALID_CATEGORY_ID", "Please provide valid category.");
			}
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Please provide valid category. It should not be empty.");
		}
	}
	
	/**
	 * Validate Term against Master Term (Term under Master Category) If term in the
	 * request exist in Master Category,then term will be valid else invalid.
	 * 
	 * @author gauraw
	 * 
	 *//*
		private void validateMasterTerm(String categoryId, String termLabel) {
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
		
		}*/

	/**
	 * @param codeError
	 * @param serverError
	 * @param identifiers
	 * @param size
	 * @return
	 * @throws Exception
	 */
	private Response createResponse(int codeError, int serverError, List<String> identifiers, int size)
			throws Exception {
		if (codeError == 0 && serverError == 0) {
			return OK("node_id", identifiers);
		} else if (codeError > 0 && serverError == 0) {
			if (codeError == size) {
				return ERROR("ERR_TERM_CODE_REQUIRED", "Unique code is required for Term", ResponseCode.CLIENT_ERROR);
			} else {
				return ERROR("ERR_TERM_CODE_REQUIRED", "Unique code is required for Term", ResponseCode.PARTIAL_SUCCESS,
						"node_id", identifiers);
			}
		} else if (codeError == 0 && serverError > 0) {
			if (serverError == size) {
				return ERROR("ERR_SERVER_ERROR", "Internal Server Error", ResponseCode.SERVER_ERROR);
			} else {
				return ERROR("ERR_SERVER_ERROR", "Partial Success with Internal Error", ResponseCode.PARTIAL_SUCCESS,
						"node_id", identifiers);
			}
		} else {
			if ((codeError + serverError) == size) {
				return ERROR("ERR_SERVER_ERROR", "Internal Server Error and also Invalid Request",
						ResponseCode.SERVER_ERROR);
			} else {
				return ERROR("ERR_SERVER_ERROR", "Internal Server Error and also Invalid Request",
						ResponseCode.PARTIAL_SUCCESS, "node_id", identifiers);
			}
		}
	}

	/**
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getRequestData(Request req) {
		if (req.get(TermEnum.term.name()) instanceof List) {
			return (List<Map<String, Object>>) req.get(TermEnum.term.name());
		} else {
			List<Map<String, Object>> requestObj = new ArrayList<Map<String, Object>>();
			requestObj.add((Map<String, Object>) req.get(TermEnum.term.name()));
			return requestObj;
		}
	}
}