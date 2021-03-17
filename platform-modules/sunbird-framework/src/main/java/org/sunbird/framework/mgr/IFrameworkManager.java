package org.sunbird.framework.mgr;

import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Response;
import org.sunbird.framework.mgr.impl.FrameworkManagerImpl;

/**
 * The Interface IFrameworkManager is the Contract for the operations that can be
 * perform on Framework Node in the Graph. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * @author gauraw
 * 
 * @see FrameworkManagerImpl
 *
 */
public interface IFrameworkManager {

	Response createFramework(Map<String, Object> map, String channelId) throws Exception;
	
	Response readFramework(String frameworkId, List<String> categories) throws Exception;
	
	Response updateFramework(String frameworkId,String channelId, Map<String, Object> map) throws Exception;
	
	Response listFramework(Map<String, Object> map) throws Exception;
	
	Response retireFramework(String frameworkId, String channelId) throws Exception;

	Response copyFramework(String frameworkId, String channelId, Map<String, Object> request) throws Exception;

	Response publishFramework(String frameworkId, String channelId) throws Exception;
}