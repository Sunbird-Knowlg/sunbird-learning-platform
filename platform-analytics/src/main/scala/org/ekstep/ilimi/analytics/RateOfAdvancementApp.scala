package org.ekstep.ilimi.analytics

import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.model.game.RateOfAdvancementModel
import org.ekstep.ilimi.analytics.util.Application

object RateOfAdvancementApp extends Application {

    def main(i: String, o: String, l: Option[String], p: Option[String]) {
        AppConf.init();
        val t1 = System.currentTimeMillis;
        RateOfAdvancementModel.compute(i, o, l.getOrElse("local"), p.getOrElse(AppConf.getConfig("default.parallelization")).toInt)
        val t2 = System.currentTimeMillis;
        Console.println("### Model run complete - Time taken to compute - " + (t2 - t1) / 1000 + " ###");
    }

}