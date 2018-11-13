package org.sunbird.media.service.impl

import org.sunbird.media.common._
import org.sunbird.media.config.AppConfig
import org.sunbird.media.service.AWSMediaService
import org.sunbird.media.util.HttpRestUtil
import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  */
object AWSMediaServiceImpl extends AWSMediaService {

  override def submitJob(jobRequest: MediaRequest): MediaResponse = {
    val url = getApiUrl("job")
    val reqBody = prepareJobRequestBody(jobRequest.request)
    val header = getDefaultHeader("POST", url, reqBody)
    val response = HttpRestUtil.post(url, header, reqBody)
    if (response.responseCode == "OK") Response.getSuccessResponse(Response.getSubmitJobResult(response)) else response
  }

  override def getJob(jobId: String): MediaResponse = {
    val response = getJobDetails(jobId)
    if (response.responseCode == "OK") Response.getSuccessResponse(Response.getJobResult(response)) else response
  }

  override def getStreamingPaths(jobId: String): MediaResponse = {
    val region = AppConfig.getConfig("aws.region");
    val streamType = AppConfig.getConfig("aws.stream.protocol").toLowerCase()
    val getResponse = getJobDetails(jobId)
    val inputs: List[Map[String, AnyRef]] = getResponse.result.getOrElse("job", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("settings", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("inputs", List).asInstanceOf[List[Map[String, AnyRef]]]
    val input: String = inputs.head.getOrElse("fileInput", "").toString
    val host = "https://s3." + region + ".amazonaws.com"
    val streamUrl: String = input.replace("s3:/", host)
      .replace("artifact", streamType)
      .replace(".mp4", ".m3u8")
    Response.getSuccessResponse(HashMap[String, AnyRef]("streamUrl" -> streamUrl))
  }

  override def listJobs(listJobsRequest: MediaRequest): MediaResponse = {
    null
  }

  override def cancelJob(cancelJobRequest: MediaRequest): MediaResponse = {
    null
  }

}
