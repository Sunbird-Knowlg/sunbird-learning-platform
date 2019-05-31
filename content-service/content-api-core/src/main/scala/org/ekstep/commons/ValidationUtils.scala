package org.ekstep.commons

import org.ekstep.common.dto.ResponseParams.StatusType


object ValidationUtils {


   def isPluginMimeType(mimeType: String): Boolean = {
    "application/vnd.ekstep.plugin-archive".equalsIgnoreCase(mimeType)
  }

   def isEcmlMimeType(mimeType: String): Boolean = {
     "application/vnd.ekstep.ecml-archive".equalsIgnoreCase(mimeType)
   }

   def isH5PMimeType(mimeType: String): Boolean = {
     "application/vnd.ekstep.h5p-archive".equalsIgnoreCase(mimeType)
   }


  def isCollectionMimeType(mimeType: String): Boolean = {
    "application/vnd.ekstep.content-collection".equalsIgnoreCase(mimeType)
  }


  def isValid(response: org.ekstep.common.dto.Response) = {
    val params = response.getParams
    if (null != params && StatusType.failed.name.equals(params.getStatus)) true
    false
  }

}
