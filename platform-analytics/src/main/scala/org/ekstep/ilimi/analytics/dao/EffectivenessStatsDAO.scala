package org.ekstep.ilimi.analytics.dao

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.ekstep.ilimi.analytics.util.AppDBUtils
import org.apache.commons.dbutils.QueryRunner
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map

object EffectivenessStatsDAO extends BaseDAO {

    def saveConceptEffectivness(userScores: Array[(String, (Float, Float, Float, Float))], gameId: String, conceptId: String) {

        val buffer = userScores.toBuffer;
        var data = buffer.map { stat => Array(getValue(stat._2._1), getValue(stat._2._2), getValue(stat._2._3), getValue(stat._2._4), conceptId, stat._1, gameId) };
        var insertSQL = "INSERT INTO STUDENT_CONCEPT_STATS (BEFORE_SCORE, AFTER_SCORE, IMPROVEMENT_DIFF, IMPROVE_PERCENT, CONCEPT_ID, UID, GAME_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        var updateSQL = "UPDATE STUDENT_CONCEPT_STATS SET BEFORE_SCORE = ?, AFTER_SCORE = ?, IMPROVEMENT_DIFF = ?, IMPROVE_PERCENT = ?, CONCEPT_ID = ? WHERE UID = ? AND GAME_ID = ?";
        batchMerge(insertSQL, updateSQL, data);

        insertSQL = "INSERT INTO CONCEPT_STATS (MIN_IMPROVEMENT, MAX_IMPROVEMENT, MEAN_IMPROVEMENT, SD_IMPROVEMENT, EFFECT_SIZE, CONCEPT_ID, GAME_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        updateSQL = "UPDATE CONCEPT_STATS SET MIN_IMPROVEMENT = ?, MAX_IMPROVEMENT = ?, MEAN_IMPROVEMENT = ?, SD_IMPROVEMENT = ?, EFFECT_SIZE = ? WHERE CONCEPT_ID = ? and GAME_ID = ?";
        data = new ListBuffer[Array[AnyRef]]();
        var stats = new DescriptiveStatistics();
        userScores.map(x => stats.addValue(x._2._3));
        val mean = stats.getMean();
        val sd = stats.getStandardDeviation();
        val tstat = mean / sd;
        data += Array(stats.getMin().asInstanceOf[AnyRef], stats.getMax().asInstanceOf[AnyRef], mean.asInstanceOf[AnyRef], sd.asInstanceOf[AnyRef], tstat.asInstanceOf[AnyRef], conceptId, gameId);
        batchMerge(insertSQL, updateSQL, data);
    }

    def saveRateOfAdvStats(userScores: Array[(String, Map[String, (Float, Float)])]) {

        var data: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        var insertSQL = "INSERT INTO STUDENT_GAME_STATS (START_LEVEL, END_LEVEL, TIME_TAKEN, ROA_RATIO, UID, GAME_ID) VALUES (?, ?, ?, ?, ?, ?)";
        var updateSQL = "UPDATE STUDENT_GAME_STATS SET START_LEVEL = ?, END_LEVEL = ?, TIME_TAKEN = ?, ROA_RATIO = ? WHERE UID = ? AND GAME_ID = ?";
        userScores.foreach { stat =>
            stat._2.foreach(f => data += Array(1.asInstanceOf[AnyRef], 5.asInstanceOf[AnyRef], getValue(f._2._1), getValue(f._2._2), stat._1, f._1))
        }

        batchMerge(insertSQL, updateSQL, data);

        insertSQL = "INSERT INTO GAME_STATS (MEAN_TIME_TO_MASTER, LEVELS, SD_TIME_TO_MASTER, EFFECT_SIZE, GAME_ID) VALUES (?, ?, ?, ?, ?)";
        updateSQL = "UPDATE GAME_STATS SET MEAN_TIME_TO_MASTER = ?, LEVELS = ?, SD_TIME_TO_MASTER = ?, EFFECT_SIZE = ? WHERE GAME_ID = ?";
        data = new ListBuffer[Array[AnyRef]]();
        var gameMap: Map[String, Buffer[Float]] = Map();
        var stats:DescriptiveStatistics = null;
        userScores.foreach(x =>
            x._2.foreach(f => {
                if (!gameMap.contains(f._1)) {
                    gameMap(f._1) = new ListBuffer[Float]();
                }
                gameMap(f._1) += f._2._2;
            }))
        gameMap.foreach(f => {
            stats = new DescriptiveStatistics();
            f._2.foreach { x => stats.addValue(getFloatValue(x)); }
            val mean = stats.getMean();
            val sd = stats.getStandardDeviation();
            val tstat = mean / sd;
            data += Array(mean.asInstanceOf[AnyRef], 5.asInstanceOf[AnyRef], sd.asInstanceOf[AnyRef], tstat.asInstanceOf[AnyRef], f._1);
        })
        batchMerge(insertSQL, updateSQL, data);
    }
}