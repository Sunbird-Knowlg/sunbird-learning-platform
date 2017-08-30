package com.ilimi.taxonomy.mgr.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentManagerImplTest {
	
	ContentManagerImpl contentManager = new ContentManagerImpl();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void beforeSetupTestSuit() {
		// TODO Auto-generated method stub

	}
	
	@After
	public void afterCleanData() {
		
	}
	
	
}
