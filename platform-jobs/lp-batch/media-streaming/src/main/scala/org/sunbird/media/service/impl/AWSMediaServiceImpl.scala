package org.sunbird.media.service.impl

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import org.sunbird.media.common._
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.service.IMediaService
import org.sunbird.media.util.{AWSSignUtils, HttpRestUtil}
import scala.collection.immutable.HashMap
import scala.reflect.io.File

/**
  *
  * @author gauraw
  */
object AWSMediaServiceImpl extends IMediaService {

  override def getToken(request: MediaRequest): MediaResponse = {
    null
  }

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

  def getJobDetails(jobId: String): MediaResponse = {
    val url = getApiUrl("job") + "/" + jobId
    val header = getDefaultHeader("GET", url, null)
    HttpRestUtil.get(url, header, null)
  }

  def prepareInputUrl(url: String): String = {
    val temp = url.split("content")
    val bucket = AppConfig.getSystemConfig("aws.content_bucket")
    val separator = File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + temp(1)
  }

  def prepareOutputUrl(contentId: String, streamType: String): String = {
    val bucket = AppConfig.getSystemConfig("aws.content_bucket")
    val separator = File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + separator + contentId + separator + streamType.toLowerCase + separator
  }

  def getApiUrl(apiName: String): String = {
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint")
    val apiVersion: String = AppConfig.getConfig("aws.api.version")
    val baseUrl: String = host + File.separator + apiVersion
    apiName.toLowerCase() match {
      case "job" => baseUrl + "/jobs"
      case _ => throw new MediaServiceException("ERR_INVALID_API_NAME", "Please Provide Valid Media Service API Name")
    }
  }

  def getSignatureHeader(): Map[String, String] = {
    val formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    val date = formatter.format(new Date())
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint").replace("https://", "")
    Map[String, String]("Content-Type" -> "application/json", "host" -> host, "x-amz-date" -> date)
  }

  def getDefaultHeader(httpMethod: String, url: String, payload: String): Map[String, String] = {
    val signHeader = getSignatureHeader
    val authToken = AWSSignUtils.generateToken(httpMethod, url, signHeader, payload)
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint").replace("https://", "")
    HashMap[String, String](
      "Content-Type" -> "application/json",
      "host" -> host,
      "x-amz-date" -> signHeader.get("x-amz-date").mkString,
      "Authorization" -> authToken
    )
  }

  def prepareJobRequestBody(jobRequest: Map[String, AnyRef]): String = {
    val queue = AppConfig.getSystemConfig("aws.service.queue")
    val role = AppConfig.getSystemConfig("aws.service.role")
    val streamType = AppConfig.getConfig("aws.stream.protocol").toLowerCase()
    val artifactUrl = jobRequest.get("artifact_url").mkString
    val contentId = jobRequest.get("content_id").mkString
    val inputFile = prepareInputUrl(artifactUrl)
    val output = prepareOutputUrl(contentId, streamType)
    AWSRequestBody.submit_hls_job
      .replace("queueId", queue)
      .replace("mediaRole", role)
      .replace("inputVideoFile", inputFile)
      .replace("outputLocation", output)
  }

}
