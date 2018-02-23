package org.ekstep.taxonomy.mgr.impl;


import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class ContentManagerImplTest extends GraphEngineTestSetup {

	@Autowired
	private IContentManager contentManager;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void init() throws Exception {

	}

	@AfterClass
	public static void clean() {

	}

	@Test
	public void contentManagerTest_01() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String artifactUrl = "https://www.youtube.com/watch?v=owr198WQpM8";
		ContentManagerImpl contentMgr = new ContentManagerImpl();
		contentMgr.validateYoutubeLicense(artifactUrl, map);
		assertEquals("Creative Commons Attribution (CC BY)", map.get("license").toString());
	}

	@Test
	public void contentManagerTest_02() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String artifactUrl = "https://www.youtube.com/watch?v=_UR-l3QI2nE";
		ContentManagerImpl contentMgr = new ContentManagerImpl();
		contentMgr.validateYoutubeLicense(artifactUrl, map);
		assertEquals("Standard YouTube License", map.get("license").toString());
	}

	@Test
	public void contentManagerTest_03() throws Exception {
		exception.expect(ClientException.class);
		Map<String, Object> map = new HashMap<String, Object>();
		String artifactUrl = "https://goo.gl/bVBJNK";
		ContentManagerImpl contentMgr = new ContentManagerImpl();
		contentMgr.validateYoutubeLicense(artifactUrl, map);
	}

}
