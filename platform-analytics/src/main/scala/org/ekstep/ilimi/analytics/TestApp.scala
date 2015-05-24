package org.ekstep.ilimi.analytics

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.ekstep.ilimi.analytics.conf.AppConf
import org.ekstep.ilimi.analytics.util.AppDBUtils
import scala.collection.mutable.ListBuffer

object TestApp {
    
    def main(args: Array[String]) {
        AppConf.init();
    }
    
}