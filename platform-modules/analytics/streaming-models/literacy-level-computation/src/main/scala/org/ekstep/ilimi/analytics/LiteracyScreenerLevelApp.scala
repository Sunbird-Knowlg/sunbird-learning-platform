package org.ekstep.ilimi.analytics

import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.util.Application
import org.ekstep.ilimi.analytics.model.LitScreenerLevelModel

object LiteracyScreenerLevelApp extends Application {
    
    def main(i: String, o: Option[String], l: Option[String], p: Option[String], outputDir: Option[String]) {
        AppConf.init();
        val t1 = System.currentTimeMillis;
        LitScreenerLevelModel.compute(i, o, outputDir, l.getOrElse("local"), p.getOrElse(AppConf.getConfig("default.parallelization")).toInt)
        val t2 = System.currentTimeMillis;
        Console.println("### Model run complete - Time taken to compute - " + (t2 - t1) / 1000 + " ###");
    }

}