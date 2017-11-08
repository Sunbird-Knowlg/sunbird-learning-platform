/**
 * 
 */
package org.ekstep.tools.loader.service;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.ekstep.tools.loader.utils.Constants;
import org.ekstep.tools.loader.utils.RestUtil;

import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author pradyumna
 *
 */
public class RegisterService {

	/**
	 * @param user
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public String register(String user, File conf) throws Exception {
		String clientId = null, masterKey = null;
		Config config = ConfigFactory.parseFile(conf).resolve();
		String registerUrl = config.getString("api.register.user");
		JsonObject requestBody = new JsonObject();
		requestBody.addProperty("clientName", user);
		JsonObject wrapper = new JsonObject();
		wrapper.add("request", requestBody);
		Unirest.setDefaultHeader("content-type", "application/json");
		Unirest.setDefaultHeader("Authorization", "Bearer " + config.getString(Constants.SUNBIRD_API_TOKEN));
		BaseRequest request = Unirest.post(registerUrl).body(wrapper.toString());
		HttpResponse<JsonNode> response = RestUtil.execute(request);
		if (RestUtil.isSuccessful(response)) {
			clientId = RestUtil.getFromResponse(response, "result.clientId");
			masterKey = RestUtil.getFromResponse(response, "result.masterKey");

			writeToFile(user, clientId, masterKey);
		} else {

		}
		return user + "," + masterKey + "," + clientId;
	}

	/**
	 * @param user
	 * @param clientId
	 * @param masterKey
	 */
	private void writeToFile(String user, String clientId, String masterKey) {
		File credFile = new File("authToken.txt");
		try {
			if (!credFile.exists()) {
				credFile.createNewFile();
				FileUtils.write(credFile, "ClientName,AuthToken,ClientId\n", true);
			}
			FileUtils.write(credFile, user + "," + masterKey + "," + clientId + "\n", true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
