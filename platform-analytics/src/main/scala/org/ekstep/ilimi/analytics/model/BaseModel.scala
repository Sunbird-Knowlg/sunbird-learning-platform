package org.ekstep.ilimi.analytics.model

import java.io.IOException
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Paths.get
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.util.S3Util
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.compact
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jvalue2extractable
import org.json4s.string2JsonInput

case class Eks(dspec: Option[Any], loc: Option[Any], length: Option[Long], ueksid: Option[String], gid: Option[String], err: Option[Any], subj: Option[String], mc: Option[String], skill: Option[String], qid: Option[String], qtype: Option[String], qlevel: Option[String], score: Option[Int], maxscore: Option[Int], exlength: Option[Int], atmpts: Option[Int], failedatmpts: Option[Int], topics: Option[Any], current: Option[Int], max: Option[Int])
case class Edata(eks: Eks)
case class Gdata(id: String, ver: String)
case class Event(eid: String, ts: Long, ver: String, gdata: Gdata, sid: Option[String], uid: Option[String], did: String, edata: Edata)

trait Output {

}

abstract class BaseModel extends Serializable {

    var location: String = null;
    var parallelization: Int = AppConf.getConfig("default.parallelization").toInt;
    @transient val df = new SimpleDateFormat("ssmmhhddMMyyyy");

    def initializeSparkContext(location: String, parallelization: Int): SparkContext = {
        this.location = location;
        this.parallelization = parallelization;
        val conf = new SparkConf().setMaster("local").setAppName("GameEffectiveness");
        val sc = new SparkContext(conf);
        if ("S3".equals(location)) {
            sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", AppConf.getConfig("s3_aws_key"));
            sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", AppConf.getConfig("s3_aws_secret"));
        }
        Console.println("### Spark Context instantiated ###");
        sc;
    }

    def closeSparkContext(sc: SparkContext) {
        sc.stop();
    }

    def getPath(btype: String, relPath: String): String = this.location match {
        case "S3" => "s3n://" + AppConf.getConfig(btype) + relPath;
        case _    => relPath
    }

    def loadInput(sc: SparkContext, input: String, filter: Event => Boolean): RDD[Event] = {
        Console.println("### Fetching Input:" + getPath("s3_input_bucket", input) + " ###");
        val rdd = sc.textFile(getPath("s3_input_bucket", input), this.parallelization).cache();
        rdd.map { x =>
            {
                implicit val formats = DefaultFormats;
                parse(x).extract[Event]
            }
        }.filter { x => filter(x) }
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

    def saveResult[T <: Output](rdd: RDD[T], outputPath: String, fileSuffix: String) = {
        val date = df.format(new Date());
        val fname = date + "_" + fileSuffix;
        val outputFKey = outputPath + fname;
        Console.println("### Saving stats to " + getPath("s3_output_bucket", outputFKey) + " ###");
        rdd.map { output =>
            {
                implicit val formats = DefaultFormats;
                compact(Extraction.decompose(output))
            }
        }.coalesce(1, true).saveAsTextFile(getTempPath(date));
        Console.println("## Saved to temp folder - " + getTempPath(date) + " ##");
        Files.copy(get(getTempPath(date) + "/part-00000"), get(getTempPath(fname)), REPLACE_EXISTING);
        deleteDirectory(getTempPath(date));
        this.location match {
            case "S3" =>
                S3Util.upload(AppConf.getConfig("s3_output_bucket"), getTempPath(fname), outputFKey);
            case _ =>
                val from = get(getTempPath(fname));
                val to = get(outputFKey);
                Files.createDirectories(get(outputPath));
                Files.move(from, to, REPLACE_EXISTING);
        }
    }

}