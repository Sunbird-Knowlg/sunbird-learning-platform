package org.sunbird.media.service.impl

import org.sunbird.media.common.{MediaRequest, MediaResponse}
import org.sunbird.media.service.IMediaService

/**
  *
  * @author gauraw
  */
object AzureMediaServiceImpl extends IMediaService {

  override def createJob(createJobRequest: MediaRequest): MediaResponse = ???

  override def getJob(jobId: String): MediaResponse = ???

  override def listJobs(listJobsRequest: MediaRequest): MediaResponse = ???

  override def cancelJob(cancelJobRequest: MediaRequest): MediaResponse = ???

  override def getStreamingUrl(jobId: String): MediaResponse = ???

  override def createPreset(createPresetRequest: MediaRequest): MediaResponse = ???

  override def getPreset(getPresetRequest: MediaRequest): MediaResponse = ???

  override def listPresets(listPresetRequest: MediaRequest): MediaResponse = ???

  override def updatePreset(updatePresetRequest: MediaRequest): MediaResponse = ???

  override def deletePreset(deletePresetRequest: MediaRequest): MediaResponse = ???

  override def createJobTemplate(createTemplateRequest: MediaRequest): MediaResponse = ???

  override def getJobTemplate(templateId: String): MediaResponse = ???

  override def listJobTemplate(listTemplateRequest: MediaRequest): MediaResponse = ???

  override def updateJobTemplate(updateTemplateRequest: MediaRequest): MediaResponse = ???

  override def deleteJobTemplate(deleteTemplateRequest: MediaRequest): MediaResponse = ???
}
