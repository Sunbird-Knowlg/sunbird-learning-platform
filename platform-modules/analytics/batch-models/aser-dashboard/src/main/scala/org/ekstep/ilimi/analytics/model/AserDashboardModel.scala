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
case class Game(id: Option[String], level: Option[Map[String, String]], secondChance: Option[String], noOfSessions: Option[Int], timeSpent: Option[Double], startTimestamp: Option[Long], endTimestamp: Option[Long]);
case class Record(uid: String, name: Option[String], age: Option[Int], language: Option[String], ekstepId: Option[String], gender: Option[String], questions: Buffer[Question], game: Option[Game]);

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
            //aserRdd.collect().foreach { x => Console.println(x) };
            executeScript(config, job, aserRdd.collect());
        } else {
            Console.println("## No Script to execute. Count of events - " + aserRdd.count() + " ##");
        }
        CommonUtil.closeSparkContext(sc);
    }

    def executeQuery(query: Query, config: Map[String, String], sc: SparkContext, p: Int): RDD[Event] = {

        var path = config.getOrElse("input", "") + "/*";
        var dates = Array[String]();
        if (query.dateFilter.nonEmpty) {
            path = CommonUtil.getInputPaths(config.getOrElse("input", ""), config.getOrElse("suffix", ""), query.dateFilter.get.from, query.dateFilter.get.to).filterNot { x => x.isEmpty() }.mkString(",");
            dates = CommonUtil.getDatesBetween(query.dateFilter.get.from.get, query.dateFilter.get.to);
        }
        Console.println("## Path - " + path + " ##");
        
        if(path.isEmpty()) {
            throw new Exception("No path found");
        }
        
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
                        CommonUtil.getGameId(x).equals(filter.get.contentId.get) || x.edata.eks.gid.getOrElse("").equals(filter.get.contentId.get);
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

    def getLength(len: Option[AnyRef]): Option[Double] = {
        if (len.nonEmpty) {
            if (len.get.isInstanceOf[String]) {
                Option(len.get.asInstanceOf[String].toDouble)
            } else if (len.get.isInstanceOf[Double]) {
                Option(len.get.asInstanceOf[Double])
            } else if (len.get.isInstanceOf[Int]) {
                Option(len.get.asInstanceOf[Int].toDouble)
            } else {
                Option(0d);
            }
        } else {
            Option(0d);
        }
    }

    def getAserData(rdd: RDD[Event], parallelization: Int, userMapping: Broadcast[Map[String, User]], langMapping: Broadcast[Map[Int, String]]): RDD[String] = {
        val userQuestions = rdd.filter { x => x.uid.nonEmpty && CommonUtil.getEventId(x).equals("OE_ASSESS") }
            .map(event => (event.uid.get, Buffer(event)))
            .partitionBy(new HashPartitioner(parallelization))
            .reduceByKey((a, b) => a ++ b).mapValues { x =>
                x.map { x => Question(x.edata.eks.qid, x.edata.eks.res, x.edata.eks.pass, x.edata.eks.score, Option(CommonUtil.getEventTS(x)), getLength(x.edata.eks.length)) }
            };
        val userGame = rdd.filter { x => x.uid.nonEmpty }
            .map(event => (event.uid.get, Buffer(event)))
            .partitionBy(new HashPartitioner(parallelization))
            .reduceByKey((a, b) => a ++ b).mapValues { x =>
                val distinctEvents = x.distinct;
                val assessEvents = distinctEvents.filter { x => CommonUtil.getEventId(x).equals("OE_ASSESS") }.sortBy { x => CommonUtil.getEventTS(x) };
                val qids = assessEvents.map { x => x.edata.eks.qid.getOrElse("") };
                val secondChance = Option(if ((qids.length - qids.distinct.length) > 0) "Yes" else "No");
                val noOfSessions = Option(distinctEvents.filter { x => CommonUtil.getEventId(x).equals("GE_LAUNCH_GAME") }.length);
                val oeStarts = distinctEvents.filter { x => CommonUtil.getEventId(x).equals("GE_LAUNCH_GAME") };
                val oeEnds = distinctEvents.filter { x => CommonUtil.getEventId(x).equals("OE_END") };
                val startTimestamp = if (oeStarts.length > 0) { Option(CommonUtil.getEventTS(oeStarts(0))) } else { Option(0l) };
                val endTimestamp = if (oeEnds.length > 0) { Option(CommonUtil.getEventTS(oeEnds(0))) } else { Option(0l) };
                val timeSpent = if (oeEnds.length > 0) { getLength(oeEnds.last.edata.eks.length) } else { Option(0d) };
                var levelMap = distinctEvents.filter { x => CommonUtil.getEventId(x).equals("OE_LEVEL_SET") }.map { x => (x.edata.eks.category.getOrElse(""), x.edata.eks.current.getOrElse("")) }.toMap;
                Game(Option(CommonUtil.getGameId(x(0))), Option(levelMap.toMap), secondChance, noOfSessions, timeSpent, startTimestamp, endTimestamp)
            }
        userQuestions.join(userGame, 1).map(f => {
            val user = userMapping.value.getOrElse(f._1, User("Anonymous", "Anonymous", "Anonymous", "Unknown", new Date(), 0));
            Record(f._1, Option(user.name), Option(CommonUtil.getAge(user.dob)), langMapping.value.get(user.language_id), Option(user.ekstep_id), Option(user.gender), f._2._1, Option(f._2._2))
        }).map { x => CommonUtil.jsonToString(x) };

    }

    def executeScript(config: Map[String, String], job: Job, events: Array[String]) = {
        Console.println("## Count of events - " + events.length + " ##");
        val outputFile = config.getOrElse("outputFile", "Aser_output") + "-" + System.currentTimeMillis() + "." + config.getOrElse("outputFileExt", "html");
        val proc = Runtime.getRuntime.exec(job.rscript.get, Array("outputFile=" + outputFile, "sourceDir=" + config.getOrElse("sourceDir", "/"), "jobConfig=" + CommonUtil.jsonToString(job)));
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
        val exitStatus = proc.waitFor();
        if (exitStatus == 0) {
            CommonUtil.sendOutput(config.getOrElse("uploadDir", "local:///tmp"), outputFile, false, config.getOrElse("uploadDirPublic", "true").equals("true"));
        }
    }

}