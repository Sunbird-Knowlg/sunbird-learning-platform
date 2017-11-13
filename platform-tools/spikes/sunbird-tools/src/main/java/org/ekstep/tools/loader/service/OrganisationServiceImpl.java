/**
 * 
 */
package org.ekstep.tools.loader.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.utils.Constants;
import org.ekstep.tools.loader.utils.JsonUtil;
import org.ekstep.tools.loader.utils.RestUtil;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;

/**
 * @author pradyumna
 *
 */
public class OrganisationServiceImpl implements OrganisationService {

	private static Logger logger = LogManager.getLogger(OrganisationServiceImpl.class);

	/**
	 * 
	 */
	public OrganisationServiceImpl(ExecutionContext context) {
		init(context);
	}

	private void init(ExecutionContext context) {
		RestUtil.init(context, Constants.SUNBIRD_API_TOKEN);
	}

	/* (non-Javadoc)
	 */
	@Override
	public String createOrg(JsonObject org, ExecutionContext context) throws Exception {
		String createUrl = context.getString(Constants.API_ORG_CREATE);

		String orgId = null;
		JsonObject wrapper = new JsonObject();
		wrapper.add("request", org);
		String body = wrapper.toString();
		Unirest.setDefaultHeader("x-authenticated-user-token", context.getAcessToken());
		BaseRequest request = Unirest.post(createUrl).body(body);
		HttpResponse<JsonNode> response = RestUtil.execute(request);

		if (RestUtil.isSuccessful(response)) {
			orgId = RestUtil.getFromResponse(response, Constants.ORG_RESULT_KEY);
			org.addProperty("organisationId", orgId);
			org.addProperty("response", "OK");
			logger.debug("Created Organisation : {}", orgId);

		} else {
			String error = RestUtil.getFromResponse(response, Constants.ERROR_RESP_KEY);
			org.addProperty("response", error);
			logger.debug("Create OrganisationFailed : {}", error);
		}

		return orgId;
	}

	/* (non-Javadoc)
	 */
	@Override
	public String updateOrg(JsonObject updateOrg, ExecutionContext context) throws Exception {
		String updateUrl = context.getString(Constants.API_ORG_UPDATE);

		String orgId = JsonUtil.getFromObject(updateOrg, "organisationId");
		Unirest.setDefaultHeader("x-authenticated-user-token", context.getAcessToken());
		JsonObject wrapper = new JsonObject();
		wrapper.add("request", updateOrg);
		String body = wrapper.toString();
		BaseRequest request = Unirest.post(updateUrl).body(body);
		HttpResponse<JsonNode> response = RestUtil.execute(request);

		if (RestUtil.isSuccessful(response)) {
			orgId = RestUtil.getFromResponse(response, Constants.ORG_RESULT_KEY);
			updateOrg.addProperty("organisationId", orgId);
			updateOrg.addProperty("response", "OK");
			logger.debug("Updated Organisation : {}", orgId);

		} else {

			String error = RestUtil.getFromResponse(response, Constants.ERROR_RESP_KEY);
			updateOrg.addProperty("response", error);
			logger.debug("Error while Updating Organisation : {}", error);
		}

		return orgId;
	}

	/* (non-Javadoc)
	 */
	@Override
	public String updateOrgType(JsonObject json, ExecutionContext context) {
		return null;
	}

	/*
	 * Updates Organisation status. Status can be BLOCKED, ACTIVE, UNBLOCKED,
	 * RETIRED
	 */
	@Override
	public String updateOrgStatus(JsonObject orgStatus, ExecutionContext context) throws Exception {
		String updateUrl = context.getString(Constants.API_ORG_UPDATE_STATUS);

		String orgId = JsonUtil.getFromObject(orgStatus, "organisationId");
		Unirest.setDefaultHeader("x-authenticated-user-token", context.getAcessToken());
		JsonObject wrapper = new JsonObject();
		wrapper.add("request", orgStatus);
		String body = wrapper.toString();
		BaseRequest request = Unirest.patch(updateUrl).body(body);

		HttpResponse<JsonNode> response = RestUtil.execute(request);

		if (RestUtil.isSuccessful(response)) {
			orgId = RestUtil.getFromResponse(response, Constants.ORG_RESULT_KEY);
			orgStatus.addProperty("organisationId", orgId);
			orgStatus.addProperty("response", "OK");
			logger.debug("Updated Organisation Status : {}", orgId);

		} else {

			String error = RestUtil.getFromResponse(response, Constants.ERROR_RESP_KEY);
			orgStatus.addProperty("response", error);
			logger.debug("Error while Updating Organisation : " + error);
		}

		return orgId;
	}

	/* (non-Javadoc)
	 */
	@Override
	public String addOrgMember(JsonObject member, ExecutionContext context) throws Exception {
		String addMemberUrl = context.getString(Constants.API_ADD_MEMBER);

		JsonObject wrapper = new JsonObject();
		wrapper.add("request", member);
		String body = wrapper.toString();
		BaseRequest request = Unirest.post(addMemberUrl).body(body);
		HttpResponse<JsonNode> addMemberResponse = RestUtil.execute(request);

		String response = "OK";

		if (RestUtil.isSuccessful(addMemberResponse)) {
			response = "OK";
		} else {
			response = RestUtil.getFromResponse(addMemberResponse, Constants.ERROR_RESP_KEY);
			logger.debug("Error while Adding Member : " + response);
		}

		return response;
	}

	/* (non-Javadoc)
	 */
	@Override
	public String removeOrgMember(JsonObject member, ExecutionContext context) throws Exception {
		String addMemberUrl = context.getString(Constants.API_REMOVE_MEMBER);

		JsonObject wrapper = new JsonObject();
		wrapper.add("request", member);
		String body = wrapper.toString();
		BaseRequest request = Unirest.post(addMemberUrl).body(body);
		HttpResponse<JsonNode> addMemberResponse = RestUtil.execute(request);

		String response = "OK";

		if (RestUtil.isSuccessful(addMemberResponse)) {
			response = "OK";
			logger.debug("Removed Member from Org");
		} else {
			response = RestUtil.getFromResponse(addMemberResponse, Constants.ERROR_RESP_KEY);
			logger.debug("Error while Removing Member : " + response);
		}

		return response;
	}

}
