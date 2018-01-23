/**
 * 
 */
package org.ekstep.framework.mgr.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.framework.enums.TermEnum;
import org.ekstep.framework.mgr.ITermManager;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.springframework.stereotype.Component;

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
	public Response createTerm(String scopeId, String category, Map<String, Object> request) throws Exception{
		if (null == request)
			return ERROR("ERR_INVALID_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		String code = (String) request.get(TermEnum.code.name());
		if (StringUtils.isBlank(code))
			return ERROR("ERR_TERM_LABEL_REQUIRED", "Unique code is required for Term", ResponseCode.CLIENT_ERROR);

		String categoryId = category;
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
		request.put("category", category);
		Response response = create(request, TERM_OBJECT_TYPE);
		if(response.getResponseCode() == ResponseCode.OK) {
			if (Platform.config.hasPath("framework.es.sync")) {
				if (Platform.config.getBoolean("framework.es.sync")) {
					generateFrameworkHierarchy(id);
				}
			}
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response createTerm(String scopeId, String category, Request req) throws Exception {
		List<Map<String, Object>> requestList = getRequestData(req);
		if (null == requestList || requestList.isEmpty())
			return ERROR("ERR_INVALID_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		String categoryId = category;
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
			// validateMasterTerm(categoryId, label); commented to remove term validation
			// with master term
		} else {
			validateCategoryId(categoryId);
		}

		int codeError = 0, serverError = 0;
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
				if (resp.getResponseCode() == ResponseCode.OK) {
					identifiers.add((String) resp.getResult().get("node_id"));
				} else {
					serverError += 1;
				}

			} else {
				codeError += 1;
			}
		}
		if (StringUtils.isNoneBlank(id)) {
			if (Platform.config.hasPath("framework.es.sync")) {
				if (Platform.config.getBoolean("framework.es.sync")) {
					generateFrameworkHierarchy(id);
				}
			}
		}
		return createResponse(codeError, serverError, identifiers, requestList.size());
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
	public Response updateTerm(String scopeId, String category, String termId, Map<String, Object> request) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_CATEGORY_INSTANCE_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (request.containsKey(TermEnum.code.name()))
			return ERROR("ERR_SERVER_ERROR", "Term Code cannot be updated", ResponseCode.SERVER_ERROR);
		
		String categoryId = category;
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
		request.put("category", category);
		Response response = update(termId, TERM_OBJECT_TYPE, request);
		if(response.getResponseCode() == ResponseCode.OK) {
			if (Platform.config.hasPath("framework.es.sync")) {
				if (Platform.config.getBoolean("framework.es.sync")) {
					generateFrameworkHierarchy(termId);
				}
			}
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
	public Response retireTerm(String scopeId, String categoryId, String termId)  throws Exception {
		if (null != scopeId) {
			categoryId = generateIdentifier(scopeId, categoryId);
			validateRequest(scopeId, categoryId);
		} else {
			validateCategoryId(categoryId);
		}
		termId = generateIdentifier(categoryId, termId);
		if (validateScopeNode(termId, categoryId)) {
			Response response = retire(termId, TERM_OBJECT_TYPE);
			if(response.getResponseCode() == ResponseCode.OK) {
				if (Platform.config.hasPath("framework.es.sync")) {
					if (Platform.config.getBoolean("framework.es.sync")) {
						generateFrameworkHierarchy(termId);
					}
				}
			}
			return response;
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
				throw new ResourceNotFoundException("ERR_INVALID_CATEGORY_ID", "Please provide valid category.");
			}
		} else {
			throw new ClientException("ERR_INVALID_CATEGORY_ID", "Please provide valid category. It should not be empty.");
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