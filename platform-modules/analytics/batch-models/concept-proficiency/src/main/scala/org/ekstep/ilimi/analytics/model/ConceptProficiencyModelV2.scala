package org.ekstep.ilimi.analytics.model

import scala.collection.mutable.Buffer
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.parse
import org.ekstep.ilimi.analytics.util.CommonUtil
import scala.collection.mutable.ListBuffer
import java.io.FileWriter
import org.ekstep.ilimi.analytics.dao.UserDAO
import org.apache.spark.HashPartitioner
import java.util.Date
import org.joda.time.LocalDate
import org.joda.time.Years

case class Events(events: Array[Event]);

case class ConceptOutput(cid: String, score: Int, maxScore: Int, attempts: Int, maxAttempts: Int);

object ConceptProficiencyModelV2 extends Serializable {

    def compute(input: String, output: Option[String], outputDir: Option[String], location: String, parallelization: Int) {

        val validEvents = Array("OE_ASSESS");
        @transient val sc = CommonUtil.getSparkContext(parallelization, "ConceptProficiency");
        val userMapping = sc.broadcast(UserDAO.getUserMapping());
        val langMapping = sc.broadcast(UserDAO.getLanguageMapping());
        val filePath = outputDir.getOrElse("user-aggregates") + "/concept_proficiency_prod.csv";
        writeHeader(filePath);

        val baseRDD = CommonUtil.loadData(sc, input, location, parallelization, x => validEvents.contains(x.eid.getOrElse("")));
        val userPairs = baseRDD.map(event => (event.uid.get, Buffer(event))).partitionBy(new HashPartitioner(parallelization));
        val userProfs = userPairs.reduceByKey((a, b) => a ++ b).mapValues(events => {
            val conceptMap = events.map(event => (event.edata.eks.mc.getOrElse(Array()), event.edata.eks.score.getOrElse(0), event.edata.eks.maxscore.getOrElse(1), event.edata.eks.atmpts.getOrElse(1), event.edata.eks.failedatmpts.getOrElse(0)))
                .filter(p => !p._1.isEmpty)
                .map(f => f._1.map { x => (getString(x), f._2, f._3, f._4, f._5) })
                .flatMap(f => f)
                .groupBy { x => x._1 };

            val scores = conceptMap.mapValues { x => x.map(f => f._2) }.mapValues { x => x.reduce(_ + _) }.toMap;
            val maxScores = conceptMap.mapValues { x => x.map(f => f._3) }.mapValues { x => x.reduce(_ + _) }.toMap;
            val attempts = conceptMap.mapValues { x => x.map(f => f._4) }.mapValues { x => x.reduce(_ + _) }.toMap;
            val maxAttempts = conceptMap.mapValues { x => x.map(f => f._5) }.mapValues { x => x.reduce(_ + _) }.toMap;
            var result = ListBuffer[ConceptOutput]();
            conceptMap.foreach(f => {
                result += ConceptOutput(f._1, scores.getOrElse(f._1, 0), maxScores.getOrElse(f._1, 0), attempts.getOrElse(f._1, 0), maxAttempts.getOrElse(f._1, 0));
            });
            result;
        }).collect();

        var records = new ListBuffer[Array[String]];
        userProfs.foreach(f => {
            val user = userMapping.value.getOrElse(f._1, User("Anonymous","Anonymous", "Anonymous", "Unknown", new Date(), 0));
            f._2.foreach { x =>
                records += Array(
                    f._1,
                    user.ekstep_id,
                    getAge(user.dob),
                    user.gender.toUpperCase(),
                    langMapping.value.getOrElse(user.language_id, "Unknown"),
                    x.cid,
                    x.score.toString(),
                    x.maxScore.toString(),
                    "",
                    x.attempts.toString(),
                    x.maxAttempts.toString());
            }
        })
        val fw = new FileWriter(filePath, true);
        records.foreach { f => fw.write(f.mkString(",") + "\n"); }
        fw.close();
    }

    def writeHeader(filePath: String) {
        val header = Array(
            "Child UID",
            "Child Ekstep id",
            "Age",
            "Gender",
            "Language",
            "Micro Concept",
            "score",
            "maxscore",
            "total_length",
            "atmpts",
            "failedatmpts");
        val fw = new FileWriter(filePath, true);
        fw.write(header.mkString(",") + "\n");
        fw.close();
    }

    def getAge(dob: Date): String = {
        val birthdate = LocalDate.fromDateFields(dob);
        val now = new LocalDate();
        val age = Years.yearsBetween(birthdate, now);
        age.getYears.toString();
    }

    def getString(str: String): String = {
        if (str == null) {
            "";
        } else {
            str.filter(_ >= ' ');
        }
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