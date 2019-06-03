package org.ekstep.commons

import org.apache.commons.lang3.StringUtils
import org.ekstep.common.dto.ResponseParams.StatusType
import org.ekstep.common.enums.TaxonomyErrorCodes
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

  def hasError(response: org.ekstep.common.dto.Response): Boolean = {
    val params = response.getParams
    if (null != params && StatusType.failed.name.equals(params.getStatus)) return true
    return false
  }

   def isValidContentId(contentId: String): Unit = {
    if (StringUtils.isBlank(contentId)) throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_OBJECT_ID.toString, "Content Object Id cannot is Blank.")
  }

  def isValidFlaggedContent(node: org.ekstep.graph.dac.model.Node): Boolean ={
    val isValidObj = StringUtils.equalsIgnoreCase(Constants.CONTENT_OBJECT_TYPE, node.getObjectType)
    val isValidStatus = StringUtils.equalsIgnoreCase(node.getMetadata.get("status").asInstanceOf[String], "Flagged")
    if (!isValidObj || !isValidStatus)
      throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name, "Invalid Flagged Content! Content Can Not Be Accepted.")
    return true
  }

  def isValidList(publishChecklistObj: Any): Boolean = {
    try {
      val publishChecklist = publishChecklistObj.asInstanceOf[List[String]]
      if (null == publishChecklist || publishChecklist.isEmpty) return false
    } catch {
      case e: Exception =>
        return false
    }
    true
  }

  def isNodeVisibilityParent(node: org.ekstep.graph.dac.model.Node): Boolean = {
    StringUtils.equals(ContentAPIParams.Parent.name, node.getMetadata.get(ContentAPIParams.visibility.name).asInstanceOf[String])
  }

  def isContent(node: org.ekstep.graph.dac.model.Node): Boolean = {
    StringUtils.equalsIgnoreCase(ContentAPIParams.Content.name, node.getObjectType)
  }

  def validateIsContent(node: org.ekstep.graph.dac.model.Node): Unit = {
    if (!isContent(node)) throw new ClientException(ContentErrorCodes.ERR_NOT_A_CONTENT.toString, "Error! Not a Content.")
  }

  def isRetired(metadata: Map[String,AnyRef]): Boolean = {
    StringUtils.equalsIgnoreCase(metadata.get(ContentAPIParams.status.name).asInstanceOf[String], ContentAPIParams.Retired.name)
  }

  def validateIsNodeRetired(metadata: Map[String,AnyRef]): Unit = {
    if (isRetired(metadata)) throw new ClientException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.toString, "Error! Content not found with id: " + metadata.get("identifier"))
  }

  def validateEmptyOrNullFileUrl(fileUrl: String): Unit = {
    if (StringUtils.isBlank(fileUrl)) throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_UPLOAD_OBJECT.toString, "File Url cannot be Blank.")
  }

  def isYoutubeMimeType(mimeType: String): Boolean = {
    StringUtils.equals("video/x-youtube", mimeType)
  }

  def validateEmptyOrNullChannelId(channelId: String): Unit = {
    if (StringUtils.isBlank(channelId)) throw new ClientException(ContentErrorCodes.ERR_CHANNEL_BLANK_OBJECT.toString, "Channel can not be blank.")
  }

}
