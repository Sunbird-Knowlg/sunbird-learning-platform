package org.sunbird.media.service.impl

import java.util.UUID

import org.sunbird.media.common.{MediaRequest, MediaResponse, ResponseCode}
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.service.IMediaService

/**
  *
  * @author gauraw
  */
object AzureMediaServiceImpl extends IMediaService {

  override def getToken(request: MediaRequest): MediaResponse = {
    null
  }

  override def submitJob(request: MediaRequest): MediaResponse = {
    null
  }

  override def getJob(jobId: String): MediaResponse = {
    null
  }

  override def getStreamingPaths(jobId: String): MediaResponse = {
    null
  }

  override def listJobs(listJobsRequest: MediaRequest): MediaResponse = {
    null
  }

  override def cancelJob(cancelJobRequest: MediaRequest): MediaResponse = {
    null
  }

  def prepareApiUrl(apiName: String): String = {

    val subscriptionId: String = AppConfig.getSystemConfig("azure.subscription_id")
    val resourceGroupName: String = AppConfig.getSystemConfig("azure.resource_group_name")
    val accountName: String = AppConfig.getSystemConfig("azure.account_name")
    val apiVersion: String = AppConfig.getConfig("azure.api.version")
    val transformName: String = AppConfig.getConfig("azure.transform.default")

    val baseUrl: String = new StringBuilder().append("https://management.azure.com/subscriptions/")
      .append(subscriptionId)
      .append("/resourceGroups/")
      .append(resourceGroupName)
      .append("/providers/Microsoft.Media/mediaServices/")
      .append(accountName).mkString


    apiName.toLowerCase() match {
      case "asset" => baseUrl + "/assets/:assetId?api-version=" + apiVersion
      case "job" => baseUrl + "/transforms/" + transformName + "/jobs/jobId?api-version=" + apiVersion
      case "create_stream_locator" => baseUrl + "/streamingLocators/streamingLocatorName?api-version=" + apiVersion
      case "list_paths" => baseUrl + "s/treamingLocators/streamingLocatorName/listPaths?api-version=" + apiVersion
      case _ => throw new MediaServiceException("ERR_INVALID_API_NAME", "Please Provide Valid Media Service API Name")
    }
  }
}
