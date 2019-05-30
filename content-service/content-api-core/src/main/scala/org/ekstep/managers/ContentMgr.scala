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

class ContentMgr extends BaseContentManager {

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

}
