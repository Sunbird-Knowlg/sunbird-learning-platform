package org.sunbird.media.service.impl

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone, UUID}

import org.sunbird.media.common._
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.service.IMediaService
import org.sunbird.media.util.{AWSSignUtils, HttpRestUtil}

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.reflect.io.File

/**
  *
  * @author gauraw
  */
object AWSMediaServiceImpl extends IMediaService {

  override def getToken(request: MediaRequest): MediaResponse = {
    null
  }

  override def submitJob(request: MediaRequest): MediaResponse = {
    null
  }

  override def getJob(jobId: String): MediaResponse = {
    val url=getApiUrl("job")+jobId
    val header=getDefaultHeader("GET",url,null)
    println("herader : "+header)
    HttpRestUtil.get(url,header,null)
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

  def prepareInputUrl(url: String): String = {
    val temp = url.split("content")
    val bucket = AppConfig.getConfig("aws.content_bucket")
    val separator = File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + temp(1)
  }

  def prepareOutputUrl(contentId: String, streamType: String): String = {
    val bucket = AppConfig.getConfig("aws.content_bucket")
    val separator = File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + separator + contentId + separator + streamType + separator
  }

  def getApiUrl(apiName: String): String = {
    val host:String=AppConfig.getSystemConfig("aws.api.endpoint")
    val apiVersion: String = AppConfig.getConfig("aws.api.version")

    val baseUrl: String = host+File.separator+apiVersion

    apiName.toLowerCase() match {
      case "job" => baseUrl + "/jobs/"
      case _ => throw new MediaServiceException("ERR_INVALID_API_NAME", "Please Provide Valid Media Service API Name")
    }
  }

  def getSignatureHeader(): Map[String, String] = {
    val formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    val date = formatter.format(new Date())
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint")
    Map[String, String]("Content-Type" -> "application/json", "host" -> host, "x-amz-date" -> date)
  }

  def getDefaultHeader(httpMethod: String, url: String, payload: String): Map[String, String] = {
    val authToken=AWSSignUtils.generateToken(httpMethod, url, getSignatureHeader, payload)
    val host: String = AppConfig.getSystemConfig("aws.api.endpoint")
    HashMap[String, String](
      "Content-Type" -> "application/json",
      "host" -> host,
      "x-amz-date"->getSignatureHeader.get("x-amz-date").mkString,
      "Authorization" -> authToken
    )
  }


}
