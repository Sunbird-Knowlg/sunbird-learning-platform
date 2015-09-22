package org.ekstep.ilimi.analytics

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
import org.apache.spark.SparkContext
import org.ekstep.ilimi.analytics.streaming.EventDecoder
import org.ekstep.ilimi.analytics.streaming.LevelAgg
import org.ekstep.ilimi.analytics.streaming.LitScreenerLevelComputation
import org.ekstep.ilimi.analytics.conf.AppConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Duration

object EventSessionization extends Application with Serializable {
    
    def getSparkStreamingContext(appName: String, duration: Duration): StreamingContext = {
        val conf = new SparkConf().setAppName(appName);
        val master = conf.getOption("spark.master");
        if (master.isEmpty) {
            Console.println("### Master not found. Setting it to local[*] ###");
            conf.setMaster("local[*]");
        }
        new StreamingContext(conf, duration);
    }

    def main(brokerList: String, topic: String, output: Option[String], of: Option[String], kt: Option[String]) {

        val validEvents = Array("GE_SESSION_START", "GE_SESSION_END", "OE_ASSESS", "OE_INTERACT");
        val ssc = getSparkStreamingContext("EventSessionization", Seconds(10));

        val loltMapping = broadcastMapping(AppConf.getConfig("mapping_file_location") + "/lo_lt_mapping.csv", ssc.sparkContext);
        val ldloMapping = broadcastMapping(AppConf.getConfig("mapping_file_location") + "/ld_lo_mapping.csv", ssc.sparkContext);
        val compldMapping = broadcastMapping(AppConf.getConfig("mapping_file_location") + "/composite_ld_mapping.csv", ssc.sparkContext);
        val litLevelsMap = broadcastLevelRanges(AppConf.getConfig("mapping_file_location") + "/lit_scr_level_ranges.csv", ssc.sparkContext);

        val resultOutput = output.getOrElse("console");

        ssc.checkpoint("./checkpoint");
        Console.println("## Started spark streaming context ##");
        val kafkaParams = Map[String, String]("metadata.broker.list" -> brokerList);
        val messages = KafkaUtils.createDirectStream[String, Event, StringDecoder, EventDecoder](ssc, kafkaParams, Set(topic));
        Console.println("## Started spark kafka consumer ##");
        val events = messages.filter(x => validEvents.contains(x._2.eid.getOrElse(""))).map[(String, Buffer[Event])](e => (e._2.sid.get, Buffer(e._2)));
        val latestSessionEvents = events.reduceByKey((a, b) => a ++ b).updateStateByKey(updatePreviousSessions);
        val completedSessions = latestSessionEvents.filter(f => f._2._2);

        completedSessions.foreachRDD(rdd => {
            rdd.collect().foreach(f => {
                val res = LitScreenerLevelComputation.compute(f._2._1, loltMapping, ldloMapping, compldMapping, litLevelsMap);
                LitScreenerLevelComputation.sendOutput(res._1, res._2, res._3, resultOutput, of, kt, brokerList)
            });
        });

        ssc.start();
        ssc.awaitTermination();
    }

    def broadcastMapping(file: String, sc: SparkContext): Broadcast[Map[String, Array[(String, String)]]] = {
        val config = sc.textFile(file, 1).map { x =>
            {
                val arr = x.split(",");
                (arr(0), arr(1));
            }
        }.collect().groupBy { x => x._1 }.toMap;
        sc.broadcast(config);
    }

    def reverseBroadcastMapping(file: String, sc: SparkContext): Broadcast[Map[String, String]] = {
        val config = sc.textFile(file, 1).map { x =>
            {
                val arr = x.split(",");
                (arr(1), arr(0));
            }
        }.distinct().collect().toMap;
        sc.broadcast(config);
    }

    def broadcastLevelRanges(file: String, sc: SparkContext): Broadcast[Map[String, Array[LevelAgg]]] = {
        val config = sc.textFile(file, 1).map { x =>
            {
                val arr = x.split(",");
                LevelAgg(arr(0), arr(1).toInt, arr(2).toInt, arr(3));
            }
        }.collect().groupBy { x => x.code }.toMap;
        sc.broadcast(config);
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
            if (CommonUtil.getEventId(prevEvents.last).equals("GE_SESSION_END")) {
                Some(prevEvents, true);
            } else {
                Some(prevEvents, false);
            }
        }
    }

}