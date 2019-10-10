package org.sunbird.cassandra.loggers

import org.slf4j.{Logger, LoggerFactory}

object TransactionLoggerFactory {

    private var loggers: Map[String, Logger] = Map()
    val basePath: String = "org.sunbird.cassandra.triggers"

    def getLogger(objectType: String): Logger = {
        val key = basePath + "." + objectType
        val logger = loggers.getOrElse(key, LoggerFactory.getLogger(key))
        if(!loggers.contains(key))
            loggers += (key -> logger)
        logger
    }
}
