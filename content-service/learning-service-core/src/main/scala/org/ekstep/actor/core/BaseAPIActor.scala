package org.ekstep.actor.core

import java.util.UUID

import akka.actor.UntypedActor
import org.ekstep.commons._
import org.ekstep.common.dto.ResponseParams
import org.ekstep.common.dto.ResponseParams.StatusType
import org.ekstep.common.exception.ResponseCode
import org.ekstep.util.JSONUtils
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, DateTimeZone}


/**
  * @author pradyumna
  */

abstract class BaseAPIActor extends UntypedActor {


  @transient val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").withZoneUTC()
  val API_VERSION = "3.0"

  @throws(classOf[Exception])
  def onReceive(request: org.ekstep.commons.Request)

  override def onReceive(message: Any): Unit = {
    val request = message.asInstanceOf[org.ekstep.commons.Request]
    try {
      onReceive(request)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        val response = errorResponseSerialized(request.apiId, ResponseCode.SERVER_ERROR.toString, "Something went wrong while processing request.", ResponseCode.SERVER_ERROR)
        sender() ! response
    }
  }

  @throws(classOf[Exception])
  def getRequestBody(message: Request): RequestBody = {
    JSONUtils.deserialize[RequestBody](message.body.getOrElse("{}"))
  }

  private def errorResponse(apiId: String, err: String, errMsg: String, responseCode: String): Response = {
    Response(apiId, API_VERSION, df.print(System.currentTimeMillis()),
      Params(UUID.randomUUID().toString, null, err, "failed", errMsg),
      responseCode, None)
  }

  def errorResponseSerialized(apiId: String, err: String, errMsg: String, responseCode: ResponseCode): org.ekstep.common.dto.Response = {
    new org.ekstep.common.dto.Response() {
      setId(apiId)
      setVer(API_VERSION)
      setTs(df.print(DateTime.now(DateTimeZone.UTC).getMillis))
      setResponseCode(responseCode)
      setParams(new ResponseParams() {
        setErr(err)
        setStatus(StatusType.failed.name)
        setErrmsg(errMsg)
      })
    }

  }

  def OK(apiId: String, response: org.ekstep.common.dto.Response): org.ekstep.common.dto.Response = {
    response.setId(apiId)
    response.setParams(new ResponseParams() {
      setErr("0")
      setStatus(StatusType.successful.name)
      setErrmsg("Operation successful")
    })

    response.setResponseCode(ResponseCode.OK)
    response.setTs(df.print(DateTime.now(DateTimeZone.UTC).getMillis))
    response.setVer(API_VERSION)

    response
  }

  def invalidAPIResponseSerialized(apiId: String): String = {
    JSONUtils.serialize(errorResponse(apiId, "INVALID_API_ID", "Invalid API id.", ResponseCode.SERVER_ERROR.toString))
  }

}
