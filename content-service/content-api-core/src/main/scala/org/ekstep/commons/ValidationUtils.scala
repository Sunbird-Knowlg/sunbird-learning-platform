package org.ekstep.commons


class ValidationUtils {


   def isPluginMimeType(mimeType: String): Boolean = {
    "application/vnd.ekstep.plugin-archive".equalsIgnoreCase(mimeType)
  }

   def isEcmlMimeType(mimeType: String): Boolean = {
     "application/vnd.ekstep.ecml-archive".equalsIgnoreCase(mimeType)
   }

   def isH5PMimeType(mimeType: String): Boolean = {
     "application/vnd.ekstep.h5p-archive".equalsIgnoreCase(mimeType)
   }


  protected def isCollectionMimeType(mimeType: String): Boolean = {
    "application/vnd.ekstep.content-collection".equalsIgnoreCase(mimeType)
  }

}
