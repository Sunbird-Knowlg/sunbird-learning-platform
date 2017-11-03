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
public class UserServiceImpl implements UserService {

	private static Logger logger = LogManager.getLogger(UserServiceImpl.class);

	public void init(ExecutionContext context) {
		RestUtil.init(context, Constants.SUNBIRD_API_TOKEN);
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public String createUser(JsonObject user, ExecutionContext context) throws Exception {
		String createUrl = context.getString(Constants.API_USER_CREATE);

		String userId = null;

		String body = user.toString();
		BaseRequest request = Unirest.post(createUrl).body(body);
		HttpResponse<JsonNode> response = RestUtil.execute(request);

		if (RestUtil.isSuccessful(response)) {
			userId = RestUtil.getFromResponse(response, Constants.USER_RESULT_KEY);
			user.addProperty("userId", userId);
			user.addProperty("response", "OK");

		} else {

			String error = RestUtil.getFromResponse(response, Constants.ERROR_RESP_KEY);
			user.addProperty("response", error);
		}

		return userId;
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public String updateUser(JsonObject user, ExecutionContext context) throws Exception {
		String createUrl = context.getString(Constants.API_USER_UPDATE);

		String userId = JsonUtil.getFromObject(user, "userId");

		String body = user.toString();
		BaseRequest request = Unirest.post(createUrl).body(body);
		HttpResponse<JsonNode> response = RestUtil.execute(request);

		if (RestUtil.isSuccessful(response)) {
			userId = RestUtil.getFromResponse(response, Constants.USER_RESULT_KEY);
			user.addProperty("userId", userId);
			user.addProperty("response", "OK");

		} else {

			String error = RestUtil.getFromResponse(response, Constants.ERROR_RESP_KEY);
			user.addProperty("response", error);
		}

		return userId;
	}

}
