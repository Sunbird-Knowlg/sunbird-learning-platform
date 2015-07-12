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

object TelemetryDataConsolidatorModel extends Serializable {

    def main(args: Array[String]): Unit = {
        S3Util.getMetadata("ep-production-backup", "logs/telemetry");
    }

    def compute(input: String, output: Option[String], outputDir: String, location: String, parallelization: Int) {

        @transient val sc = CommonUtil.getSparkContext(location, parallelization, "GameEffectiveness");

        val path = S3Util.getAllKeys(AppConf.getConfig("s3_input_bucket"), input).map { x => CommonUtil.getPath("s3_input_bucket", "/" + x, location) }.mkString(",");
        Console.println("path - " + path);
        val rdd = sc.textFile(path, parallelization).cache();
        val filePath = outputDir + "/" + output.getOrElse("telemetry-consolidated.json");
        val accum = sc.accumulator(0, "Event Count Accumulator")
        Console.println("RDD Count - " + rdd.count());
        val events = rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                parse(line).extract[SyncEvent]
            }
        }.map { x => x.data.getOrElse(LineData(None, None, None, Array())).events}.map { x => {
            accum += x.size;
            val fw = new FileWriter(filePath, true);
            x.foreach { x => fw.write(CommonUtil.jsonToString(x) + "\r\n");}
            fw.close();
        } }.count();
        Console.println("#Events Size - " + accum.value);        
    }

}