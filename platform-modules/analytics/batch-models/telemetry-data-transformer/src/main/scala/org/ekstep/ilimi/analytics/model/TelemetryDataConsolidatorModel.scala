package org.ekstep.ilimi.analytics.model

import scala.collection.mutable.Buffer
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.parse
import org.ekstep.ilimi.analytics.util.CommonUtil
import java.io.File
import org.ekstep.ilimi.analytics.util.S3Util
import org.ekstep.ilimi.analytics.conf.AppConf
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import org.joda.time.Days
import org.joda.time.LocalDate
import scala.collection.immutable.IndexedSeq

object TelemetryDataConsolidatorModel extends Serializable {

    def main(args: Array[String]): Unit = {
        S3Util.getMetadata("ep-production-backup", "logs/telemetry");
    }

    def datesBetween(from: LocalDate, to: LocalDate): IndexedSeq[LocalDate] = {
        val numberOfDays = Days.daysBetween(from, to).getDays()
        for (f <- 0 to numberOfDays) yield from.plusDays(f)
    }

    def getDeltaInput(input: String, delta: Int): String = {
        val to = LocalDate.fromDateFields(new Date);
        val cal = Calendar.getInstance;
        cal.add(Calendar.DAY_OF_MONTH, -delta);
        val from = LocalDate.fromCalendarFields(cal);
        val dates = datesBetween(from, to);
        dates.map { x =>  
            val suffix = "-" + x.toString();
            CommonUtil.getInputPaths(input, suffix);
        }.filterNot { x => x.isEmpty() }.mkString(",")
    }

    def compute(input: String, output: Option[String], outputDir: String, parallelization: Int, duration: String, delta: Int) {

        val path = duration match {
            case "delta" =>
                getDeltaInput(input, delta);
            case _ =>
                CommonUtil.getInputPaths(input);
        }

        if (path.isEmpty()) {
            Console.println("## No input path found. Nothing to process ##");
            return ;
        }

        Console.println("## Input Path - " + path);

        @transient val sc = CommonUtil.getSparkContext(parallelization, "TelemetryDataConsolidator");
        val rdd = sc.textFile(path, parallelization).cache();
        val filePath = outputDir + "/" + output.getOrElse("telemetry-consolidated.json");
        val accum = sc.accumulator(0, "Event Count Accumulator")
        Console.println("## Log Events Count - " + rdd.count() + " ##");
        val events = rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                parse(line).extract[SyncEvent]
            }
        }.map { x => x.data.getOrElse(LineData(None, None, None, None, Array())).events }.collect();
        
        events.foreach { x =>
            {
                accum += x.distinct.size;
                val fw = new FileWriter(filePath, true);
                x.distinct.foreach { x => fw.write(CommonUtil.jsonToString(x) + "\n"); }
                fw.close();
            }
        };
        
        Console.println("## Events Size - " + accum.value + " ##");
        CommonUtil.closeSparkContext(sc);
    }

}