package org.ekstep.ilimi.analytics.dao

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

import org.ekstep.ilimi.analytics.model.game.RateOfAdvancementOutput

object EffectivenessStatsDAO extends BaseDAO {

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