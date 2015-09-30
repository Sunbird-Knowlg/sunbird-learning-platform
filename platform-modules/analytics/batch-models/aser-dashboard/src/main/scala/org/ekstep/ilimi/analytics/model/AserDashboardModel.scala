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
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkFiles
import org.apache.spark.HashPartitioner
import org.ekstep.ilimi.analytics.dao.UserDAO
import org.apache.spark.broadcast.Broadcast
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormat
import scala.io.Source
import java.io.PrintWriter

case class Question(qid: Option[String], res: Option[Array[String]], pass: Option[String], score: Option[Int], timeStamp: Option[Long], timeSpent: Option[Double]);
case class Game(id: Option[String], level: Option[String], secondChance: Option[String], noOfSessions: Option[Int], timeSpent: Option[Double], startTimestamp: Option[Long], endTimestamp: Option[Long]);
case class Record(uid: String, name: Option[String], age: Option[Int], language: Option[String], ekstepId: Option[String], gender: Option[String], questions: Buffer[Question]);

object AserDashboardModel extends Serializable {

    @transient val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");

    def compute(job: Job) {

        val config = job.config.getOrElse(Map[String, String]());
        val p = CommonUtil.getParallelization(config);
        @transient val sc = CommonUtil.getSparkContext(p, "GenericDashboard");
        val userMapping = sc.broadcast(UserDAO.getUserMapping());
        val langMapping = sc.broadcast(UserDAO.getLanguageMapping());

        if (job.rscript.nonEmpty) {
            sc.addFile(job.rscript.get);
        }

        val rdd = executeQuery(job.query, config, sc, p);
        val filterRdd = filter(job.filter, rdd);
        val sortedRdd = sort(job.sort, filterRdd);
        val aserRdd = getAserData(sortedRdd, p, userMapping, langMapping);
        if (job.rscript.nonEmpty) {
            invokeRscript(job, aserRdd.collect());
        } else {
            Console.println("Count of events - " + aserRdd.count());
            aserRdd.collect().foreach { x => Console.println(CommonUtil.jsonToString(x)) };
        }
        CommonUtil.closeSparkContext(sc);
    }

    def executeQuery(query: Query, config: Map[String, String], sc: SparkContext, p: Int): RDD[Event] = {
        var path = config.getOrElse("input", "") + "/*";
        var dates = Array[String]();
        if (query.dateFilter.nonEmpty) {
            path = CommonUtil.getInputPaths(config.getOrElse("input", ""), config.getOrElse("suffix", ""), query.dateFilter.get.from, query.dateFilter.get.to).mkString(",");
            dates = CommonUtil.getDatesBetween(query.dateFilter.get.from.get, query.dateFilter.get.to);
        }
        Console.println("## Path - " + path + " ##");
        val rdd = sc.textFile(path, p).cache();
        val baseRdd = rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                parse(line).extract[Event]
            }
        }
        if (dates.length > 0) {
            baseRdd.filter { x =>
                {
                    try {
                        dates.contains(df.parseLocalDate(x.ts.get).toString())
                    } catch {
                        case _: Exception => false
                    }
                }
            }
        } else {
            baseRdd;
        }
    }

    def filter(filter: Option[Filter], rdd: RDD[Event]): RDD[Event] = {
        var filterRDD = rdd;
        if (filter.nonEmpty) {
            if (filter.get.contentId.nonEmpty) {
                filterRDD = filterRDD.filter { x =>
                    {
                        CommonUtil.getGameId(x).equals(filter.get.contentId.get);
                    }
                }
            }
            if (filter.get.eventIds.nonEmpty) {
                val validEvents = filter.get.eventIds.get.split(',');
                filterRDD = filterRDD.filter { x =>
                    {
                        validEvents.contains(CommonUtil.getEventId(x));
                    }
                }
            }
        }
        filterRDD;
    }

    def sort(sort: Option[Sort], rdd: RDD[Event]): RDD[Event] = {
        var sortedRdd = rdd;
        if (sort.nonEmpty) {
            if (sort.get.by.nonEmpty) {
                val ascending = sort.get.order.getOrElse("asc").equals("asc");
                sortedRdd = sort.get.by.get match {
                    case "uid" =>
                        rdd.sortBy(f => CommonUtil.getUserId(f), ascending, 1);
                    case "ts" =>
                        rdd.sortBy(f => f.ts.get, ascending, 1);
                }
            }
        }
        sortedRdd;
    }

    def getAserData(rdd: RDD[Event], parallelization: Int, userMapping: Broadcast[Map[String, User]], langMapping: Broadcast[Map[Int, String]]): RDD[String] = {
        val userPairs = rdd.filter { x => x.uid.nonEmpty }.map(event => (event.uid.get, Buffer(event))).partitionBy(new HashPartitioner(parallelization));
        userPairs.reduceByKey((a, b) => a ++ b).mapValues { x =>
            x.map { x =>
                {
                    Question(x.edata.eks.qid, x.edata.eks.res, x.edata.eks.pass, x.edata.eks.score, Option(CommonUtil.getEventTS(x.ts.get)), x.edata.eks.length);
                }
            }
        }.map(f => {
            val user = userMapping.value.getOrElse(f._1, User("Anonymous", "Anonymous", "Anonymous", "Unknown", new Date(), 0));
            Record(f._1, Option(user.name), Option(CommonUtil.getAge(user.dob)), langMapping.value.get(user.language_id), Option(user.ekstep_id), Option(user.gender), f._2)
        }).map { x => CommonUtil.jsonToString(x) };
    }

    def invokeRscript(job: Job, events: Array[String]) = {

        val proc = Runtime.getRuntime.exec(job.rscript.get, Array("outputFile=" + job.config.get.getOrElse("outputFile", "/tmp/Aser_output.html"), "sourceDir=" + job.config.get.getOrElse("sourceDir", "/")));
        new Thread("stderr reader for " + job.rscript.get) {
            override def run() {
                for (line <- Source.fromInputStream(proc.getErrorStream).getLines)
                    Console.err.println(line)
            }
        }.start();
        new Thread("stdin writer for " + job.rscript.get) {
            override def run() {
                val out = new PrintWriter(proc.getOutputStream)
                for (elem <- events)
                    out.println(elem)
                out.close()
            }
        }.start();
        val outputLines = Source.fromInputStream(proc.getInputStream).getLines;
        println(outputLines.toList);
    }

}