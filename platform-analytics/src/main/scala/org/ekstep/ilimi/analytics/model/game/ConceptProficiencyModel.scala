package org.ekstep.ilimi.analytics.model.game

import scala.collection.mutable.Map

import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.ekstep.ilimi.analytics.model.BaseModel

object ConceptProficiencyModel extends BaseModel {

    /**
     * Arguments that are required in specific order
     * 1. input
     * 2. output
     * 3. File Location
     * 4. Game Data - [List of comma separated value of GameId,Before Screener Game, After Screener Game, Concept in the specific order]
     */
    def main(args: Array[String]) {
        AppConf.init();
        val t1 = System.currentTimeMillis;
        compute(args);
        val t2 = System.currentTimeMillis;
        Console.println("Time taken to compute - " + (t2 - t1) / 1000);
    }

    def validate(args: Array[String]): Boolean = {
        Console.println("### Arguments - " + args.mkString(" # ") + " ###");
        if(args.length < 4) 
            return false;
        return true;
    }

    def compute(args: Array[String]) {
        if (validate(args))
            computeGameEffectiveness(args(0), args(1), args(2), args(3));
        else
            throw new Exception("Invalid arguments")
    }

    def computeGameEffectiveness(input: String, output: String, location: String, gameData: String) {

        var gd: Array[String] = gameData.split(",");
        val gameId = gd(0);
        val beforeScreener = gd(1);
        val afterScreener = gd(2);
        val conceptId = gd(3);
        initializeSparkContext(location);
        loadInput(input, "telemetry_events");
        Console.println("### Computing concept improvement stats ###");
        //val validSessions = queryData("SELECT distinct sid FROM telemetry_events where edata.eks.gid in ('" + beforeScreener + "', '" + afterScreener + "')").collect().map { x => x.getString(0) }
        //Console.println("### Valid Sessions - " + validSessions.length + " ###");
        //val results = queryData("SELECT uid, ts, eid, sid, edata.eks.gid, edata.eks.score, edata.eks.maxscore FROM telemetry_events where eid in ('GE_LAUNCH_GAME', 'GE_GAME_END', 'OE_ASSESS') and sid in ('" + validSessions.mkString("','") + "')");
        val results = queryData("SELECT uid, ts, eid, sid, edata.eks.gid, edata.eks.score, edata.eks.maxscore FROM telemetry_events where eid in ('GE_LAUNCH_GAME', 'GE_GAME_END', 'OE_ASSESS')").persist();
        val userPairs = results.map(row => (row.getString(0), Array(row))).reduceByKey((a, b) => a ++ b, 1).mapValues(rows => {
            rows.sortBy { row => row.getLong(1) };
            var gameMap: Map[String, (Float, Int)] = Map();
            var currGame: String = null;
            rows.foreach { row =>
                {
                    row.getString(2) match {
                        case "GE_LAUNCH_GAME" =>
                            currGame = row.getString(4);
                            if (!gameMap.contains(currGame)) {
                                gameMap(currGame) = (0, 0);
                            }
                        case "OE_ASSESS" =>
                            if (null != currGame) {
                                var currVal = gameMap(currGame);
                                gameMap(currGame) = (currVal._1 + (row.getLong(5).floatValue() / row.getLong(6).floatValue()), currVal._2 + 1);
                            }
                        case _ => {}
                    }
                }
            }

            ((gameMap(beforeScreener)._1 / gameMap(beforeScreener)._2) * 100, (gameMap(afterScreener)._1 / gameMap(afterScreener)._2) * 100);
        }).mapValues(f => (f._1, f._2, f._2 - f._1, (f._2 - f._1)/f._1));
        val outputJson = userPairs.map(x => "{\"uid\":" + x._1 + ",\"before_score\":" + x._2._1 + ",\"after_score\":" + x._2._2 + ",\"difference\":" + x._2._3 + "}")
        Console.println("### Saving Concept Improvement stats to " + getPath(output + "/concept_improvement") + " ###");
        outputJson.saveAsTextFile(getPath(output + "/concept_improvement"))
        
        EffectivenessStatsDAO.saveConceptEffectivness(userPairs.collect(), gameId, conceptId);
        closeSparkContext;
    }

}