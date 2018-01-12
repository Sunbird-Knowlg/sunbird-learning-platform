package org.ekstep.framework.mgr.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.framework.enums.ChannelEnum;
import org.ekstep.framework.mgr.IChannelManager;
import org.springframework.stereotype.Component;

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
		return create(request, CHANNEL_OBJECT_TYPE);
	}

	@Override
	public Response readChannel(String channelId) {
		return read(channelId, CHANNEL_OBJECT_TYPE, ChannelEnum.channel.name());

	}

	@Override
	public Response updateChannel(String channelId, Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return update(channelId, CHANNEL_OBJECT_TYPE, map);
	}

	@Override
	public Response listChannel(Map<String, Object> map) {
		if (null == map)
			return ERROR("ERR_INVALID_CHANNEL_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		return search(map, CHANNEL_OBJECT_TYPE, "channels", null);
	}

	@Override
	public Response retireChannel(String channelId) {
		return retire(channelId, CHANNEL_OBJECT_TYPE);
	}
}
