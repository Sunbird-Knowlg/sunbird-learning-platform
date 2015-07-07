package org.ekstep.ilimi.analytics.streaming

import scala.annotation.migration
import scala.collection.mutable.Buffer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream.toPairDStreamFunctions
import org.apache.spark.streaming.kafka.KafkaUtils
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.util.Application
import org.ekstep.ilimi.analytics.util.CommonUtil

import kafka.serializer.StringDecoder

object EventSessionization extends Application with Serializable {

    def main(brokerList: String, topic: String, output: Option[String], outputDir: Option[String]) {

        val ssc = CommonUtil.getSparkStreamingContext("EventSessionization", Seconds(10));
        val allQuestions = ssc.sparkContext.textFile("src/main/resources/questions.csv", 1).map { x =>
            {
                val arr = x.split(",");
                (arr(0), (arr(1), arr(2), arr(3)));
            }
        }.collect().toMap;
        val questionsMap = ssc.sparkContext.broadcast(allQuestions);
        val resultOutput = output.getOrElse("console");

        ssc.checkpoint("./checkpoint");
        Console.println("## Started spark streaming context ##");
        val kafkaParams = Map[String, String]("metadata.broker.list" -> brokerList);
        val messages = KafkaUtils.createDirectStream[String, Event, StringDecoder, EventDecoder](ssc, kafkaParams, Set(topic));
        Console.println("## Started spark kafka consumer ##");

        val events = messages.map[(String, Buffer[Event])](e => (e._2.sid.get, Buffer(e._2)));
        val latestSessionEvents = events.reduceByKey((a, b) => a ++ b).updateStateByKey(updatePreviousSessions);
        val completedSessions = latestSessionEvents.filter(f => f._2._2);

        completedSessions.foreachRDD(rdd => {
            rdd.collect().foreach(f => LitScreenerLevelComputation.compute(f._2._1, questionsMap, resultOutput, outputDir, brokerList));
        });

        ssc.start();
        ssc.awaitTermination();
    }

    def updatePreviousSessions(values: Seq[Buffer[Event]], state: Option[(Buffer[Event], Boolean)]): Option[(Buffer[Event], Boolean)] = {

        val currState = state.getOrElse((Buffer[Event](), false));
        if (currState._2) {
            None;
        } else {
            var prevEvents = currState._1;
            //Console.println("Current Values - " + values.size + " | State size - " + currState.size);
            values.foreach { x =>
                {
                    //Console.println(" x size - " + x.size);
                    prevEvents ++= x;
                }
            };
            if (prevEvents.last.eid.equals("GE_SESSION_END")) {
                Some(prevEvents, true);
            } else {
                Some(prevEvents, false);
            }
        }
    }

}