package org.ekstep.commons

import org.ekstep.common.Platform
import com.fasterxml.jackson.databind.ObjectMapper

object Constants {


  val ERR_INVALID_REQUEST: String = "ERR_INVALID_REQUEST"
  val EDIT_MODE: String = "edit"

  /**
    * Request parsing params key
    */
  val MODE: String = "mode"
  val IDENTIFIER: String = "identifier"
  val OBJECT_TYPE: String = "objectType"
  val FIELDS: String = "fields"



  /**
    * The Default 'ContentImage' Object Suffix (Content_Object_Identifier +
    * ".img")
    */
  val DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img"

  val DEFAULT_MIME_TYPE = "assets"

  /** The Disk Location where the operations on file will take place. */
  val tempFileLocation = "/data/contentBundle/"

  /** The Default Manifest Version */
  val DEFAULT_CONTENT_MANIFEST_VERSION = "1.2"

  /**
    * Content Image Object Type
    */
  val CONTENT_IMAGE_OBJECT_TYPE = "ContentImage"

  /**
    * Content Object Type
    */
  val CONTENT_OBJECT_TYPE = "Content"

  val TAXONOMY_ID = "domain"


  val DIALCODE_GENERATE_URI: String = if (Platform.config.hasPath("dialcode.api.generate.url")) Platform.config.getString("dialcode.api.generate.url")
  else "http://localhost:8080/learning-service/v3/dialcode/generate"
  val COLLECTION_MIME_TYPE = "application/vnd.ekstep.content-collection"

  val DEFAULT_CONTENT_VERSION = 1
  val DEFAULT_COLLECTION_VERSION = 2

  val CONTENT_CACHE_TTL: Integer = if (Platform.config.hasPath("content.cache.ttl")) Platform.config.getInt("content.cache.ttl")
  else 259200

  val COLLECTION_CACHE_KEY_PREFIX = "hierarchy_"

  val LATEST_CONTENT_VERSION = 2

}
