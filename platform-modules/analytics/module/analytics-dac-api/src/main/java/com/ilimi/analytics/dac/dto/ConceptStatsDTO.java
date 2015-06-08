package com.ilimi.analytics.dac.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConceptStatsDTO implements Serializable {

	private static final long serialVersionUID = -4755466399848263516L;

	private String gameId;

	private String conceptId;

	private List<StatsDTO> stats = new ArrayList<StatsDTO>();

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
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
		builder.append("ConceptStatsDTO [gameId=");
		builder.append(gameId);
		builder.append(", conceptId=");
		builder.append(conceptId);
		builder.append(", stats=");
		builder.append(stats);
		builder.append("]");
		return builder.toString();
	}

}
