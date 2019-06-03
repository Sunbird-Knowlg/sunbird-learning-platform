package org.ekstep.mgr.impl

import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.mgr.ConvertGraphNode
import org.ekstep.commons.{Constants, Request, TaxonomyAPIParams}
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.ContentAPIParams
import org.ekstep.telemetry.logger.TelemetryManager
import org.ekstep.content.util.JSONUtils

import scala.collection.JavaConverters._


/**
  * This manager is responsible for read operation
  */

class ContentManagerImpl extends BaseContentManager {

  def read(request: Request) : Response = {
    val params = request.params.getOrElse(Map())
    var identifier = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    var fields = params.getOrElse(Constants.FIELDS, List()).asInstanceOf[List[String]]
    val mode = params.getOrElse(Constants.MODE, "").asInstanceOf[String]
    val definition: DefinitionDTO = getDefinitionNode(TAXONOMY_ID, CONTENT_OBJECT_TYPE)
    var contentMap: Map[String, AnyRef] = Map()
    if (!StringUtils.equalsIgnoreCase("edit", mode)) {
      if(CONTENT_CACHE_ENABLED) {
        contentMap = getContentfromRedis(identifier,definition)
      } else{
        contentMap = getContentFromNeo4j(identifier, definition, null)
      }
    } else {
      contentMap = getContentFromNeo4j(identifier, definition, mode)
      identifier = contentMap.getOrElse("identifier", identifier).asInstanceOf[String]
    }
    validateAndMigrateEcml(identifier, contentMap)
    if(!fields.isEmpty){
      fields + ("identifier")
      contentMap = contentMap.filterKeys(p=> !fields.contains(p))
    }
    val externalPropsToFetch = fields.intersect(getExternalPropList(definition)).asInstanceOf[List[String]]
    if (null != externalPropsToFetch && !externalPropsToFetch.isEmpty) {
      val getContentPropsRes = getContentProperties(identifier, externalPropsToFetch)
      if (!checkError(getContentPropsRes)) {
        val resProps: Map[String, AnyRef] = getContentPropsRes.get("values").asInstanceOf[Map[String, AnyRef]]
        if (null != resProps && !resProps.isEmpty)
          contentMap ++ resProps
      }
    }
    // Get all the languages for a given Content
    val languages:List[String] = contentMap.get(TaxonomyAPIParams.language.toString).getOrElse(Array).asInstanceOf[Array[String]].toList
    val languageCodeMap: Map[String, AnyRef] = Platform.config.getAnyRef("language_map").asInstanceOf[java.util.HashMap[String, AnyRef]].asScala.toMap[String, AnyRef]
    // Eval the language code for all Content Languages
    val languageCodes = languages.map(language => languageCodeMap.getOrElse(language.toLowerCase, "")).toList
    if (!languageCodes.isEmpty && languageCodes.size == 1)
      contentMap + (TaxonomyAPIParams.languageCode.toString -> languageCodes.head)
    else
      contentMap + (TaxonomyAPIParams.languageCode.toString -> languageCodes)
    contentMap = updateContentTaggedProperty(contentMap, mode)
    val response : org.ekstep.common.dto.Response = new org.ekstep.common.dto.Response()
    response.put(TaxonomyAPIParams.content.toString, contentCleanUp(contentMap))
    response.setParams(getSucessStatus())
    return response

  }

  def getContentfromRedis(identifier: String, definition: DefinitionDTO): Map[String, AnyRef] = {
    val content = RedisStoreUtil.get(identifier)
    if (StringUtils.isNotBlank(content)) {
      JSONUtils.deserialize(content).asInstanceOf[Map[String, Object]]
    } else {
      getContentFromNeo4j(identifier, definition, null)
    }
  }

  def getContentFromNeo4j(identifier: String, definition: DefinitionDTO, mode: String): Map[String, AnyRef] = {
    TelemetryManager.log("Fetching the Data For Content Id: " + identifier)
    val node: Node = getContentNode(TAXONOMY_ID, identifier, null)
    val contentMap:Map[String, AnyRef]  = ConvertGraphNode.convertGraphNode(node, TAXONOMY_ID, definition, null).asScala.toMap.asInstanceOf[Map[String, AnyRef]]
    if (null == mode && CONTENT_CACHE_ENABLED && CONTENT_CACHE_FINAL_STATUS.contains(contentMap.get(ContentAPIParams.status.name()).toString()))
      RedisStoreUtil.saveData(identifier, contentMap.asJava, 0)
    contentMap
  }

  def validateAndMigrateEcml(identifier: String, contentMap: Map[String, AnyRef]) = {
    val channel: String = contentMap.getOrElse("channel", {if(Platform.config.hasPath("channel.default")) Platform.config.getString("channel.default") else "in.ekstep"}).asInstanceOf[String]
    val version = contentMap.getOrElse("version", null).asInstanceOf[Number]
    val mimeType:String = contentMap.getOrElse("mimeType", null).asInstanceOf[String]
    if (null != mimeType
      && StringUtils.equalsIgnoreCase(mimeType.asInstanceOf[String], "application/vnd.ekstep.ecml-archive")
      && (version == null || version.intValue() < 2)) {
      generateMigrationInstructionEvent(identifier, channel)
    }
  }

}
