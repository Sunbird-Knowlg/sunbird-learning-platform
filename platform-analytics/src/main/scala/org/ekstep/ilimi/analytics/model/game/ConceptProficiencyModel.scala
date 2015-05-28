package org.ekstep.ilimi.analytics.model.game

import scala.collection.mutable.Map

import org.apache.spark.HashPartitioner
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.ekstep.ilimi.analytics.model.BaseModel
import org.ekstep.ilimi.analytics.model.Event

case class ConceptProficiencyOutput(uid: String, before_screener: String, after_screener: String, before_score: Float, after_score: Float, difference: Float, percent_improvement: Float);

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
        Console.println("### Model run complete - Time taken to compute - " + (t2 - t1) / 1000 + " ###");
    }

    override def validate(args: Array[String]): Boolean = {
        Console.println("### Arguments - " + args.mkString(" # ") + " ###");
        if (args.length < 4)
            return false;
        return true;
    }

    override def compute(args: Array[String]) {
        if (validate(args)) {
            val parallelization = { if (args.length > 4) args(4) else null };
            val sc = initializeSparkContext(args(2), parallelization);
            computeGameEffectiveness(sc, args(0), args(1), args(3));
            closeSparkContext(sc);
        } else
            throw new Exception("Invalid arguments")
    }

    val validEvents = Array("GE_LAUNCH_GAME", "GE_GAME_END", "OE_ASSESS");
    def filter(e: Event): Boolean = {
        validEvents.contains(e.eid)
    }

    def computeGameEffectiveness(sc: SparkContext, input: String, output: String, gameData: String) {

        var gd: Array[String] = gameData.split(",");
        val gameId = gd(0);
        val beforeScreener = gd(1);
        val afterScreener = gd(2);
        val conceptId = gd(3);
        val baseRDD = loadInput(sc, input, filter);
        Console.println("### Computing concept improvement stats ###");
        val userPairs = baseRDD.map(event => (event.uid.get, Array(event))).partitionBy(new HashPartitioner(this.parallelization));
        val userScores = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            events.sortBy { event => event.ts };
            var gameMap: Map[String, (Float, Int)] = Map();
            var currGame: String = null;
            events.foreach { event =>
                {
                    event.eid match {
                        case "GE_LAUNCH_GAME" =>
                            currGame = event.edata.eks.gid.get;
                            if (!gameMap.contains(currGame)) {
                                gameMap(currGame) = (0, 0);
                            }
                        case "OE_ASSESS" =>
                            if (null != currGame) {
                                var currVal = gameMap(currGame);
                                gameMap(currGame) = (currVal._1 + (event.edata.eks.score.get.floatValue() / event.edata.eks.maxscore.get.floatValue()), currVal._2 + 1);
                            }
                        case _ => {}
                    }
                }
            }

            ((gameMap(beforeScreener)._1 / gameMap(beforeScreener)._2) * 100, (gameMap(afterScreener)._1 / gameMap(afterScreener)._2) * 100);
        }).map(f => ConceptProficiencyOutput(f._1, beforeScreener, afterScreener, f._2._1, f._2._2, f._2._2 - f._2._1, (f._2._2 - f._2._1) / f._2._1))

        val result = userScores.collect().toBuffer;
        Console.println("### Saving Concept Improvement stats to " + getPath(output + "/concept_improvement") + " ###");
        saveResult(sc, result, output + "/concept_improvement");
        Console.println("### Saving Concept Improvement stats to RDS ###");
        EffectivenessStatsDAO.saveConceptEffectivness(result, gameId, conceptId);
    }

}