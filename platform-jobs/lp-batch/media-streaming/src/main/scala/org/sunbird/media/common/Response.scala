package org.sunbird.media.common

import java.util.UUID

import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  *
  */
object Response {

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
}
