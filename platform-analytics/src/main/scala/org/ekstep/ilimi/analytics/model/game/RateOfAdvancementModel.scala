package org.ekstep.ilimi.analytics.model.game

import scala.collection.mutable.Map
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.ekstep.ilimi.analytics.model.BaseModel
import org.ekstep.ilimi.analytics.model.Event
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.pretty
import org.apache.spark.SparkContext

case class GameOutput(gameId: String, levels: Int, time_taken: Float, roa_ratio: Float);
case class RateOfAdvancementOutput(uid: String, games: Array[GameOutput]);

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
        Console.println("Time taken to compute - " + (t2 - t1) / 1000);
    }

    def validate(args: Array[String]): Boolean = {
        Console.println("### Arguments - " + args.mkString(" # ") + " ###");
        if (args.length < 3)
            return false;
        return true;
    }

    def compute(args: Array[String]) {
        if (validate(args)) {
            val parallelization = { if (args.length > 3) args(3) else null };
            val sc = initializeSparkContext(args(2), parallelization);
            computeGameEffectiveness(sc, args(0), args(1));
            closeSparkContext(sc);
        } else
            throw new Exception("Invalid arguments")
    }

    val validEvents = Array("GE_LAUNCH_GAME", "GE_GAME_END", "OE_ASSESS");
    def filter(e: Event): Boolean = {
        validEvents.contains(e.eid)
    }

    //TODO: Remove level hard coding
    def computeGameEffectiveness(sc: SparkContext, input: String, output: String) {
        val baseRDD = loadInput(sc, input, filter);
        Console.println("### Computing rate of advancement stats ###");
        val userPairs = baseRDD.map(event => (event.uid.get, Array(event))).partitionBy(new HashPartitioner(this.parallelization));
        val userScores = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            events.sortBy { event => event.ts };
            var gameMap: Map[String, (Long, Long)] = Map();
            var currGame: String = null;
            events.foreach { event =>
                {
                    event.eid match {
                        case "GE_LAUNCH_GAME" =>
                            currGame = event.edata.eks.gid.get;
                            if (!gameMap.contains(currGame)) {
                                gameMap(currGame) = (0, 0);
                            }
                        case "OE_LEVEL_SET" =>
                            if (null != currGame) {
                                var currVal = gameMap(currGame);
                                var minTime = currVal._1;
                                var maxTime = currVal._2;
                                event.edata.eks.current.get match {
                                    case 1 =>
                                        if (minTime.intValue().equals(0))
                                            minTime = event.ts;
                                    case 5 => maxTime = event.ts;
                                    case _ =>
                                }
                                gameMap(currGame) = (minTime, maxTime);
                            }
                        case _ => {}
                    }
                }
            }
            gameMap.map(f => (f._1, ((f._2._2 - f._2._1).toFloat / 3600000))).map(f => (f._1, (f._2, 5.toFloat / f._2)))
                .map(f => GameOutput(f._1, 5, f._2._1, f._2._2));
        }).map(f => RateOfAdvancementOutput(f._1, f._2.toArray));

        val result = userScores.collect().toBuffer;
        Console.println("### Saving Rate of advancement stats to " + getPath(output + "/concept_improvement") + " ###");
        saveResult(sc, result, output + "/rate_of_advancement");
        Console.println("### Saving Rate of advancement stats to RDS ###");
        EffectivenessStatsDAO.saveRateOfAdvStats(result);
    }
}