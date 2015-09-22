package org.ekstep.ilimi.analytics.model

import scala.collection.mutable.Buffer
import org.apache.spark.HashPartitioner
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.parse
import org.ekstep.ilimi.analytics.util.CommonUtil

case class ConceptProficiencyOutput(uid: String, before_screener: String, after_screener: String, before_score: Float, after_score: Float, difference: Float, percent_improvement: Float) extends Output;

object ConceptProficiencyModelV1 extends Serializable {

    /**
     * Arguments that are required in specific order
     * 1. input
     * 2. output
     * 3. File Location
     * 4. Game Data - [List of comma separated value of GameId,Before Screener Game, After Screener Game, Concept in the specific order]
     */
    def compute(input: String, output: String, location: String, gameData: String, parallelization: Int) {
        @transient val sc = CommonUtil.getSparkContext(parallelization, "GameEffectiveness");
        var gd: Array[String] = gameData.split(",");
        val gameId = gd(0);
        val beforeScreener = gd(1);
        val afterScreener = gd(2);
        val conceptId = gd(3);
        val validGames = Array(beforeScreener, afterScreener);
        val validEvents = Array("OE_ASSESS");

        val baseRDD = CommonUtil.loadData(sc, input, location, parallelization, x => validEvents.contains(x.eid) && validGames.contains(CommonUtil.getGameId(x)));
        Console.println("### Computing concept improvement stats ###");
        val userPairs = baseRDD.map(event => (event.uid.get, Buffer(event))).partitionBy(new HashPartitioner(parallelization));
        val userScores = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            val gameMap = events.map(event => (CommonUtil.getGameId(event), (event.edata.eks.score.get.floatValue() / event.edata.eks.maxscore.get.floatValue()) * 100))
                .groupBy { x => x._1 }
                .mapValues { x => x.map(f => f._2) }.mapValues { x => x.reduce(_ + _) / x.size }
                .toMap;
            (gameMap(beforeScreener), gameMap(afterScreener));
        }).map(f => ConceptProficiencyOutput(f._1, beforeScreener, afterScreener, f._2._1, f._2._2, f._2._2 - f._2._1, (f._2._2 - f._2._1) / f._2._1)).persist();

        CommonUtil.saveOutput(userScores, output, "concept_improvement.json", location);
        val stats = userScores.map { x => x.difference }.stats();
        Console.println("### Saving Concept Improvement stats to RDS ###");
        EffectivenessStatsDAO.saveConceptEffectivness(userScores.collect().toBuffer, gameId, conceptId, stats.mean, stats.stdev, stats.min, stats.max);
        CommonUtil.closeSparkContext(sc);
    }

}