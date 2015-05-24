package com.ilimi.analytics.dac.service;

import java.util.List;

import com.ilimi.common.dto.Response;

public interface IGameStatsDataService {
	
	Response getGameStats(List<String> gameIds);
	
}
