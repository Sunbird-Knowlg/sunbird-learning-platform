package org.sunbird.media.service.impl

import java.util
import java.util.{Map, UUID}

import com.amazonaws.services.mediaconvert.AWSMediaConvert
import com.amazonaws.services.mediaconvert.model.transform.{JobSettingsJsonUnmarshaller, JobSettingsMarshaller}
import com.amazonaws.services.mediaconvert.model.{BadRequestException, _}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.google.gson.Gson
import org.json4s.jackson.JsonMethods
import org.sunbird.media.common._
import org.sunbird.media.config.AppConfig
import org.sunbird.media.exception.MediaServiceException
import org.sunbird.media.service.IMediaService
import org.sunbird.media.util.MediaServiceClientUtill

import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  */
object AWSMediaServiceImpl extends IMediaService {

  val mediaClient: AWSMediaConvert = MediaServiceClientUtill.getClient().asInstanceOf[AWSMediaConvert]
  val mapper: ObjectMapper = new ObjectMapper() //.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  override def createJob(createJobRequest: MediaRequest): MediaResponse = {
    val template = AppConfig.getConfig("aws_ms_hls_template")
    val role = AppConfig.getConfig("aws_ms_role")
    null
  }

  override def getJob(jobId: String): MediaResponse = {
    try {
      val result: GetJobResult = mediaClient.getJob(new GetJobRequest().withId(jobId))
      val jsonString = new Gson().toJson(result)
      val map = JsonMethods.parse(jsonString).values.asInstanceOf[scala.collection.immutable.Map[String, AnyRef]]
      getSuccessResponse(map)
    } catch {
      case ex: BadRequestException => getFailureResponse("CLIENT_ERROR", "ERR_FETCH_JOB", "Unable to Fetch Job Status")
      case ex: Exception => getFailureResponse("EXCEPTION", "SYSTEM_ERROR", "Server Error Encountered! Please try again later.")
    }
  }

  override def listJobs(listJobsRequest: MediaRequest): MediaResponse = {
    val listOfJobs = mediaClient.listJobs(prepareListJobRequest(listJobsRequest))
    null
  }

  override def cancelJob(cancelJobRequest: MediaRequest): MediaResponse = {
    null
  }

  override def getStreamingUrl(jobId: String): MediaResponse = {
    null
  }

  override def createPreset(createPresetRequest: MediaRequest): MediaResponse = {
    null
  }

  override def getPreset(getPresetRequest: MediaRequest): MediaResponse = {
    null
  }

  override def listPresets(listPresetRequest: MediaRequest): MediaResponse = {
    null
  }

  override def updatePreset(updatePresetRequest: MediaRequest): MediaResponse = {
    null
  }

  override def deletePreset(deletePresetRequest: MediaRequest): MediaResponse = {
    null
  }

  override def createJobTemplate(createTemplateRequest: MediaRequest): MediaResponse = {
    null
  }

  override def getJobTemplate(templateId: String): MediaResponse = {
    null
  }

  override def listJobTemplate(listTemplateRequest: MediaRequest): MediaResponse = {
    null
  }

  override def updateJobTemplate(updateTemplateRequest: MediaRequest): MediaResponse = {
    null
  }

  override def deleteJobTemplate(deleteTemplateRequest: MediaRequest): MediaResponse = {
    null
  }


  def prepareListJobRequest(request: MediaRequest): ListJobsRequest = {
    val listRequest = new ListJobsRequest()
    val params = request.params;
    if (!params.isEmpty) {
      if (!params.get("queue").isEmpty)
        listRequest.setQueue(params.get("queue").asInstanceOf[String])
    }
    listRequest
  }


  def getResult(result: Any): Map[String, Any] = {
    //Map("status" -> result.getJob.getStatus)
    null
  }

  def getSuccessResponse(result: scala.collection.immutable.Map[String, AnyRef]): MediaResponse = {
    new MediaResponse(UUID.randomUUID().toString, System.currentTimeMillis().toString, new HashMap[String, AnyRef], ResponseCode.OK.toString, result);
  }

  def getFailureResponse(errorType: String, errorCode: String, errorMessage: String): MediaResponse = {
    val respCode: String = if (errorType.equalsIgnoreCase("CLIENT_ERROR")) ResponseCode.CLIENT_ERROR.toString else ResponseCode.SERVER_ERROR.toString
    val params = HashMap[String, String](
      "err" -> errorCode,
      "errMsg" -> errorMessage
    )
    new MediaResponse(UUID.randomUUID().toString, System.currentTimeMillis().toString, params, respCode, new HashMap[String, AnyRef]);
  }

  def getEndPoint(): String = {
    mediaClient.describeEndpoints(new DescribeEndpointsRequest()).getEndpoints.get(0).getUrl
  }

  def getQueueList(): ListQueuesResult = {
    mediaClient.listQueues(new ListQueuesRequest())
  }

  def getQueue(queueId: String): GetQueueResult = {
    mediaClient.getQueue(new GetQueueRequest().withName(queueId))
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

  def getTemplateSetting(templateId: String): GetJobTemplateResult = {
    mediaClient.getJobTemplate(new GetJobTemplateRequest().withName(templateId))
  }

}
