package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.router.RequestRouterPool;
import org.ekstep.framework.enums.ChannelEnum;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;


@Component
public class ChannelManagerImpl extends BaseFrameworkManager implements IChannelManager {

	private static final String CHANNEL_OBJECT_TYPE = "Channel";
	private static final String LICENSE_NOT_FOUND_ERROR = "License Not Found With Name: ";
	private static final String LICENSE_REDIS_KEY = "edge_license";
	private static final String CHANNEL_LICENSE_CACHE_PREFIX = "channel_";
	private static final String CHANNEL_LICENSE_CACHE_SUFFIX = "_license";
	private SearchProcessor processor = null;
	
	@PostConstruct
	public void init() {
		processor = new SearchProcessor();
	}
	
	@Override
	public Response createChannel(Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get(ChannelEnum.code.name()) || StringUtils.isBlank((String)request.get(ChannelEnum.code.name())))
			return ERROR("ERR_CHANNEL_CODE_REQUIRED", "Unique code is mandatory for Channel", ResponseCode.CLIENT_ERROR);
		request.put(ChannelEnum.identifier.name(), (String)request.get(ChannelEnum.code.name()));
		validateLicense(request);
		Response response = create(request, CHANNEL_OBJECT_TYPE);
		channelLicenseCache(response,request);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response readChannel(String channelId) throws Exception{
		Response response = read(channelId, CHANNEL_OBJECT_TYPE, ChannelEnum.channel.name());
		if (Platform.config.hasPath("channel.fetch.suggested_frameworks") && Platform.config.getBoolean("channel.fetch.suggested_frameworks")) {
			Map<String, Object> responseMap = (Map<String, Object>) response.get(ChannelEnum.channel.name());
			List<Object> frameworkList = (List<Object>) responseMap.get(ChannelEnum.frameworks.name());
			if(null == frameworkList || frameworkList.isEmpty()) {
				List<Object> searchedFrameworkList = getAllFrameworkList();
				if (null != searchedFrameworkList && !searchedFrameworkList.isEmpty()) {
					responseMap.put("suggested_frameworks",searchedFrameworkList);
				}
			}
		}
		return response;
	}

	@Override
	public Response updateChannel(String channelId, Map<String, Object> map){
		if (null == map)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		validateLicense(map);
		Response response = update(channelId, CHANNEL_OBJECT_TYPE, map);
		channelLicenseCache(response,map);
		return response;
	}

	@Override
	public Response listChannel(Map<String, Object> map) {
		return search(map, CHANNEL_OBJECT_TYPE, "channels", null);
	}

	@Override
	public Response retireChannel(String channelId) {
		return retire(channelId, CHANNEL_OBJECT_TYPE);
	}

	private List<Object> getAllFrameworkList() throws Exception {
		List<Object> searchResult = new ArrayList<Object>();
		SearchDTO searchDto = new SearchDTO();
		searchDto.setFuzzySearch(false);
		searchDto.setProperties(setSearchProperties());
		searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchDto.setFields(getFields());
		searchResult = Await.result(
				processor.processSearchQuery(searchDto, false, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false),
				RequestRouterPool.WAIT_TIMEOUT.duration());

		return searchResult;
	}

	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add(ChannelEnum.identifier.name());
		fields.add(ChannelEnum.name.name());
		fields.add(ChannelEnum.code.name());
		return fields;
	}

	private List<Map> setSearchProperties() {
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<>();
		property.put(ChannelEnum.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put(ChannelEnum.propertyName.name(), ChannelEnum.objectType.name());
		property.put(ChannelEnum.values.name(), ChannelEnum.Framework.name());
		properties.add(property);
		
		property = new HashMap<>();
		property.put(ChannelEnum.operation.name(), CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put(ChannelEnum.propertyName.name(), ChannelEnum.status.name());
		property.put(ChannelEnum.values.name(), ChannelEnum.Live.name());
		properties.add(property);

		return properties;
	}

	private void validateLicense(Map<String, Object> request) {
		if (request.containsKey(ChannelEnum.defaultLicense.name())) {
			List<Object> licenseList = RedisStoreUtil.getList(LICENSE_REDIS_KEY);
			if (CollectionUtils.isEmpty(licenseList)) {
				Request searchReq = getRequest(GRAPH_ID, GraphEngineManagers.SEARCH_MANAGER, "getNodesByObjectType", GraphDACParams.object_type.name(), "License");
				Response response = getResponse(searchReq);
				if (checkError(response))
					throw new ServerException("ERR_FETCHING_LICENSE", "Error while fetching license.");
				Map<String, Object> result = response.getResult();
				List<Node> resultList = (List<Node>) result.get("node_list");
				if (CollectionUtils.isEmpty(resultList)) {
					throw new ClientException(ChannelEnum.ERR_INVALID_LICENSE.name(), LICENSE_NOT_FOUND_ERROR + request.get(ChannelEnum.defaultLicense.name()) , ResponseCode.CLIENT_ERROR);
				}
				licenseList.addAll(resultList.stream().map(node -> node.getMetadata().get("name")).collect(Collectors.toList()));
				RedisStoreUtil.saveList(LICENSE_REDIS_KEY, licenseList);
			}
			if (!licenseList.contains(request.get(ChannelEnum.defaultLicense.name()))) {
				throw new ClientException(ChannelEnum.ERR_INVALID_LICENSE.name(), LICENSE_NOT_FOUND_ERROR + request.get(ChannelEnum.defaultLicense.name()) , ResponseCode.CLIENT_ERROR);
			}
		}
	}

	private void channelLicenseCache(Response response, Map<String, Object> request){
		if (!checkError(response) && response.getResult().containsKey(ChannelEnum.node_id.name()) && request.containsKey(ChannelEnum.defaultLicense.name())) {
			RedisStoreUtil.save(CHANNEL_LICENSE_CACHE_PREFIX + response.getResult().get(ChannelEnum.node_id.name()) + CHANNEL_LICENSE_CACHE_SUFFIX, (String) request.get(ChannelEnum.defaultLicense.name()), 0);
		}
	}
}
