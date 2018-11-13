package org.sunbird.media.common

import java.util.UUID
import org.sunbird.media.config.AppConfig
import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  *
  */
object Response {

  val MEDIA_SERVICE_TYPE = AppConfig.getConfig("media_service_type")

  def getSuccessResponse(result: Map[String, AnyRef]): MediaResponse = {
    new MediaResponse(UUID.randomUUID().toString, System.currentTimeMillis().toString, new HashMap[String, AnyRef], ResponseCode.OK.toString, result);
  }

  def getFailureResponse(result: Map[String, AnyRef], errorCode: String, errorMessage: String): MediaResponse = {
    val respCode: String = errorCode match {
      case "BAD_REQUEST" => ResponseCode.CLIENT_ERROR.toString
      case "RESOURCE_NOT_FOUND" => ResponseCode.RESOURCE_NOT_FOUND.toString
      case "METHOD_NOT_ALLOWED" => ResponseCode.CLIENT_ERROR.toString
      case "SERVER_ERROR" => ResponseCode.SERVER_ERROR.toString
    }
    val params = HashMap[String, String](
      "err" -> errorCode,
      "errMsg" -> errorMessage
    )
    new MediaResponse(UUID.randomUUID().toString, System.currentTimeMillis().toString, params, respCode, result);
  }

  def getSubmitJobResult(response: MediaResponse): Map[String, AnyRef] = {
    var jobId: String = ""
    var jobStatus: String = ""
    var submittedOn: String = ""
    var lastModifiedOn: String = ""

    if (MEDIA_SERVICE_TYPE.equalsIgnoreCase("aws")) {
      val job: Map[String, AnyRef] = response.result.getOrElse("job", Map).asInstanceOf[Map[String, AnyRef]]
      val timing: Map[String, AnyRef] = job.getOrElse("timing", Map).asInstanceOf[Map[String, AnyRef]]
      jobId = job.getOrElse("id", "").toString
      jobStatus = job.getOrElse("status", "").toString.toUpperCase()
      //TODO: Convert submittedOn, lastModifiedOn to UTC
      submittedOn = timing.getOrElse("submitTime", "").toString
      lastModifiedOn = timing.getOrElse("finishTime", "").toString

    } else if (MEDIA_SERVICE_TYPE.equalsIgnoreCase("azure")) {
      val result = response.result
      jobId = result.getOrElse("name", "").toString
      val output: Map[String, AnyRef] = result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("outputs", List).asInstanceOf[List[Map[String, AnyRef]]].head
      jobStatus = output.getOrElse("state", "").toString.toUpperCase()
      submittedOn = result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("created", "").toString
      lastModifiedOn = result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("lastModified", "").toString
    }

    HashMap[String, AnyRef](
      "job" -> HashMap[String, AnyRef](
        "id" -> jobId,
        "status" -> jobStatus,
        "submittedOn" -> submittedOn,
        "lastModifiedOn" -> lastModifiedOn
      )
    )
  }

  def getJobResult(response: MediaResponse): Map[String, AnyRef] = {
    var jobId: String = ""
    var jobStatus: String = ""
    var error: Map[String, String] = null
    var submittedOn: String = ""
    var lastModifiedOn: String = ""

    if (MEDIA_SERVICE_TYPE.equalsIgnoreCase("aws")) {
      val job: Map[String, AnyRef] = response.result.getOrElse("job", Map).asInstanceOf[Map[String, AnyRef]]
      val timing: Map[String, AnyRef] = job.getOrElse("timing", Map).asInstanceOf[Map[String, AnyRef]]
      jobId = job.getOrElse("id", "").toString
      jobStatus = job.getOrElse("status", "").toString.toUpperCase()

      if (jobStatus == "ERROR") {
        error = HashMap[String, String](
          "errorCode" -> job.getOrElse("errorCode", "").toString,
          "errorMessage" -> job.getOrElse("errorMessage", "").toString
        )
      } else {
        error = null
      }

      submittedOn = timing.getOrElse("submitTime", "").toString
      lastModifiedOn = timing.getOrElse("finishTime", "").toString

    } else if (MEDIA_SERVICE_TYPE.equalsIgnoreCase("azure")) {
      val result = response.result
      jobId = result.getOrElse("name", "").toString
      val output: Map[String, AnyRef] = result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("outputs", List).asInstanceOf[List[Map[String, AnyRef]]].head
      jobStatus = output.getOrElse("state", "").toString.toUpperCase()

      if (jobStatus == "ERROR") {
        val errorMap: Map[String, AnyRef] = output.getOrElse("error", Map).asInstanceOf[Map[String, AnyRef]]
        error = HashMap[String, String](
          "errorCode" -> errorMap.getOrElse("code", "").toString,
          "errorMessage" -> errorMap.getOrElse("details", List).asInstanceOf[List[Map[String, AnyRef]]].head.getOrElse("message", "").toString
        )
      } else {
        error = null
      }
      submittedOn = result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("created", "").toString
      lastModifiedOn = result.getOrElse("properties", Map).asInstanceOf[Map[String, AnyRef]].getOrElse("lastModified", "").toString
    }

    HashMap[String, AnyRef](
      "job" -> HashMap[String, AnyRef](
        "id" -> jobId,
        "status" -> jobStatus,
        "submittedOn" -> submittedOn,
        "lastModifiedOn" -> lastModifiedOn,
        "error" -> error
      )
    )
  }

  def getCancelJobResult(response: MediaResponse): Map[String, AnyRef] = {
    null
  }

  def getListJobResult(response: MediaResponse): Map[String, AnyRef] = {
    null
  }
}
