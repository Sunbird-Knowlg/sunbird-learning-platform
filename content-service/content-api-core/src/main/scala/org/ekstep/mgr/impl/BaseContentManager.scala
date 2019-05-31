package org.ekstep.mgr.impl


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.StringUtils.equalsIgnoreCase
import org.ekstep.common.dto.Request
import org.ekstep.common.exception.{ClientException, ResourceNotFoundException}
import org.ekstep.common.mgr.ConvertGraphNode
import org.ekstep.common.router.RequestRouterPool
import org.ekstep.common.{Platform, dto}
import org.ekstep.commons.ContentErrorCodes
import org.ekstep.content.util.LanguageCode
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.common.enums.GraphHeaderParams
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.{DefinitionDTO, MetadataDefinition}
import org.ekstep.kafka.KafkaClient
import org.ekstep.learning.common.enums.LearningActorNames
import org.ekstep.learning.contentstore.{ContentStoreOperations, ContentStoreParams}
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.mgr.IContentManager
import org.ekstep.telemetry.util.LogTelemetryEventUtil

import scala.collection.JavaConverters._
import scala.collection.mutable.MutableList


/**
  * This is responsible for talking to Graph engine
  * This holds the basic required operations, can be used by all managers
  */

abstract class BaseContentManager extends IContentManager {

  val objectMapper = new ObjectMapper()
  val DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX: String = ".img"

 /* /**
    * Actors initializations
    */
  val system = ActorSystem.create("learningActor")
  val learningActor = system.actorOf(Props[ContentStoreActor], name = "learningActor")
  LearningRequestRouterPool.init()
  SearchRequestRouterPool.init(RequestRouterPool.getActorSystem())*/

  /**
    * To get a definition node for content type
    * @return
    */
  protected def getDefinitionNode(graphId: String, objectType: String): DefinitionDTO = {
    val requestDto = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.object_type.name, objectType);
    getResponse(requestDto,RequestRouterPool.getRequestRouter()).get(GraphDACParams.definition_node.name).asInstanceOf[DefinitionDTO]
  }

  /**
    * To remove image from content-id
    * @param contentNode
    * @return
    */
  def contentCleanUp(contentNode: Map[String, AnyRef]) = {
    if (contentNode.contains("identifier") && contentNode.get("identifier").get.asInstanceOf[String].endsWith(".img")) {
        contentNode + ("identifier"-> contentNode.get("identifier").get.asInstanceOf[String].replace(".img", ""))
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
    val externalPropsList = getExternalPropList(definitionDTO)
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


  def getExternalPropList(definitionDTO: DefinitionDTO): List[String] = {
    if (null != definitionDTO) {
      definitionDTO.getProperties.asScala.toList.asInstanceOf[List[MetadataDefinition]].map(prop => {
        if (prop.getDataType.equalsIgnoreCase("external"))
          prop.getPropertyName.trim
      }).toList.asInstanceOf[List[String]]
    } else{
      List[String]()
    }
  }


  def buildResponse(contentNode: Map[String, AnyRef]) = {
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



  def updateContentTaggedProperty(contentMap: Map[String, AnyRef], mode: String): Map[String, AnyRef] = {
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
        contentMap + (contentTaggedKeys(i) -> toAddProp)
      }
    }
    contentMap
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



  /*def getResponse(request: org.ekstep.common.dto.Request, router: ActorRef):org.ekstep.common.dto.Response ={

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


  }*/

  /*def getErrorStatus(errorCode: String, errorMessage: String): ResponseParams = {
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
  }*/

  protected def getContentNode(graphId: String, contentId: String, mode: String): Node = {
    if (equalsIgnoreCase("edit", mode)) {
      val contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX
      val responseNode = getDataNode(graphId, contentImageId)
      if (!checkError(responseNode)) {
        val content = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
        return content
      }
    }
    val responseNode = getDataNode(graphId, contentId)
    if (checkError(responseNode)) {
      throw new ResourceNotFoundException(ContentErrorCodes.ERR_CONTENT_NOT_FOUND.asInstanceOf[String], "Content not found with id: " + contentId)
    }
    val content = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
    content
  }

  protected def getContentProperties(contentId: String, properties: List[String]): org.ekstep.common.dto.Response = {
    val request: org.ekstep.common.dto.Request = new Request()
    request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name)
    request.setOperation(ContentStoreOperations.getContentProperties.name)
    request.put(ContentStoreParams.content_id.name, contentId)
    request.put(ContentStoreParams.properties.name, properties.asJava)
    val response: org.ekstep.common.dto.Response = getResponse(request, LearningRequestRouterPool.getRequestRouter)
    response
  }

  protected def generateMigrationInstructionEvent(identifier: String, channel: String): Unit = {
    try
      pushInstructionEvent(identifier, channel)
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  @throws[Exception]
  private def pushInstructionEvent(contentId: String, channel: String): Unit = {
    var actor: Map[String, AnyRef] = Map[String, AnyRef] (
      "id" -> "Collection Migration Samza Job",
      "type" -> "System",
      "pdata" -> Map[String, AnyRef] (
        "id" -> "org.ekstep.platform",
        "ver" -> "1.0"
      )
    )
    var context: Map[String, AnyRef] = {if(Platform.config.hasPath("cloud_storage.env"))Map[String, AnyRef](
      "env" -> Platform.config.getString("cloud_storage.env")
    )else Map()}
    var `object`: Map[String, AnyRef] = Map[String, AnyRef](
      "id" -> contentId.replace(".img",""),
      "type" -> "content",
      "channel" -> channel
    )
    var edata: Map[String, AnyRef] = Map[String, AnyRef](
      "action" -> "ecml-migration",
      "contentType" -> "Ecml"
    )
    val beJobRequestEvent: String = LogTelemetryEventUtil.logInstructionEvent(actor.asJava, context.asJava, `object`.asJava, edata.asJava)
    val topic: String = Platform.config.getString("kafka.topics.instruction")
    if (org.apache.commons.lang3.StringUtils.isBlank(beJobRequestEvent)) throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Event is not generated properly.")
    if (org.apache.commons.lang3.StringUtils.isNotBlank(topic)) KafkaClient.send(beJobRequestEvent, topic)
    else throw new ClientException("BE_JOB_REQUEST_EXCEPTION", "Invalid topic id.")
  }
}
