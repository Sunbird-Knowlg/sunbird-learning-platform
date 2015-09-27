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
import org.apache.spark.SparkContext
import org.apache.spark.Accumulator

object TelemetryDataMigrationModel extends Serializable {

    def main(args: Array[String]): Unit = {
        val arr = getInput("s3://ep-production-backup/logs/telemetry");
        arr.foreach { x => Console.println(x); };
    }

    def datesBetween(from: LocalDate, to: LocalDate): IndexedSeq[LocalDate] = {
        val numberOfDays = Days.daysBetween(from, to).getDays()
        for (f <- 0 to numberOfDays) yield from.plusDays(f)
    }

    def getDeltaInput(input: String, delta: Int): Array[String] = {
        val to = LocalDate.fromDateFields(new Date);
        val cal = Calendar.getInstance;
        cal.add(Calendar.DAY_OF_MONTH, -delta);
        val from = LocalDate.fromCalendarFields(cal);
        val dates = datesBetween(from, to);
        dates.map { x =>  
            val suffix = "-" + x.toString();
            CommonUtil.getInputPaths(input, suffix);
        }.filterNot { x => x.isEmpty() }.mkString(",").split(',');
    }
    
    def getInput(input: String) : Array[String] = {
        CommonUtil.getInputPaths(input).split(',');
    }

    def compute(input: String, output: Option[String], outputDir: String, parallelization: Int, duration: String, delta: Int) {

        val path = duration match {
            case "delta" =>
                getDeltaInput(input, delta);
            case _ =>
                getInput(input);
        }

        if (null == path || path.length == 0) {
            Console.println("## No input path found. Nothing to process ##");
            return ;
        }

        Console.println("## Input Path - " + path);

        @transient val sc = CommonUtil.getSparkContext(parallelization, "TelemetryDataMigrator");
        val accum = sc.accumulator(0, "Total Event Count Accumulator");
        val fileAccum = sc.accumulator(0, "Event Count Accumulator");
        path.foreach { x => outputEventPerLine(x, sc, parallelization, outputDir, accum, fileAccum) }
        Console.println("## Total Events Size - " + accum.value + " ##");
        CommonUtil.closeSparkContext(sc);
    }
    
    def outputEventPerLine(path: String, sc: SparkContext, parallelization: Int, outputDir: String, accum: Accumulator[Int], fileAccum: Accumulator[Int]) {
        
        fileAccum.setValue(0);
        val rdd = sc.textFile(path, parallelization).cache();
        val filePath = outputDir + "/" + path.split("/").last.replaceAll(".gz", ".log");
        Console.println("## Input Path - " + path + " ##");
        Console.println("## Log Events Count - " + rdd.count() + " ##");
        
        val events = rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                try {
                    parse(line).extract[SyncEvent]   
                } catch {
                    case e:Exception =>
                        Console.println("Error parsing JSON", e.getClass.getName);
                        e.printStackTrace();
                        null
                }
            }
        }.filter(_ != null).map { x => x.data.getOrElse(LineData(None, None, None, Array())).events }.collect();
        
        events.foreach { x =>
            {
                accum += x.distinct.size;
                fileAccum += x.distinct.size;
                val fw = new FileWriter(filePath, true);
                x.distinct.foreach { x => fw.write(CommonUtil.jsonToString(x) + "\n"); }
                fw.close();
            }
        };
        Console.println("## Events Size - " + fileAccum.value + " ##");
        Console.println("## Output Path - " + filePath + " ##");
        Console.println();
    }

}