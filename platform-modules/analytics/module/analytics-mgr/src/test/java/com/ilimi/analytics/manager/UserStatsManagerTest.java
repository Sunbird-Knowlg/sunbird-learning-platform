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

import com.ilimi.analytics.dac.dto.UserStatsDTO;
import com.ilimi.analytics.enums.AnalyticsParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams.StatusType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:mw-spring/analytics-test-mgr-context.xml" })
public class UserStatsManagerTest {

	@Autowired
	private IUserStatsManager userStatsManager;

	@Test
	@SuppressWarnings("unchecked")
	public void testSearchStats() {
		List<String> uids = new ArrayList<String>();
		Response resp = userStatsManager.searchUserStats(uids);
		assertEquals(resp.getParams().getStatus(), StatusType.SUCCESS.name());
		List<UserStatsDTO> stats = (List<UserStatsDTO>) resp
				.get(AnalyticsParams.user_stats.name());
		assertNotNull(stats);
		assertTrue(stats.size() > 0);
		System.out.println(stats.get(0));
		System.out.println(stats.size());

		uids.add("08d5e763-0666-4bf2-9aea-0675684bc2de");
		resp = userStatsManager.searchUserStats(uids);
		assertEquals(resp.getParams().getStatus(), StatusType.SUCCESS.name());
		stats = (List<UserStatsDTO>) resp.get(AnalyticsParams.user_stats.name());
		assertNotNull(stats);
		System.out.println(stats.size());
		assertEquals(stats.get(0).getTotalTime().longValue(), 19320);
	}

}
