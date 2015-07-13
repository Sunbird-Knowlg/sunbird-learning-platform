package org.ekstep.ilimi.analytics.model

import scala.collection.mutable.Buffer
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods.parse
import org.ekstep.ilimi.analytics.util.CommonUtil
import org.ekstep.ilimi.analytics.EventSessionization
import org.ekstep.ilimi.analytics.streaming.LitScreenerLevelComputation

case class Events(events: Array[Event]);

object LitScreenerLevelModel extends Serializable {

    def compute(input: String, output: Option[String], outputDir: Option[String], location: String, parallelization: Int) {

        val validEvents = Array("OE_ASSESS");
        @transient val sc = CommonUtil.getSparkContext(parallelization, "GameEffectiveness");
        val loltMapping = EventSessionization.broadcastMapping("src/main/resources/lo_lt_mapping.csv", sc);
        val ldloMapping = EventSessionization.broadcastMapping("src/main/resources/ld_lo_mapping.csv", sc);
        val compldMapping = EventSessionization.broadcastMapping("src/main/resources/composite_ld_mapping.csv", sc);
        val litLevelsMap = EventSessionization.broadcastLevelRanges("src/main/resources/lit_scr_level_ranges.csv", sc);

        val resultOutput = output.getOrElse("console");
        val rdd = sc.textFile(CommonUtil.getPath("s3_input_bucket", input, location), parallelization).cache();
        val events = rdd.map { line =>
            {
                implicit val formats = DefaultFormats;
                parse(line).extract[Events]
            }
        }.map { le => le.events }.reduce((a, b) => a ++ b);

        events.groupBy { event => event.uid.get }.foreach(f => LitScreenerLevelComputation.compute(f._2.toBuffer, loltMapping, ldloMapping, compldMapping, litLevelsMap, resultOutput, outputDir, null));
    }

}