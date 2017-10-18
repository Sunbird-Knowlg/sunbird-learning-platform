/**
 * 
 */
package org.ekstep.tools.loader.service;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public interface UserService {

	public String createUser(JsonObject user, ExecutionContext context) throws Exception;

	public String updateUser(JsonObject user, ExecutionContext context) throws Exception;

}
