package org.ekstep.managers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.Patterns
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.ekstep.common.exception.{ResponseCode, ServerException}
import org.ekstep.common.{Platform, dto}
import org.ekstep.common.mgr.{BaseManager, ConvertGraphNode}
import org.ekstep.common.router.RequestRouterPool
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.actor.ContentStoreActor
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.search.router.SearchRequestRouterPool
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.learning.common.enums.{ContentErrorCodes, LearningActorNames}
import org.ekstep.commons.{Constants}
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.common.enums.GraphHeaderParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.util.LanguageCode

import scala.collection.JavaConverters._
import scala.collection.mutable.MutableList
import scala.concurrent.Await


import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.dto.ResponseParams
import org.ekstep.common.dto.ResponseParams.StatusType


/**
  * This is responsible for talking to Graph engine
  * This holds the basic required operations, can be used by all managers
  */

abstract class BaseContentManager /*extends BaseManager*/ {

  var objectMapper = new ObjectMapper()

  /**
    * Actors initializations
    */
  val system = ActorSystem.create("learningActor")
  val learningActor = system.actorOf(Props[ContentStoreActor], name = "learningActor")
  LearningRequestRouterPool.init()
  SearchRequestRouterPool.init(RequestRouterPool.getActorSystem())

  /**
    * To get a definition node for content type
    * @return
    */
  protected def getDefinitionNode(): DefinitionDTO = {
    val requestDto = getRequest(Constants.TAXONOMY_ID, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.object_type.name, Constants.CONTENT_OBJECT_TYPE);
    getResponse(requestDto,RequestRouterPool.getRequestRouter()).get(GraphDACParams.definition_node.name).asInstanceOf[DefinitionDTO]

  }

  /**
    * To remove image from content-id
    * @param contentNode
    * @return
    */
  def contentCleanUp(contentNode: ObjectNode) = {
    if (contentNode.has("identifier")) {
      val identifier = contentNode.get("identifier").asText()
      if (identifier.endsWith(".img")) {
        val newIdentifier = identifier.replace(".img", "")
        contentNode.put("identifier", newIdentifier)
      }
    }
    contentNode
  }

  /**
    * Creates external properties
    * Connecting cassandra
    *
    * @param identifier
    * @param objectType
    * @param fields
    * @param definitionDTO
    * @return
    */
  def getExternalProps(identifier: String, objectType: String, fields: List[String], definitionDTO: DefinitionDTO)
  : org.ekstep.common.dto.Response = {
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
      new org.ekstep.common.dto.Response()
    }
  }


  private def getExternalPropList(objectType: String, definitionDTO: DefinitionDTO): List[String] = {
    val it = definitionDTO.getProperties().iterator
    val list: List[String] = List[String]()

    while (it.hasNext) {
      val metadata = it.next()
      if (metadata.getDataType.equalsIgnoreCase("external")) {
        list + metadata.getPropertyName().trim()
      }
    }
    return list
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

    val responseNode = getDataNodeX("domain", identifier)
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
    val responseNode = getDataNodeX("domain", imageContentId)
    val node = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
    val contentMap = ConvertGraphNode.convertGraphNode(node, "domainId", definitionDto, null)
    contentMap
  }

  def getDataNodeX(taxonomyId: String, id: String)={
    import org.ekstep.graph.dac.enums.GraphDACParams
    import org.ekstep.graph.engine.router.GraphEngineManagers
    val request = getRequest(taxonomyId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode", GraphDACParams.node_id.name, id)
    getResponse(request,RequestRouterPool.getRequestRouter())
  }

  def getRequest(graphId: String, manager: String, operation: String, paramName: String, vo: String)={
    val request = new org.ekstep.common.dto.Request() {
      getContext.put(GraphHeaderParams.graph_id.toString, graphId)
      setManagerName(manager)
      setOperation(operation)
      put(paramName, vo)
    }
    request
  }



  def getResponse(request: org.ekstep.common.dto.Request, router: ActorRef):org.ekstep.common.dto.Response ={

    try {
      val future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT)
      val obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration)
      if (obj.isInstanceOf[org.ekstep.common.dto.Response]) {
        val response = obj.asInstanceOf[org.ekstep.common.dto.Response]
        response
      }
      else ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name, "System Error", ResponseCode.SERVER_ERROR)
    } catch {
      case e: Exception =>
        throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name, "System Error", e)
    }


  }

  def getErrorStatus(errorCode: String, errorMessage: String): ResponseParams = {
    val params = new ResponseParams
    params.setErr(errorCode)
    params.setStatus(StatusType.failed.name)
    params.setErrmsg(errorMessage)
    return params
  }


  def ERROR(errorCode: String, errorMessage: String, responseCode: org.ekstep.common.exception.ResponseCode): org.ekstep.common.dto.Response = {
    val response = new org.ekstep.common.dto.Response
    response.setParams(getErrorStatus(errorCode, errorMessage))
    response.setResponseCode(responseCode)
    return response
  }


}
