package org.sunbird.media.util

import com.mashape.unirest.http.{HttpResponse, Unirest}
import org.sunbird.media.common.{MediaResponse, Response}
import org.sunbird.media.exception.MediaServiceException
import org.apache.commons.lang.StringUtils
import org.json4s.jackson.JsonMethods
import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  */
object HttpRestUtil {

  def get(url: String, headers: Map[String, String], queryParam: Map[String, String]): MediaResponse = {
    if (null == headers) throw new MediaServiceException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
    try {
      getResponse(Unirest.get(url).headers(headers.asJava).asString)
    } catch {
      case e: Exception => Response.getFailureResponse(new HashMap[String, AnyRef], "SERVER_ERROR", "Some Error Occurred While Calling Cloud Service API's.  ")
    }
  }

  def post(url: String, headers: Map[String, String], requestBody: String): MediaResponse = {
    if (null == headers) throw new MediaServiceException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
    if (StringUtils.isBlank(requestBody)) throw new MediaServiceException("ERR_INVALID_REQUEST_BODY", "Request body is Mandatory.")
    try {
      getResponse(Unirest.post(url).headers(headers.asJava).body(requestBody).asString)
    } catch {
      case e: Exception => Response.getFailureResponse(new HashMap[String, AnyRef], "SERVER_ERROR", "Some Error Occurred While Calling Cloud Service API's.  ")
    }
  }

  def post(url: String, headers: Map[String, String], requestBody: Map[String, AnyRef]): MediaResponse = {
    if (null == headers) throw new MediaServiceException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
    try {
      getResponse(Unirest.post(url).headers(headers.asJava).fields(requestBody.asJava).asString())
    } catch {
      case e: Exception => {
        e.printStackTrace()
        Response.getFailureResponse(new HashMap[String, AnyRef], "SERVER_ERROR", "Some Error Occurred While Calling Cloud Service API's.  ")
      }
    }
  }

  def put(url: String, headers: Map[String, String], requestBody: String): MediaResponse = {
    if (null == headers) throw new MediaServiceException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
    if (StringUtils.isBlank(requestBody)) throw new MediaServiceException("ERR_INVALID_REQUEST_BODY", "Request body is Mandatory.")
    try {
      getResponse(Unirest.put(url).headers(headers.asJava).body(requestBody).asString)
    } catch {
      case e: Exception => Response.getFailureResponse(new HashMap[String, AnyRef], "SERVER_ERROR", "Some Error Occurred While Calling Cloud Service API's.  ")
    }
  }

  def delete(url: String, headers: Map[String, String]): MediaResponse = {
    if (null == headers) throw new MediaServiceException("ERR_INVALID_HEADER_PARAM", "Header Parameter is Mandatory.")
    try {
      getResponse(Unirest.delete(url).headers(headers.asJava).asString)
    } catch {
      case e: Exception => Response.getFailureResponse(new HashMap[String, AnyRef], "SERVER_ERROR", "Some Error Occurred While Calling Cloud Service API's.  ")
    }
  }

  def getResponse(response: HttpResponse[String]): MediaResponse = {
    val status = response.getStatus
    var result: Map[String, AnyRef] = new HashMap[String, AnyRef]

    try {
      val body = response.getBody()
      if (StringUtils.isNotBlank(body))
        result = JsonMethods.parse(body).values.asInstanceOf[scala.collection.immutable.Map[String, AnyRef]]
    } catch {
      case e: UnsupportedOperationException => e.printStackTrace()
      case e: Exception => e.printStackTrace()
    }

    status match {
      case 200 => Response.getSuccessResponse(result)
      case 201 => Response.getSuccessResponse(result)
      case 400 => Response.getFailureResponse(result, "BAD_REQUEST", "Please Provide Correct Request Data.")
      case 401 => Response.getFailureResponse(result, "SERVER_ERROR", "Access Token Expired.")
      case 404 => Response.getFailureResponse(result, "RESOURCE_NOT_FOUND", "Resource Not Found.")
      case 405 => Response.getFailureResponse(result, "METHOD_NOT_ALLOWED", "Requested Operation Not Allowed.")
      case 500 => Response.getFailureResponse(result, "SERVER_ERROR", "Internal Server Error. Please Try Again Later!")
      case _ => Response.getFailureResponse(result, "SERVER_ERROR", "Internal Server Error. Please Try Again Later!")
    }

  }

}