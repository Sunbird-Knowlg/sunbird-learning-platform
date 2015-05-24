package com.ilimi.analytics.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ilimi.analytics.dac.dto.ConceptStatsDTO;
import com.ilimi.analytics.enums.AnalyticsParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams.StatusType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:mw-spring/analytics-test-mgr-context.xml" })
public class ConceptStatsManagerTest {

	@Autowired
	private IConceptStatsManager conceptStatsManager;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConceptStats() {

		// empty test
		List<String> conceptIds = new ArrayList<String>();
		Response resp = conceptStatsManager.getConceptStats(conceptIds);
		assertEquals(resp.getParams().getStatus(), StatusType.successful.name());

		List<ConceptStatsDTO> conceptStats = (List<ConceptStatsDTO>) resp.get(AnalyticsParams.concept_stats.name());
		assertNotNull(conceptStats);
		assertTrue(conceptStats.size() > 0);
		for (ConceptStatsDTO conceptStat : conceptStats) {
			System.out.println("Concept Stat - " + conceptStat);
		}
		
		conceptIds.add("Concept:2");
		resp = conceptStatsManager.getConceptStats(conceptIds);
		assertEquals(resp.getParams().getStatus(), StatusType.successful.name());
		conceptStats = (List<ConceptStatsDTO>) resp.get(AnalyticsParams.concept_stats.name());
		assertNotNull(conceptStats);
		assertTrue(conceptStats.size() == 0);
	}
}
