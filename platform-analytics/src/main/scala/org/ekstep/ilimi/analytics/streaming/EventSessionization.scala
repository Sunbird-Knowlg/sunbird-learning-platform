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

object EventSessionization extends Application with Serializable {

    def main(brokerList: String, topic: String) {

        val litScreenerId = "org.eks.lit_screener";
        val ssc = CommonUtil.getSparkStreamingContext("EventSessionization", Seconds(10));
        val allQuestions = ssc.sparkContext.textFile("src/main/resources/questions.csv", 1).map { x =>
            {
                val arr = x.split(",");
                (arr(0), (arr(1), arr(2), arr(3)));
            }
        }.collect().toMap;

        ssc.checkpoint("/Users/santhosh/ekStep/spark-checkpoint");
        Console.println("## Started spark streaming context ##");
        val kafkaParams = Map[String, String]("metadata.broker.list" -> brokerList);
        val messages = KafkaUtils.createDirectStream[String, Event, StringDecoder, EventDecoder](ssc, kafkaParams, Set(topic));
        Console.println("## Started spark kafka consumer ##");

        val events = messages.map[(String, Buffer[Event])](e => (e._2.sid.get, Buffer(e._2)));
        val latestSessionEvents = events.reduceByKey((a, b) => a ++ b).updateStateByKey(updatePreviousSessions);
        val completedSessions = latestSessionEvents.filter(f => f._2._2);

        completedSessions.foreachRDD(rdd => {

            rdd.collect().foreach(f => {
                val ge = f._2._1;
                val oeAssesEvents = ge.filter { x => (x.eid.equals("OE_ASSESS") && x.gdata.id.equals(litScreenerId)) }.map { x =>
                    {
                        (x.edata.eks.qid.get, if (x.edata.eks.pass.get == "Yes") 1 else 0, x.uid.get, allQuestions(x.edata.eks.qid.get));
                    }
                };
                val uid = oeAssesEvents.last._3;
                val ltCodes = oeAssesEvents.groupBy(f => f._4._1).mapValues(f => f.map(f => f._2)).mapValues { x => x.reduce(_ + _) }.toMap;
                val loCodes = oeAssesEvents.groupBy(f => f._4._2).mapValues(f => f.map(f => f._2)).mapValues { x => x.reduce(_ + _) }.toMap;
                val ldCodes = oeAssesEvents.groupBy(f => f._4._3).mapValues(f => f.map(f => f._2)).mapValues { x => x.reduce(_ + _) }.toMap;
                val ltLevels = ltCodes.map(f => { (uid, f._1, f._2, getLTLevel(f._1, f._2)) });
                val loLevels = loCodes.map(f => { (uid, f._1, f._2, getLOLevel(f._1, f._2)) });
                val ldLevels = ldCodes.map(f => { (uid, f._1, f._2, getLDLevel(f._1, f._2)) });
                val ldMap = ldCodes.toMap;
                val composite1 = (uid, "Composite 1", ldMap("LD1") + ldMap("LD2"), getCompositeLevel("Composite 1", ldMap("LD1") + ldMap("LD2")));
                val composite2 = (uid, "Composite 2", ldMap("LD3") + ldMap("LD4"), getCompositeLevel("Composite 2", ldMap("LD3") + ldMap("LD3")));
                val composite3 = (uid, "Composite 3", ldMap("LD1") + ldMap("LD2") + ldMap("LD3") + ldMap("LD4"), getCompositeLevel("Composite 3", ldMap("LD1") + ldMap("LD2") + ldMap("LD3") + ldMap("LD4")));
                ltLevels.foreach(f => Console.println(f));
                loLevels.foreach(f => Console.println(f));
                ldLevels.foreach(f => Console.println(f));
                Console.println(composite1);
                Console.println(composite2);
                Console.println(composite3);
            });
        });

        ssc.start();
        ssc.awaitTermination();
    }

    def getLTLevel(code: String, score: Int): String = {
        code match {
            case "LT1" =>
                score match {
                    case d if (d > 5) && (d <= 6)   => "Primary 1";
                    case d if (d > 6) && (d <= 10)  => "Primary 2";
                    case d if (d > 10) && (d <= 14) => "Primary 3";
                    case d if (d > 14) && (d <= 18) => "Primary 4";
                    case d if (d > 18) && (d <= 20) => "Primary 5";
                    case _                          => "";
                }
            case "LT2" =>
                score match {
                    case d if (d > 35) && (d <= 40) => "Primary 2";
                    case d if (d > 40) && (d <= 50) => "Primary 3";
                    case d if (d > 50) && (d <= 60) => "Primary 4";
                    case d if (d > 60) && (d <= 70) => "Primary 5";
                    case _                          => "";
                }
            case "LT3" =>
                score match {
                    case 2 => "Primary 1";
                    case 3 => "Primary 2";
                    case 4 => "Primary 3";
                    case 5 => "Primary 4";
                    case _ => "";
                }
            case "LT4" =>
                score match {
                    case d if (d > 10) && (d <= 12) => "Primary 1";
                    case d if (d > 12) && (d <= 14) => "Primary 2";
                    case d if (d > 14) && (d <= 16) => "Primary 3";
                    case d if (d > 16) && (d <= 18) => "Primary 4";
                    case d if (d > 18) && (d <= 20) => "Primary 5";
                    case _                          => "";
                }
            case "LT5" =>
                score match {
                    case d if (d > 20) && (d <= 22) => "Primary 1";
                    case d if (d > 22) && (d <= 35) => "Primary 2";
                    case d if (d > 35) && (d <= 50) => "Primary 3";
                    case d if (d > 50) && (d <= 70) => "Primary 4";
                    case d if (d > 70) && (d <= 80) => "Primary 5";
                    case _                          => "";
                }
            case "LT6" =>
                score match {
                    case d if (d > 10) && (d <= 14) => "Primary 2";
                    case d if (d > 14) && (d <= 16) => "Primary 3";
                    case d if (d > 16) && (d <= 18) => "Primary 4";
                    case d if (d > 18) && (d <= 20) => "Primary 5";
                    case _                          => "";
                }
            case "LT7" =>
                score match {
                    case d if (d > 40) && (d <= 46) => "Primary 2";
                    case d if (d > 46) && (d <= 64) => "Primary 3";
                    case d if (d > 64) && (d <= 70) => "Primary 4";
                    case d if (d > 70) && (d <= 72) => "Primary 5";
                    case _                          => "";
                }
            case "LT8" =>
                score match {
                    case d if (d > 3) && (d <= 4)  => "Primary 2";
                    case d if (d > 4) && (d <= 7)  => "Primary 3";
                    case d if (d > 7) && (d <= 8)  => "Primary 4";
                    case d if (d > 8) && (d <= 10) => "Primary 5";
                    case _                         => "";
                }
            case "LT9" =>
                score match {
                    case 2 => "Primary 2";
                    case 3 => "Primary 3";
                    case 4 => "Primary 4";
                    case 5 => "Primary 5";
                    case _ => "";
                }
            case "LT10" =>
                score match {
                    case 2 => "Primary 1";
                    case 3 => "Primary 2";
                    case 4 => "Primary 3";
                    case 5 => "Primary 4";
                    case _ => "";
                }
            case "LT11" =>
                score match {
                    case 2 => "Primary 1";
                    case 3 => "Primary 2";
                    case 4 => "Primary 3";
                    case 5 => "Primary 4";
                    case _ => "";
                }
            case "LT12" =>
                score match {
                    case 3                         => "Primary 2";
                    case 4                         => "Primary 3";
                    case 5                         => "Primary 4";
                    case d if (d > 6) && (d <= 11) => "Primary 5";
                    case _                         => "";
                }
            case "LT13" =>
                score match {
                    case 3                         => "Primary 2";
                    case 4                         => "Primary 3";
                    case d if (d > 4) && (d <= 6)  => "Primary 4";
                    case d if (d > 6) && (d <= 11) => "Primary 5";
                    case _                         => "";
                }
            case _ =>
                score match {
                    case d if (d > 1) && (d <= 2)  => "Primary 1";
                    case d if (d > 2) && (d <= 3)  => "Primary 2";
                    case d if (d > 3) && (d <= 4)  => "Primary 3";
                    case d if (d > 4) && (d <= 6)  => "Primary 4";
                    case d if (d > 6) && (d <= 11) => "Primary 5";
                    case _                         => "";
                }
        }
    }

    def getLOLevel(code: String, score: Int): String = {
        code match {
            case "LO1" =>
                score match {
                    case 6                          => "Primary 1";
                    case d if (d > 6) && (d <= 10)  => "Primary 2";
                    case d if (d > 10) && (d <= 14) => "Primary 3";
                    case d if (d > 14) && (d <= 18) => "Primary 4";
                    case d if (d > 18) && (d <= 20) => "Primary 5";
                    case _                          => "";
                }
            case "LO2" =>
                score match {
                    case d if (d > 35) && (d <= 40) => "Primary 2";
                    case d if (d > 40) && (d <= 50) => "Primary 3";
                    case d if (d > 50) && (d <= 60) => "Primary 4";
                    case d if (d > 60) && (d <= 70) => "Primary 5";
                    case _                          => "";
                }
            case "LO3" =>
                score match {
                    case 2 => "Primary 1";
                    case 3 => "Primary 2";
                    case 4 => "Primary 3";
                    case 5 => "Primary 4";
                    case _ => "";
                }
            case "LO4" =>
                score match {
                    case d if (d > 10) && (d <= 12) => "Primary 1";
                    case d if (d > 12) && (d <= 14) => "Primary 2";
                    case d if (d > 14) && (d <= 16) => "Primary 3";
                    case d if (d > 16) && (d <= 18) => "Primary 4";
                    case d if (d > 18) && (d <= 20) => "Primary 5";
                    case _                          => "";
                }
            case "LO5" =>
                score match {
                    case d if (d > 20) && (d <= 22) => "Primary 1";
                    case d if (d > 22) && (d <= 35) => "Primary 2";
                    case d if (d > 35) && (d <= 50) => "Primary 3";
                    case d if (d > 50) && (d <= 70) => "Primary 4";
                    case d if (d > 70) && (d <= 80) => "Primary 5";
                    case _                          => "";
                }
            case "LO6" =>
                score match {
                    case d if (d > 10) && (d <= 14) => "Primary 2";
                    case d if (d > 14) && (d <= 16) => "Primary 3";
                    case d if (d > 16) && (d <= 18) => "Primary 4";
                    case d if (d > 18) && (d <= 20) => "Primary 5";
                    case _                          => "";
                }
            case "LO7" =>
                score match {
                    case d if (d > 44) && (d <= 50) => "Primary 2";
                    case d if (d > 50) && (d <= 71) => "Primary 3";
                    case d if (d > 71) && (d <= 78) => "Primary 4";
                    case d if (d > 78) && (d <= 82) => "Primary 5";
                    case _                          => "";
                }
            case "LO8" =>
                score match {
                    case 2 => "Primary 2";
                    case 3 => "Primary 3";
                    case 4 => "Primary 4";
                    case 5 => "Primary 5";
                    case _ => "";
                }
            case "LO9" =>
                score match {
                    case d if (d > 4) && (d <= 6)   => "Primary 1";
                    case d if (d > 6) && (d <= 9)   => "Primary 2";
                    case d if (d > 9) && (d <= 12)  => "Primary 3";
                    case d if (d > 12) && (d <= 15) => "Primary 4";
                    case d if (d > 15) && (d <= 18) => "Primary 5";
                    case _                          => "";
                }
            case "LO10" =>
                score match {
                    case 3                         => "Primary 1";
                    case 4                         => "Primary 2";
                    case d if (d > 4) && (d <= 6)  => "Primary 3";
                    case d if (d > 6) && (d <= 11) => "Primary 4";
                    case _                         => "";
                }
            case _ =>
                score match {
                    case d if (d > 1) && (d <= 2)  => "Primary 1";
                    case d if (d > 2) && (d <= 3)  => "Primary 2";
                    case d if (d > 3) && (d <= 4)  => "Primary 3";
                    case d if (d > 4) && (d <= 6)  => "Primary 4";
                    case d if (d > 6) && (d <= 11) => "Primary 5";
                    case _                         => "";
                }
        }
    }

    def getLDLevel(code: String, score: Int): String = {
        code match {
            case "LD1" =>
                score match {
                    case 41                         => "Primary 1";
                    case d if (d > 41) && (d <= 50) => "Primary 2";
                    case d if (d > 50) && (d <= 64) => "Primary 3";
                    case d if (d > 64) && (d <= 78) => "Primary 4";
                    case d if (d > 78) && (d <= 90) => "Primary 5";
                    case _                          => "";
                }
            case "LD2" =>
                score match {
                    case d if (d > 11) && (d <= 14) => "Primary 1";
                    case d if (d > 14) && (d <= 17) => "Primary 2";
                    case d if (d > 17) && (d <= 20) => "Primary 3";
                    case d if (d > 20) && (d <= 23) => "Primary 4";
                    case d if (d > 23) && (d <= 25) => "Primary 5";
                    case _                          => "";
                }
            case "LD3" =>
                score match {
                    case d if (d > 20) && (d <= 22) => "Primary 1";
                    case d if (d > 22) && (d <= 35) => "Primary 2";
                    case d if (d > 35) && (d <= 50) => "Primary 3";
                    case d if (d > 50) && (d <= 70) => "Primary 4";
                    case d if (d > 70) && (d <= 80) => "Primary 5";
                    case _                          => "";
                }
            case "LD4" =>
                score match {
                    case d if (d > 54) && (d <= 66)   => "Primary 2";
                    case d if (d > 66) && (d <= 90)   => "Primary 3";
                    case d if (d > 90) && (d <= 100)  => "Primary 4";
                    case d if (d > 100) && (d <= 107) => "Primary 5";
                    case _                            => "";
                }
            case "LD5" =>
                score match {
                    case d if (d > 6) && (d <= 8)   => "Primary 1";
                    case d if (d > 8) && (d <= 12)  => "Primary 2";
                    case d if (d > 12) && (d <= 16) => "Primary 3";
                    case d if (d > 16) && (d <= 21) => "Primary 4";
                    case d if (d > 21) && (d <= 29) => "Primary 5";
                    case _                          => "";
                }
            case _ =>
                score match {
                    case d if (d > 54) && (d <= 66)   => "Primary 2";
                    case d if (d > 66) && (d <= 90)   => "Primary 3";
                    case d if (d > 90) && (d <= 100)  => "Primary 4";
                    case d if (d > 100) && (d <= 107) => "Primary 5";
                    case _                            => "";
                }
        }
    }

    def getCompositeLevel(code: String, score: Int): String = {
        code match {
            case "Composite 1" =>
                score match {
                    case d if (d > 51) && (d <= 55)   => "Primary 1";
                    case d if (d > 55) && (d <= 67)   => "Primary 2";
                    case d if (d > 67) && (d <= 84)   => "Primary 3";
                    case d if (d > 84) && (d <= 101)  => "Primary 4";
                    case d if (d > 101) && (d <= 115) => "Primary 5";
                    case _                            => "";
                }
            case "Composite 2" =>
                score match {
                    case d if (d > 74) && (d <= 76)   => "Primary 1";
                    case d if (d > 76) && (d <= 101)  => "Primary 2";
                    case d if (d > 101) && (d <= 140) => "Primary 3";
                    case d if (d > 140) && (d <= 170) => "Primary 4";
                    case d if (d > 170) && (d <= 187) => "Primary 5";
                    case _                            => "";
                }
            case "Composite 3" =>
                score match {
                    case d if (d > 131) && (d <= 139) => "Primary 1";
                    case d if (d > 139) && (d <= 180) => "Primary 2";
                    case d if (d > 180) && (d <= 240) => "Primary 3";
                    case d if (d > 240) && (d <= 292) => "Primary 4";
                    case d if (d > 292) && (d <= 331) => "Primary 5";
                    case _                            => "";
                }
        }
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