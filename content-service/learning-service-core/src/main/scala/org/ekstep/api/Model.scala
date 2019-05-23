package org.ekstep.api

object Model {

}


case class Params(resmsgid: String, msgid: String, err: String, status: String, errmsg: String, client_key: Option[String] = None);
case class RequestBody(id: String, ver: String, ts: String, request: Map[String, AnyRef], params: Option[Params]);

case class Request(apiId: String, body: Option[String], params: Option[Map[String, AnyRef]]);
case class Response(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]);


object ResponseCode extends Enumeration {
  type Code = Value
  val OK = Value(200)
  val CLIENT_ERROR = Value(400)
  val SERVER_ERROR = Value(500)
  val REQUEST_TIMEOUT = Value(500)
  val RESOURCE_NOT_FOUND = Value(404)
}


object RespCode extends Enumeration {
  type Code = Value
  val OK, CLIENT_ERROR, SERVER_ERROR, REQUEST_TIMEOUT, RESOURCE_NOT_FOUND, FORBIDDEN = Value
}

object APIIds {
  val ES_READ = "org.ekstep.es.read"
  val READ_CONTENT = "org.ekstep.content.read"
  val READ_FRAMEWORK = "org.ekstep.framework.read"
  val READ_CHANNEL = "org.ekstep.channel.read"
  val SEARCH_DIALCODE = "org.ekstep.dialcode.search"
  val CHECK_HEALTH = "learning-service.health"

}