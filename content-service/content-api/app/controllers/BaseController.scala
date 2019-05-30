package controllers

import akka.util.Timeout
import akka.util.Timeout.durationToTimeout
import com.typesafe.config.Config
import play.api.mvc.{Controller, Result}
import org.ekstep.common.dto.Response
import org.ekstep.common.exception.ResponseCode
import org.ekstep.util.JSONUtils

import scala.concurrent.duration.DurationInt

/**
  * @author mahesh
  */



abstract class BaseController extends Controller {
  implicit val timeout: Timeout = 30 seconds;
  implicit val config: Config = play.Play.application.configuration.underlying();

  protected def sendResponse(response: Response): Result = {
    val res = JSONUtils.serialize(response)
    val result = response.getResponseCode match {
      case ResponseCode.OK => Ok(res)
      case ResponseCode.CLIENT_ERROR => BadRequest(res)
      case ResponseCode.RESOURCE_NOT_FOUND => NotFound(res)
      case ResponseCode.SERVER_ERROR => ServiceUnavailable(res)
      case _ =>  ServiceUnavailable(res)
    }

    result.withHeaders(CONTENT_TYPE -> "application/json")
  }


}