package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.springframework.stereotype.Component;

import org.ekstep.framework.enums.ChannelEnum;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;

@Component
public class ChannelManagerImpl extends BaseFrameworkManager implements IChannelManager {

	private static final String CHANNEL_OBJECT_TYPE = "Channel";
	private String host = "localhost";
	private int port = 9200;
	private SearchProcessor processor = null;
	
	@PostConstruct
	public void init() {
		host = Platform.config.hasPath("dialcode.es_host") ? Platform.config.getString("dialcode.es_host") : host;
		port = Platform.config.hasPath("dialcode.es_port") ? Platform.config.getInt("dialcode.es_port") : port;
		processor = new SearchProcessor(host, port);
	}
	
	@Override
	public Response createChannel(Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get(ChannelEnum.code.name()) || StringUtils.isBlank((String)request.get(ChannelEnum.code.name())))
			return ERROR("ERR_CHANNEL_CODE_REQUIRED", "Unique code is mandatory for Channel", ResponseCode.CLIENT_ERROR);
		request.put(ChannelEnum.identifier.name(), (String)request.get(ChannelEnum.code.name()));
		return create(request, CHANNEL_OBJECT_TYPE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response readChannel(String channelId) throws Exception{
		Response response = read(channelId, CHANNEL_OBJECT_TYPE, ChannelEnum.channel.name());
		Map<String, Object> responseMap = (Map<String, Object>) response.get(ChannelEnum.channel.name());
		List<Object> frameworkList = (List<Object>) responseMap.get(ChannelEnum.frameworks.name());
		if(null == frameworkList || frameworkList.isEmpty()) {
			List<Object> searchedFrameworkList = getAllFrameworkList();
			if (null != searchedFrameworkList && !searchedFrameworkList.isEmpty()) {
				responseMap.put(ChannelEnum.frameworks.name(),searchedFrameworkList);
			}
		}
		return response;
	}

	@Override
	public Response updateChannel(String channelId, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return update(channelId, CHANNEL_OBJECT_TYPE, map);
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
		//searchDto.setLimit(1);

		searchResult = (List<Object>) processor.processSearchQuery(searchDto, false,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false);

		return searchResult;
	}

	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		//fields.add(ChannelEnum.identifier.name());
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
}
