package org.ekstep.managers

import com.fasterxml.jackson.databind.ObjectMapper
import org.ekstep.common._
import org.ekstep.common.mgr.{ConvertGraphNode}

import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.model.node.{DefinitionDTO, MetadataDefinition}

import org.ekstep.graph.cache.util._
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.JsonNode
import org.ekstep.util._
import scala.collection.mutable.MutableList
import org.ekstep.commons.Constants


/**
  * This manager is responsible for read operation
  */

class ReadContentMgr extends ContentManager {

  def read(request: org.ekstep.commons.Request) = {

    val params = request.params.getOrElse(Map())

    val identifier: String = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    val objectType: String = params.getOrElse(Constants.OBJECT_TYPE, "").asInstanceOf[String]
    val fields: List[String] = params.getOrElse(Constants.FIELDS, List()).asInstanceOf[List[String]]
    val mode: String = params.getOrElse(Constants.MODE, "").asInstanceOf[String]


    val definitionDto = getDefinitionNode()

    val contentMap = mode match {
      case Constants.EDIT_MODE =>
        editMode(identifier, objectType, definitionDto)
      case _ =>
        nonEditMode(identifier, definitionDto)
    }

    val contentNode: JsonNode = new ObjectMapper().valueToTree(contentMap)
    val externalPropsResp = getExternalProps(identifier, objectType, fields, definitionDto)

    //TODO: pushEvent to kafka, for ecml content.

    addExternalProps(externalPropsResp, contentNode.asInstanceOf[ObjectNode])
    addlangageCode(contentNode.asInstanceOf[ObjectNode])
    updateContentTaggedProperty(contentNode.asInstanceOf[ObjectNode], mode)

    buildResponse(contentNode.asInstanceOf[ObjectNode])

  }

  def buildResponse(contentNode: ObjectNode) = {
    val response = new org.ekstep.common.dto.Response() {
      put("content", contentCleanUp(contentNode))
    }
    response
  }


  def addExternalProps(externalPropsResp: org.ekstep.common.dto.Response, contentNode: ObjectNode) = {
    val props = externalPropsResp.get("values")
    if (props != null) {
      contentNode.setAll(props.asInstanceOf[ObjectNode])
    }
  }

  def addlangageCode(contentNode: ObjectNode) = {

    val it = contentNode.get("language").elements()

    var codes = MutableList[String]()
    while (it.hasNext) {
      val metadata: String = it.next().toString
      val lang = metadata.replaceAll("\"", "").toLowerCase()
      codes += LanguageCode.getLanguageCode(lang)

    }

    if (null != codes && (codes.length == 1)) contentNode.put("languageCode", codes(0))
    else contentNode.put("languageCode", codes.toString)
  }



  def updateContentTaggedProperty(contentMap: ObjectNode, mode: String): Unit = {
    val contentTaggingFlag =
      if (Platform.config.hasPath("content.tagging.backward_enable")) Platform.config.getBoolean("content.tagging.backward_enable")
      else false

    if (!mode.equals("edit") && contentTaggingFlag) {
      val contentTaggedKeys: Array[String] = if (Platform.config.hasPath("content.tagging.property"))
        Platform.config.getString("content.tagging.property").split(",")
      else {
        val prop = "subject, medium"
        prop.split(",")
      }

      for (i <- 0 until contentTaggedKeys.length) {
        val toAddProp = contentMap.get(contentTaggedKeys(i))
        contentMap.put(contentTaggedKeys(i), toAddProp)
      }
    }
  }


  def nonEditMode(identifier: String, definitionDto: DefinitionDTO) = {

    val responseNode = getDataNode("domain", identifier)
    val node: Node = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
    val content = RedisStoreUtil.get(identifier)

    if (content != null) {
      new ObjectMapper().readValue(content, classOf[Map[String, Any]])
    } else {
      ConvertGraphNode.convertGraphNode(node, "domainId", definitionDto, null)
    }

  }

  def editMode(identifier: String, objectType: String, definitionDto: DefinitionDTO) = {
    val imageContentId = identifier + ".img"
    val responseNode = getDataNode("domain", imageContentId)
    val node = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
    val contentMap = ConvertGraphNode.convertGraphNode(node, "domainId", definitionDto, null)
    contentMap
  }



}
