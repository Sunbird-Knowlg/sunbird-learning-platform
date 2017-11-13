/**
 * 
 */
package org.ekstep.tools.loader.destination;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ekstep.tools.loader.service.ConceptServiceImpl;
import org.ekstep.tools.loader.service.ContentServiceImpl;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.OrganisationServiceImpl;
import org.ekstep.tools.loader.service.UserServiceImpl;
import org.ekstep.tools.loader.shell.ShellContext;
import org.ekstep.tools.loader.utils.Constants;
import org.ekstep.tools.loader.utils.RestUtil;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;
import com.typesafe.config.Config;

/**
 * @author pradyumna
 *
 */
public class ServiceProvider {

	private static Config config = null;
	private static String user = null;
	private static String authToken = null;
	private static String clientId = null;
	private static String password = null;
	private static ExecutionContext context = null;

	private static ShellContext shellContext = null;


	public static Object getService(String service) {
		shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		authToken = shellContext.getAuthToken();
		clientId = shellContext.getClientId();
		password = shellContext.getPassword();

		context = new ExecutionContext(config, user, authToken, clientId);
		switch (service) {
		case "content":
			authToken = getAuthToken(user, password);
			context = new ExecutionContext(config, user, authToken, clientId, authToken);
			return new ContentServiceImpl(context);

		case "concept":
			context = new ExecutionContext(config, user, authToken, clientId);
			return new ConceptServiceImpl(context);

		case "org":
			authToken = getAuthToken(user, password);
			context = new ExecutionContext(config, user, authToken, clientId, authToken);
			return new OrganisationServiceImpl(context);

		case "user":
			authToken = getAuthToken(user, password);
			context = new ExecutionContext(config, user, authToken, clientId, authToken);
			return new UserServiceImpl(context);

		default:
			return null;
		}
	}

	/**
	 * @param userName
	 * @param passwrd
	 * @return
	 * @throws Exception
	 */
	private static String getAuthToken(String userName, String passwrd) {
		String body = "client_id=admin-cli&username=" + userName + "&password=" + passwrd + "&grant_type=password";
		try {
			BaseRequest request = Unirest.post(context.getString(Constants.AUTH_TOKEN_API))
					.header("Content-Type", "application/x-www-form-urlencoded").body(body);
			HttpResponse<JsonNode> response = RestUtil.execute(request);
			if (response.getStatus() == 200) {
				return RestUtil.getFromResponse(response, "access_token");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * @return the context
	 */
	public static ExecutionContext getContext() {
		return context;
	}

	/**
	 * @param string
	 * @throws IOException
	 */
	public static void writeOutput(File file, String output) {
		try {
			FileWriter outputFile = new FileWriter(file, true);
			outputFile.append(output + "\n");
			outputFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
