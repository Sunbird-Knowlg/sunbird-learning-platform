package org.sunbird.media.service.impl

import org.sunbird.media.common._
import org.sunbird.media.config.AppConfig
import org.sunbird.media.service.IMediaService

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

  def prepareInputUrl(url: String): String = {
    val temp = url.split("content")
    val bucket = AppConfig.getConfig("aws_ms_content_bucket")
    val separator = java.io.File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + temp(1)
  }

  def prepareOutputUrl(contentId: String, streamType: String): String = {
    val bucket = AppConfig.getConfig("aws_ms_content_bucket")
    val separator = java.io.File.separator;
    "s3:" + separator + separator + bucket + separator + "content" + separator + contentId + separator + streamType + separator
  }


}
