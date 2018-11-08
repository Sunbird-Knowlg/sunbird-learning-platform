package org.sunbird.media.util

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.mediaconvert.AWSMediaConvertClientBuilder
import com.microsoft.azure.management.mediaservices.v2018_07_01.implementation.MediaManager
import org.apache.commons.lang.StringUtils
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException

object MediaServiceClientUtill {

  val MEDIA_SERVICE_TYPE: String = AppConfig.getServiceType()
  val CLIENT_KEY: String = AppConfig.getConfig(MEDIA_SERVICE_TYPE + "_mediaservice_key")
  val CLIENT_SECRET: String = AppConfig.getConfig(MEDIA_SERVICE_TYPE + "_mediaservice_secret")

  def getClient(): AnyRef = {
    if (StringUtils.isBlank(MEDIA_SERVICE_TYPE))
      throw new MediaServiceException("INVALID_MEDIA_SERVICE", "Please Provide Valid Media Service Name")

    if (StringUtils.equalsIgnoreCase("aws", MEDIA_SERVICE_TYPE)) {
      val region = AppConfig.config.getString(MEDIA_SERVICE_TYPE + "_mediaservice_region")
      val endPoint = AppConfig.getConfig(MEDIA_SERVICE_TYPE + "_mediaservice_endpoint")
      val credentials: AWSCredentials = new BasicAWSCredentials(CLIENT_KEY, CLIENT_SECRET)
      val provider: AWSCredentialsProvider = new AWSStaticCredentialsProvider(credentials)
      val endPointConfig: EndpointConfiguration = new EndpointConfiguration(endPoint, region)
      return AWSMediaConvertClientBuilder.standard().withCredentials(provider).withEndpointConfiguration(endPointConfig).build()
    }

    if (StringUtils.equalsIgnoreCase("azure", MEDIA_SERVICE_TYPE)) {

    }
    null
  }
}
