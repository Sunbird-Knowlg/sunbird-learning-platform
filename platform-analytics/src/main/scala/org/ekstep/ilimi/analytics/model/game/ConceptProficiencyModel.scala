package org.ekstep.ilimi.analytics.model.game

import scala.collection.mutable.Buffer

import org.apache.spark.HashPartitioner
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
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
    def compute(input: String, output: String, location: String, gd: String, parallelization: Int) {
        @transient val sc = initializeSparkContext(location, parallelization);
        computeGameEffectiveness(sc, input, output, gd);
        closeSparkContext(sc);
    }

    val validEvents = Array("OE_ASSESS");
    var validGames: Array[String] = null;

    def filter(e: Event): Boolean = {
        validEvents.contains(e.eid) && validGames.contains(e.gdata.id)
    }

    def computeGameEffectiveness(@transient sc: SparkContext, input: String, output: String, gameData: String) {

        var gd: Array[String] = gameData.split(",");
        val gameId = gd(0);
        val beforeScreener = gd(1);
        val afterScreener = gd(2);
        val conceptId = gd(3);
        validGames = Array(beforeScreener, afterScreener);

        val baseRDD = loadInput(sc, input, filter);
        Console.println("### Computing concept improvement stats ###");
        val userPairs = baseRDD.map(event => (event.uid.get, Buffer(event))).partitionBy(new HashPartitioner(this.parallelization));
        val userScores = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            val gameMap = events.map(event => (event.gdata.id, (event.edata.eks.score.get.floatValue() / event.edata.eks.maxscore.get.floatValue()) * 100))
                .groupBy { x => x._1 }
                .mapValues { x => x.map(f => f._2) }.mapValues { x => x.reduce(_ + _) / x.size }
                .toMap;
            (gameMap(beforeScreener), gameMap(afterScreener));
        }).map(f => ConceptProficiencyOutput(f._1, beforeScreener, afterScreener, f._2._1, f._2._2, f._2._2 - f._2._1, (f._2._2 - f._2._1) / f._2._1))

        val result = userScores.collect().toBuffer;
        Console.println("### Saving Concept Improvement stats to " + getPath(output + "/concept_improvement") + " ###");
        saveResult(sc, result, output + "/concept_improvement");
        Console.println("### Saving Concept Improvement stats to RDS ###");
        EffectivenessStatsDAO.saveConceptEffectivness(result, gameId, conceptId);
    }

}