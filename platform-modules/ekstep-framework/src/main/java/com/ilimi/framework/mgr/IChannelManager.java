package com.ilimi.framework.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

public interface IChannelManager {
	
	Response createChannel(Map<String,Object> request);

	Response readChannel(String graphId, String channelId);

	Response updateChannel(String channelId, Map<String, Object> map);

	Response listChannel(Map<String, Object> map);

}
