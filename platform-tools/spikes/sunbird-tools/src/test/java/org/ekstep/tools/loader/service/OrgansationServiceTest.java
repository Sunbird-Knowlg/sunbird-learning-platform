/**
 * 
 */
package org.ekstep.tools.loader.service;

import java.net.URL;
import java.nio.charset.Charset;

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
	private String user = null;
	private ExecutionContext context = null;
	private JsonObject organisation = null;

	public OrgansationServiceTest() {
	}
	
	@Before
	public void loadOrganisation() throws Exception {
		config = ConfigFactory.parseResources("loader.conf");
        config = config.resolve();
        user = "bulk-loader-test";
        context = new ExecutionContext(config, user);
        
		URL resource = Resources.getResource("organisation.json");
		String orgData = Resources.toString(resource, Charset.defaultCharset());
        JsonParser parser = new JsonParser();
		organisation = parser.parse(orgData).getAsJsonObject();
	}

	@Test
	public void testCreate() throws Exception {
		OrganisationServiceImpl service = new OrganisationServiceImpl();

		service.init(context);
		service.createOrg(organisation, context);

		if (organisation.get("orgId") != null) {
			logger.info("OrgId Create : {}", organisation.get("orgId"));
			// Retire the Organisation
			String obj = "{\"organisationId\":\"" + organisation.get("orgId") + "\", \"status\":3}";
			JsonObject req = new JsonParser().parse(obj).getAsJsonObject();
			service.updateOrgStatus(req, context);
		}
	}

}
