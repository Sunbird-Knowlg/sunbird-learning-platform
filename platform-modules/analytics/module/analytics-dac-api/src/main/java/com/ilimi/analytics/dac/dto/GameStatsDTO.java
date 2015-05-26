package com.ilimi.analytics.dac.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameStatsDTO implements Serializable {

	private static final long serialVersionUID = -4755466399848263516L;

	private String gameId;

	private List<StatsDTO> stats = new ArrayList<StatsDTO>();

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public List<StatsDTO> getStats() {
		return stats;
	}

	public void setStats(List<StatsDTO> stats) {
		this.stats = stats;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GameStatsDTO [gameId=");
		builder.append(gameId);
		builder.append(", stats=");
		builder.append(stats);
		builder.append("]");
		return builder.toString();
	}
}
