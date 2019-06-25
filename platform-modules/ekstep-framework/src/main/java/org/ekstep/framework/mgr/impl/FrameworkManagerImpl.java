
package org.ekstep.framework.mgr.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.Slug;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.framework.enums.FrameworkEnum;
import org.ekstep.framework.mgr.IFrameworkManager;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private List<String> categoriesCached = Platform.config.hasPath("framework.categories_cached") ?
			Platform.config.getStringList("framework.categories_cached"): Arrays.asList("subject", "medium", "gradeLevel", "board");
	private static final String CACHE_SUFFIX = "_categories";
	private boolean cacheEnabled = Platform.config.hasPath("framework.cache.read") ?
			Platform.config.getBoolean("framework.cache.read"): false;
	private int cacheTtl = Platform.config.hasPath("framework.cache.ttl") ?
			Platform.config.getInt("framework.cache.ttl"): 86400;

	/*
	 * create framework
	 * 
	 * @param Map request
	 * 
	 */
	@Override
	public Response createFramework(Map<String, Object> request, String channelId) throws Exception {
		if (null == request)
			return ERROR("ERR_INVALID_FRAMEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

		String code = (String) request.get("code");
		if (StringUtils.isBlank(code))
			throw new ClientException("ERR_FRAMEWORK_CODE_REQUIRED", "Unique code is mandatory for framework",
					ResponseCode.CLIENT_ERROR);

		request.put("identifier", code);

		if (validateObject(channelId)) {
			Response response = create(request, FRAMEWORK_OBJECT_TYPE);
			return response;
		} else {
			return ERROR("ERR_INVALID_CHANNEL_ID", "Invalid Channel Id. Channel doesn't exist.",
					ResponseCode.CLIENT_ERROR);
		}

	}

	/**
	 * Read Framework By Id
	 * @param frameworkId
	 * @param returnCategories
	 * @return Response
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response readFramework(String frameworkId, List<String> returnCategories) throws Exception {
		Response response = new Response();
		Map<String, Object> framework = null;

		if(CollectionUtils.isNotEmpty(returnCategories) && categoriesCached.containsAll(returnCategories) && cacheEnabled){
			String filteredCategories = RedisStoreUtil.get(frameworkId + CACHE_SUFFIX);
			if(StringUtils.isNotBlank(filteredCategories)){
				framework = mapper.readValue(filteredCategories, new TypeReference<Map<String, Object>>(){});
			}
		}
		if(MapUtils.isEmpty(framework)){
			Response getHierarchyResp = getFrameworkHierarchy(frameworkId);
			if (!checkError(getHierarchyResp)) {
				framework = (Map<String, Object>) getHierarchyResp.get("framework");
				Map<String, Object> cacheFW = new HashMap<>(framework);
				filterFrameworkCategories(cacheFW, categoriesCached);
				RedisStoreUtil.save(frameworkId + CACHE_SUFFIX, mapper.writeValueAsString(cacheFW), cacheTtl);
			}
		}

		if (MapUtils.isNotEmpty(framework)) {
			filterFrameworkCategories(framework, returnCategories);
			response = OK();
			response.put(FrameworkEnum.framework.name(), framework);
		} else {
			Response readResponse = read(frameworkId, FRAMEWORK_OBJECT_TYPE, FrameworkEnum.framework.name());
			if (checkError(readResponse)) {
				return readResponse;
			} else {
				framework = (Map<String, Object>) readResponse.getResult().get("framework");
				response = OK();
				response.put(FrameworkEnum.framework.name(), framework);
				return response;
			}
		}
		return response;
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
					"Framework Not Found With Id : " + frameworkId);
		Node graphNode = (Node) getNodeResponse.get(GraphDACParams.node.name());
		String ownerChannelId = (String) graphNode.getMetadata().get("channel");
		if (!(channelId.equalsIgnoreCase(ownerChannelId))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Channel Id Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}
		
		Response response = update(frameworkId, FRAMEWORK_OBJECT_TYPE, map);
		return response;

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
			throw new ClientException("ERR_INVALID_SEARCH_REQUEST", "Invalid Search Request");

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
					"Framework Not Found With Id : " + frameworkId);
		Node frameworkNode = (Node) getNodeResponse.get(GraphDACParams.node.name());

		String ownerChannelId = (String) frameworkNode.getMetadata().get("channel");

		if (!(channelId.equalsIgnoreCase(ownerChannelId))) {
			return ERROR("ERR_SERVER_ERROR_UPDATE_FRAMEWORK", "Invalid Request. Channel Id Not Matched.",
					ResponseCode.CLIENT_ERROR);
		}

		return retire(frameworkId, FRAMEWORK_OBJECT_TYPE);

	}

	@Override
	public Response copyFramework(String frameworkId, String channelId, Map<String, Object> request) throws Exception {
	    if (null == request)
	      return ERROR("ERR_INVALID_FRAMEWORK_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);

	    String code = (String) request.get("code");
	    if (StringUtils.isBlank(code))
	      throw new ClientException("ERR_FRAMEWORK_CODE_REQUIRED", 
	          "Unique code is mandatory for framework",
	          ResponseCode.CLIENT_ERROR);

	    if(StringUtils.equals(frameworkId, code))
	      throw new ClientException("ERR_FRAMEWORKID_CODE_MATCHES", 
	          "FrameworkId and code should not be same.", 
	          ResponseCode.CLIENT_ERROR);
	    
	    if(validateObject(code)) {
			throw new ClientException("ERR_FRAMEWORK_EXISTS", 
			          "Framework with code: " + code + ", already exists.",
			          ResponseCode.CLIENT_ERROR);
		}
	    
	    if (validateObject(channelId)) {
	    		String sluggifiedFrameworkId = Slug.makeSlug(frameworkId);
	    		String sluggifiedCode = Slug.makeSlug(code);
	    		Response response = copyHierarchy(frameworkId, code, sluggifiedFrameworkId, sluggifiedCode, request);
	    		return response;
	    } else {
	    		return ERROR("ERR_INVALID_CHANNEL_ID", "Invalid Channel Id. Channel doesn't exist.",
	    				ResponseCode.CLIENT_ERROR);
	    }
	}

	@Override
	public Response publishFramework(String frameworkId, String channelId) throws Exception {
		if (!validateObject(channelId)) {
			return ERROR("ERR_INVALID_CHANNEL_ID", "Invalid Channel Id. Channel doesn't exist.",
					ResponseCode.CLIENT_ERROR);
		}
		if (StringUtils.isNotBlank(frameworkId) && validateObject(frameworkId)) {
			generateFrameworkHierarchy(frameworkId);
			Response response = OK();
			response.put(FrameworkEnum.publishStatus.name(),
					"Publish Operation for Framework Id '" + frameworkId + "' Started Successfully!");

			return response;
		} else {
			return ERROR("ERR_INVALID_FRAMEOWRK_ID", "Invalid Framework Id. Framework doesn't exist.",
					ResponseCode.CLIENT_ERROR);
		}

	}

}