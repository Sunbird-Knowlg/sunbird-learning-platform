package org.sunbird.media.config

import com.typesafe.config.{Config, ConfigFactory}
import org.sunbird.media.exception.MediaServiceException

/**
  *
  * @author gauraw
  */
object AppConfig {

  val defaultConfig = ConfigFactory.load();
  val envConfig = ConfigFactory.systemEnvironment();
  val config = envConfig.withFallback(defaultConfig);

  def getConfig(key: String): String = {
    if (config.hasPath(key))
      config.getString(key);
    else throw new MediaServiceException("CONFIG_NOT_FOUND", "Configuration for key [" + key + "] Not Found.")
  }

  def getSystemConfig(key: String): String = {
    val sysKey=key.replaceAll("\\.","_")
    if (config.hasPath(sysKey))
      config.getString(sysKey);
    else throw new MediaServiceException("CONFIG_NOT_FOUND", "Configuration for key [" + sysKey + "] Not Found.")
  }

  def getConfig(): Config = {
    return config;
  }

  def getServiceType(): String = {
    getConfig("media_service_type");
  }
}
