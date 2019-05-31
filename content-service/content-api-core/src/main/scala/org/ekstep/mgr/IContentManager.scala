package org.ekstep.mgr

import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.mgr.BaseManager

trait IContentManager extends BaseManager {

  val TAXONOMY_ID: String = "domain"
  val CONTENT_OBJECT_TYPE: String = "Content"
  protected val CONTENT_CACHE_FINAL_STATUS = List("Live", "Unlisted")
  protected val reviewStatus =  List("Review", "FlagReview")
  protected var finalStatus = List("Flagged", "Live", "Unlisted")
  protected val CONTENT_CACHE_ENABLED = if (Platform.config.hasPath("content.cache.read")) Platform.config.getBoolean("content.cache.read")
  else false

  def read(request: org.ekstep.commons.Request) : Response
  def update(request: org.ekstep.commons.Request) : Response




}
