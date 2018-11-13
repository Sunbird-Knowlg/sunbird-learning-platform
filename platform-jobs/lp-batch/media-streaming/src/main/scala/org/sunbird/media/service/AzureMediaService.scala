package org.sunbird.media.service

import org.apache.commons.lang3.StringUtils
import org.sunbird.media.common.{AzureRequestBody, MediaResponse}
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.util.HttpRestUtil

import scala.collection.immutable.HashMap
import scala.reflect.io.File

/**
  *
  * @author gauraw
  *
  */
abstract class AzureMediaService extends IMediaService {

  private val API_ACCESS_TOKEN = getToken()

  private def getToken(): String = {
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

    val response = HttpRestUtil.post(loginUrl, header, data)
    response.result.getOrElse("access_token", "").toString
  }

  protected def getJobDetails(jobId: String): MediaResponse = {
    val url = getApiUrl("job").replace("jobIdentifier", jobId)
    HttpRestUtil.get(url, getDefaultHeader(), null)
  }

  protected def createAsset(assetId: String, jobId: String): MediaResponse = {
    val url = getApiUrl("asset").replace("assetId", assetId)
    val requestBody = AzureRequestBody.create_asset.replace("assetId", assetId)
      .replace("assetDescription", "Output Asset for " + jobId)
    HttpRestUtil.put(url, getDefaultHeader(), requestBody)
  }

  protected def createStreamingLocator(streamingLocatorName: String, assetName: String): MediaResponse = {
    val url = getApiUrl("stream_locator").replace("streamingLocatorName", streamingLocatorName)
    val streamingPolicyName = AppConfig.getConfig("azure.stream.policy_name")
    val reqBody = AzureRequestBody.create_stream_locator.replace("assetId", assetName).replace("policyName", streamingPolicyName)
    HttpRestUtil.put(url, getDefaultHeader(), reqBody)
  }

  protected def getStreamingLocator(streamingLocatorName: String): MediaResponse = {
    val url = getApiUrl("stream_locator").replace("streamingLocatorName", streamingLocatorName)
    HttpRestUtil.get(url, getDefaultHeader(), null)
  }

  protected def getStreamUrls(streamingLocatorName: String): MediaResponse = {
    val url = getApiUrl("list_paths").replace("streamingLocatorName", streamingLocatorName)
    HttpRestUtil.post(url, getDefaultHeader(), "{}")
  }

  protected def getApiUrl(apiName: String): String = {
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
      case "asset" => baseUrl + "/assets/assetId?api-version=" + apiVersion
      case "job" => baseUrl + "/transforms/" + transformName + "/jobs/jobIdentifier?api-version=" + apiVersion
      case "stream_locator" => baseUrl + "/streamingLocators/streamingLocatorName?api-version=" + apiVersion
      case "list_paths" => baseUrl + "/streamingLocators/streamingLocatorName/listPaths?api-version=" + apiVersion
      case _ => throw new MediaServiceException("ERR_INVALID_API_NAME", "Please Provide Valid Media Service API Name")
    }
  }

  protected def getDefaultHeader(): Map[String, String] = {
    val accessToken = if (StringUtils.isNotBlank(API_ACCESS_TOKEN)) API_ACCESS_TOKEN else getToken()
    val authToken = "Bearer " + accessToken
    HashMap[String, String](
      "Content-Type" -> "application/json",
      "Accept" -> "application/json",
      "Authorization" -> authToken
    )
  }

  protected def prepareStreamingUrl(streamLocatorName: String, jobId: String): Map[String, AnyRef] = {
    val streamType = AppConfig.getConfig("azure.stream.protocol")
    val streamHost = AppConfig.getConfig("azure.stream.base_url")
    var url = ""
    val listPathResponse = getStreamUrls(streamLocatorName)
    if (listPathResponse.responseCode.equalsIgnoreCase("OK")) {
      val urlList: List[Map[String, AnyRef]] = listPathResponse.result.getOrElse("streamingPaths", List).asInstanceOf[List[Map[String, AnyRef]]]
      urlList.map(streamMap => {
        if (StringUtils.equalsIgnoreCase(streamMap.getOrElse("streamingProtocol", null).toString, streamType)) {
          url = streamMap.get("paths").get.asInstanceOf[List[String]].head
        }
      })
      val streamUrl = streamHost + url.replace("aapl", "aapl-v3")
      HashMap[String, AnyRef]("streamUrl" -> streamUrl)
    } else {
      val getResponse: MediaResponse = getJobDetails(jobId)
      val fileName: String = getResponse.result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("input", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("files", List).asInstanceOf[List[AnyRef]].head.toString
      val getStreamResponse = getStreamingLocator(streamLocatorName);
      val locatorId = getStreamResponse.result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("streamingLocatorId", "").toString
      val streamUrl = streamHost + File.separator + locatorId + File.separator + fileName.replace(".mp4", ".ism") + "/manifest(format=m3u8-aapl-v3)"
      HashMap[String, AnyRef]("streamUrl" -> streamUrl)
    }
  }
}
