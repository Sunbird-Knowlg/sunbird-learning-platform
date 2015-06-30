package org.ekstep.ilimi.analytics.dao

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

import org.apache.commons.dbutils.QueryRunner
import org.ekstep.ilimi.analytics.util.AppDBUtils

case class GameLevel(gid: String, level: Int);
case class UserGameLevel(uid: String, games: Array[GameLevel]);

object UserStatsDAO extends BaseDAO {

    def saveUserGameLevels(userGames: Array[UserGameLevel]) {
        
        if(userGames.length == 0) return;
        
        val conn = AppDBUtils.getConnection;
        val qr = new QueryRunner();

        var checkData: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        var updData: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        var insertData: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        userGames.foreach { stat =>
            stat.games.foreach(f => {
                checkData += Array(f.gid, stat.uid, f.gid);
                updData += Array(f.level.asInstanceOf[AnyRef], stat.uid, f.gid, f.level.asInstanceOf[AnyRef]);
                insertData += Array(f.level.asInstanceOf[AnyRef], stat.uid, f.gid);
            });
        }

        var insertSQL = "INSERT INTO USER_GAME_LEVELS (CURRENT_LEVEL, UID, GAME_ID) VALUES (?, ?, ?)";
        var updateSQL = "UPDATE USER_GAME_LEVELS SET CURRENT_LEVEL = ? WHERE UID = ? AND GAME_ID = ? AND ? > CURRENT_LEVEL";
        var checkSQL = "UPDATE USER_GAME_LEVELS SET GAME_ID = ? WHERE UID = ? AND GAME_ID = ?";

        // Update the game levels if the current level is greater than the stored level
        qr.batch(conn, updateSQL, updData.toArray)

        // Check whether the game/level entry exists for the user
        val ids: Array[Int] = qr.batch(conn, checkSQL, checkData.toArray);

        var insertBuffer: Buffer[Array[AnyRef]] = new ListBuffer[Array[AnyRef]]();
        for (i <- 0 until ids.length) {
            if (ids(i) == 0) {
                insertBuffer += insertData(i);
            }
        }
        // Insert the missing records
        if (insertBuffer.size > 0) {
            Console.println("### New data to insert - " + insertBuffer.size + " ###");
            qr.batch(conn, insertSQL, insertBuffer.toArray);
        }
        AppDBUtils.closeConnection(conn);

    }

}