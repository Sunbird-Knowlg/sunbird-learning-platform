package org.ekstep.taxonomy.util;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ServerException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

/**
 * Contains methods for authorizing a user and caching credentials.
 * 
 * @author gauraw
 *
 */
public class YoutubeAuthorizationHelper {

	/**
	 * Define a global instance of the HTTP transport.
	 */
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/**
	 * Define a global instance of the JSON factory.
	 */
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * This is the directory that will be used under the user's home directory
	 * where OAuth tokens will be stored.
	 */
	// TODO: Need to decide on making it as configuration.
	private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

	/**
	 * Authorizes the installed application to access user's protected data.
	 *
	 * @param scopes
	 *            list of scopes needed to run youtube upload.
	 * @param credentialDatastore
	 *            name of the credential datastore to cache OAuth tokens
	 */
	public static Credential authorize(List<String> scopes, String credentialDatastore) throws Exception {

		// Load client secrets.
		// TODO: Need to Check, if Credentials can be configurable through
		// application.conf
		Reader clientSecretReader = new InputStreamReader(
				YoutubeAuthorizationHelper.class.getResourceAsStream("/client_secrets.json"));

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

		if (StringUtils.isBlank(clientSecrets.getDetails().getClientId())
				|| StringUtils.isBlank(clientSecrets.getDetails().getClientSecret())) {
			throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
					"Something Went Wrong While Processing Your Request. Please Try Again After Sometime!");
		}

		// This creates the credentials datastore at
		// ~/.oauth-credentials/${credentialDatastore}
		FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(
				new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
		DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, scopes).setCredentialDataStore(datastore).build();

		// Build the local server and bind it to port 8080
		// TODO: Need to check if this will work with tomcat
		LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

		// Authorize.
		// TODO: Need to Resolve CTE
		return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
	}

}
