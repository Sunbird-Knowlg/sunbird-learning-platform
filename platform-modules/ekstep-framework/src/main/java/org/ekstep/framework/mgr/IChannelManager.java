package org.ekstep.framework.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;

public interface IChannelManager {
	
	Response createChannel(Map<String,Object> request);

	Response readChannel(String channelId);

	Response updateChannel(String channelId, Map<String, Object> map);

	Response listChannel(Map<String, Object> map);

	Response retireChannel(String channelId);

}
