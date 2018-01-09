package org.ekstep.dialcode.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.dialcode.mgr.impl.DialCodeManagerImpl;

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

	Response updateDialCode(String dialCodeId, String channelId, Map<String, Object> map) throws Exception;

	Response listDialCode(String channelId, Map<String, Object> map) throws Exception;

	Response searchDialCode(String channelId, Map<String, Object> map) throws Exception;

	Response publishDialCode(String dialCodeId, String channelId) throws Exception;
	
	Response createPublisher(Map<String, Object> map, String channelId) throws Exception;

    Response readPublisher(String publisherId) throws Exception;

    Response updatePublisher(String publisherId, String channelId, Map<String, Object> map) throws Exception;


}