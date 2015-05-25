package com.ilimi.analytics.dac.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "STUDENT_GAME_STATS")
public class StudentGameStatsEntity implements Serializable {

	private static final long serialVersionUID = 5895194416637216001L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "STUDENT_GAME_STATS_ID_SEQ")
	@SequenceGenerator(name = "STUDENT_GAME_STATS_ID_SEQ", sequenceName = "STUDENT_GAME_STATS_ID_SEQ")
	@Column(name = "id", length = 11)
	private Integer id;

	@Column(name = "UID")
	private String uid;

	@Column(name = "GAME_ID")
	private String gameId;

	@Column(name = "START_LEVEL")
	private Integer startLevel;

	@Column(name = "END_LEVEL")
	private Integer endLevel;

	@Column(name = "TIME_TAKEN")
	private Float timeTaken;

	@Column(name = "ROA_RATIO")
	private Float roaRatio;

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
			StudentGameStatsEntity otherEntity = (StudentGameStatsEntity) other;
			return (this.getId() == otherEntity.getId());
		}

		return true;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Integer getStartLevel() {
		return startLevel;
	}

	public void setStartLevel(Integer startLevel) {
		this.startLevel = startLevel;
	}

	public Integer getEndLevel() {
		return endLevel;
	}

	public void setEndLevel(Integer endLevel) {
		this.endLevel = endLevel;
	}

	public Float getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(Float timeTaken) {
		this.timeTaken = timeTaken;
	}

	public Float getRoaRatio() {
		return roaRatio;
	}

	public void setRoaRatio(Float roaRatio) {
		this.roaRatio = roaRatio;
	}

}
