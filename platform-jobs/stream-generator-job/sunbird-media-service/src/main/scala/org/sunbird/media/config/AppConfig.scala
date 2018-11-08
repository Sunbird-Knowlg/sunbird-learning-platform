package org.sunbird.media.config

import com.typesafe.config.{Config, ConfigFactory}
import org.sunbird.media.exception.MediaServiceException

/**
  *
  * @author gauraw
  */
object AppConfig {

  lazy val defaultConfig = ConfigFactory.load();
  lazy val envConfig = ConfigFactory.systemEnvironment();
  lazy val config = defaultConfig.withFallback(envConfig);

  def getConfig(key: String): String = {
    if (config.hasPath(key))
      config.getString(key);
    else throw new MediaServiceException("CONFIG_NOT_FOUND", "Configuration for key [" + key + "] Not Found.")
  }

  def getConfig(): Config = {
    return config;
  }

  def getServiceType(): String = {
    getConfig("media_service_type");
  }
}
