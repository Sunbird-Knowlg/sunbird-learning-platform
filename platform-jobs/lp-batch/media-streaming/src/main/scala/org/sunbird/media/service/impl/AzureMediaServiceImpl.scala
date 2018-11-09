package org.sunbird.media.service.impl

import com.google.gson.Gson
import org.sunbird.media.common.{MediaRequest, MediaResponse, ResponseCode}
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.service.IMediaService
import org.sunbird.media.util.HttpRestUtil

import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  */
object AzureMediaServiceImpl extends IMediaService {

  var API_ACCESS_TOKEN: String = _

  override def getToken(request: MediaRequest): MediaResponse = {
    val tanent = AppConfig.getSystemConfig("azure.tanent")
    val clientKey = AppConfig.getSystemConfig("azure.token.client_key")
    val clientSecret = AppConfig.getSystemConfig("azure.token.client_secret")
    val loginUrl = "https://login.microsoftonline.com/" + tanent + "/oauth2/token"

    val data = HashMap[String, String](
      "grant_type" -> "client_credentials",
      "client_id" -> clientKey,
      "client_secret" -> clientSecret,
      "resource" -> "https://management.core.windows.net/"
    )

    val header = HashMap[String, String](
      "Content-Type" -> "application/x-www-form-urlencoded",
      "Keep-Alive" -> "true"
    )

    HttpRestUtil.post(loginUrl, header, data)
  }

  override def submitJob(request: MediaRequest): MediaResponse = {
    null
  }

  override def getJob(jobId: String): MediaResponse = {
    val url = getApiUrl("job").replace("jobId", jobId)
    HttpRestUtil.get(url, getDefaultHeader(), null)
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

  def createAsset(assetId: String): MediaResponse = {
    val url = getApiUrl("asset")
    null
  }

  def createStreamingLocator(streamingLocatorName: String): MediaResponse = {
    val url = getApiUrl("stream_locator")
    null
  }

  def getStreamUrls(streamingLocatorName: String): MediaResponse = {
    val url = getApiUrl("list_paths")
    null
  }

  def getApiUrl(apiName: String): String = {

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
      case "stream_locator" => baseUrl + "/streamingLocators/streamingLocatorName?api-version=" + apiVersion
      case "list_paths" => baseUrl + "s/treamingLocators/streamingLocatorName/listPaths?api-version=" + apiVersion
      case _ => throw new MediaServiceException("ERR_INVALID_API_NAME", "Please Provide Valid Media Service API Name")
    }
  }

  def getDefaultHeader(): Map[String, String] = {
    val authToken = "Bearer " + API_ACCESS_TOKEN
    HashMap[String, String](
      "Content-Type" -> "application/json",
      "Accept" -> "application/json",
      "Authorization" -> authToken
    )
  }
}
