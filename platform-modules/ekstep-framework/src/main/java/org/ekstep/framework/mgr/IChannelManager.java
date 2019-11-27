package org.ekstep.framework.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.MiddlewareException;

public interface IChannelManager {
	
	Response createChannel(Map<String,Object> request) throws MiddlewareException;

	Response readChannel(String channelId) throws Exception;

	Response updateChannel(String channelId, Map<String, Object> map) throws MiddlewareException;

	Response listChannel(Map<String, Object> map);

	Response retireChannel(String channelId);

}
