package com.ilimi.analytics.manager;

import java.util.List;

import com.ilimi.common.dto.Response;

public interface IGameStatsManager {
	
	Response getGameStats(List<String> gameIds);

}
