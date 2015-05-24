package org.ekstep.ilimi.analytics.model.game

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.Row
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.StatUtils
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.ekstep.ilimi.analytics.conf.AppConf
import org.apache.spark.sql.hive.HiveContext
import org.ekstep.ilimi.analytics.model.BaseModel

object RateOfAdvancementModel extends BaseModel {
    
    /**
     * Arguments that are required in specific order 
     * 1. input
     * 2. output
     * 3. File Location
     */
    def main(args: Array[String]) {
        val t1 = System.currentTimeMillis;
        AppConf.init();
        compute(args);
        val t2 = System.currentTimeMillis;
        Console.println("Time taken to compute - " + (t2 -t1)/1000);
    }

    def validate(args: Array[String]): Boolean = {
        Console.println("### Arguments - " + args.mkString(" # ") + " ###");
        return true;
    }

    def compute(args: Array[String]) {
        if (validate(args))
            computeGameEffectiveness(args(0), args(1), args(2));
        else
            throw new Exception("Invalid arguments")
    }

    //TODO: Remove level hard coding
    def computeGameEffectiveness(input: String, output: String, location: String) {
        initializeSparkContext(location);
        loadInput(input, "telemetry_events");
        Console.println("### Computing rate of advancement stats ###");
        val results = queryData("SELECT uid, ts, eid, sid, edata.eks.gid as gid, edata.eks.curr as curr, edata.eks.max as max FROM telemetry_events where eid in ('GE_LAUNCH_GAME', 'OE_LEVEL_SET')");
        val userPairs = results.map(row => (row.getString(0), Array(row))).reduceByKey((a, b) => a ++ b, 1).mapValues(rows => {
            rows.sortBy { row => row.getLong(1) };
            var gameMap: Map[String, (Long, Long)] = Map();
            var currGame: String = null;
            rows.foreach { row =>
                {
                    row.getString(2) match {
                        case "GE_LAUNCH_GAME" =>
                            currGame = row.getString(4);
                            if (!gameMap.contains(currGame)) {
                                gameMap(currGame) = (0, 0);
                            }
                        case "OE_LEVEL_SET" =>
                            if(null != currGame) {
                            var currVal = gameMap(currGame);
                            var minTime = currVal._1;
                            var maxTime = currVal._2;
                            row.getLong(5) match {
                                case 1 => 
                                    if(minTime.intValue().equals(0)) 
                                        minTime = row.getLong(1);
                                case 5 => maxTime = row.getLong(1);
                                case _ =>
                            }
                            gameMap(currGame) = (minTime, maxTime);
                            }
                        case _ => {}
                    }
                }
            }
            gameMap.map(f => (f._1, (f._2._2 - f._2._1)/3600000)).map(f => (f._1, f._2, 5.toFloat/f._2));
            (gameMap);
        });
        
        val outputJson = userPairs.map(x => 
            "{\"uid\":" + x._1 + ",\"games\":[" + 
            x._2.map(f => Array("{\"gameId\":\"" + f._1 + "\", \"levels\": 5, \"time_taken\": "+f._2._1+", \"roa_ratio\": "+f._2._1+"}")).mkString(",")
            + "]}");
        Console.println("### Saving Rate of advancement stats to " + getPath(output + "/concept_improvement") + " ###");
        outputJson.saveAsTextFile(getPath(output + "/rate_of_advancement"))
        EffectivenessStatsDAO.saveRateOfAdvStats(userPairs.collect());
        closeSparkContext;
    }
}