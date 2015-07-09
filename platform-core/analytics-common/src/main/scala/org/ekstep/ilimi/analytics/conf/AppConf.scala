package org.ekstep.ilimi.analytics.conf

import java.util.Properties
import java.io.FileInputStream
import scala.io.Source

object AppConf {

    var initialized = false;
    var properties: Properties = null;

    def init() {
        if (!initialized) {
            val is = getClass.getResourceAsStream("/config.properties")
            properties = new Properties();
            properties.load(is)
            is.close()
            initialized = true;
        }
    }

    def getConfig(key: String): String = {
        if (!initialized) {
            init();
        }
        properties.getProperty(key);
    }

}