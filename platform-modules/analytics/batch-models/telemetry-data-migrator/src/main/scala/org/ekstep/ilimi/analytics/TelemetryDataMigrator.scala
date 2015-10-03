package org.ekstep.ilimi.analytics

import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.util.Application
import org.ekstep.ilimi.analytics.model.TelemetryDataMigrationModel

object TelemetryDataMigrator extends Application {
    
    def main(i: String, o: String, d: Option[String], delta: Option[String], p: Option[String], brokerList: Option[String]) {
        AppConf.init();
        val t1 = System.currentTimeMillis;
        TelemetryDataMigrationModel.compute(i, o, p.getOrElse(AppConf.getConfig("default.parallelization")).toInt, d.getOrElse("full"), delta.getOrElse("1").toInt, brokerList);
        val t2 = System.currentTimeMillis;
        Console.println("## Model run complete - Time taken to compute - " + (t2 - t1) / 1000 + " ##");
    }

}