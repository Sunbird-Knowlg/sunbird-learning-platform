package org.sunbird.media.exception

/**
  *
  * @author gauraw
  */
class MediaServiceException(var errorCode: String = null, msg: String, ex: Exception = null) extends Exception(msg, ex)
