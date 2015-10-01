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
import org.apache.spark.SparkConf

case class Eks(subj: Option[String], mc: Option[Array[String]], mmc: Option[Array[String]], pass: Option[String], qid: Option[String], qtype: Option[String], qlevel: Option[String], score: Option[Int], maxscore: Option[Int], res: Option[Array[String]], exres: Option[Array[String]], length: Option[String], exlength: Option[Double], atmpts: Option[Int], failedatmpts: Option[Int], category: Option[String], current: Option[String], max: Option[String], `type`: Option[String], extype: Option[String], id: Option[String], gid: Option[String])
case class Edata(eks: Eks)
case class Gdata(id: Option[String], ver: Option[String])
case class Event(eid: Option[String], ts: Option[String], ver: Option[String], gdata: Option[Gdata], sid: Option[String], uid: Option[String], did: Option[String], edata: Edata)

case class Pdata(id: String, mod: String, ver: String)
case class EventData(eks: Map[String, AnyRef])
case class EventOutput(eid: String, ts: String, ver: String, uid: Option[String], cid: Option[String], ctype: Option[String], gdata: Option[Gdata], pdata: Option[Pdata], edata: EventData)

case class LineEventData(eks: Option[Map[String, AnyRef]], ext: Option[Map[String, AnyRef]], current: Option[String], max: Option[String]);
case class LineEvent(eid: Option[String], ts: Option[String], ver: Option[String], gdata: Option[Gdata], sid: Option[String], uid: Option[String], did: Option[String], edata: LineEventData, tags: Option[Array[Map[String, AnyRef]]])
case class LineData(id: Option[String], ver: Option[String], ts: Option[String], params: Option[Map[String, AnyRef]], events: Array[LineEvent]);
case class SyncEvent(apiType: Option[String], level: Option[String], msg: Option[String], time: Option[String], errorMsg: Option[String], data: Option[LineData]);

case class User(name: String, encoded_id: String, ekstep_id: String, gender: String, dob: Date, language_id: Int);

case class Filter(contentId: Option[String], eventIds: Option[String]);
case class Sort(by: Option[String], order: Option[String]);
case class DateFilter(from: Option[String], to: Option[String]);
case class Query(dateFilter: Option[DateFilter]);
case class Job(query: Query, filter: Option[Filter], sort: Option[Sort], rscript: Option[String], config: Option[Map[String, String]]);


trait Output {}

class Models extends Serializable {}