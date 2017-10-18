/**
 * 
 */
package org.ekstep.tools.loader.service;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public interface OrganisationService {
	public String createOrg(JsonObject organisation, ExecutionContext context) throws Exception;

	public String updateOrg(JsonObject json, ExecutionContext context) throws Exception;

	public String updateOrgType(JsonObject json, ExecutionContext context);

	public String updateOrgStatus(JsonObject json, ExecutionContext context) throws Exception;

	public String addOrgMember(JsonObject json, ExecutionContext context) throws Exception;

	public String removeOrgMember(JsonObject json, ExecutionContext context) throws Exception;
}
