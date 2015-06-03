package org.ekstep.ilimi.analytics.model.game

import scala.annotation.migration
import scala.collection.mutable.Buffer

import org.apache.spark.HashPartitioner
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.ekstep.ilimi.analytics.model.BaseModel
import org.ekstep.ilimi.analytics.model.Event
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.compact

case class GameOutput(gameId: String, levels: Int, time_taken: Float, roa_ratio: Float);
case class RateOfAdvancementOutput(uid: String, games: Array[GameOutput]);

object RateOfAdvancementModel extends BaseModel {

    def compute(input: String, output: String, location: String, parallelization: Int) {
        val sc = initializeSparkContext(location, parallelization);
        computeGameEffectiveness(sc, input, output);
        closeSparkContext(sc);
    }

    val validEvents = Array("OE_LEVEL_SET");
    def filter(e: Event): Boolean = {
        validEvents.contains(e.eid)
    }

    def computeGameEffectiveness(sc: SparkContext, input: String, output: String) {
        val baseRDD = loadInput(sc, input, filter);
        Console.println("### Computing rate of advancement stats ###");
        val userPairs = baseRDD.map(event => (event.uid.get, Buffer(event))).partitionBy(new HashPartitioner(this.parallelization));
        val userScores = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            events.map(event => (event.gdata.id, event.ts, event.edata.eks.current.get, event.edata.eks.max.get))
                .groupBy { x => x._1 }
                .mapValues { x =>
                    {
                        x.sortBy(f => (f._3, f._2));
                        val min = x.take(1)(0);
                        val max = x.takeRight(1)(0);
                        (max._3 - min._3 + 1, max._2 - min._2)
                    }
                }
                .mapValues(f => (f._1, f._2.toFloat / 3600000))
                .mapValues(f => (f._1, f._2, f._1.toFloat / f._2))
                .map(f => GameOutput(f._1, f._2._1, f._2._2, f._2._3));
        }).map(f => RateOfAdvancementOutput(f._1, f._2.toArray)).persist();

        //val result = userScores.collect().toBuffer;
        Console.println("### Saving Rate of advancement stats to " + getPath(output + "/rate_of_advancement") + " ###");
        //saveResult(sc, result, output + "/rate_of_advancement");
        saveResult(userScores, output + "/rate_of_advancement");
        Console.println("### Saving Rate of advancement stats to RDS ###");
        //EffectivenessStatsDAO.saveRateOfAdvStats(result);
        EffectivenessStatsDAO.saveRateOfAdvStats(userScores.collect().toBuffer);
    }

    def saveResult(rdd: RDD[RateOfAdvancementOutput], output: String) = {
        rdd.map { output =>
            {
                implicit val formats = DefaultFormats;
                compact(Extraction.decompose(output))
            }
        }.saveAsTextFile(getPath(output));
    }
}