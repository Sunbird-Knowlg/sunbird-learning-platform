package org.ekstep.ilimi.analytics.streaming

import scala.collection.mutable.Buffer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.spark.broadcast.Broadcast
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.util.CommonUtil
import java.io.File

case class Pdata(id: String, mod: String, ver: String)
case class Gdata(id: String, ver: String)
case class EventOutput(eid: String, ts: Long, ver: String, uid: Option[String], cid: Option[String], gdata: Option[Gdata], pdata: Option[Pdata], edata: Map[String, AnyRef])

object LitScreenerLevelComputation extends Serializable {

    def compute(events: Buffer[Event], questionsMap: Broadcast[Map[String, (String, String, String)]], output: String, outputDir: Option[String], brokerList: String) {

        val litScreenerId = "org.eks.lit_screener";
        val qmap = questionsMap.value;
        val oeAssesEvents = events.filter { x => (x.eid.equals("OE_ASSESS") && x.gdata.id.equals(litScreenerId)) }.map { x =>
            {
                (x.edata.eks.qid.get, if (x.edata.eks.pass.get == "Yes") 1 else 0, x.uid.get, qmap(x.edata.eks.qid.get));
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
        var result = Buffer[(String, String, Int, String)]();
        ltLevels.foreach(f => result += f);
        loLevels.foreach(f => result += f);
        ldLevels.foreach(f => result += f);
        result += composite1;
        result += composite2;
        result += composite3;

        output match {
            case "console" =>
                Console.println("## Printing output to console ##");
                var resultEvents = Buffer[EventOutput]();
                result.foreach(f => resultEvents += getEventOutput(f));
                resultEvents.foreach { x => Console.println(CommonUtil.jsonToString(x)) };
            case "csv" =>
                Console.println("## Printing output to csv ##");
                CommonUtil.printToFile(new File(outputDir.getOrElse("user-aggregates") + "/lit_scr_levels_" + uid + ".csv")) { p =>
                    result.foreach(f => {
                        p.println(f._1+","+f._2+","+f._3+","+f._4);
                    })
                }
            case "kafka" =>
                Console.println("## Printing output to kafka topic ##");
                var resultEvents = Buffer[EventOutput]();
                result.foreach(f => resultEvents += getEventOutput(f));
                KafkaEventProducer.sendEvents(resultEvents, outputDir.getOrElse("user_aggregates"), brokerList);
        }
    }

    def getEventOutput(event: (String, String, Int, String)): EventOutput = {
        val edata = Map[String, AnyRef]("score" -> event._3.asInstanceOf[AnyRef], "current_level" -> event._4);
        EventOutput("ME_USER_GAME_LEVEL", System.currentTimeMillis(), "1.0", Some(event._1), Some(event._2), Some(Gdata("org.eks.lit_screener", "1.0")),
            Some(Pdata("AssessmentPipeline", "LitScreenerLevelComputation", "1.0")), edata);
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

}