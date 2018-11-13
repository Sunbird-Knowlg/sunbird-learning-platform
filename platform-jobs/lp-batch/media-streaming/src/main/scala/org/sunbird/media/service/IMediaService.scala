package org.sunbird.media.service

import org.sunbird.media.common.{MediaRequest, MediaResponse}

/**
  *
  * @author gauraw
  */
trait IMediaService {

  def submitJob(request: MediaRequest): MediaResponse

  def getJob(jobId: String): MediaResponse

  def getStreamingPaths(jobId: String): MediaResponse

  def listJobs(listJobsRequest: MediaRequest): MediaResponse

  def cancelJob(cancelJobRequest: MediaRequest): MediaResponse

}
