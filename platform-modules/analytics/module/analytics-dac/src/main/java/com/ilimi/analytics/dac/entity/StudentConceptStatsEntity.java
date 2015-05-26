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
@Table(name = "STUDENT_CONCEPT_STATS")
public class StudentConceptStatsEntity implements Serializable {

	private static final long serialVersionUID = 5895194416637216001L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "STUDENT_CONCEPT_STATS_ID_SEQ")
	@SequenceGenerator(name = "STUDENT_CONCEPT_STATS_ID_SEQ", sequenceName = "STUDENT_CONCEPT_STATS_ID_SEQ")
	@Column(name = "id", length = 11)
	private Integer id;

	@Column(name = "UID")
	private String uid;

	@Column(name = "GAME_ID")
	private String gameId;

	@Column(name = "CONCEPT_ID")
	private String conceptId;

	@Column(name = "BEFORE_SCORE")
	private Float beforeScore;

	@Column(name = "AFTER_SCORE")
	private Float afterScore;

	@Column(name = "IMPROVEMENT_DIFF")
	private Float improvementDiff;

	@Column(name = "IMPROVE_PERCENT")
	private Float improvePercent;

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
			StudentConceptStatsEntity otherEntity = (StudentConceptStatsEntity) other;
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

	public Float getBeforeScore() {
		return beforeScore;
	}

	public void setBeforeScore(Float beforeScore) {
		this.beforeScore = beforeScore;
	}

	public Float getAfterScore() {
		return afterScore;
	}

	public void setAfterScore(Float afterScore) {
		this.afterScore = afterScore;
	}

	public Float getImprovementDiff() {
		return improvementDiff;
	}

	public void setImprovementDiff(Float improvementDiff) {
		this.improvementDiff = improvementDiff;
	}

	public Float getImprovePercent() {
		return improvePercent;
	}

	public void setImprovePercent(Float improvePercent) {
		this.improvePercent = improvePercent;
	}

}
