package org.sunbird.graph.dac.util;

import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.graph.service.request.validator.Neo4JBoltDataVersionKeyValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Neo4JBoltDataVersionKeyValidator.class)
@PowerMockIgnore("javax.management.*")
public class Neo4jBoltDataVersionKeyValidatorTest {

	private static final String method ="getNeo4jNodeVersionKey";
	private String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);

	@Test
	public void testVersionValidator() throws Exception {
		Neo4JBoltDataVersionKeyValidator spy = PowerMockito.spy(new Neo4JBoltDataVersionKeyValidator());
		PowerMockito.doReturn("123345").when(spy, method, anyString(), anyString());

		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");

		node.setMetadata(new HashMap<String, Object>(){{
			put("versionKey", "123345");
		}});
		boolean isvalid = spy.validateUpdateOperation("domain", node, "STRICT", "1233345");
		Assert.assertTrue(isvalid);
	}

	@Test
	public void testVersionValidator2() throws Exception {
		Neo4JBoltDataVersionKeyValidator spy = PowerMockito.spy(new Neo4JBoltDataVersionKeyValidator());
		PowerMockito.doReturn("123345").when(spy, method, anyString(), anyString());

		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");

		node.setMetadata(new HashMap<String, Object>(){{
			put("versionKey", "123345");
		}});
		boolean isvalid = spy.validateUpdateOperation("domain", node, "STRICT", "123345");
		Assert.assertTrue(isvalid);
	}

	@Test(expected = ClientException.class)
	public void testVersionValidator3() throws Exception {
		Neo4JBoltDataVersionKeyValidator spy = PowerMockito.spy(new Neo4JBoltDataVersionKeyValidator());
		PowerMockito.doReturn("12345").when(spy, method, anyString(), anyString());

		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");

		node.setMetadata(new HashMap<String, Object>(){{
			put("versionKey", "123345");
		}});
		spy.validateUpdateOperation("domain", node, "STRICT", "1233345");
	}

	@Test(expected = ClientException.class)
	public void testVersionValidator4() throws Exception {
		Neo4JBoltDataVersionKeyValidator spy = PowerMockito.spy(new Neo4JBoltDataVersionKeyValidator());
		PowerMockito.doReturn("12345").when(spy, method, anyString(), anyString());

		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");

		node.setMetadata(new HashMap<String, Object>(){{
			put("versionKey", "");
		}});
		spy.validateUpdateOperation("domain", node, "STRICT", "1233345");
	}

	@Test
	public void testVersionValidator6() throws Exception {
		Neo4JBoltDataVersionKeyValidator spy = PowerMockito.spy(new Neo4JBoltDataVersionKeyValidator());
		PowerMockito.doReturn("123345").when(spy, method, anyString(), anyString());

		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");

		node.setMetadata(new HashMap<String, Object>(){{
			put("versionKey", graphPassportKey);
		}});
		boolean isvalid = spy.validateUpdateOperation("domain", node, "STRICT", "123345");
		Assert.assertTrue(isvalid);
	}


	@Test(expected = ClientException.class)
	public void testVersionValidator7() throws Exception {
		Neo4JBoltDataVersionKeyValidator spy = PowerMockito.spy(new Neo4JBoltDataVersionKeyValidator());
		PowerMockito.doReturn("12345").when(spy, method, anyString(), anyString());
		PowerMockito.doReturn(new HashMap<String, Object>()).when(spy, "getNeo4jNodeProperty", anyString(), anyString());

		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");

		node.setMetadata(new HashMap<String, Object>(){{
			put("versionKey", "12345");
		}});
		spy.validateUpdateOperation("domain", node, "STRICT", "");
	}

}
