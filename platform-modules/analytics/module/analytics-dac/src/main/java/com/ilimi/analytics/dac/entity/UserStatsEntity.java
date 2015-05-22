package com.ilimi.analytics.dac.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ilimi.dac.DataAccessEntity;

@Entity
@Table(name = "USER_STATS")
public class UserStatsEntity implements DataAccessEntity {

	private static final long serialVersionUID = 5895194416637216001L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "USER_STATS_ID_SEQ")
	@SequenceGenerator(name = "USER_STATS_ID_SEQ", sequenceName = "USER_STATS_ID_SEQ")
	@Column(name = "id", length = 11)
	private Integer id;

	@Column(name = "UID")
	private String uid;

	@Column(name = "TOTAL_TIME")
	private Long totalTime;

	@Column(name = "TOTAL_SESSIONS")
	private Integer totalSessions;

	@Column(name = "AVG_GAME_SESSION")
	private Float avgGameSession;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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
	public String getLastModifiedBy() {
		return null;
	}

	@Override
	public void setLastModifiedBy(String lastModifiedBy) {

	}

	@Override
	public Date getLastModifiedOn() {
		return null;
	}

	@Override
	public void setLastModifiedOn(Date lastModifiedOn) {

	}

	@Override
	public int hashCode() {
		if (getId() == null) {
			return super.hashCode();
		}
		return getId();
	}

	@Override
	public boolean equals(Object other) {

		if (other == null) {
			return false;
		}

		if (other.getClass().equals(this.getClass())) {
			UserStatsEntity otherEntity = (UserStatsEntity) other;
			return (this.getId() == otherEntity.getId());
		}

		return true;
	}

}
