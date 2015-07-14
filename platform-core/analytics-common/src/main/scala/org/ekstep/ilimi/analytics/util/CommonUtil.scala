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
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.model.Event
import org.ekstep.ilimi.analytics.model.Output
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.compact
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jvalue2extractable
import org.json4s.string2JsonInput
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.StreamingContext

object CommonUtil {

    @transient val df = new SimpleDateFormat("ssmmhhddMMyyyy");

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

    def getSparkStreamingContext(appName: String, duration: Duration): StreamingContext = {
        val conf = new SparkConf().setAppName(appName);
        val master = conf.getOption("spark.master");
        if (master.isEmpty) {
            Console.println("### Master not found. Setting it to local[*] ###");
            conf.setMaster("local[*]");
        }
        new StreamingContext(conf, duration);
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
                val prefix = a.replaceFirst("s3://", "").replaceFirst(bucket + "/", "") + (if (null != suffix) suffix else "");
                S3Util.getAllKeys(bucket, prefix).map { x => "s3n://" + bucket + "/" + x }.mkString(",");
            case a if a.startsWith("local://") =>
                a.replaceFirst("local://", "");
            case _ =>
                throw new Exception("Invalid input. Valid input should start with s3:// (for S3 input) or local:// (for file input)");

        }
    }
    
    def getInputPaths(input: String) : String = {
        val arr = input.split(',');
        arr.map { x => getInputPath(x, null) }.mkString(",");
    }
    
    def getInputPaths(input: String, suffix: String) : String = {
        val arr = input.split(',');
        arr.map { x => getInputPath(x, suffix) }.mkString(",");
    }

}