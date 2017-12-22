package com.ilimi.dialcode.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;
import com.ilimi.dialcode.mgr.impl.DialCodeManagerImpl;

/**
 * The Interface IDialCodeManager is the Contract for the operations that can be
 * perform on DialCodes in Cassandra. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * @author gauraw
 * 
 * @see DialCodeManagerImpl
 *
 */
public interface IDialCodeManager {

	Response generateDialCode(Map<String, Object> map, String channelId) throws Exception;
	
	Response readDialCode(String dialCodeId) throws Exception;
	
	Response updateDialCode(String dialCodeId,String channelId, Map<String, Object> map) throws Exception;
	
	Response listDialCode(Map<String, Object> map) throws Exception;
	
	Response publishDialCode(String dialCodeId,String channelId ) throws Exception;
}