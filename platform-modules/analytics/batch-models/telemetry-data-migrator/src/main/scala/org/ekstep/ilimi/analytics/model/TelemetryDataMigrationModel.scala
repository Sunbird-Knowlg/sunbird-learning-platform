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

    def compute(input: String, output: String, parallelization: Int, duration: String, delta: Int) {

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
        val fileAccum = sc.accumulator(0, "Event Count Accumulator");
        path.foreach { x => outputEventPerLine(x, sc, parallelization, output, accum, fileAccum) }
        Console.println("## Total Events Size - " + accum.value + " ##");
        CommonUtil.closeSparkContext(sc);
    }

    def outputEventPerLine(path: String, sc: SparkContext, parallelization: Int, output: String, accum: Accumulator[Int], fileAccum: Accumulator[Int]) {

        fileAccum.setValue(0);
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
        Files.createFile(Paths.get(tmpFilePath));
        events.foreach { x =>
            {
                accum += x.distinct.size;
                fileAccum += x.distinct.size;
                val fw = new FileWriter(tmpFilePath, true);
                x.distinct.foreach { x => fw.write(CommonUtil.jsonToString(x) + "\n"); }
                fw.close();
            }
        };
        Console.println("## Events Size - " + fileAccum.value + " ##");
        Console.println("## Temp Output Path - " + tmpFilePath + " ##");
        moveToOutput(output, tmpFilePath);
        Console.println();
    }

    def moveToOutput(output: String, tmpFilePath: String) = {

        Console.println("## Zipping the file - gzip ##");
        val filePath = CommonUtil.gzip(tmpFilePath);
        val fileName = filePath.split("/").last;
        Console.println("## Gzip complete. File path - " + filePath + " ##");

        output match {
            case a if a.startsWith("local://") =>
                Console.println("## Saving file to local store ##");
                val outputPath = a.replaceFirst("local://", "");
                val from = Paths.get(filePath);
                val to = Paths.get(outputPath + "/" + fileName);
                Files.createDirectories(Paths.get(outputPath));
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
                Console.println("## File saved to localstore at " + outputPath + "/" + fileName + " ##");
            case a if a.startsWith("s3://") =>
                Console.println("## Uploading file to S3 ##");
                val arr = a.replaceFirst("s3://", "").split('/');
                val bucket = arr(0);
                var prefix = a.replaceFirst("s3://", "").replaceFirst(bucket, "");
                if(prefix.startsWith("/")) prefix = prefix.replaceFirst("/", "");
                var uploadFileName = "";
                if(prefix.length() > 0) {
                    uploadFileName = prefix + "/" + fileName;
                } else {
                    uploadFileName = fileName;
                }
                S3Util.upload(bucket, filePath, uploadFileName);
                CommonUtil.deleteFile(filePath);
                Console.println("## File uploaded to S3 at s3://" + bucket + "/" + uploadFileName + " ##");
            case _ =>
                throw new Exception("Invalid output location. Valid output location should start with s3:// (for S3 upload) or local:// (for local save)");
        }
        CommonUtil.deleteFile(tmpFilePath);
    }

}