package com.ilimi.analytics.dac.dto;

import java.io.Serializable;

public class UserStatsDTO implements Serializable {

	private static final long serialVersionUID = -4755466399848263516L;

	private String uid;
	
	private Long totalTime;
	
	private Integer totalSessions;
	
	private Float avgGameSession;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Long totalTime) {
		this.totalTime = totalTime;
	}

	public Integer getTotalSessions() {
		return totalSessions;
	}

	public void setTotalSessions(Integer totalSessions) {
		this.totalSessions = totalSessions;
	}

	public Float getAvgGameSession() {
		return avgGameSession;
	}

	public void setAvgGameSession(Float avgGameSession) {
		this.avgGameSession = avgGameSession;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserStatsDTO [uid=");
		builder.append(uid);
		builder.append(", totalTime=");
		builder.append(totalTime);
		builder.append(", totalSessions=");
		builder.append(totalSessions);
		builder.append(", avgGameSession=");
		builder.append(avgGameSession);
		builder.append("]");
		return builder.toString();
	}

}
