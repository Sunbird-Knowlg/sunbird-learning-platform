package org.ekstep.managers

import akka.actor.{ActorSystem, Props}
import com.fasterxml.jackson.databind.ObjectMapper
import org.ekstep.commons.{Request, Response}
import org.ekstep.common._
import org.ekstep.common.mgr.{BaseManager, ConvertGraphNode}
import org.ekstep.common.router.RequestRouterPool
import org.ekstep.graph.common.enums.GraphHeaderParams
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.{DefinitionDTO, MetadataDefinition}
import org.ekstep.learning.actor.ContentStoreActor
import org.ekstep.learning.common.enums.LearningActorNames
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.search.router._

import scala.collection.JavaConverters._
import org.ekstep.util.{CommonUtil, JSONUtils}
import com.fasterxml.jackson.core._
import org.ekstep.graph.cache.util._
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import java.util
import org.ekstep.util._
import scala.collection.mutable.MutableList


/**
  * This manager is responsible for reading a data from DB
  * Talks to graph-engine
  *
  * Needs to be loosely coupled
  * Associations required rather extends.
  */

class ReadContentMgr extends BaseManager{

  val ERR_INVALID_REQUEST: String = "ERR_INVALID_REQUEST"
  val EDIT_MODE: String = "edit"

  val system = ActorSystem.create("learningActor")
  val learningActor = system.actorOf(Props[ContentStoreActor], name = "learningActor")

  def read(request: org.ekstep.commons.Request) = {

    LearningRequestRouterPool.init()
    SearchRequestRouterPool.init(RequestRouterPool.getActorSystem())

    val params = request.params.getOrElse(Map())

    val identifier: String = params.getOrElse("identifier", "").asInstanceOf[String]
    val objectType: String = params.getOrElse("objectType", "").asInstanceOf[String]
    val fields: List[String] = params.getOrElse("fields", List()).asInstanceOf[List[String]]
    val mode: String = params.getOrElse("mode", "").asInstanceOf[String]

    //println("read operaions called " +identifier)

    val requestDto = getRequest("domain", GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.object_type.name, "Content");//buildRequest("domain", objectType)

    val definitionDto = getResponse(requestDto).get(GraphDACParams.definition_node.name).asInstanceOf[DefinitionDTO]

    val contentMap = mode match {
      case EDIT_MODE =>
        editMode(identifier, objectType, definitionDto)
      case _ =>
        nonEditMode(identifier, definitionDto)
    }

    val contentNode: JsonNode = new ObjectMapper().valueToTree(contentMap)
    val externalPropsResp = getExternalProps(identifier,objectType, fields, definitionDto)

    //TODO: pushEvent to kafka, for ecml content.

    addExternalProps(externalPropsResp, contentNode.asInstanceOf[ObjectNode])
    addlangageCode(contentNode.asInstanceOf[ObjectNode])
    updateContentTaggedProperty(contentNode.asInstanceOf[ObjectNode], mode)

    buildResponse(contentNode.asInstanceOf[ObjectNode])

  }

   def buildResponse (contentNode: ObjectNode)= {
    var response = new org.ekstep.common.dto.Response(){
      put("content", contentCleanUp(contentNode))
    }
    response
  }


   def addExternalProps( externalPropsResp:org.ekstep.common.dto.Response, contentNode: ObjectNode)  = {
      val props = externalPropsResp.get("values")
      if(props != null){
        contentNode.putAll(props.asInstanceOf[ObjectNode])
      }
  }

   def addlangageCode(contentNode: ObjectNode)  = {

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


   def contentCleanUp(contentNode: ObjectNode)  = {
    if (contentNode.has("identifier")) {
      val identifier = contentNode.get("identifier").asText()
      if (identifier.endsWith(".img")) {
        val newIdentifier = identifier.replace(".img", "")
        contentNode.put("identifier", newIdentifier)
      }
    }
    contentNode
  }


   def updateContentTaggedProperty(contentMap:ObjectNode, mode: String): Unit = {
    val contentTaggingFlag =
      if (Platform.config.hasPath("content.tagging.backward_enable")) Platform.config.getBoolean("content.tagging.backward_enable")
      else false

    if (!mode.equals("edit") && contentTaggingFlag) {
      val contentTaggedKeys:Array[String] = if (Platform.config.hasPath("content.tagging.property"))
        Platform.config.getString("content.tagging.property").split(",")
      else{
        val prop = "subject, medium"
        prop.split(",")
      }

      for(i <- 0 until contentTaggedKeys.length){
        val toAddProp = contentMap.get(contentTaggedKeys(i))
        contentMap.put(contentTaggedKeys(i), toAddProp)
      }
    }
  }

  /**
    * Not used as of not. getRequest got called
    * @param graphId
    * @param objectType
    * @return
    */
  private def buildRequest(graphId: String, objectType: String): org.ekstep.common.dto.Request = {

    val request = new org.ekstep.common.dto.Request() {
      getContext.put(GraphHeaderParams.graph_id.toString, graphId)
      setManagerName(GraphEngineManagers.SEARCH_MANAGER)
      setOperation("getDataNode")
      put(GraphDACParams.object_type.name, objectType)
    }
    return request

  }

   def nonEditMode(identifier: String, definitionDto: DefinitionDTO) ={

    val responseNode = getDataNode("domain", identifier)
    val node:Node = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]

    val content = RedisStoreUtil.get(identifier)

    val contentMap = ConvertGraphNode.convertGraphNode(node,"domainId", definitionDto, null)

     if(content != null){
       new ObjectMapper().readValue(content, classOf[Map[String, Any]])

     } else {
       ConvertGraphNode.convertGraphNode(node,"domainId", definitionDto, null)
     }

  }

   def editMode(identifier: String, objectType: String, definitionDto: DefinitionDTO) ={
    val imageContentId = identifier+".img"
    val responseNode = getDataNode("domain", imageContentId)
    val node = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
    val contentMap = ConvertGraphNode.convertGraphNode(node,"domainId", definitionDto, null)
    contentMap
  }

   def getExternalProps(identifier: String,objectType: String, fields: List[String], definitionDTO: DefinitionDTO)
  : org.ekstep.common.dto.Response= {
    val externalPropsList = getExternalPropList(objectType, definitionDTO)
    val propsList = fields.intersect(externalPropsList)

    if (propsList.nonEmpty) {
      val request: dto.Request = new dto.Request() {
        setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name())
        setOperation("getContentProperties")
        put("content_id", identifier)
        put("properties", propsList.asJava)
      }
      getResponse(request, LearningRequestRouterPool.getRequestRouter())


    } else {
      /*Map[String, AnyRef]().asJava*/
      new org.ekstep.common.dto.Response()
    }
  }

   def getExternalPropList(objectType: String, definitionDTO: DefinitionDTO): List[String] = {
    //println("definitionDTO= "+definitionDTO.getProperties())
    val it = definitionDTO.getProperties().iterator
    val list: List[String] = List[String]()

    while (it.hasNext){
      val metadata = it.next()
      if(metadata.getDataType.equalsIgnoreCase("external")){
        list + metadata.getPropertyName().trim()
      }
    }
    return list
  }


}
