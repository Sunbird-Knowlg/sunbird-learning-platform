package org.sunbird.framework.mgr;

import java.util.Map;

import org.sunbird.common.dto.Response;

public interface IChannelManager {
	
	Response createChannel(Map<String,Object> request);

	Response readChannel(String channelId) throws Exception;

	Response updateChannel(String channelId, Map<String, Object> map);

	Response listChannel(Map<String, Object> map);

	Response retireChannel(String channelId);

}
