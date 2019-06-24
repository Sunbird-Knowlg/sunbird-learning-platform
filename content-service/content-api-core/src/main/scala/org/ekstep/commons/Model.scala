package org.ekstep.commons

import scala.collection.mutable

object Model {

}


case class Params(resmsgid: String, msgid: String, err: String, status: String, errmsg: String, client_key: Option[String] = None);
case class RequestBody(request: Map[String, AnyRef])
case class Request(apiId: String, body: Option[String], requestParams: Option[Map[String, AnyRef]], params: Option[Map[String, AnyRef]], context: Option[mutable.Map[String, AnyRef]]);
case class Response(id: String, ver: String, ts: String, params: Params, responseCode: String, result: Option[Map[String, AnyRef]]);


object ResponseCode extends Enumeration {
  type Code = Value
  val OK = Value(200)
  val CLIENT_ERROR = Value(400)
  val SERVER_ERROR = Value(500)
  val REQUEST_TIMEOUT = Value(500)
  val RESOURCE_NOT_FOUND = Value(404)
  val FORBIDDEN = Value(403)
}

object APIIds {

  val CHECK_HEALTH = "ekstep.learning.service.health"

  val CREATE_CONTENT = "ekstep.learning.content.create"
  val READ_CONTENT = "ekstep.content.find"
  val UPDATE_CONTENT = "ekstep.learning.content.update"
  val UPLOAD_CONTENT = "ekstep.learning.content.upload"
  val REVIEW_CONTENT = "ekstep.learning.content.review"
  val BUNDLE_CONTENT = "ekstep.learning.content.archive"
  val PUBLIC_PUBLISH_CONTENT = "ekstep.learning.content.publish"
  val UNLISTED_PUBLISH_CONTENT = "ekstep.learning.content.unlisted.publish"
  val COPY_CONTENT = "ekstep.content.copy"
  val RETIRE_CONTENT = "ekstep.content.retire"
  val ACCEPT_FLAG_CONTENT = "ekstep.content.accept.flag"
  val READ_HIERACHY = "ekstep.learning.content.hierarchy"
  val READ_HIERACHY_WITH_BOOKMARK = "ekstep.learning.content.hierarchy"
  val UPDATE_HIERARCHY = "content.hierarchy.update"
  val GET_PRESIGNED_URL = "ekstep.learning.content.upload.url"
  val CONTENT_HIERARCHY_SYNC = "content.hierarchy.sync"

  val DIALCODE_LINK = "ekstep.content.dialcode.link"
  val DIALCODE_COLLECTION_LINK = "ekstep.collection.dialcode.link"
  val DIALCODE_RESERVE = "ekstep.learning.content.dialcode.reserve"
  val DIALCODE_RELEASE = "ekstep.learning.content.dialcode.release"

}

object ContentErrorCodes extends Enumeration {
  type String = Value
  val ERR_CONTENT_BLANK_OBJECT, ERR_CONTENT_INVALID_UPLOAD_OBJECT,
  ERR_CONTENT_BLANK_OBJECT_ID, ERR_CONTENT_INVALID_OBJECT_TYPE, ERR_CONTENT_INVALID_PARAM,
  ERR_CONTENT_BLANK_UPLOAD_OBJECT, ERR_CONTENT_UPLOAD_NO_SUPPORT, ERR_CONTENT_UPLOAD_FILE,
  ERR_CONTENT_INVALID_SEARCH_CRITERIA, ERR_CONTENT_INVALID_BUNDLE_CRITERIA, ERR_CONTENT_NOT_FOUND,
  ERR_CONTENT_BLANK_ID, ERR_CONTENT_BLANK_UPLOAD_RESOURCE, ERR_CONTENT_BLANK_PUBLISHER,
  ERR_CONTENT_PUBLISH, ERR_CONTENT_MANIFEST_PARSE_ERROR, ERR_CONTENT_INVALID_PLUGIN_ID,
  ERR_CONTENT_MISSING_VERSION, ERR_CONTENT_EXTRACT, ERR_INVALID_RELATION_NAME,
  ERR_ECAR_BUNDLE_FAILED, ERR_CONTENT_SEARCH_ERROR, ERR_CONTENT_JSON_INVALID,
  ERR_CONTENT_BODY_INVALID, ERR_CONTENT_WP_JSON_PARSE_ERROR, ERR_CONTENT_WP_XML_PARSE_CONFIG_ERROR,
  ERR_CONTENT_WP_NOT_WELL_FORMED_XML, ERR_CONTENT_WP_XML_IO_ERROR, ERR_CONTENT_WP_OBJECT_CONVERSION,
  ERR_CONTENT_OPTIMIZE, INVALID_NODE, INVALID_EXTRACTION, INVALID_ECAR, INVALID_ARTIFACT, INVALID_FILE,
  EXTRACTION_ERROR, UPLOAD_DENIED, INVALID_SNAPSHOT, OPERATION_DENIED, INVALID_YOUTUBE_URL,
  MISSING_YOUTUBE_URL, MISSING_FILE, ERR_CONTENT_BLANK_FILE_NAME, ERR_CONTENT_CREATE,
  ERR_CONTENT_UPDATE, ERR_CHANNEL_NOT_FOUND, ERR_CATEGORY_NOT_FOUND, ERR_CATEGORY_INSTANCE_NOT_FOUND,
  ERR_FRAMEWORK_NOT_FOUND, ERR_CONTENT_INVALID_PUBLISH_CHECKLIST, ERR_CONTENT_COPY_ARTIFACT,
  CONTENTTYPE_ASSET_CAN_NOT_COPY, ERR_CONTENT_RETIRE, ERR_CONTENT_CONTENTTYPE, ERR_CONTENT_INVALID_CHANNEL,
  ERR_CHANNEL_BLANK_OBJECT, ERR_REQUEST_BLANK, ERR_INVALID_COUNT, ERR_INVALID_PUBLISHER, ERR_NOT_A_TEXTBOOK,
  ERR_NO_RESERVED_DIALCODES, ERR_NOT_A_CONTENT, ERR_ALL_DIALCODES_UTILIZED, ERR_INVALID_PRESIGNED_URL_TYPE, ERR_INVALID_INPUT, ERR_CONTENT_INVALID_FILE_NAME:String = Value
}

object TaxonomyAPIParams extends Enumeration {
  type TaxonomyAPIParams = Value
  val taxonomy, taxonomy_hierarchy, concepts, search_criteria, property_keys, unique_constraint, status, Flagged,
  FlagDraft, Live, Draft, isImageObject, node_id, Processing, body, languageCode, language, edit, identifier, content,
  mimeType, contentEncoding, contentDisposition, lastSubmittedOn, channel, Unlisted, Pending = Value
}

object ContentMetadata {

  object ContentDisposition extends Enumeration {
    type ContentDisposition = Value
    val inline, online, attachment = Value
  }

  object ContentEncoding extends Enumeration {
    type ContentEncoding = Value
    val gzip, identity = Value
  }

  object DialCodeEnum extends Enumeration {
    type String = Value
    val ERR_DIALCODE_LINK, ERR_DIALCODE_LINK_REQUEST, dialcodes, dialcode, identifier, count= Value
  }

}

