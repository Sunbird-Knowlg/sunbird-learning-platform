package org.ekstep.mgr.impl

import java.util.Date

import com.fasterxml.jackson.core.`type`.TypeReference
import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.exception.{ResponseCode, ServerException}
import org.ekstep.common.mgr.{ConvertGraphNode, ConvertToGraphNode}
import org.ekstep.commons.{Constants, Request, TaxonomyAPIParams, ValidationUtils}
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.common.DateUtils
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.ContentAPIParams
import org.ekstep.learning.contentstore.ContentStoreParams
import org.ekstep.telemetry.logger.TelemetryManager

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer


/**
  * This manager is responsible for read operation
  */

class ContentManagerImpl extends BaseContentManager {

  def read(request: Request) : Response = {
    val params = request.params.getOrElse(Map())

    var identifier = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    var fields = params.getOrElse(Constants.FIELDS, ListBuffer()).asInstanceOf[ListBuffer[String]]
    val mode = params.getOrElse(Constants.MODE, "").asInstanceOf[String]
    var contentMap: Map[String, AnyRef] = Map()

    val definition: DefinitionDTO = getDefinitionNode(TAXONOMY_ID, CONTENT_OBJECT_TYPE)
    val externalPropsList = getExternalPropList(definition)

    //TODO: this is only for backward compatibility. remove after this release.
    if (fields.contains("tags")) {
      fields -= "tags"
      fields += "keywords"
    }

    if (!StringUtils.equalsIgnoreCase("edit", mode)) {
      var content:String = ""
      if(CONTENT_CACHE_ENABLED)
        content = RedisStoreUtil.get(identifier)

      if (StringUtils.isNotBlank(content)) {
        try{
          contentMap = objectMapper.readValue(content, new TypeReference[Map[String,Object]]() {
          })
        }catch{
          case e:Exception=>{
            TelemetryManager.error("Error Occurred While Parsing Hierarchy for Content Id : " + identifier + " | Error is: ", e)
            throw new ServerException("ERR_CONTENT_PARSE","Something Went Wrong While Processing the Content. ",e)
          }
        }

      } else {
        TelemetryManager.log("Fetching the Data For Content Id: " + identifier)
        val node : Node  = getContentNode(TAXONOMY_ID, identifier, null)
        contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null).asScala.asInstanceOf[Map[String,AnyRef]]
        if(CONTENT_CACHE_ENABLED && CONTENT_CACHE_FINAL_STATUS.contains(contentMap.get(ContentAPIParams.status.name()).toString()))
          RedisStoreUtil.saveData(identifier, contentMap.asJava, 0)
      }
    } else {
      TelemetryManager.log("Fetching the Data For Content Id: " + identifier)
      val node: Node = getContentNode(TAXONOMY_ID, identifier, mode)
      contentMap = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null).asScala.asInstanceOf[Map[String,AnyRef]]
      identifier = node.getIdentifier()
    }

    val channel: String = contentMap.getOrElse("channel", {if(Platform.config.hasPath("channel.default")) Platform.config.getString("channel.default") else "in.ekstep"}).asInstanceOf[String]
    val version = contentMap.getOrElse("version", null).asInstanceOf[Number]
    val mimeType:String = contentMap.getOrElse("mimeType", null).asInstanceOf[String]
    if (null != mimeType
      && StringUtils.equalsIgnoreCase(mimeType.asInstanceOf[String], "application/vnd.ekstep.ecml-archive")
      && (version == null || version.intValue() < 2)) {
      generateMigrationInstructionEvent(identifier, channel)
    }

    // Filter contentMap based on Fields
    if(!fields.isEmpty){
      fields += ("identifier")
      contentMap = contentMap.filterKeys(p=> !fields.contains(p))
    }

    val externalPropsToFetch = fields.intersect(externalPropsList).asInstanceOf[List[String]]

    if (null != externalPropsToFetch && !externalPropsToFetch.isEmpty) {
      val getContentPropsRes = getContentProperties(identifier, externalPropsToFetch)
      if (!checkError(getContentPropsRes)) {
        val resProps: Map[String, AnyRef] = getContentPropsRes.get(TaxonomyAPIParams.values.asInstanceOf[String]).asInstanceOf[Map[String, AnyRef]]
        if (null != resProps && !resProps.isEmpty)
          contentMap ++ resProps
      }
    }

    // Get all the languages for a given Content
    val languages:List[String] = contentMap.get(TaxonomyAPIParams.language.asInstanceOf[String]).asInstanceOf[List[String]]
    val languageCodeMap = Platform.config.getAnyRef("language_map").asInstanceOf[Map[String, String]]
    // Eval the language code for all Content Languages
    val languageCodes = languages.map(language => languageCodeMap.getOrElse(language.toLowerCase, "")).toList
    if (!languageCodes.isEmpty && languageCodes.size == 1)
      contentMap + (TaxonomyAPIParams.languageCode.asInstanceOf[String] -> languageCodes.head)
    else
      contentMap + (TaxonomyAPIParams.languageCode.asInstanceOf[String] -> languageCodes)
    updateContentTaggedProperty(contentMap, mode)
    val response : org.ekstep.common.dto.Response = new org.ekstep.common.dto.Response()
    response.put(TaxonomyAPIParams.content.asInstanceOf[String], contentCleanUp(contentMap))
    response.setParams(getSucessStatus())
    return response

  }

  @throws(classOf[Exception])
  def update(request: org.ekstep.commons.Request) : Response ={
    val params = request.params.getOrElse(Map())

    var contentId = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    var contentMap: Map[String, AnyRef] = Map()
    if (null == contentMap) return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR)

    if (contentMap.contains("dialcodes")) contentMap - "dialcodes"


    val definition = getDefinitionNode(TAXONOMY_ID, CONTENT_OBJECT_TYPE)
    restrictProps(definition, contentMap, "status", "framework")

    val originalId = contentId
    var objectType = CONTENT_OBJECT_TYPE
    contentMap + "objectType" -> CONTENT_OBJECT_TYPE
    contentMap + "identifier" -> contentId

    val mimeType = contentMap.get(TaxonomyAPIParams.mimeType.toString).asInstanceOf[String]
    updateDefaultValuesByMimeType(contentMap, mimeType)

    var isImageObjectCreationNeeded = false
    var imageObjectExists = false

    val contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX

    var getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId)
    if (!ValidationUtils.isValid(getNodeResponse)) {
      TelemetryManager.log("Content image not found: " + contentImageId)
      isImageObjectCreationNeeded = true
      getNodeResponse = getDataNode(TAXONOMY_ID, contentId)
      TelemetryManager.log("Content node response: " + getNodeResponse)
    }
    else imageObjectExists = true

    if (!ValidationUtils.isValid(getNodeResponse)) {
      TelemetryManager.log("Content not found: " + contentId)
      return getNodeResponse
    }

    if (contentMap.contains(ContentAPIParams.body.name)) contentMap + (ContentAPIParams.artifactUrl.name -> null)

    val externalProps: Map[String, AnyRef] = Map()
    val externalPropsList = getExternalPropList(definition)
    if (null != externalPropsList && !externalPropsList.isEmpty) {
      for (prop <- externalPropsList) {
        if (null != contentMap.get(prop)) externalProps + (prop -> contentMap.get(prop))
        if (StringUtils.equalsIgnoreCase(ContentAPIParams.screenshots.name, prop) && null != contentMap.get(prop)) contentMap + (prop -> null)
        else contentMap - (prop)
      }
    }

    val graphNode = getNodeResponse.get(GraphDACParams.node.name).asInstanceOf[Node]
    TelemetryManager.log("Graph node found: " + graphNode.getIdentifier)
    val metadata = graphNode.getMetadata
    val status = metadata.get("status").asInstanceOf[String]
    val inputStatus = contentMap.get("status").asInstanceOf[String]
    if (null != inputStatus) if (reviewStatus.contains(inputStatus) && !reviewStatus.contains(status)) contentMap + ("lastSubmittedOn"-> DateUtils.format(new Date()))

    var checkError = false
    var createResponse = new Response
    if (finalStatus.contains(status)) {
      if (isImageObjectCreationNeeded) {
        graphNode.setIdentifier(contentImageId)
        graphNode.setObjectType(Constants.CONTENT_IMAGE_OBJECT_TYPE)
        metadata.put("status", "Draft")
        val lastUpdatedBy = contentMap.get("lastUpdatedBy")
        if (null != lastUpdatedBy) metadata.put("lastUpdatedBy", lastUpdatedBy)
        graphNode.setGraphId(TAXONOMY_ID)
        createResponse = createDataNode(graphNode)
        checkError = ValidationUtils.isValid(createResponse)
        if (!checkError) {
          TelemetryManager.log("Updating external props for: " + contentImageId)
          val bodyResponse = getContentProperties(contentId, externalPropsList)
          checkError = ValidationUtils.isValid(bodyResponse)
          if (!checkError) {
            val extValues = bodyResponse.get(ContentStoreParams.values.name).asInstanceOf[Map[String, AnyRef]]
            if (null != extValues) updateContentProperties(contentImageId, extValues)
          }
          contentMap + "versionKey" -> createResponse.get("versionKey")
        }
      }
      objectType = CONTENT_IMAGE_OBJECT_TYPE
      contentId = contentImageId
    } else if (imageObjectExists) {
      objectType = CONTENT_IMAGE_OBJECT_TYPE
      contentId = contentImageId
    }

    if (checkError) return createResponse

    TelemetryManager.log("Updating content node: " + contentId)
    val domainObj = ConvertToGraphNode.convertToGraphNode(contentMap.asJava, definition, graphNode)
    domainObj.setGraphId(TAXONOMY_ID)
    domainObj.setIdentifier(contentId)
    domainObj.setObjectType(objectType)
    createResponse = updateDataNode(domainObj)
    checkError = ValidationUtils.isValid(createResponse)
    if (checkError) return createResponse

    createResponse.put(GraphDACParams.node_id.name, originalId)

    if (null != externalProps ) {
      val externalPropsResponse: org.ekstep.common.dto.Response = updateContentProperties(contentId, externalProps)
      if (ValidationUtils.isValid(externalPropsResponse)) {
        return externalPropsResponse
      }
    }
    return createResponse;
  }


}
