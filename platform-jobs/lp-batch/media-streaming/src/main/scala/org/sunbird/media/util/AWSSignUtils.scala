package org.sunbird.media.util

import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang3.StringUtils
import org.sunbird.media.config.AppConfig

import scala.util.parsing.json.JSONObject

object AWSSignUtils {

  val secret = AppConfig.getSystemConfig("aws.token.access_secret")
  val region = AppConfig.getConfig("aws.region")
  val service = AppConfig.getConfig("aws.service.name")


  def getSiginingkey() : String = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd")
    val date = dateFormat.format(new Date()).getBytes("UTF8")
    HmacSHA256(HmacSHA256(HmacSHA256(HmacSHA256("AWS4" + secret, date), region.getBytes("UTF8")), service.getBytes("UTF8")), "aws4_request".getBytes("UTF8"))
  }


  def getStringToSign(httpMethod: String, url: String, headers:Map[String, String], payload : Map[String, AnyRef]) : String = {
    val canonicalUri = getCanonicalUri(url)
    val canonicalQueryString = getCanonicalQueryString(url)
    val hashedPayload = getHashedPayload(payload)
    val canonicalHeaders = getCanonicalHeaders(headers, hashedPayload)
    val signedHeaders = getSignedHeaders(headers.keySet)


    val canonicalRequest = httpMethod + "\n" + canonicalUri + "\n" + canonicalQueryString +"\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedPayload

    val timeStampISO8601Format = new SimpleDateFormat("yyyyMMddThhmmssZ").format(new Date())
    val scope = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + region + "/" + service + "/aws4_request"

    "AWS4-HMAC-SHA256" + "\n" + timeStampISO8601Format + "\n" + scope + "\n" + Hex.encodeHex(sha256Hash(canonicalRequest).getBytes("UTF8")).toString
  }

  def generateToken(httpMethod: String, url: String, headers:Map[String, String], payload : Map[String, AnyRef]) : String = {
    val signature: String = Hex.encodeHex(HmacSHA256(getStringToSign(httpMethod, url, headers, payload), getSiginingkey().getBytes("UTF8")).getBytes("UTF8"), true).toString
    "AWS4-HMAC-SHA256 Credential=" + "/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + region + "/" + service + "/aws4_request,SignedHeaders=" + getSignedHeaders(headers.keySet) + ",Signature=" +signature
  }


  @throws[Exception]
  def HmacSHA256(data: String, key: Array[Byte]) = {
    val algorithm : String = "HmacSHA256"
    val mac :Mac = Mac.getInstance(algorithm)
    mac.init(new SecretKeySpec(key, algorithm))
    mac.doFinal(data.getBytes("UTF8")).toString
  }


  def uriEncode(input: Array[Char], encodeSlash: Boolean): String = {
    val result: StringBuilder = new StringBuilder()
    var i: Int = 0
    for( i <- 0 to input.length()) {
      val ch = input.charAt(i)
      if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-' || ch == '~' || ch == '.') {
        result.append(ch);
      } else if (ch == '/') {
        if(encodeSlash) result.append("%2F") else result.append(ch)
      } else {
        result.append(Integer.toHexString((ch.asInstanceOf[Integer])));
      }
    }

    result.toString()
  }

  def sha256Hash(input: String): String = {
    String.format("%032x", new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(input.getBytes("UTF-8"))))
  }


  def getCanonicalUri(url: String): String = {
    val uri = url.split(AppConfig.getConfig("aws.version"))(1)
    if(StringUtils.isBlank(uri)) "" else uri
  }

  def getCanonicalQueryString(url: String): String = {
    val queryString: String = url.split("\\?")(1)
    var result:String = ""
    if(StringUtils.isNotBlank(queryString)){
      for(param <- queryString.split("\\&")){
        if(param.split("=").length == 2)
          result += uriEncode(param.split("=")(0).toCharArray, false) + "=" +  uriEncode(param.split("=")(1).toCharArray, false) + "&"
        else
          result += uriEncode(param.split("=")(0).toCharArray, false) + "&"
      }
      result = result.substring(0, result.length - 2)
    }
    result
  }

  def getCanonicalHeaders(headers: Map[String, String], hashedPayload: String): String = {
    var result:String = ""
    headers.foreach(header => {
      result += header._1.toLowerCase + ":" + header._2.trim + "\n"
    })

    result += "x-amz-content-sha256" + ":" + hashedPayload.trim
    result
  }

  def getSignedHeaders(keySet: Set[String]) :String = {
    var result :String = ""
    keySet.foreach(key => {
      result += key + ";"
    })

    result += "x-amz-content-sha256"
    result
  }


  def getHashedPayload(payload: Map[String, AnyRef]) : String = {
    if(null != payload && !payload.isEmpty) {
      Hex.encodeHex(sha256Hash(JSONObject(payload).toString()).getBytes("UTF8")).toString
    }else {
      Hex.encodeHex(sha256Hash("").getBytes("UTF8")).toString
    }
  }
}