package com.ilimi.framework.mgr.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.framework.enums.ChannelEnum;
import com.ilimi.framework.mgr.IChannelManager;

@Component
public class ChannelManagerImpl extends BaseFrameworkManager implements IChannelManager {

	private static final String CHANNEL_OBJECT_TYPE = "Channel";


	@Override
	public Response createChannel(Map<String, Object> request) {
		if (null == request)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
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
