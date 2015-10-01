package org.ekstep.ilimi.analytics

import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.util.Application
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.ekstep.ilimi.analytics.model.Job
import org.ekstep.ilimi.analytics.model.AserDashboardModel

object AserDashboard extends Application {

    def main(i: String) {
        
        Console.println("Input", i);
        AppConf.init();
        val t1 = System.currentTimeMillis;
        val mapper = new ObjectMapper();
        mapper.registerModule(DefaultScalaModule);
        try {
            val job = mapper.readValue[Job](i, classOf[Job]);
            AserDashboardModel.compute(job);
        } catch {
            case e: Exception =>
                Console.err.println("Error parsing JSON", e.getClass.getName, e.getMessage);
                e.printStackTrace();
        }
        val t2 = System.currentTimeMillis;
        Console.println("## Model run complete - Time taken to compute - " + (t2 - t1) / 1000 + " ##");
    }

}