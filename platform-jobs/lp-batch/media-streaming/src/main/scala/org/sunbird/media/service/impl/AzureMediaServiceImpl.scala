package org.sunbird.media.service.impl

import org.sunbird.media.common.{MediaRequest, MediaResponse}
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
}
