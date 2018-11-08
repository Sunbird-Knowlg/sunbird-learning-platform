package org.sunbird.media.common

import scala.collection.immutable.HashMap

/**
  *
  * @author gauraw
  */
object Model {

}

case class MediaRequest(id: String, params: Map[String, AnyRef] = new HashMap[String, AnyRef], request: Map[String, AnyRef] = new HashMap[String, AnyRef]);

case class MediaResponse(id: String, ts: String, params: Map[String, Any] = new HashMap[String, AnyRef], responseCode: String, result: Map[String, AnyRef] = new HashMap[String, AnyRef])

object ResponseCode extends Enumeration {
  type Code = Value
  val OK = Value(200)
  val CLIENT_ERROR = Value(400)
  val SERVER_ERROR = Value(500)
  val RESOURCE_NOT_FOUND = Value(404)
}
