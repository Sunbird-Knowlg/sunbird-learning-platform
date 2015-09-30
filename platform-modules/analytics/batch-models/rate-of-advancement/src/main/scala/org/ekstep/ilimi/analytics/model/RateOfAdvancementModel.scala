package org.ekstep.ilimi.analytics.model.game

import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import org.apache.spark.HashPartitioner
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.ekstep.ilimi.analytics.dao.EffectivenessStatsDAO
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.model.Output
import org.ekstep.ilimi.analytics.util.CommonUtil
import java.util.Date
import java.text.SimpleDateFormat

case class GameOutput(gameId: String, levels: Int, time_taken: Float, roa_ratio: Float);
case class RateOfAdvancementOutput(uid: String, games: Array[GameOutput]) extends Output;

object RateOfAdvancementModel {

    @transient val df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss+z");

    def compute(input: String, output: String, location: String, parallelization: Int) {
        val validEvents = Array("OE_LEVEL_SET");
        val sc = CommonUtil.getSparkContext(parallelization, "GameEffectiveness");
        val baseRDD = CommonUtil.loadData(sc, input, location, parallelization, e => validEvents.contains(e.eid))
        Console.println("### Computing rate of advancement stats ###");
        val userPairs = baseRDD.map(event => (event.uid.get, Buffer(event))).partitionBy(new HashPartitioner(parallelization));
        val userScores = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            events.map(event => (CommonUtil.getGameId(event), event.ts, event.edata.eks.current.get.toInt, event.edata.eks.max.get.toInt))
                .groupBy { x => x._1 }
                .mapValues { x =>
                    {
                        x.sortBy(f => (f._3, f._2));
                        val min = x.take(1)(0);
                        val max = x.takeRight(1)(0);
                        (max._3 - min._3 + 1, df.parse(max._2.get).getTime - df.parse(min._2.get).getTime)
                    }
                }
                .mapValues(f => (f._1, f._2.toFloat / 3600000))
                .mapValues(f => (f._1, f._2, f._1.toFloat / f._2))
                .map(f => GameOutput(f._1, f._2._1, f._2._2, f._2._3));
        }).map(f => RateOfAdvancementOutput(f._1, f._2.toArray)).persist();

        CommonUtil.saveOutput(userScores, output, "rate_of_advancement.json", location);
        var gameMap: Map[String, Buffer[Float]] = Map();
        val result = userScores.collect().toBuffer;
        result.foreach(x =>
            x.games.foreach(f => {
                if (!gameMap.contains(f.gameId)) {
                    gameMap(f.gameId) = new ListBuffer[Float]();
                }
                gameMap(f.gameId) += f.roa_ratio;
            }));
        val data = new ListBuffer[Array[AnyRef]]();
        gameMap.foreach(f => {
            val stats = sc.parallelize(f._2).stats()
            val mean = stats.mean;
            val sd = stats.stdev;
            val tstat = mean / sd;
            data += Array(mean.asInstanceOf[AnyRef], 5.asInstanceOf[AnyRef], sd.asInstanceOf[AnyRef], tstat.asInstanceOf[AnyRef], f._1);
        })
        Console.println("### Saving Rate of advancement stats to RDS ###");
        EffectivenessStatsDAO.saveRateOfAdvStats(result, data);
        CommonUtil.closeSparkContext(sc);
    }

}