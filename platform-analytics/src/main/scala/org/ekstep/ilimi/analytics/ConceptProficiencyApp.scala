package org.ekstep.ilimi.analytics

import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.model.game.ConceptProficiencyModel
import org.ekstep.ilimi.analytics.util.Application

object ConceptProficiencyApp extends Application {

    def main(i: String, o: String, l: Option[String], p: Option[String], gd: String) {
        AppConf.init();
        val t1 = System.currentTimeMillis;
        ConceptProficiencyModel.compute(i, o, l.getOrElse("local"), gd, p.getOrElse(AppConf.getConfig("default.parallelization")).toInt)
        val t2 = System.currentTimeMillis;
        Console.println("### Model run complete - Time taken to compute - " + (t2 - t1) / 1000 + " ###");
    }

}