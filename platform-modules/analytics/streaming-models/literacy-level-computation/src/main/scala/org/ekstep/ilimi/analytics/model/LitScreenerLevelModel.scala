package org.ekstep.ilimi.analytics.model

import scala.collection.mutable.Buffer
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.parse
import org.ekstep.ilimi.analytics.util.CommonUtil
import org.ekstep.ilimi.analytics.EventSessionization
import org.ekstep.ilimi.analytics.streaming.LitScreenerLevelComputation
import org.ekstep.ilimi.analytics.dao.LitScreenerLevelDAO
import scala.collection.mutable.ListBuffer
import java.io.FileWriter
import java.util.Date
import org.ekstep.ilimi.analytics.util.S3Util
import java.io.File
import org.ekstep.ilimi.analytics.conf.AppConf

case class Data(events: Array[Event]);
case class Events(data: Data);

object LitScreenerLevelModel extends Serializable {

    private val litScreenerIds = AppConf.getConfig("literacy_screener_games").split(',');

    def compute(input: String, output: Option[String], outputDir: Option[String], location: String, parallelization: Int) {

        val validEvents = Array("OE_ASSESS", "OE_INTERACT");
        @transient val sc = CommonUtil.getSparkContext(parallelization, "GameEffectiveness");
        val loltMapping = EventSessionization.broadcastMapping("src/main/resources/lo_lt_mapping.csv", sc);
        val ltloMapping = EventSessionization.reverseBroadcastMapping("src/main/resources/lo_lt_mapping.csv", sc);
        val ldloMapping = EventSessionization.broadcastMapping("src/main/resources/ld_lo_mapping.csv", sc);
        val loldMapping = EventSessionization.reverseBroadcastMapping("src/main/resources/ld_lo_mapping.csv", sc);
        val compldMapping = EventSessionization.broadcastMapping("src/main/resources/composite_ld_mapping.csv", sc);
        val litLevelsMap = EventSessionization.broadcastLevelRanges("src/main/resources/lit_scr_level_ranges.csv", sc);
        val userMapping = LitScreenerLevelDAO.getUserMapping();

        var records = new ListBuffer[Array[String]];
        records += getHeader();

        val resultOutput = output.getOrElse("console");
        val rdd = sc.textFile(CommonUtil.getPath("s3_input_bucket", input, location), parallelization).distinct().cache();
        val events = rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                parse(line).extract[Events]
            }
        }.map { le => le.data.events }.reduce((a, b) => a ++ b);

        events.groupBy { event => CommonUtil.getUserId(event) }.foreach(f => {
            val events = f._2.toBuffer;
            val res = LitScreenerLevelComputation.compute(events, loltMapping, ldloMapping, compldMapping, litLevelsMap);
            val filterEvents = events.distinct.filter { x => (validEvents.contains(x.eid.get) && litScreenerIds.contains(CommonUtil.getGameId(x))) };
            val uid = userMapping.getOrElse(f._1, f._1);
            // LitScreenerLevelComputation.sendOutput(uid, res._2, res._3, resultOutput, null, null, "");
            // Write to CSV & upload to S3
            filterEvents.foreach { event =>
                var ltCode = "";
                var loCode = "";
                var ldCode = "";
                CommonUtil.getEventId(event) match {
                    case "OE_ASSESS" =>
                        ltCode = getString(event.edata.eks.qid.get.split('.')(3));
                        loCode = ltloMapping.value.getOrElse(ltCode, "").toString();
                        ldCode = loldMapping.value.getOrElse(loCode, "").toString();
                    case "OE_INTERACT" =>
                        ;
                }
                records += Array(
                    f._1,
                    getString(uid),
                    "",
                    getString(CommonUtil.getEventId(event)),
                    getString(CommonUtil.getGameId(event)),
                    getString(event.edata.eks.subj),
                    getString(ldCode),
                    getStringFromArray(event.edata.eks.mc),
                    getString(ltCode),
                    getString(event.edata.eks.qid),
                    getString(event.edata.eks.qtype),
                    getString(event.edata.eks.qlevel),
                    "",
                    getString(event.edata.eks.pass),
                    getStringFromArray(event.edata.eks.mmc),
                    getStringFromInt(event.edata.eks.score),
                    getStringFromInt(event.edata.eks.maxscore),
                    getStringFromArray(event.edata.eks.res),
                    getStringFromArray(event.edata.eks.exres),
                    getString(event.edata.eks.length),
                    getStringFromInt(event.edata.eks.atmpts),
                    getStringFromInt(event.edata.eks.failedatmpts),
                    getString(event.edata.eks.category),
                    getString(event.edata.eks.current),
                    getString(event.edata.eks.`type`),
                    getString(event.edata.eks.id),
                    getString(event.ts));
            }

            res._3.foreach(f => {
                records += Array(
                    f._1,
                    getString(uid),
                    "",
                    "LEVEL_SET",
                    getString("org.ekstep.lit.scrnr.kan.basic"),
                    getString("LIT"),
                    getString(""),
                    getString(f._2),
                    getString(""),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    getStringFromInt(Option(f._3)),
                    getStringFromInt(Option(f._4)),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    getString(Option(f._5)),
                    " ",
                    " ",
                    " ")
            })
        });
        sendOutput(records, output.getOrElse("csv-file"), outputDir);
    }

    def getHeader(): Array[String] = {
        Array(
            "Uid",
            "Child Genie id",
            "Location",
            "Event ID",
            "Game ID",
            "Subj",
            "Concept (Dimensions)",
            "Micro Concept (Learning Objective)",
            "Task Code",
            "qid",
            "qtype",
            "qlevel",
            "qtech",
            "pass",
            "mmc",
            "score",
            "maxscore",
            "res",
            "exres",
            "length",
            "atmpts",
            "failedatmpts",
            "category",
            "current",
            "type",
            "id",
            "ts");
    }

    def sendOutput(records: Buffer[Array[String]], output: String, outputDir: Option[String]) = {

        val currDate = new Date();
        output match {
            case "csv-file" =>
                Console.println("## Printing output to csv ##");
                val filePath = outputDir.getOrElse(CommonUtil.getTempPath(currDate)) + "/consolidated_output_" + System.currentTimeMillis() + ".csv";
                val fw = new FileWriter(filePath, true);
                records.foreach { f => fw.write(f.mkString(",") + "\n"); }
                fw.close();
            case "csv-s3" =>
                Console.println("## Printing output to csv and uploading to S3 ##");
                val filePath = CommonUtil.getTempPath(currDate) + "/consolidated_output_" + System.currentTimeMillis() + ".csv";
                CommonUtil.printToFile(new File(filePath)) { p =>
                    records.foreach(f => {
                        p.println(f.mkString(","));
                    })
                }
                S3Util.uploadPublic("lit-screener-level-compute", filePath, "consolidated_output_" + System.currentTimeMillis() + ".csv");
                CommonUtil.deleteFile(filePath);
            case _ =>
                Console.println("## No output handler found for " + output + " ##");
        }
    }

    def getString(str: String): String = {
        str.filter(_ >= ' ');
    }

    def getStringFromArray(str: Option[Array[String]]): String = {
        str.getOrElse(Array()).mkString(",").filter(_ >= ' ');
    }

    def getString(str: Option[String]): String = {
        str.getOrElse("").filter(_ >= ' ');
    }

    def getStringFromInt(i: Option[Int]): String = {
        if (i.isEmpty) {
            return "";
        }
        i.get.toString();
    }

    def getStringFromDouble(db: Option[Double]): String = {
        db.getOrElse(0).toString();
    }

}