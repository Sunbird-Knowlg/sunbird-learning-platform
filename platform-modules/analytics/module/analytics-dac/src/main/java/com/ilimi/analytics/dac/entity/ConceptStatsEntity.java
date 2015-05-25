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
@Table(name = "CONCEPT_STATS")
public class ConceptStatsEntity implements Serializable {

	private static final long serialVersionUID = 5895194416637216001L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "CONCEPT_STATS_ID_SEQ")
	@SequenceGenerator(name = "CONCEPT_STATS_ID_SEQ", sequenceName = "CONCEPT_STATS_ID_SEQ")
	@Column(name = "id", length = 11)
	private Integer id;

	@Column(name = "GAME_ID")
	private String gameId;

	@Column(name = "CONCEPT_ID")
	private String conceptId;

	@Column(name = "MIN_IMPROVEMENT")
	private Float minImprovement;

	@Column(name = "MAX_IMPROVEMENT")
	private Float maxImprovement;

	@Column(name = "MEAN_IMPROVEMENT")
	private Float meanImprovement;

	@Column(name = "EFFECT_SIZE")
	private Float effectSize;

	@Column(name = "SD_IMPROVEMENT")
	private Float sdImprovement;

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
			ConceptStatsEntity otherEntity = (ConceptStatsEntity) other;
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

	public String getConceptId() {
		return conceptId;
	}

	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}

	public Float getMinImprovement() {
		return minImprovement;
	}

	public void setMinImprovement(Float minImprovement) {
		this.minImprovement = minImprovement;
	}

	public Float getMaxImprovement() {
		return maxImprovement;
	}

	public void setMaxImprovement(Float maxImprovement) {
		this.maxImprovement = maxImprovement;
	}

	public Float getMeanImprovement() {
		return meanImprovement;
	}

	public void setMeanImprovement(Float meanImprovement) {
		this.meanImprovement = meanImprovement;
	}

	public Float getEffectSize() {
		return effectSize;
	}

	public void setEffectSize(Float effectSize) {
		this.effectSize = effectSize;
	}

	public Float getSdImprovement() {
		return sdImprovement;
	}

	public void setSdImprovement(Float sdImprovement) {
		this.sdImprovement = sdImprovement;
	}

}
