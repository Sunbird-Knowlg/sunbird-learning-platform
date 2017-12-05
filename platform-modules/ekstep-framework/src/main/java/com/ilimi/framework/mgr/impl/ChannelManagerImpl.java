package com.ilimi.framework.mgr.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.framework.enums.CategoryEnum;
import com.ilimi.framework.enums.ChannelEnum;
import com.ilimi.framework.mgr.IChannelManager;

@Component
public class ChannelManagerImpl extends BaseFrameworkManager implements IChannelManager {

	private static final String CHANNEL_OBJECT_TYPE = "Channel";


	@Override
	public Response createChannel(Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (null == request.get("code") || StringUtils.isBlank((String)request.get("code")))
			return ERROR("ERR_CHANNEL_CODE_REQUIRED", "Unique code is mandatory for Channel", ResponseCode.CLIENT_ERROR);
		request.put("identifier", (String)request.get("code"));
		return create(request, CHANNEL_OBJECT_TYPE, null);
	}

	@Override
	public Response readChannel(String graphId, String channelId) {
		return read(channelId, CHANNEL_OBJECT_TYPE, ChannelEnum.channel.name());

	}

	@Override
	public Response updateChannel(String channelId, Map<String, Object> map) {
		return update(channelId, CHANNEL_OBJECT_TYPE, map);
	}

	@Override
	public Response listChannel(Map<String, Object> map) {
		return search(map, CHANNEL_OBJECT_TYPE, "channels", null);

	}
	
}
