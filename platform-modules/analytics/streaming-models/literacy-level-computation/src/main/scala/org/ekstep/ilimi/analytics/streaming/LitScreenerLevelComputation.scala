package org.ekstep.ilimi.analytics.streaming

import scala.collection.mutable.Buffer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.spark.broadcast.Broadcast
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.util.CommonUtil
import java.io.File
import org.ekstep.ilimi.analytics.util.S3Util
import org.ekstep.ilimi.analytics.model.EventOutput
import org.ekstep.ilimi.analytics.model.Pdata
import org.ekstep.ilimi.analytics.model.Gdata
import java.io.FileWriter
import org.ekstep.ilimi.analytics.model.EventData
import java.util.Date
import org.ekstep.ilimi.analytics.conf.AppConf
import org.joda.time.DateTime

case class LevelAgg(code: String, min: Int, max: Int, level: String);

object LitScreenerLevelComputation extends Serializable {

    def main(cmdline: Array[String]) {
        val qid = "EK.L.KAN.LT1.Q1";
        val qids = qid.split('.');
        Console.println("qids.length - " + qids.length + " | qids - " + qids.mkString("|"));
    }

    private val litScreenerIds = AppConf.getConfig("literacy_screener_games").split(',');
    private val screenerVersion = "1.30";

    def compute(events: Buffer[Event], loltMapping: Broadcast[Map[String, Array[(String, String)]]], ldloMapping: Broadcast[Map[String, Array[(String, String)]]], compldMapping: Broadcast[Map[String, Array[(String, String)]]], litLevelsMap: Broadcast[Map[String, Array[LevelAgg]]]): (String, String, Buffer[(String, String, Int, Int, String, String)]) = {

        var result = Buffer[(String, String, Int, Int, String, String)]();
        val loltMap = loltMapping.value;
        val ldloMap = ldloMapping.value;
        val compldMap = compldMapping.value;
        val levelMap = litLevelsMap.value;
        val oeAssesEvents = events.filter { x => (x.eid.get.equals("OE_ASSESS") && litScreenerIds.contains(CommonUtil.getGameId(x))) }.map { x =>
            {
                val qids = x.edata.eks.qid.get.split('.');
                (x.edata.eks.qid.get, x.uid.get, if ("Yes".equalsIgnoreCase(x.edata.eks.pass.get)) 1 else 0, qids(3), x.edata.eks.maxscore.getOrElse(1), CommonUtil.getGameId(x));
            }
        };
        val distinctEvents = oeAssesEvents.distinct;
        Console.println("Before Count - " + oeAssesEvents.size + " | After count - " + distinctEvents.size);
        if (distinctEvents.size == 0) {
            return (null, null, result);
        }
        val uid = distinctEvents.last._2;
        val gid = distinctEvents.last._6;

        val ltScores = distinctEvents.groupBy(f => f._4).mapValues(f => f.map(f => (f._3, f._5))).mapValues(x => {
            x.reduce((a, b) => (a._1 + b._1, a._2 + b._2))
        }).toMap;
        val loScores = getCodeMap(loltMap, ltScores);
        val ldScores = getCodeMap(ldloMap, loScores);
        val compositeScores = getCodeMap(compldMap, ldScores);
        val ltLevels = ltScores.map(f => { (uid, f._1, f._2._1, f._2._2, getLevel(f._1, f._2._1, levelMap), "LT") });
        val loLevels = loScores.map(f => { (uid, f._1, f._2._1, f._2._2, getLevel(f._1, f._2._1, levelMap), "LO") });
        val ldLevels = ldScores.map(f => { (uid, f._1, f._2._1, f._2._2, getLevel(f._1, f._2._1, levelMap), "LD") });
        val compositeLevels = compositeScores.map(f => { (uid, f._1, f._2._1, f._2._2, getLevel(f._1, f._2._1, levelMap), "COMPOSITE") });

        ltLevels.foreach(f => result += f);
        loLevels.foreach(f => result += f);
        ldLevels.foreach(f => result += f);
        compositeLevels.foreach(f => result += f);

        (uid, gid, result);
    }

    def getEventOutput(event: (String, String, Int, Int, String, String), gid: String): EventOutput = {
        val edata = Map[String, AnyRef]("score" -> event._3.asInstanceOf[AnyRef], "maxscore" -> event._4.asInstanceOf[AnyRef], "current_level" -> event._5);
        EventOutput("ME_USER_GAME_LEVEL", CommonUtil.formatEventDate(new DateTime), "1.0", Some(event._1), Some(event._2), Some(event._6), Some(Gdata(Option(gid), Option(screenerVersion))),
            Some(Pdata("AssessmentPipeline", "LitScreenerLevelComputation", "1.0")), EventData(edata));
    }

    def getCodeMap(codeMap: Map[String, Array[(String, String)]], valueMap: Map[String, (Int, Int)]): Map[String, (Int, Int)] = {
        codeMap.mapValues(f => { f.map(f => valueMap.getOrElse(f._2, (0, 0))) }).mapValues { x =>
            {
                x.reduce((a, b) => (a._1 + b._1, a._2 + b._2))
            }
        }.toMap;
    }

    def mergeMapsByKey(map1: Map[String, Int], map2: Map[String, Int]): Map[String, (Int, Int)] = {
        map1.map(f => {
            (f._1, (f._2, map2.getOrElse(f._1, f._2)))
        })
    }

    def getLevel(code: String, score: Int, levelMap: Map[String, Array[LevelAgg]]): String = {

        var level = "NL";
        val arr = levelMap.get(code).getOrElse(null);
        if (arr != null) {
            arr.foreach { levelAgg =>
                {
                    if (score >= levelAgg.min && score <= levelAgg.max) {
                        level = levelAgg.level;
                    }
                }
            }
        }
        level;
    }

    def sendOutput(uid: String, gid: String, result: Buffer[(String, String, Int, Int, String, String)], output: String, outputFile: Option[String], kafkaTopic: Option[String], brokerList: String) = {

        val currDate = new Date();
        output match {
            case "console" =>
                Console.println("## Printing output to console ##");
                var resultEvents = Buffer[EventOutput]();
                result.foreach(f => resultEvents += getEventOutput(f, gid));
                resultEvents.foreach { x => Console.println(CommonUtil.jsonToString(x)) };
            //result.foreach { x => Console.println(x) };
            case "csv-file" =>
                Console.println("## Printing output to csv ##");
                val filePath = outputFile.getOrElse(CommonUtil.getTempPath(currDate) + "/" + uid + ".csv");
                val fw = new FileWriter(filePath, true);
                result.foreach { f => fw.write(f._1 + "," + f._2 + "," + f._3 + "," + f._4 + "\n"); }
                fw.close();
            case "csv-s3" =>
                Console.println("## Printing output to csv and uploading to S3 ##");
                val filePath = CommonUtil.getTempPath(currDate) + "/" + uid + ".csv";
                CommonUtil.printToFile(new File(filePath)) { p =>
                    result.foreach(f => {
                        p.println(f._1 + "," + f._2 + "," + f._3 + "," + f._4);
                    })
                }
                S3Util.uploadPublic("lit-screener-level-compute", filePath, uid + "_" + System.currentTimeMillis() + ".csv");
                CommonUtil.deleteFile(filePath);
            case "kafka" =>
                Console.println("## Printing output to kafka topic ##");
                var resultEvents = Buffer[EventOutput]();
                result.foreach(f => resultEvents += getEventOutput(f, gid));
                KafkaEventProducer.sendEvents(resultEvents, kafkaTopic.getOrElse("user_aggregates"), brokerList);
            case _ =>
                Console.println("## No output handler found for " + output + " ##");
        }
    }

}