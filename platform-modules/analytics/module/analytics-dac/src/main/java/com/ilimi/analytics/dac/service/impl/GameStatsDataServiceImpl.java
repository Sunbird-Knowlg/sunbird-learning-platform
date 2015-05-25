package com.ilimi.analytics.dac.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.genericdao.search.Search;
import com.ilimi.analytics.dac.dao.GameStatsDAO;
import com.ilimi.analytics.dac.dto.GameStatsDTO;
import com.ilimi.analytics.dac.dto.StatsDTO;
import com.ilimi.analytics.dac.entity.GameStatsEntity;
import com.ilimi.analytics.dac.service.IGameStatsDataService;
import com.ilimi.analytics.enums.AnalyticsParams;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;

@Component
@Transactional
public class GameStatsDataServiceImpl extends BaseDataAccessService implements IGameStatsDataService {

	@Autowired
	private GameStatsDAO gameStatsDAO;

	@Override
	public Response getGameStats(List<String> gameIds) {

		List<GameStatsDTO> gameStatsList = new ArrayList<GameStatsDTO>();
		List<GameStatsEntity> entities = null;
		if (null == gameIds || gameIds.isEmpty()) {
			entities = gameStatsDAO.findAll();
		} else {
			Search search = new Search();
			search.addFilterIn("gameId", gameIds);
			entities = gameStatsDAO.search(search);
		}

		if (null == entities) {
			return OK(AnalyticsParams.game_stats.name(), gameStatsList);
		}

		for (GameStatsEntity entity : entities) {
			GameStatsDTO dto = new GameStatsDTO();
			dto.setGameId(entity.getGameId());
			dto.getStats().add(new StatsDTO("levels", "Levels in this game", entity.getLevels().floatValue()));
			dto.getStats().add(new StatsDTO("mean", "Mean time to master the game (in hrs)", entity.getMeanTimeToMaster()));
			dto.getStats().add(new StatsDTO("sd", "Standard deviation", entity.getSdTimeToMaster()));
			dto.getStats().add(new StatsDTO("effectSize", "Effect Size (T-Statistic)", entity.getEffectSize()));
			gameStatsList.add(dto);
		}

		return OK(AnalyticsParams.game_stats.name(), gameStatsList);
	}

}
