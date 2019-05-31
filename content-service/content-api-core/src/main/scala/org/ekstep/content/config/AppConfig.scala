package org.ekstep.content.config

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Configuration for content service.
  *
  *@author Kumar Gauraw
  */
object AppConfig {

  val defaultConfig = ConfigFactory.load()
  val envConfig = ConfigFactory.systemEnvironment()
  val config = envConfig.withFallback(defaultConfig)

  def getConfig(key: String): String = {
    if (config.hasPath(key))
      config.getString(key)
    else null
  }

  def getSystemConfig(key: String): String = {
    val sysKey=key.replaceAll("\\.","_")
    if (config.hasPath(sysKey))
      config.getString(sysKey)
    else null
  }

  def getConfig(): Config = {
    config
  }

}
