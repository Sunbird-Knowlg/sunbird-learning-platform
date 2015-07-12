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

case class Eks(dspec: Option[Any], loc: Option[Any], length: Option[Long], ueksid: Option[String], gid: Option[String], err: Option[Any], subj: Option[String], mc: Option[Array[String]], mmc: Option[Array[String]], pass: Option[String], qid: Option[String], qtype: Option[String], qlevel: Option[String], score: Option[Int], maxscore: Option[Int], exlength: Option[Int], atmpts: Option[Int], failedatmpts: Option[Int], topics: Option[Any], current: Option[Int], max: Option[Int])
case class Edata(eks: Eks)
case class Gdata(id: String, ver: String)
//case class Event(eid: String, ts: Long, ver: String, gdata: Gdata, sid: Option[String], uid: Option[String], did: String, edata: Edata)
case class Event(eid: String, ts: String, ver: Option[String], gdata: Gdata, sid: Option[String], uid: Option[String], did: String, edata: Edata)

case class Pdata(id: String, mod: String, ver: String)
case class EventOutput(eid: String, ts: Long, ver: String, uid: Option[String], cid: Option[String], gdata: Option[Gdata], pdata: Option[Pdata], edata: Map[String, AnyRef])
case class LineData(id: Option[String], ver: Option[String], ts: Option[String], events: Array[Event]);
case class SyncEvent(apiType: Option[String], level: Option[String], msg: Option[String], time: Option[String], data: Option[LineData]);


trait Output {}

abstract class BaseModel extends Serializable {}