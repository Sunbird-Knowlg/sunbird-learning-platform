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
@Table(name = "GAME_STATS")
public class GameStatsEntity implements Serializable {

	private static final long serialVersionUID = 5895194416637216001L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "GAME_STATS_ID_SEQ")
	@SequenceGenerator(name = "GAME_STATS_ID_SEQ", sequenceName = "GAME_STATS_ID_SEQ")
	@Column(name = "id", length = 11)
	private Integer id;

	@Column(name = "GAME_ID")
	private String gameId;

	@Column(name = "MEAN_TIME_TO_MASTER")
	private Float meanTimeToMaster;

	@Column(name = "LEVELS")
	private Integer levels;

	@Column(name = "EFFECT_SIZE")
	private Float effectSize;

	@Column(name = "SD_TIME_TO_MASTER")
	private Float sdTimeToMaster;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
			GameStatsEntity otherEntity = (GameStatsEntity) other;
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

	public Float getMeanTimeToMaster() {
		return meanTimeToMaster;
	}

	public void setMeanTimeToMaster(Float meanTimeToMaster) {
		this.meanTimeToMaster = meanTimeToMaster;
	}

	public Integer getLevels() {
		return levels;
	}

	public void setLevels(Integer levels) {
		this.levels = levels;
	}

	public Float getEffectSize() {
		return effectSize;
	}

	public void setEffectSize(Float effectSize) {
		this.effectSize = effectSize;
	}

	public Float getSdTimeToMaster() {
		return sdTimeToMaster;
	}

	public void setSdTimeToMaster(Float sdTimeToMaster) {
		this.sdTimeToMaster = sdTimeToMaster;
	}

}
