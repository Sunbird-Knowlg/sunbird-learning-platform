package org.ekstep.ilimi.analytics.model

import scala.collection.mutable.Buffer
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.parse
import org.ekstep.ilimi.analytics.util.CommonUtil
import org.ekstep.ilimi.analytics.EventSessionization
import org.ekstep.ilimi.analytics.streaming.LitScreenerLevelComputation
import org.ekstep.ilimi.analytics.dao.LitScreenerLevelDAO
import scala.collection.mutable.ListBuffer
import java.io.FileWriter
import org.apache.spark.rdd.RDD
import org.apache.spark.HashPartitioner
import scala.collection.mutable.HashMap

case class Event2(childId: String, eventId: String, mc: String, qid: String, pass: String, score: String, maxScore: String, `type`: String, ts: String)
object LitScreenerLevelModel_V2 extends Serializable {
    
    def compute(input: String, output: Option[String], outputDir: Option[String], location: String, parallelization: Int) {

        val validEvents = Array("OE_ASSESS", "OE_INTERACT");
        @transient val sc = CommonUtil.getSparkContext(parallelization, "GameEffectiveness");
        val accum = sc.accumulator(0, "Event Count Accumulator")
        val filePath = outputDir.getOrElse("user-aggregates") + "/sprint2_tumkur_consolidated_2.csv";
        writeHeader(filePath);
            
        Console.println("Input:" + input);        
        val rdd = sc.textFile(input, parallelization).cache();
        val userPairs: RDD[(String, Buffer[Event2])] = rdd
        .map {x => x.split(',')}.filter { x => validEvents.contains(x(2)) }
        .map { arr => {
            //Console.print("Row ID:" + arr(2) + "Length" + arr.length);
            Event2(arr(0), arr(2),  arr(6), arr(8), arr(12), arr(14), arr(15), arr(23), arr(25)); 
        }}.map { event => (event.childId, Buffer(event)) }.partitionBy(new HashPartitioner(parallelization));
        val userGroupEvents = userPairs.reduceByKey((a, b) => a ++ b).mapValues { x => 
            var eventMap = new HashMap[Event2, Buffer[Event2]]();
            var tempBuffer = new ListBuffer[Event2]();
            x.foreach { x =>  
                x.eventId match {
                    case "OE_INTERACT" =>
                        tempBuffer += x;
                    case "OE_ASSESS" =>
                        eventMap(x) = tempBuffer;
                        tempBuffer = new ListBuffer[Event2]();
                    case _ =>
                }
            }    
            val map2 = eventMap.map(f => {
                val eventCountMap = f._2.groupBy { x => x.`type` }.mapValues { x => x.map { x => 1 } }.mapValues { x => x.reduce(_ + _) }.toMap;
                (f._1, (eventCountMap.getOrElse("START", 0), eventCountMap.getOrElse("CHOOSE", 0), eventCountMap.getOrElse("LISTEN", 0)));
            });
            map2;
        }.collect();
        
        var records = new ListBuffer[Array[String]];
        userGroupEvents.foreach(f => {
            f._2.foreach { x =>
                records += Array(
                    f._1,
                    x._1.mc,
                    x._1.qid,
                    x._1.pass,
                    x._1.score,
                    x._1.maxScore,
                    x._2._1.toString(),
                    x._2._2.toString(),
                    x._2._3.toString());
            }
        })
        val fw = new FileWriter(filePath, true);
        records.foreach { f => fw.write(f.mkString(",") + "\n"); }
        fw.close();

    }

    def writeHeader(filePath: String) {
        val header = Array(
                    "Child Genie id",
                    "Micro Concept",
                    "qid",
                    "pass",
                    "score",
                    "maxscore",
                    "START",
                    "CHOOSE",
                    "LISTEN");
        val fw = new FileWriter(filePath, true);
        fw.write(header.mkString(",") + "\n");
        fw.close();
    }

    def getString(str: String): String = {
        str.filter(_ >= ' ');
    }

    def getStringFromArray(str: Option[Array[String]]): String = {
        str.getOrElse(Array()).mkString(",").filter(_ >= ' ');
    }

    def getString(str: Option[String]): String = {
        str.getOrElse("").filter(_ >= ' ');
    }

    def getStringFromInt(i: Option[Int]): String = {
        if (i.isEmpty) {
            return "";
        }
        i.get.toString();
    }

    def getStringFromDouble(db: Option[Double]): String = {
        db.getOrElse(0).toString();
    }

}