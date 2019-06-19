package org.ekstep.content.util

import com.mashape.unirest.http.{HttpResponse, Unirest}
import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.exception.ServerException
import org.ekstep.commons
import org.ekstep.commons.{Response, ResponseCode}
import org.ekstep.telemetry.logger.TelemetryManager

import scala.collection.JavaConverters._

/**
  * Utility Object to make REST API Calls
  *
  * @author Kumar Gauraw
  */
object HttpRestUtil {

    val EKSTEP_PLATFORM_API_USERID = "System"
    val DEFAULT_CONTENT_TYPE = "application/json"
    val EKSTEP_API_AUTHORIZATION_KEY = "Bearer " + {
        if (Platform.config.hasPath("dialcode.api.authorization")) Platform.config.getString("dialcode.api.authorization") else ""
    }

    Unirest.setDefaultHeader("Content-Type", DEFAULT_CONTENT_TYPE)
    Unirest.setDefaultHeader("Authorization", EKSTEP_API_AUTHORIZATION_KEY)
    Unirest.setDefaultHeader("user-id", EKSTEP_PLATFORM_API_USERID)

    /**
      *
      * @param uri
      * @param requestMap
      * @param headerParam
      * @return
      */
    def post(uri: String, requestMap: Map[String, AnyRef], headerParam: Map[String, String]): Response = {
        TelemetryManager.log("HttpRestUtil:post |  Request Url:" + uri)
        TelemetryManager.log("HttpRestUtil:post |  Request Body:" + requestMap)
        if (null == headerParam) throw new ServerException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
        if (null == requestMap) throw new ServerException("ERR_INVALID_REQUEST_BODY", "Request Body is Manadatory")
        try {
            //TODO: Check replacement of toJson with JSONUtils or mkString
            val response: HttpResponse[String] = Unirest.post(uri).headers(headerParam.asJava).body(toJson(requestMap)).asString()
            getResponse(response)
        } catch {
            case e: Exception =>
                TelemetryManager.log("HttpRestUtil:post |  Error is :" + e)
                throw new ServerException("ERR_CALL_API", "Something Went Wrong While Making API Call | Error is: " + e.getMessage)
        }
    }

    /**
      *
      * @param urlWithIdentifier
      * @param queryParam
      * @param headerParam
      * @return
      */
    def get(urlWithIdentifier: String, queryParam: String, headerParam: Map[String, String]): Response = {
        if (null == headerParam) throw new ServerException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
        val req = if (null != queryParam) urlWithIdentifier + queryParam else urlWithIdentifier
        try {
            val response = Unirest.get(req).headers(headerParam.asJava).asString
            getResponse(response)
        } catch {
            case e: Exception =>
                TelemetryManager.log("HttpRestUtil:get |  Error is :" + e)
                throw new ServerException("ERR_CALL_API", "Something Went Wrong While Making API Call | Error is: " + e.getMessage)
        }
    }

    /**
      *
      * @param response
      * @return
      */
    def getResponse(response: HttpResponse[String]): commons.Response = {
        try {
            val resp: Response = {
                if (StringUtils.isNotBlank(response.getBody))
                    JSONUtils.deserialize[Response](response.getBody)
                else Response(null, null, null, null, ResponseCode.SERVER_ERROR.toString, Some(Map()))
            }
            resp
        } catch {
            case e: Exception =>
                TelemetryManager.info("HttpRestUtil | Error Occurred While Parsing Response Body :" + e)
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name, e.getMessage)
        }
    }

    /**
      *
      * @param query
      * @return
      */
    def toJson(query: Any): String = query match {
        case m: Map[String, Any] => s"{${m.map(toJson(_)).mkString(",")}}"
        case t: (String, Any) => s""""${t._1}":${toJson(t._2)}"""
        case ss: Seq[Any] => s"""[${ss.map(toJson(_)).mkString(",")}]"""
        case s: String => s""""$s""""
        case null => "null"
        case _ => query.toString
    }

}
