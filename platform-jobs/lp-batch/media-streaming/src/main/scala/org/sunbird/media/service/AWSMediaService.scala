package org.sunbird.media.service

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import org.sunbird.media.common.{AWSRequestBody, MediaResponse}
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.util.{AWSSignUtils, HttpRestUtil}

import scala.collection.immutable.HashMap
import scala.reflect.io.File

/**
  *
  *
  * @author gauraw
  */
abstract class AWSMediaService extends IMediaService {

  protected def getJobDetails(jobId: String): MediaResponse = {
    val url = getApiUrl("job") + "/" + jobId
    val header = getDefaultHeader("GET", url, null)
    HttpRestUtil.get(url, header, null)
  }

  protected def prepareInputUrl(url: String): String = {
    val temp = url.split("content")
    val bucket = AppConfig.getSystemConfig("aws.content_bucket")
    val separator = File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + temp(1)
  }

  protected def prepareOutputUrl(contentId: String, streamType: String): String = {
    val bucket = AppConfig.getSystemConfig("aws.content_bucket")
    val separator = File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + separator + contentId + separator + streamType.toLowerCase + separator
  }

  protected def getApiUrl(apiName: String): String = {
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint")
    val apiVersion: String = AppConfig.getConfig("aws.api.version")
    val baseUrl: String = host + File.separator + apiVersion
    apiName.toLowerCase() match {
      case "job" => baseUrl + "/jobs"
      case _ => throw new MediaServiceException("ERR_INVALID_API_NAME", "Please Provide Valid Media Service API Name")
    }
  }

  protected def getSignatureHeader(): Map[String, String] = {
    val formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    val date = formatter.format(new Date())
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint").replace("https://", "")
    Map[String, String]("Content-Type" -> "application/json", "host" -> host, "x-amz-date" -> date)
  }

  protected def getDefaultHeader(httpMethod: String, url: String, payload: String): Map[String, String] = {
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

  protected def prepareJobRequestBody(jobRequest: Map[String, AnyRef]): String = {
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
