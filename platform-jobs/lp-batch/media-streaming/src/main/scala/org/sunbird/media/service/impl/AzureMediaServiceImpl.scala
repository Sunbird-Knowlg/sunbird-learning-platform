package org.sunbird.media.service.impl

import org.apache.commons.lang3.StringUtils
import org.sunbird.media.common.{AzureRequestBody, MediaRequest, MediaResponse, Response}
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
    val inputUrl = request.request.getOrElse("artifact_url", "").toString
    val contentId = request.request.get("content_id").mkString
    val jobId = contentId + "_" + System.currentTimeMillis()
    val temp = inputUrl.splitAt(inputUrl.lastIndexOf("/") + 1)
    val assetId = "asset-" + jobId

    val createAssetResponse = createAsset(assetId, jobId)

    if (createAssetResponse.responseCode.equalsIgnoreCase("OK")) {
      val apiUrl = getApiUrl("job").replace("jobIdentifier", jobId)
      val reqBody = AzureRequestBody.submit_job.replace("assetId", assetId).replace("baseInputUrl", temp._1).replace("inputVideoFile", temp._2)
      HttpRestUtil.put(apiUrl, getDefaultHeader(), reqBody)
    } else {
      Response.getFailureResponse(createAssetResponse.result, "SERVER_ERROR", "Output Asset [ " + assetId + " ] Creation Failed for Job : " + jobId)
    }
  }

  override def getJob(jobId: String): MediaResponse = {
    val url = getApiUrl("job").replace("jobId", jobId)
    HttpRestUtil.get(url, getDefaultHeader(), null)
  }

  override def getStreamingPaths(jobId: String): MediaResponse = {
    val streamLocatorName = "sl-" + jobId
    val assetName = "asset-" + jobId
    val locatorResponse = createStreamingLocator(streamLocatorName, assetName)
    if (locatorResponse.responseCode.equalsIgnoreCase("OK")) {
      Response.getSuccessResponse(prepareStreamingUrl(streamLocatorName, jobId))
    } else {
      Response.getFailureResponse(new HashMap[String, AnyRef], "SERVER_ERROR", "Streaming Locator [" + streamLocatorName + "] Creation Failed for Job : " + jobId)
    }
  }

  override def listJobs(listJobsRequest: MediaRequest): MediaResponse = {
    null
  }

  override def cancelJob(cancelJobRequest: MediaRequest): MediaResponse = {
    null
  }

  def createAsset(assetId: String, jobId: String): MediaResponse = {
    val url = getApiUrl("asset").replace("assetId", assetId)
    val requestBody = AzureRequestBody.create_asset.replace("assetId", assetId)
      .replace("assetDescription", "Output Asset for " + jobId)
    HttpRestUtil.put(url, getDefaultHeader(), requestBody)
  }

  def createStreamingLocator(streamingLocatorName: String, assetName: String): MediaResponse = {
    val url = getApiUrl("stream_locator").replace("streamingLocatorName", streamingLocatorName)
    val streamingPolicyName = AppConfig.getConfig("azure.stream.policy_name")
    val reqBody = AzureRequestBody.create_stream_locator.replace("assetId", assetName).replace("policyName", streamingPolicyName)
    HttpRestUtil.put(url, getDefaultHeader(), reqBody)
  }

  def getStreamUrls(streamingLocatorName: String): MediaResponse = {
    val url = getApiUrl("list_paths").replace("streamingLocatorName", streamingLocatorName)
    HttpRestUtil.post(url, getDefaultHeader(), "{}")
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
      case "asset" => baseUrl + "/assets/assetId?api-version=" + apiVersion
      case "job" => baseUrl + "/transforms/" + transformName + "/jobs/jobId?api-version=" + apiVersion
      case "stream_locator" => baseUrl + "/streamingLocators/streamingLocatorName?api-version=" + apiVersion
      case "list_paths" => baseUrl + "/streamingLocators/streamingLocatorName/listPaths?api-version=" + apiVersion
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

  //TODO: Complete implementation
  def prepareStreamingUrl(streamLocatorName: String, jobId: String): Map[String, AnyRef] = {
    val streamType = AppConfig.getConfig("azure.stream.protocol")
    var streamUrl = ""
    val listPathResponse = getStreamUrls(streamLocatorName)
    if (listPathResponse.responseCode.equalsIgnoreCase("OK")) {
      val urlList: List[Map[String, AnyRef]] = listPathResponse.result.getOrElse("streamingPaths", List).asInstanceOf[List[Map[String, AnyRef]]]

      urlList.map(streamMap => {
        if(StringUtils.equalsIgnoreCase(streamMap.getOrElse("streamingProtocol", null).toString, streamType)) {
          streamUrl = streamMap.get("paths").get.asInstanceOf[List[String]].head
        }
      })
      println("urlList:" + urlList)
      println("streamUrl : " + streamUrl)
      null
    } else {
      val getJobResponse = getJob(jobId)
      //val videoName=getJobResponse.result.get()
      null
    }
  }
}
