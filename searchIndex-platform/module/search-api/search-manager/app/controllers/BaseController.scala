package controllers

import java.util

import org.apache.commons.lang3.StringUtils
import org.sunbird.common.dto.{HeaderParam, Request, RequestParams}
import org.sunbird.telemetry.logger.TelemetryManager
import play.api.mvc._
import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import org.sunbird.common.Platform
import org.sunbird.common.exception.ResponseCode
import org.sunbird.telemetry.TelemetryParams

import scala.concurrent.{ExecutionContext, Future}

class BaseController(protected val cc: ControllerComponents)(implicit exec: ExecutionContext) extends AbstractController(cc) {

    private val API_ID_PREFIX = "ekstep"
    val xHeaderNames = Map("x-session-id" -> "SESSION_ID", "X-Consumer-ID" -> HeaderParam.CHANNEL_ID.name, "x-device-id" -> HeaderParam.DEVICE_ID.name, "x-app-id" -> HeaderParam.APP_ID.name, "x-authenticated-userid" -> HeaderParam.CONSUMER_ID.name, "x-channel-id" -> HeaderParam.CHANNEL_ID.name)
    private val mapper = new ObjectMapper
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @throws(classOf[Exception])
    def deserialize[T: Manifest](value: String): T = mapper.readValue(value, typeReference[T]);

    private[this] def typeReference[T: Manifest] = new TypeReference[T] {
        override def getType = typeFromManifest(manifest[T])
    }

    private[this] def typeFromManifest(m: Manifest[_]): Type = {
        if (m.typeArguments.isEmpty) { m.runtimeClass }
        // $COVERAGE-OFF$Disabling scoverage as this code is impossible to test
        else new ParameterizedType {
            def getRawType = m.runtimeClass
            def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
            def getOwnerType = null
        }
        // $COVERAGE-ON$
    }

    protected def getResult(response: Future[org.sunbird.common.dto.Response]) = {
        response.map(x => {
            val xStr = mapper.writeValueAsString(x)
            x.getResponseCode match {
                case ResponseCode.OK => play.api.mvc.Results.Ok(xStr).as("application/json")
                case ResponseCode.CLIENT_ERROR => play.api.mvc.Results.BadRequest(xStr).as("application/json")
                case ResponseCode.RESOURCE_NOT_FOUND => play.api.mvc.Results.NotFound(xStr).as("application/json")
                case _ => play.api.mvc.Results.InternalServerError(xStr).as("application/json")
            }
        })
    }

    protected def getAPIId(apiId: String): String = API_ID_PREFIX + "." + apiId

    protected def getAPIVersion(path: String): String = {
        var version = "3.0"
        if (path.contains("/v2") || path.contains("/search-service")) version = "2.0"
        else if (path.contains("/v3")) version = "3.0"
        version
    }

    def requestBody()(implicit request: play.api.mvc.Request[AnyContent]) = {
        val body = request.body.asJson.getOrElse("{}").toString
        deserialize[java.util.Map[String, Object]](body)
    }

    protected def getRequest(apiId: String)(implicit playRequest: play.api.mvc.Request[AnyContent]): Request = {
        TelemetryManager.log(apiId)
        val request: Request = new Request
        if (null != playRequest) {

            val requestMap: util.Map[String, AnyRef] = requestBody()
            if (null != requestMap && !requestMap.isEmpty) {
                val id: String = if (requestMap.get("id") == null || StringUtils.isBlank(requestMap.get("id").asInstanceOf[String])) getAPIId(apiId)
                else requestMap.get("id").asInstanceOf[String]
                val ver: String = if (requestMap.get("ver") == null || StringUtils.isBlank(requestMap.get("ver").asInstanceOf[String])) getAPIVersion(playRequest.uri)
                else requestMap.get("ver").asInstanceOf[String]
                val ts: String = requestMap.get("ts").asInstanceOf[String]
                request.setId(id)
                request.setVer(ver)
                request.setTs(ts)
                val reqParams: AnyRef = requestMap.get("params")
                if (null != reqParams) try {
                    val params: RequestParams = mapper.convertValue(reqParams, classOf[RequestParams]).asInstanceOf[RequestParams]
                    request.setParams(params)
                } catch {
                    case e: Exception =>
                        e.printStackTrace()
                }
                val requestObj: AnyRef = requestMap.get("request")
                if (null != requestObj) try {
                    val strRequest: String = mapper.writeValueAsString(requestObj)
                    val map: util.Map[String, AnyRef] = deserialize[java.util.Map[String, Object]](strRequest)
                    if (null != map && !map.isEmpty) request.setRequest(map)
                } catch {
                    case e: Exception =>
                        e.printStackTrace()
                }
            }
            else {
                request.setId(getAPIId(apiId))
                request.setVer(getAPIVersion(playRequest.uri))
            }
        }
        else {
            request.setId(apiId)
            request.setVer(getAPIVersion(playRequest.uri))
        }
        request
    }

    protected def setHeaderContext(searchRequest: Request)(implicit playRequest: play.api.mvc.Request[AnyContent]) : Unit = {

        val headers = playRequest.headers.headers.groupBy(_._1).mapValues(_.map(_._2))
        val appHeaders = headers.filter(header => xHeaderNames.keySet.contains(header._1.toLowerCase))
            .map(entry => (xHeaderNames.get(entry._1.toLowerCase()).get, entry._2.head))
        searchRequest.getContext.put(TelemetryParams.ENV.name, "search")
        appHeaders.map(entry => {
            searchRequest.getContext.put(entry._1, entry._2)
        })
        if (StringUtils.isBlank(searchRequest.getContext.getOrDefault(HeaderParam.CHANNEL_ID.name, "").asInstanceOf[String])) {
            searchRequest.getContext.put(HeaderParam.CHANNEL_ID.name, Platform.config.getString("channel.default"))
        }

        if (null != searchRequest.getContext.get(HeaderParam.CONSUMER_ID.name)) searchRequest.put(TelemetryParams.ACTOR.name, searchRequest.getContext.get(HeaderParam.CONSUMER_ID.name))
        else if (null != searchRequest && null != searchRequest.getParams.getCid) searchRequest.put(TelemetryParams.ACTOR.name, searchRequest.getParams.getCid)
        else searchRequest.put(TelemetryParams.ACTOR.name, "learning.platform")
    }
}
