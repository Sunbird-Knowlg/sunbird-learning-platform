package org.ekstep.commons

import org.apache.commons.lang3.StringUtils
import org.ekstep.common.dto.ResponseParams.StatusType
import org.ekstep.common.exception.ClientException
import org.ekstep.learning.common.enums.ContentAPIParams


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


  def isNodeVisibilityParent(node: org.ekstep.graph.dac.model.Node): Boolean = {
    StringUtils.equals(ContentAPIParams.Parent.name, node.getMetadata.get(ContentAPIParams.visibility.name).asInstanceOf[String])
  }

  protected def isContent(node: org.ekstep.graph.dac.model.Node): Boolean = {
    StringUtils.equalsIgnoreCase(ContentAPIParams.Content.name, node.getObjectType)
  }

  protected def validateIsContent(node: org.ekstep.graph.dac.model.Node): Unit = {
    if (!isContent(node)) throw new ClientException(ContentErrorCodes.ERR_NOT_A_CONTENT.toString, "Error! Not a Content.")
  }

  protected def isRetired(metadata: Map[String,AnyRef]): Boolean = {
    StringUtils.equalsIgnoreCase(metadata.get(ContentAPIParams.status.name).asInstanceOf[String], ContentAPIParams.Retired.name)
  }

  protected def validateIsNodeRetired(metadata: Map[String,AnyRef]): Unit = {
    if (isRetired(metadata)) throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.toString, "Error! Content not found with id: " + metadata.get("identifier"))
  }

  protected def validateEmptyOrNullFileUrl(fileUrl: String): Unit = {
    if (StringUtils.isBlank(fileUrl)) throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.toString, "File Url cannot be Blank.")
  }

  protected def isYoutubeMimeType(mimeType: String): Boolean = {
    StringUtils.equals("video/x-youtube", mimeType)
  }

  protected def validateEmptyOrNullChannelId(channelId: String): Unit = {
    if (StringUtils.isBlank(channelId)) throw new ClientException(ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.toString, "Channel can not be blank.")
  }

}
