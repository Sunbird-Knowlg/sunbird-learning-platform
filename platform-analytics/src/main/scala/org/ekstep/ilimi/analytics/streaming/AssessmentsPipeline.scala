package org.ekstep.ilimi.analytics.streaming

import scala.collection.mutable.Buffer

import org.apache.logging.log4j.LogManager
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.ekstep.ilimi.analytics.dao.GameLevel
import org.ekstep.ilimi.analytics.dao.UserGameLevel
import org.ekstep.ilimi.analytics.dao.UserStatsDAO
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.util.Application
import org.ekstep.ilimi.analytics.util.CommonUtil

import kafka.serializer.StringDecoder

object AssessmentsPipeline extends Application {

    @transient val logger = LogManager.getLogger(AssessmentsPipeline.getClass);

    def main(brokerList: String, topic: String) {
        val ssc = CommonUtil.getSparkStreamingContext("AssessmentsPipeline", Seconds(10));
        Console.println("## Started spark streaming context ##");
        val kafkaParams = Map[String, String]("metadata.broker.list" -> brokerList);
        val messages = KafkaUtils.createDirectStream[String, Event, StringDecoder, EventDecoder](ssc, kafkaParams, Set(topic));
        Console.println("## Started spark kafka consumer ##");
        computeUserGameLevels(messages);
        ssc.start();
        ssc.awaitTermination();
    }

    def computeUserGameLevels(messages: InputDStream[(String, Event)]) = {
        val validEvents = Array("OE_LEVEL_SET");
        messages.foreachRDD(rdd =>
            {
                val userGames = rdd.map(msg => {
                    logger.info(CommonUtil.jsonToString(msg._2));
                    msg._2;
                })
                    .filter { e => validEvents.contains(e.eid) }
                    .map(event => (event.uid.get, Buffer(GameLevel(event.gdata.id, event.edata.eks.current.get))))
                    .reduceByKey((a, b) => a ++ b).mapValues(events => {
                        events.map(event => (event.gid, event.level))
                            .groupBy { x => x._1 }
                            .mapValues { x =>
                                {
                                    //Console.println(x.mkString("|"));
                                    x.map(f => f._2).max;
                                }
                            }
                            .map(f => GameLevel(f._1, f._2));
                    })
                    .map(f => UserGameLevel(f._1, f._2.toArray));
                UserStatsDAO.saveUserGameLevels(userGames.collect());
            });

    }
}