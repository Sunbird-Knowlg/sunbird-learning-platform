package controllers

import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.{HeaderParam, Response}
import org.ekstep.common.exception.ResponseCode
import org.ekstep.content.util.JSONUtils
import org.ekstep.telemetry.TelemetryParams
import play.api.mvc.{AnyContent, Controller, Request, Result}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

abstract class BaseController extends Controller {
  implicit val timeout: Timeout = 30 seconds;
  implicit val config: Config = play.Play.application.configuration.underlying();

  protected def sendResponse(response: Response): Result = {
    val res = JSONUtils.serialize(response)
    val result = response.getResponseCode match {
      case ResponseCode.OK => Ok(res)
      case ResponseCode.CLIENT_ERROR => BadRequest(res)
      case ResponseCode.RESOURCE_NOT_FOUND => NotFound(res)
      case ResponseCode.PARTIAL_SUCCESS => PartialContent(res)
      case ResponseCode.SERVER_ERROR => InternalServerError(res)
        // TODO: Put Exact Match for Service Unavailable.
      case _ => ServiceUnavailable(res)
    }

    result.withHeaders(CONTENT_TYPE -> "application/json")
  }

  /**
    *
    * @param httpRequest
    * @param request
    */
  protected def getContext(httpRequest: Request[AnyContent]): mutable.Map[String, AnyRef] = {
    val reqContext = new mutable.HashMap[String, AnyRef]
    val sessionId = httpRequest.headers.get("X-Session-ID").getOrElse("")
    val consumerId = httpRequest.headers.get("X-Consumer-ID").getOrElse("")
    val deviceId = httpRequest.headers.get("X-Device-ID").getOrElse("")
    //TODO: Check the usase.
    val authUserId = httpRequest.headers.get("X-Authenticated-User-Token").getOrElse("")
    val channelId = httpRequest.headers.get("X-Channel-ID").getOrElse("")
    val appId = httpRequest.headers.get("X-App-Id").getOrElse("")

    if (StringUtils.isNotBlank(sessionId)) reqContext.put("SESSION_ID", sessionId)
    if (StringUtils.isNotBlank(consumerId)) reqContext.put(HeaderParam.CONSUMER_ID.name, consumerId)
    if (StringUtils.isNotBlank(deviceId)) reqContext.put(HeaderParam.DEVICE_ID.name, deviceId)
    if (StringUtils.isNotBlank(channelId)) reqContext.put(HeaderParam.CHANNEL_ID.name, channelId)
    else reqContext.put(HeaderParam.CHANNEL_ID.name, Platform.config.getString("channel.default"))
    if (StringUtils.isNotBlank(appId)) reqContext.put(HeaderParam.APP_ID.name, appId)
    reqContext.put(TelemetryParams.ENV.name, "learning-service")
    //TODO: Validate this once.
    if (null != reqContext.get(HeaderParam.CONSUMER_ID.name)) reqContext.put(TelemetryParams.ACTOR.name, consumerId)

    return reqContext
  }
}