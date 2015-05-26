package com.ilimi.analytics.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.analytics.dac.service.IGameStatsDataService;
import com.ilimi.analytics.manager.IGameStatsManager;
import com.ilimi.common.dto.Response;

@Component
public class GameStatsManagerImpl implements IGameStatsManager {

	@Autowired
	private IGameStatsDataService gameDataService;

	@Override
	public Response getGameStats(List<String> gameIds) {
		return gameDataService.getGameStats(gameIds);
	}

}
