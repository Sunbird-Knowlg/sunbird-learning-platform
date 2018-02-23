package org.ekstep.learning.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;

public interface IFormManager {

	public String getIdentifier(String type, String operation, String channel) throws Exception;
	
	public String getObjectType();
	
	public Response create(Map<String, Object> map);

	public Response update(String identifier, Map<String, Object> map);

	public Response read(String identifier, String type, String operation) throws Exception;

}
