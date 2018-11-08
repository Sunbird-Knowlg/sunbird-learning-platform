package org.sunbird.media.util

import scala.io.Source
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.ekstep.analytics.framework.Response
import com.fasterxml.jackson.core.JsonParseException
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpRequestBase
import org.sunbird.media.util.JSONUtils

/**
  * @author Santhosh
  */
object RestUtil {

  implicit val className = "org.ekstep.analytics.framework.util.RestUtil"

  private def _call[T](request: HttpRequestBase)(implicit mf: Manifest[T]) = {

    val httpClient = HttpClients.createDefault();
    try {
      val httpResponse = httpClient.execute(request);
      val entity = httpResponse.getEntity()
      val inputStream = entity.getContent()
      val content = Source.fromInputStream(inputStream, "UTF-8").getLines.mkString;
      inputStream.close
      if ("java.lang.String".equals(mf.toString())) {
        content.asInstanceOf[T];
      } else {
        JSONUtils.deserialize[T](content);
      }
    } finally {
      httpClient.close()
    }
  }

  def get[T](apiURL: String, headers: Map[String, AnyRef])(implicit mf: Manifest[T]) = {
    val request = new HttpGet(apiURL);
    headers.foreach(p => request.addHeader(p._1, p._2))
    try {
      _call(request.asInstanceOf[HttpRequestBase]);
    } catch {
      case ex: Exception =>
        JobLogger.log(ex.getMessage, Option(Map("url" -> apiURL)), ERROR)
        ex.printStackTrace();
        null.asInstanceOf[T];
    }
  }

  def post[T](apiURL: String, headers: Map[String, AnyRef], body: String)(implicit mf: Manifest[T]) = {

    val request = new HttpPost(apiURL);
    headers.foreach(p => request.addHeader(p._1, p._2))
    request.addHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(body));
    try {
      _call(request.asInstanceOf[HttpRequestBase]);
    } catch {
      case ex: Exception =>
        JobLogger.log(ex.getMessage, Option(Map("url" -> apiURL, "body" -> body)), ERROR)
        ex.printStackTrace();
        null.asInstanceOf[T];
    }
  }

  def patch[T](apiURL: String, headers: Map[String, AnyRef], body: String)(implicit mf: Manifest[T]) = {

    val request = new HttpPatch(apiURL);
    headers.foreach(p => request.addHeader(p._1, p._2))
    request.addHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(body));
    try {
      _call(request.asInstanceOf[HttpRequestBase]);
    } catch {
      case ex: Exception =>
        JobLogger.log(ex.getMessage, Option(Map("url" -> apiURL, "body" -> body)), ERROR)
        ex.printStackTrace();
        null.asInstanceOf[T];
    }
  }

}