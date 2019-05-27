package org.ekstep.util

import java.util.UUID

import org.ekstep.commons.{Params, Response, ResponseCode,RespCode}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, DateTimeZone}

object CommonUtil {
  @transient val df: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").withZoneUTC();

  def errorResponseSerialized(apiId: String, err: String, responseCode: String): String = {
    JSONUtils.serialize(errorResponse(apiId, err, responseCode))
  }

  def errorResponse(apiId: String, err: String, responseCode: String): Response = {
    Response(apiId, "1.0", df.print(System.currentTimeMillis()),
      Params(UUID.randomUUID().toString, null, responseCode, "failed", err),
      responseCode, None)
  }

  def OK(apiId: String, result: Map[String, AnyRef]): Response = {
    Response(apiId, "1.0", df.print(DateTime.now(DateTimeZone.UTC).getMillis), Params(UUID.randomUUID().toString(), null, null, "successful", null), RespCode.OK.toString, Option(result));
  }
}
