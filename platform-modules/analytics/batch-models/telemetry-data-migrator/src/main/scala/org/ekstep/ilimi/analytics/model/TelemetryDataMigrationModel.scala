package org.ekstep.ilimi.analytics.model

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.ekstep.ilimi.analytics.streaming.KafkaEventProducer

object TelemetryDataMigrationModel extends Serializable {

    def main(args: Array[String]): Unit = {
        val arr = getDeltaInput("s3://ep-production-backup/logs/telemetry/telemetry.log", 2);
        arr.foreach { x => Console.println(x); };
        /*
        @transient val sc = CommonUtil.getSparkContext(8, "TelemetryDataMigrator");
        val accum = sc.accumulator(0, "Total Event Count Accumulator");
        val fileAccum = sc.accumulator(0, "Event Count Accumulator");
        outputEventPerLine("s3n://ep-production-backup/logs/telemetry/telemetry.log-2015-06-30-1435645954.gz", sc, 8, "s3://ekstep-telemetry", accum, fileAccum)
        CommonUtil.closeSparkContext(sc);
        * 
        */
    }

    def getDeltaInput(input: String, delta: Int): Array[String] = {
        val to = LocalDate.fromDateFields(new Date);
        val cal = Calendar.getInstance;
        cal.add(Calendar.DAY_OF_MONTH, -delta);
        val from = LocalDate.fromCalendarFields(cal);
        val dates = CommonUtil.datesBetween(from, to);
        dates.map { x =>
            val suffix = "-" + x.toString();
            CommonUtil.getInputPaths(input, suffix);
        }.filterNot { x => x.isEmpty() }.mkString(",").split(',');
    }

    def getInput(input: String): Array[String] = {
        CommonUtil.getInputPaths(input).split(',');
    }

    def compute(input: String, output: String, parallelization: Int, duration: String, delta: Int, brokerList: Option[String]) {

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
        @transient val sc = CommonUtil.getSparkContext(parallelization, "TelemetryDataMigrator");
        val accum = sc.accumulator(0, "Total Event Count Accumulator");
        path.foreach { x => outputEventPerLine(x, sc, parallelization, output, accum, brokerList) }
        Console.println("## Total Events Size - " + accum.value + " ##");
        CommonUtil.closeSparkContext(sc);
    }

    def outputEventPerLine(path: String, sc: SparkContext, parallelization: Int, output: String, accum: Accumulator[Int], brokerList: Option[String]) {

        val rdd = sc.textFile(path, parallelization).cache();
        val fileName = path.split("/").last.replaceAll(".gz", ".log").replaceAll("telemetry.log", "telemetry.raw")
        val tmpFilePath = AppConf.getConfig("spark_output_temp_dir") + fileName;
        Console.println("## Input Path - " + path + " ##");

        val events = rdd.map { line =>
            {
                val mapper = new ObjectMapper();
                mapper.registerModule(DefaultScalaModule);
                try {
                    mapper.readValue[SyncEvent](line, classOf[SyncEvent]);
                } catch {
                    case e: Exception =>
                        Console.err.println("Error parsing JSON", e.getClass.getName, e.getMessage);
                        //e.printStackTrace();
                        null
                }
            }
        }.filter(_ != null).map { x => x.data.getOrElse(LineData(None, None, None, None, Array())).events }.collect();

        Console.println("## Log Events Count - " + rdd.count() + " | Distinct Events Count - " + events.length + " ##");
        
        val result = Buffer[String]();
        events.foreach { x => { x.foreach { y => result += CommonUtil.jsonToString(y) } } };
        
        val distinctEvents = result.distinct;
        accum += distinctEvents.size;

        Console.println("## Events Size - " + distinctEvents.size + " ##");
        Console.println("## Temp Output Path - " + tmpFilePath + " ##");
        moveToOutput(distinctEvents, output, tmpFilePath, brokerList);
        Console.println();
    }
    
    def createTempFile(tmpFilePath: String, events: Buffer[String]) = {
        Files.createFile(Paths.get(tmpFilePath));
        val fw = new FileWriter(tmpFilePath, true);
        events.foreach { x => {fw.write(x + "\n");}};
        fw.close();
    }

    def moveToOutput(events: Buffer[String], output: String, tmpFilePath: String, brokerList: Option[String]) = {

        output match {
            case a if a.startsWith("kafka://") =>
                Console.println("## Sending events to Kafka ##");
                val topic = a.replaceFirst("kafka://", "");
                KafkaEventProducer.publishEvents(events, topic, brokerList.getOrElse("localhost:9092"));
                Console.println("## Events sent to topic " + topic + " ##");
            case _ =>
                CommonUtil.sendOutput(output, tmpFilePath, true, false);
        }
    }

}