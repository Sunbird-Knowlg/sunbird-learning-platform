package org.sunbird.media.service

import org.sunbird.media.common.{MediaRequest, MediaResponse}

/**
  *
  * @author gauraw
  */
trait IMediaService {

  //Job
  def createJob(createJobRequest: MediaRequest): MediaResponse

  def getJob(jobId: String): MediaResponse

  def listJobs(listJobsRequest: MediaRequest): MediaResponse

  def cancelJob(cancelJobRequest: MediaRequest): MediaResponse

  def getStreamingUrl(jobId: String): MediaResponse

  // Preset
  def createPreset(createPresetRequest: MediaRequest): MediaResponse

  def getPreset(getPresetRequest: MediaRequest): MediaResponse

  def listPresets(listPresetRequest: MediaRequest): MediaResponse

  def updatePreset(updatePresetRequest: MediaRequest): MediaResponse

  def deletePreset(deletePresetRequest: MediaRequest): MediaResponse

  //Template
  def createJobTemplate(createTemplateRequest: MediaRequest): MediaResponse

  def getJobTemplate(templateId: String): MediaResponse

  def listJobTemplate(listTemplateRequest: MediaRequest): MediaResponse

  def updateJobTemplate(updateTemplateRequest: MediaRequest): MediaResponse

  def deleteJobTemplate(deleteTemplateRequest: MediaRequest): MediaResponse

}
