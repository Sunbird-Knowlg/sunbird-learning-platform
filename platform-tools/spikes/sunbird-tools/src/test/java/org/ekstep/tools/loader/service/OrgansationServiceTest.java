/**
 * 
 */
package org.ekstep.tools.loader.service;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author pradyumna
 *
 */
public class OrgansationServiceTest {

	private Logger logger = LogManager.getLogger(OrgansationServiceTest.class);
	private Config config = null;
	private String user = null, authToken = null, clientId = null;
	private ExecutionContext context = null;
	private JsonObject organisation = null;

	public OrgansationServiceTest() {
	}
	
	@Before
	public void loadOrganisation() throws Exception {
		config = ConfigFactory.parseResources("loader.conf");
        config = config.resolve();
		user = "dev_testing";
		authToken = "3f0c407c-8ce6-320c-b2f1-ef6adb3348a1";
		clientId = "0123697446204784640";
		context = new ExecutionContext(config, user, authToken, clientId);
        
		URL resource = Resources.getResource("organisation.json");
		String orgData = Resources.toString(resource, Charset.defaultCharset());
        JsonParser parser = new JsonParser();
		organisation = parser.parse(orgData).getAsJsonObject();
	}

	@Test
	public void testCreate() throws Exception {
		OrganisationServiceImpl service = new OrganisationServiceImpl(context);
		organisation.remove("channel");
		organisation.addProperty("channel", "test" + UUID.randomUUID().toString().substring(0, 8));
		service.createOrg(organisation, context);

		if (organisation.get("orgId") != null) {
			logger.info("OrgId Create : {}", organisation.get("organisationId"));
			// Retire the Organisation
			String obj = "{\"organisationId\":\"" + organisation.get("organisationId") + "\", \"status\":3}";
			JsonObject req = new JsonParser().parse(obj).getAsJsonObject();
			service.updateOrgStatus(req, context);
		}
	}

}
