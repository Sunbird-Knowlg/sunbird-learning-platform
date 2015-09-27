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

object GenericDashboardModel extends Serializable {

    def compute(job: Job) {

        val config = job.config.getOrElse(Map[String, String]());
        val p = CommonUtil.getParallelization(config);
        @transient val sc = CommonUtil.getSparkContext(p, "GenericDashboard");
        sc.addFile(job.rscript);
        val rdd = executeQuery(job.query, config, sc, p);
        val filterRdd = filter(job.filter, rdd);
        val sortedRdd = sort(job.sort, filterRdd);
        val pipedRdd = sortedRdd.pipe(SparkFiles.get(job.rscript));
        pipedRdd.collect();
    }

    def executeQuery(query: Query, config: Map[String, String], sc: SparkContext, p: Int): RDD[Event] = {
        var path = config.getOrElse("input", "") + "/*";
        if (query.dateFilter.nonEmpty) {
            path = CommonUtil.getInputPaths(config.getOrElse("input", ""), config.getOrElse("suffix", ""), query.dateFilter.get.from, query.dateFilter.get.to).mkString(",");
        }
        val rdd = sc.textFile(path, p).cache();
        rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                parse(line).extract[Event]
            }
        }
    }

    def filter(filter: Option[Filter], rdd: RDD[Event]): RDD[Event] = {
        var filterRDD = rdd;
        if (filter.nonEmpty) {
            if (filter.get.contentId.nonEmpty) {
                filterRDD = rdd.filter { x =>
                    {
                        CommonUtil.getGameId(x).equals(filter.get.contentId.get);
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

}