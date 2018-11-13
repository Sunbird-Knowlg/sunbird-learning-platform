package org.sunbird.media.service.impl

import org.sunbird.media.common.{AzureRequestBody, MediaRequest, MediaResponse, Response}
import org.sunbird.media.service.AzureMediaService
import org.sunbird.media.util.HttpRestUtil

import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  */
object AzureMediaServiceImpl extends AzureMediaService {

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
      val response = HttpRestUtil.put(apiUrl, getDefaultHeader(), reqBody)
      if (response.responseCode == "OK") Response.getSuccessResponse(Response.getSubmitJobResult(response)) else response
    } else {
      Response.getFailureResponse(createAssetResponse.result, "SERVER_ERROR", "Output Asset [ " + assetId + " ] Creation Failed for Job : " + jobId)
    }
  }

  override def getJob(jobId: String): MediaResponse = {
    val response = getJobDetails(jobId)
    if (response.responseCode == "OK") Response.getSuccessResponse(Response.getSubmitJobResult(response)) else response
  }

  override def getStreamingPaths(jobId: String): MediaResponse = {
    val streamLocatorName = "sl-" + jobId
    val assetName = "asset-" + jobId
    val locatorResponse = createStreamingLocator(streamLocatorName, assetName)
    if (locatorResponse.responseCode == "OK" || locatorResponse.responseCode == "CLIENT_ERROR") {
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

}
