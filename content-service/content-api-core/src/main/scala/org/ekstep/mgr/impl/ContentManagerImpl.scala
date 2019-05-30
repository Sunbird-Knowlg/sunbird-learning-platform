package org.ekstep.mgr.impl

import com.fasterxml.jackson.core.`type`.TypeReference
import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.exception.ServerException
import org.ekstep.common.mgr.ConvertGraphNode
import org.ekstep.commons.{Constants, Request, TaxonomyAPIParams}
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.ContentAPIParams
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
}
