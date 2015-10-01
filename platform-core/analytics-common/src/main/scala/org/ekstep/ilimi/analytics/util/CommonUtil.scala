package org.ekstep.ilimi.analytics.util

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths.get
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.StreamingContext
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.model.Output
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.compact
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jvalue2extractable
import org.json4s.string2JsonInput
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.ekstep.ilimi.analytics.model.Gdata
import org.joda.time.LocalDate
import org.joda.time.Days
import java.util.Calendar
import scala.collection.mutable.ListBuffer
import java.io.File
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.util.zip.GZIPOutputStream
import java.io.FileOutputStream
import java.io.FileNotFoundException
import org.joda.time.Years
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object CommonUtil {

    @transient val df = new SimpleDateFormat("ssmmhhddMMyyyy");
    @transient val df2 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssXXX");
    @transient val df3: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
    @transient val df4: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    def getParallelization(parallelization: Int): Int = {
        if (parallelization == 0) {
            AppConf.getConfig("default.parallelization").toInt
        } else {
            parallelization
        }
    }

    def getSparkContext(parallelization: Int, appName: String): SparkContext = {

        Console.println("### Initializing Spark Context ###");
        val conf = new SparkConf().setAppName(appName);
        val master = conf.getOption("spark.master");
        if (master.isEmpty) {
            Console.println("### Master not found. Setting it to local[*] ###");
            conf.setMaster("local[*]");
        }
        val sc = new SparkContext(conf);
        sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", AppConf.getConfig("s3_aws_key"));
        sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", AppConf.getConfig("s3_aws_secret"));
        Console.println("### Spark Context initialized ###");
        sc;
    }

    def closeSparkContext(sc: SparkContext) {
        sc.stop();
    }

    def getPath(btype: String, relPath: String, location: String): String = location match {
        case "S3" => "s3n://" + AppConf.getConfig(btype) + relPath;
        case _    => relPath
    }

    def loadData(sc: SparkContext, input: String, location: String, parallelization: Int, filter: Event => Boolean): RDD[Event] = {
        Console.println("### Fetching Input:" + getPath("s3_input_bucket", input, location) + " ###");
        val rdd = sc.textFile(getPath("s3_input_bucket", input, location), parallelization).distinct().cache();
        rdd.map { x => getEvent(x) }.filter { x => filter(x) }
    }

    def getEvent(line: String): Event = {
        implicit val formats = DefaultFormats;
        parse(line).extract[Event]
    }

    def getTempPath(date: String): String = {
        AppConf.getConfig("spark_output_temp_dir") + date;
    }

    def getTempPath(date: Date): String = {
        AppConf.getConfig("spark_output_temp_dir") + df.format(date);
    }

    class Visitor extends java.nio.file.SimpleFileVisitor[java.nio.file.Path] {
        override def visitFile(
            file: java.nio.file.Path,
            attrs: java.nio.file.attribute.BasicFileAttributes): java.nio.file.FileVisitResult =
            {
                Files.delete(file);

                java.nio.file.FileVisitResult.CONTINUE;
            } // visitFile

        override def postVisitDirectory(
            dir: java.nio.file.Path,
            exc: IOException): java.nio.file.FileVisitResult =
            {
                Files.delete(dir);
                java.nio.file.FileVisitResult.CONTINUE;
            } // visitFile
    }

    def deleteDirectory(dir: String) {
        val path = get(dir);
        Files.walkFileTree(path, new Visitor());
    }

    def deleteFile(file: String) {
        Files.delete(get(file));
    }

    def saveOutput[T <: Output](rdd: RDD[T], outputPath: String, fileSuffix: String, location: String) = {
        val date = df.format(new Date());
        val fname = date + "_" + fileSuffix;
        val outputFKey = outputPath + fname;
        Console.println("### Saving stats to " + getPath("s3_output_bucket", outputFKey, location) + " ###");
        rdd.map { output =>
            {
                implicit val formats = DefaultFormats;
                compact(Extraction.decompose(output))
            }
        }.coalesce(1, true).saveAsTextFile(getTempPath(date));
        Console.println("## Saved to temp folder - " + getTempPath(date) + " ##");
        Files.copy(get(getTempPath(date) + "/part-00000"), get(getTempPath(fname)), REPLACE_EXISTING);
        deleteDirectory(getTempPath(date));
        location match {
            case "S3" =>
                S3Util.upload(AppConf.getConfig("s3_output_bucket"), getTempPath(fname), outputFKey);
            case _ =>
                val from = get(getTempPath(fname));
                val to = get(outputFKey);
                Files.createDirectories(get(outputPath));
                Files.move(from, to, REPLACE_EXISTING);
        }
    }

    def jsonToString(obj: Any): String = {
        implicit val formats = DefaultFormats;
        compact(Extraction.decompose(obj))
    }

    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
        val p = new java.io.PrintWriter(f)
        try { op(p) } finally { p.close() }
    }

    def checkContains(a: String, b: String): Boolean = {
        a.contains(b);
    }

    def getInputPath(input: String, suffix: String): String = {
        input match {
            case a if a.startsWith("s3://") =>
                val arr = a.replaceFirst("s3://", "").split('/');
                val bucket = arr(0);
                var prefix = a.replaceFirst("s3://", "").replaceFirst(bucket, "");
                if (prefix.startsWith("/")) {
                    prefix = prefix.replaceFirst("/", "");
                }
                if (null != suffix) {
                    prefix = prefix + suffix
                }
                S3Util.getAllKeys(bucket, prefix).map { x => "s3n://" + bucket + "/" + x }.mkString(",");
            case a if a.startsWith("local://") =>
                a.replaceFirst("local://", "");
            case _ =>
                throw new Exception("Invalid input. Valid input should start with s3:// (for S3 input) or local:// (for file input)");

        }
    }

    def getInputPaths(input: String): String = {
        val arr = input.split(',');
        arr.map { x => getInputPath(x, null) }.mkString(",");
    }

    def getInputPaths(input: String, suffix: String): String = {
        val arr = input.split(',');
        arr.map { x => getInputPath(x, suffix) }.mkString(",");
    }

    def datesBetween(from: LocalDate, to: LocalDate): IndexedSeq[LocalDate] = {
        val numberOfDays = Days.daysBetween(from, to).getDays()
        for (f <- 0 to numberOfDays) yield from.plusDays(f)
    }

    def getDatesBetween(fromDate: String, toDate: Option[String]): Array[String] = {
        var to = LocalDate.fromDateFields(new Date);
        if (toDate.nonEmpty) {
            to = df4.parseLocalDate(toDate.get);
        }
        val from = df4.parseLocalDate(fromDate);
        val dates = datesBetween(from, to);
        dates.map { x => df4.print(x) }.toArray;
    }

    def getInputPaths(input: String, suffix: String, fromDate: Option[String], toDate: Option[String]): Array[String] = {
        if (fromDate.nonEmpty) {
            val dates = getDatesBetween(fromDate.get, toDate);
            var paths = ListBuffer[String]();
            dates.foreach { x =>
                {
                    paths ++= getInputPath(input, suffix + x).split(',');
                }
            }
            paths.toArray;
        } else {
            getInputPath(input, suffix).split(',');
        }
    }

    def formatEventDate(date: DateTime): String = {
        df3.print(date);
    }

    def main(args: Array[String]): Unit = {
        Console.println(formatEventDate(new DateTime));
    }

    def getEventId(event: Event): String = {
        event.eid.getOrElse("");
    }

    def getEventTS(event: Event): Long = {
        try {
            df3.parseLocalDate(event.ts.getOrElse("")).toDate.getTime;
        } catch {
            case _: Exception =>
                Console.err.println("Invalid event time", event.ts.getOrElse(""));
                0
        }
    }

    def getGameId(event: Event): String = {
        event.gdata.getOrElse(Gdata(Option(""), Option(""))).id.getOrElse("");
    }

    def getUserId(event: Event): String = {
        event.uid.getOrElse("");
    }

    def getParallelization(config: Option[Map[String, String]]): Int = {
        getParallelization(config.getOrElse(Map[String, String]()));
    }

    def getParallelization(config: Map[String, String]): Int = {
        var parallelization = AppConf.getConfig("default.parallelization");
        if (config != null && config.nonEmpty) {
            parallelization = config.getOrElse("parallelization", parallelization);
        }
        parallelization.toInt;
    }

    def gzip(path: String): String = {
        val buf = new Array[Byte](1024);
        val src = new File(path);
        val dst = new File(path ++ ".gz");

        try {
            val in = new BufferedInputStream(new FileInputStream(src))
            try {
                val out = new GZIPOutputStream(new FileOutputStream(dst))
                try {
                    var n = in.read(buf)
                    while (n >= 0) {
                        out.write(buf, 0, n)
                        n = in.read(buf)
                    }
                } finally {
                    out.flush
                }
            } catch {
                case _: FileNotFoundException =>
                    System.err.printf("Permission Denied: %s", path ++ ".gz")
                case _: SecurityException =>
                    System.err.printf("Permission Denied: %s", path ++ ".gz")
            }
        } catch {
            case _: FileNotFoundException =>
                System.err.printf("File Not Found: %s", path)
            case _: SecurityException =>
                System.err.printf("Permission Denied: %s", path)
        }
        path ++ ".gz";
    }

    def getAge(dob: Date): Int = {
        val birthdate = LocalDate.fromDateFields(dob);
        val now = new LocalDate();
        val age = Years.yearsBetween(birthdate, now);
        age.getYears;
    }

    def sendOutput(output: String, tmpFilePath: String, gzip: Boolean, isPublic: Boolean) = {

        Console.println("## Zipping the file - gzip ##");
        val filePath = if (gzip) CommonUtil.gzip(tmpFilePath) else tmpFilePath;
        val fileName = filePath.split("/").last;
        if (gzip) Console.println("## Gzip complete. File path - " + filePath + " ##");

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
                if (prefix.startsWith("/")) prefix = prefix.replaceFirst("/", "");
                var uploadFileName = "";
                if (prefix.length() > 0) {
                    uploadFileName = prefix + "/" + fileName;
                } else {
                    uploadFileName = fileName;
                }
                if(isPublic) {
                    S3Util.uploadPublic(bucket, filePath, uploadFileName);
                } else {
                    S3Util.upload(bucket, filePath, uploadFileName);
                }
                CommonUtil.deleteFile(filePath);
                Console.println("## File uploaded to S3 at s3://" + bucket + "/" + uploadFileName + " ##");
            case _ =>
                throw new Exception("Invalid output location. Valid output location should start with s3:// (for S3 upload) or local:// (for local save)");
        }
        if (gzip) CommonUtil.deleteFile(tmpFilePath);
    }

}