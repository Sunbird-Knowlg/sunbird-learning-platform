package org.ekstep.ilimi.analytics.dao

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

import org.ekstep.ilimi.analytics.model.game.ConceptProficiencyOutput
import org.ekstep.ilimi.analytics.model.game.RateOfAdvancementOutput

object EffectivenessStatsDAO extends BaseDAO {

    def saveConceptEffectivness(userScores: Buffer[ConceptProficiencyOutput], gameId: String, conceptId: String, mean: Double, sd: Double, min: Double, max: Double) {

        var data = userScores.map { stat => Array(getValue(stat.before_score), getValue(stat.after_score), getValue(stat.difference), getValue(stat.percent_improvement), conceptId, stat.uid, gameId) };
        var insertSQL = "INSERT INTO STUDENT_CONCEPT_STATS (BEFORE_SCORE, AFTER_SCORE, IMPROVEMENT_DIFF, IMPROVE_PERCENT, CONCEPT_ID, UID, GAME_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        var updateSQL = "UPDATE STUDENT_CONCEPT_STATS SET BEFORE_SCORE = ?, AFTER_SCORE = ?, IMPROVEMENT_DIFF = ?, IMPROVE_PERCENT = ?, CONCEPT_ID = ? WHERE UID = ? AND GAME_ID = ?";
        batchMerge(insertSQL, updateSQL, data);

        insertSQL = "INSERT INTO CONCEPT_STATS (MIN_IMPROVEMENT, MAX_IMPROVEMENT, MEAN_IMPROVEMENT, SD_IMPROVEMENT, EFFECT_SIZE, CONCEPT_ID, GAME_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        updateSQL = "UPDATE CONCEPT_STATS SET MIN_IMPROVEMENT = ?, MAX_IMPROVEMENT = ?, MEAN_IMPROVEMENT = ?, SD_IMPROVEMENT = ?, EFFECT_SIZE = ? WHERE CONCEPT_ID = ? and GAME_ID = ?";
        data = new ListBuffer[Array[AnyRef]]();
        val tstat = mean / sd;
        data += Array(min.asInstanceOf[AnyRef], max.asInstanceOf[AnyRef], mean.asInstanceOf[AnyRef], sd.asInstanceOf[AnyRef], tstat.asInstanceOf[AnyRef], conceptId, gameId);
        batchMerge(insertSQL, updateSQL, data);
    }

    def saveRateOfAdvStats(userScores: Buffer[RateOfAdvancementOutput], gameStats: ListBuffer[Array[AnyRef]]) {

        var data: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        var insertSQL = "INSERT INTO STUDENT_GAME_STATS (START_LEVEL, END_LEVEL, TIME_TAKEN, ROA_RATIO, UID, GAME_ID) VALUES (?, ?, ?, ?, ?, ?)";
        var updateSQL = "UPDATE STUDENT_GAME_STATS SET START_LEVEL = ?, END_LEVEL = ?, TIME_TAKEN = ?, ROA_RATIO = ? WHERE UID = ? AND GAME_ID = ?";
        userScores.foreach { stat =>
            stat.games.foreach(f => data += Array(1.asInstanceOf[AnyRef], f.levels.asInstanceOf[AnyRef], getValue(f.time_taken), getValue(f.roa_ratio), stat.uid, f.gameId))
        }

        batchMerge(insertSQL, updateSQL, data);

        insertSQL = "INSERT INTO GAME_STATS (MEAN_TIME_TO_MASTER, LEVELS, SD_TIME_TO_MASTER, EFFECT_SIZE, GAME_ID) VALUES (?, ?, ?, ?, ?)";
        updateSQL = "UPDATE GAME_STATS SET MEAN_TIME_TO_MASTER = ?, LEVELS = ?, SD_TIME_TO_MASTER = ?, EFFECT_SIZE = ? WHERE GAME_ID = ?";
        batchMerge(insertSQL, updateSQL, gameStats);
    }
}