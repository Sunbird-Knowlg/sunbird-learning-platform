package org.sunbird.media.common

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import org.apache.commons.lang3.StringUtils

import scala.collection.immutable.HashMap

object AWSResult {
  val formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  formatter.setTimeZone(TimeZone.getTimeZone("UTC"))

  def getSubmitJobResult(response: MediaResponse): Map[String, AnyRef] = {
    val job: Map[String, AnyRef] = response.result.getOrElse("job", Map).asInstanceOf[Map[String, AnyRef]]
    val timing: Map[String, AnyRef] = job.getOrElse("timing", Map).asInstanceOf[Map[String, AnyRef]]
    HashMap[String, AnyRef](
      "job" -> HashMap[String, AnyRef](
        "id" -> job.getOrElse("id", "").toString,
        "status" -> job.getOrElse("status", "").toString.toUpperCase(),
        "submittedOn" -> formatter.format(new Date(timing.getOrElse("submitTime", "").toString.toLong*1000))
      )
    )

  }

  def getJobResult(response: MediaResponse): Map[String, AnyRef] = {
    val job: Map[String, AnyRef] = response.result.getOrElse("job", Map).asInstanceOf[Map[String, AnyRef]]
    val timing: Map[String, AnyRef] = job.getOrElse("timing", Map).asInstanceOf[Map[String, AnyRef]]

    HashMap[String, AnyRef](
      "job" -> HashMap[String, AnyRef](
        "id" -> job.getOrElse("id", "").toString,
        "status" -> job.getOrElse("status", "").toString.toUpperCase(),
        "submittedOn" -> formatter.format(new Date(timing.getOrElse("submitTime", "").toString.toLong*1000)),
        "lastModifiedOn" -> { if(StringUtils.isNotBlank(timing.getOrElse("finishTime", "").toString)){formatter.format(new Date(timing.getOrElse("finishTime", "").toString.toLong*1000))} else {""}},
        "error" -> {
          if (StringUtils.equalsIgnoreCase(job.getOrElse("status", "").toString.toUpperCase(), "ERROR")) {
            HashMap[String, String](
              "errorCode" -> job.getOrElse("errorCode", "").toString,
              "errorMessage" -> job.getOrElse("errorMessage", "").toString
            )
          }else{
            null
          }
        }
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
