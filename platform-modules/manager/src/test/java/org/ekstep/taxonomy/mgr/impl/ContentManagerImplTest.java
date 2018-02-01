package org.ekstep.taxonomy.mgr.impl;

import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class ContentManagerImplTest extends GraphEngineTestSetup {

	@Autowired
	private IContentManager contentManager;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void init() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json");
	}

	@AfterClass
	public static void clean() {

	}

}
