package org.sunbird.media.util

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.sunbird.media.config.AppConfig

import scala.util.parsing.json.JSONObject

object AWSSignUtils {

  val accessKey = AppConfig.getSystemConfig("aws.token.access_key")
  val secret = AppConfig.getSystemConfig("aws.token.access_secret")
  val region = AppConfig.getConfig("aws.region")
  val service = AppConfig.getConfig("aws.service.name")
  val version = AppConfig.getConfig("aws.api.version")

  val dateFormat = new SimpleDateFormat("yyyyMMdd")
  dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))


  def getSiginingkey() : Array[Byte] = {
    val date = dateFormat.format(new Date()).getBytes("UTF8")
    val kSecret = ("AWS4" + secret).getBytes("UTF8");
    val kDate = HmacSHA256(date, kSecret);
    val kRegion = HmacSHA256(region.getBytes("UTF8"), kDate);
    val kService = HmacSHA256(service.getBytes("UTF8"), kRegion);
    val kSigning = HmacSHA256("aws4_request".getBytes("UTF8"), kService);

    kSigning
  }


  def getStringToSign(httpMethod: String, url: String, headers:Map[String, String], payload : String) : String = {
    val canonicalUri = getCanonicalUri(url)
    val canonicalQueryString = getCanonicalQueryString(url)
    val hashedPayload = getHashedPayload(payload)
    val canonicalHeaders = getCanonicalHeaders(headers, hashedPayload)
    val signedHeaders = getSignedHeaders(headers.keySet)

    val canonicalRequest = httpMethod + "\n" + canonicalUri + "\n" + canonicalQueryString +"\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedPayload

    val timeStampISO8601Format = headers.get("x-amz-date").get
    val scope = dateFormat.format(new Date()) + "/" + region + "/" + service + "/aws4_request"

    val stringToSign = "AWS4-HMAC-SHA256" + "\n" + timeStampISO8601Format + "\n" + scope + "\n" + sha256Hash(canonicalRequest)

    stringToSign
  }

  def generateToken(httpMethod: String, url: String, headers: Map[String, String], payload : String) : String = {
    val signature = new String(Hex.encodeHex(HmacSHA256(getStringToSign(httpMethod, url, headers, payload).getBytes("UTF-8"), getSiginingkey())))

    "AWS4-HMAC-SHA256 Credential=" + accessKey +  "/" + dateFormat.format(new Date()) + "/" + region + "/" + service + "/aws4_request,SignedHeaders=" + getSignedHeaders(headers.keySet) + ",Signature=" + signature
  }


  @throws[Exception]
  def HmacSHA256(data: Array[Byte], key: Array[Byte]) = {
    val algorithm : String = "HMacSha256"
    val mac :Mac = Mac.getInstance(algorithm)
    mac.init(new SecretKeySpec(key, algorithm))
    mac.doFinal(data)
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

    DigestUtils.sha256Hex(input)
  }


  def getCanonicalUri(url: String): String = {
    var uri = url.split(version)(1)
    if(StringUtils.isBlank(uri)) "" else{uri = "/" + version + uri; uri}
  }

  def getCanonicalQueryString(url: String): String = {
    var result:String = ""
    if(url.split("\\?").length > 1){
      val queryString: String = url.split("\\?")(1)
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
    }else {
      result
    }
  }

  def getCanonicalHeaders(headers: Map[String, String], hashedPayload: String): String = {
    var result:String = ""
    headers.foreach(header => {
      result += header._1.toLowerCase + ":" + header._2.trim + "\n"
    })

    //result += "x-amz-content-sha256" + ":" //+ hashedPayload.trim
    result
  }

  def getSignedHeaders(keySet: scala.collection.Set[String]) :String = {
    var result :String = ""
    keySet.foreach(key => {
      result += key.toLowerCase + ";"
    })

    //result += "x-amz-content-sha256"
    result.substring(0, result.length - 1)
  }


  def getHashedPayload(payload: String) : String = {
    if(StringUtils.isNotBlank(payload)) {
      sha256Hash(payload)
    }else {
      sha256Hash("")
    }
  }


  def stringtoHex(str: String) : String = {
    DatatypeConverter.printHexBinary(str.getBytes("UTF8"))
  }

}