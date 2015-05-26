package com.ilimi.analytics.manager;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ilimi.analytics.dac.dto.GameStatsDTO;
import com.ilimi.analytics.enums.AnalyticsParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams.StatusType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:mw-spring/analytics-test-mgr-context.xml" })
public class GameStatsManagerTest {

	@Autowired
	private IGameStatsManager gameStatsManager;

	@SuppressWarnings("unchecked")
	@Test
	public void testGameStats() {

		// empty test
		List<String> gameIds = new ArrayList<String>();
		Response resp = gameStatsManager.getGameStats(gameIds);
		assertEquals(resp.getParams().getStatus(), StatusType.successful.name());

		List<GameStatsDTO> gameStats = (List<GameStatsDTO>) resp.get(AnalyticsParams.game_stats.name());
		assertNotNull(gameStats);
		assertTrue(gameStats.size() > 0);
		for (GameStatsDTO gameStat : gameStats) {
			System.out.println("Game Stat - " + gameStat);
		}
	}

}
