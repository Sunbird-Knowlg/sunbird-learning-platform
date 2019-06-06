package org.ekstep.mgr.impl


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.{BooleanUtils, StringUtils}
import org.apache.commons.lang3.StringUtils.equalsIgnoreCase
import org.ekstep.common.dto.{Request, RequestParams, Response}
import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.exception.{ClientException, ResourceNotFoundException}
import org.ekstep.common.mgr.{BaseManager, ConvertGraphNode}
import org.ekstep.common.router.RequestRouterPool
import org.ekstep.common.util.YouTubeUrlUtil
import org.ekstep.common.{Platform, dto}
import org.ekstep.commons.{Constants, ContentErrorCodes, ContentMetadata, TaxonomyAPIParams, ValidationUtils}
import org.ekstep.commons.ContentErrorCodes
import org.ekstep.content.enums.ContentWorkflowPipelineParams
import org.ekstep.content.mimetype.mgr.IMimeTypeManager
import org.ekstep.content.util.{LanguageCode, MimeTypeManagerFactory}
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.common.enums.GraphHeaderParams
import org.ekstep.graph.dac.enums.{GraphDACParams, SystemNodeTypes}
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.{DefinitionDTO, MetadataDefinition}
import org.ekstep.graph.service.common.DACConfigurationConstants
import org.ekstep.kafka.KafkaClient
import org.ekstep.learning.common.enums.{ContentAPIParams, LearningActorNames}
import org.ekstep.learning.contentstore.{ContentStoreOperations, ContentStoreParams}
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.telemetry.logger.TelemetryManager
import org.ekstep.telemetry.util.LogTelemetryEventUtil

import scala.collection.JavaConverters._
import scala.collection.mutable.MutableList


/**
  * This is responsible for talking to Graph engine
  * This holds the basic required operations, can be used by all managers
  */

 class BaseContentManager extends BaseManager {

  val objectMapper = new ObjectMapper()
  val DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX: String = ".img"
  val CONTENT_IMAGE_OBJECT_TYPE = "ContentImage"

  val TAXONOMY_ID: String = "domain"
  val CONTENT_OBJECT_TYPE: String = "Content"
  protected val CONTENT_CACHE_FINAL_STATUS = List("Live", "Unlisted")
  protected val reviewStatus =  List("Review", "FlagReview")
  protected var finalStatus = List("Flagged", "Live", "Unlisted")
  protected val CONTENT_CACHE_ENABLED = if (Platform.config.hasPath("content.cache.read")) Platform.config.getBoolean("content.cache.read")
  else false


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
    * @param fields
    * @param definitionDTO
    * @return
    */
  def getExternalProps(identifier: String, fields: List[String], definitionDTO: DefinitionDTO)
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
      definitionDTO.getProperties.asScala.toList.asInstanceOf[List[MetadataDefinition]].filter(prop => prop.getDataType.equalsIgnoreCase("external")).map(prop => {prop.getPropertyName.trim}).toList
    } else{
      List[String]()
    }
  }

  def getExternalPropListX(definitionDTO: DefinitionDTO): List[String] = {
    val list = scala.collection.mutable.MutableList[String]()

    if (null != definitionDTO) {
      definitionDTO.getProperties.asScala.toList.map(prop => {
        if (prop.getDataType.equalsIgnoreCase("external"))
          list += prop.getPropertyName
      })
    }
    return list.toList
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
      val contentTaggedKeys: List[String] = (
        if (Platform.config.hasPath("content.tagging.property"))
          Platform.config.getString("content.tagging.property").split(",")
        else {
          val prop = "subject, medium"
          prop.split(",")
        }
      ).toList

      contentTaggedKeys.map(key => {
        if(contentMap.keySet.contains(key))
          contentMap + key -> contentMap.get(key).get.asInstanceOf[List[String]](0)
      })

    }
    contentMap
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

  def editMode(identifier: String, definitionDto: DefinitionDTO) = {
    val imageContentId = identifier + ".img"
    var responseNode = getDataNode("domain", imageContentId)
    if(!ValidationUtils.hasError(responseNode)){
      responseNode = getDataNode("domain", identifier)
    }

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


  protected def restrictProps(definition: DefinitionDTO, map: Map[String, AnyRef], props: String*): Unit = {
    props.map(key =>{
      val allow = definition.getMetadata.get("allowupdate_" + key)
      if((allow!=None || BooleanUtils.isFalse(allow.asInstanceOf[Boolean])) && map.keySet.contains(key)){
        throw new ClientException(ContentErrorCodes.ERR_CONTENT_UPDATE.toString, "Error! " + key + " can't be set for the content.")
      }
    })
  }

  // TODO: push this to publish-pipeline.
  protected def updateDefaultValuesByMimeType(map: Map[String, AnyRef], mimeType: String): Unit = {
    if (StringUtils.isNotBlank(mimeType)) {
      if (mimeType.endsWith("archive") || mimeType.endsWith("vnd.ekstep.content-collection") || mimeType.endsWith("epub"))
        map + TaxonomyAPIParams.contentEncoding.toString -> ContentMetadata.ContentEncoding.identity
      else map + TaxonomyAPIParams.contentEncoding.toString -> ContentMetadata.ContentEncoding.identity
      if (mimeType.endsWith("youtube") || mimeType.endsWith("x-url"))
        map + TaxonomyAPIParams.contentDisposition.toString -> ContentMetadata.ContentDisposition.online
      else map + TaxonomyAPIParams.contentDisposition.toString -> ContentMetadata.ContentDisposition.inline
    }
  }

  protected def createDataNode(node: Node) = {
    var response = new Response
    if (null != node) {
      val request = getRequest(node.getGraphId, GraphEngineManagers.NODE_MANAGER, "createDataNode")
      request.put(GraphDACParams.node.name, node)
      TelemetryManager.log("Creating the Node ID: " + node.getIdentifier)
      response = getResponse(request)
    }
    response
  }

  protected def updateContentProperties(contentId: String, properties: Map[String, AnyRef])= {
    val request = new org.ekstep.common.dto.Request()
    request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name)
    request.setOperation(ContentStoreOperations.updateContentProperties.name)
    request.put(ContentStoreParams.content_id.name, contentId)
    request.put(ContentStoreParams.properties.name, properties)
    val response = getResponse(request, LearningRequestRouterPool.getRequestRouter)
    response
  }

  protected def updateDataNode(node: Node) = {
    var response = new Response
    if (null != node) {
      val contentId = node.getIdentifier
      // Checking if Content Image Object is being Updated, then return
      // the Original Content Id
      if (BooleanUtils.isTrue(node.getMetadata.get(TaxonomyAPIParams.isImageObject).asInstanceOf[Boolean])) {
        node.getMetadata.remove(TaxonomyAPIParams.isImageObject)
        node.setIdentifier(node.getIdentifier + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)
      }
      TelemetryManager.log("Getting Update Node Request For Node ID: " + node.getIdentifier)
      val updateReq = getRequest(node.getGraphId, GraphEngineManagers.NODE_MANAGER, "updateDataNode")
      updateReq.put(GraphDACParams.node.name, node)
      updateReq.put(GraphDACParams.node_id.name, node.getIdentifier)
      TelemetryManager.log("Updating the Node ID: " + node.getIdentifier)
      response = getResponse(updateReq)
      response.put(TaxonomyAPIParams.node_id.toString, contentId)
      TelemetryManager.log("Returning Node Update Response.")
    }
    response
  }

   def buildRequest(requestMap: Map[String,AnyRef])= {
    val request = new Request
    if (null != requestMap && !requestMap.isEmpty) {
      val id = requestMap.get("id").asInstanceOf[String]
      val ver = requestMap.get("ver").asInstanceOf[String]
      val ts = requestMap.get("ts").asInstanceOf[String]
      request.setId(id)
      request.setVer(ver)
      request.setTs(ts)
      val reqParams = requestMap.get("params")
      if (null != reqParams) try {
        val params = objectMapper.convertValue(reqParams, classOf[RequestParams])
        request.setParams(params)
      } catch {
        case e: Exception =>

      }
      val requestObj = requestMap.get("request")
      if (null != requestObj) try {
        val strRequest = objectMapper.writeValueAsString(requestObj)
        val map = objectMapper.readValue(strRequest, classOf[Map[String,AnyRef]])
        if (null != map && !map.isEmpty) request.setRequest(map.asJava)
      } catch {
        case e: Exception =>
      }
    }
    request
  }

  protected def getNodeForOperation(contentId: String, operation: String) = {
    var node = new Node()
    TelemetryManager.log("Fetching the Content Node. | [Content ID: " + contentId + "]")
    val contentImageId = contentId +DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX
    var response = getDataNode(TAXONOMY_ID, contentImageId)
    if (ValidationUtils.hasError(response)) {
      TelemetryManager.log("Unable to Fetch Content Image Node for Content Id: " + contentId)
      TelemetryManager.log("Trying to Fetch Content Node (Not Image Node) for Content Id: " + contentId)
      response = getDataNode(TAXONOMY_ID, contentId)
      TelemetryManager.log("Checking for Fetched Content Node (Not Image Node) for Content Id: " + contentId)
      if (ValidationUtils.hasError(response))
        throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name, "Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]")

      node = response.get(GraphDACParams.node.name).asInstanceOf[Node]
      if (!equalsIgnoreCase(operation, "publish") && !equalsIgnoreCase(operation, "review")) { // Checking if given Content Id is Image Node
        if (null != node && isContentImageObject(node))
          throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name, "Invalid Content Identifier! | [Given Content Identifier '" + node.getIdentifier + "' does not Exist.]")
        val status = node.getMetadata.get(TaxonomyAPIParams.status).asInstanceOf[String]
        if (StringUtils.isNotBlank(status) && (equalsIgnoreCase(TaxonomyAPIParams.Live.toString, status) || equalsIgnoreCase(TaxonomyAPIParams.Unlisted.toString, status)
          || equalsIgnoreCase(TaxonomyAPIParams.Flagged.toString, status))) node = createContentImageNode(TAXONOMY_ID, contentImageId, node)
      }
    }
    else { // Content Image Node is Available so assigning it as node
      node = response.get(GraphDACParams.node.name).asInstanceOf[Node]
      TelemetryManager.log("Getting Content Image Node and assigning it as node" + node.getIdentifier)
    }
    TelemetryManager.log("Returning the Node for Operation with Identifier: " + node.getIdentifier)
    node
  }


  protected def isContentImageObject(node: Node): Boolean = {
    val isConImg = if (null != node && equalsIgnoreCase(node.getObjectType, ContentWorkflowPipelineParams.ContentImage.name)) true
    else false
    isConImg
  }

  protected def createContentImageNode(taxonomyId: String, contentImageId: String, node: Node) = {
    val imageNode = new Node(taxonomyId, SystemNodeTypes.DATA_NODE.name, CONTENT_IMAGE_OBJECT_TYPE)
    imageNode.setGraphId(taxonomyId)
    imageNode.setIdentifier(contentImageId)
    imageNode.setMetadata(node.getMetadata)
    imageNode.setInRelations(node.getInRelations)
    imageNode.setOutRelations(node.getOutRelations)
    imageNode.setTags(node.getTags)
    imageNode.getMetadata.put(TaxonomyAPIParams.status.toString, TaxonomyAPIParams.Draft.toString)
    val response = createDataNode(imageNode)
    if (checkError(response)) throw new ClientException(TaxonomyErrorCodes.ERR_NODE_CREATION.name, "Error! Something went wrong while performing the operation. | [Content Id: " + node.getIdentifier + "]")
    val resp = getDataNode(taxonomyId, contentImageId)
    val nodeData = resp.get(GraphDACParams.node.name).asInstanceOf[Node]
    TelemetryManager.log("Returning Content Image Node Identifier" + nodeData.getIdentifier)
    nodeData
  }


  protected def isNodeUnderProcessing(node: Node, operation: String): Unit = {
    val status = List[String](TaxonomyAPIParams.Processing.toString)
    val isProcessing = checkNodeStatus(node, status)
    if (BooleanUtils.isTrue(isProcessing)) {
      TelemetryManager.log("Given Content is in Processing Status.")
      throw new ClientException(TaxonomyErrorCodes.ERR_NODE_ACCESS_DENIED.name(), "Operation Denied! | [Cannot Apply '" + operation + "' Operation on the Content in '" + node.getMetadata.get(TaxonomyAPIParams.status).asInstanceOf[String] + "' Status.] ")
    }
    else TelemetryManager.log("Given Content is not in " + node.getMetadata.get(TaxonomyAPIParams.status.toString) + " Status.")
  }

  private def checkNodeStatus(node: Node, status: List[String]) = {
    var inGivenStatus = false
    try
        if (null != node && null != node.getMetadata) {
          status.map(key=>{
            if (equalsIgnoreCase(node.getMetadata.get(TaxonomyAPIParams.status).asInstanceOf[String],key)) inGivenStatus = true
          })

        }
    catch {
      case e: Exception =>
        TelemetryManager.error("Something went wrong while checking the object whether it is under processing or not.", e)
    }
    inGivenStatus
  }

  protected def getContentBody(contentId: String) = {
    val request = new Request()
    request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name)
    request.setOperation(ContentStoreOperations.getContentBody.name)
    request.put(ContentStoreParams.content_id.name, contentId)
    val response = getResponse(request, LearningRequestRouterPool.getRequestRouter)
    val body = response.get(ContentStoreParams.body.name).asInstanceOf[String]
    body
  }

  protected def getContentTypeFrom(node: Node): String = node.getMetadata.get("contentType").asInstanceOf[String]

  protected def getMimeTypeFrom(node: Node): String = node.getMetadata.get(ContentAPIParams.mimeType.name).asInstanceOf[String]

  protected def getArtifactUrlFrom(node: Node): String = node.getMetadata.get(ContentAPIParams.artifactUrl.name).asInstanceOf[String]


  protected def deleteHierarchy(identifiers: List[String])= {
    val request = new Request()
    request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name)
    request.setOperation(ContentStoreOperations.deleteHierarchy.name)
    request.put(ContentStoreParams.content_id.name, identifiers)
    val response = getResponse(request, LearningRequestRouterPool.getRequestRouter)
    response
  }

  protected def updateDataNodes(map: Map[String, AnyRef], idList: List[String], graphId: String) = {
    TelemetryManager.log("Getting Update Node Request For Node ID: " + idList)
    val updateReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "updateDataNodes")
    updateReq.put(GraphDACParams.node_ids.name, idList)
    updateReq.put(GraphDACParams.metadata.name, map)
    TelemetryManager.log("Updating DialCodes for :" + idList)
    val response = getResponse(updateReq)
    TelemetryManager.log("Returning Node Update Response.")
    response
  }

  /**
    * Cassandra call to fetch hierarchy data
    *
    * @param contentId
    * @return
    */
  def getCollectionHierarchy(contentId: String) = {
    val request = new Request()
    request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name)
    request.setOperation(ContentStoreOperations.getCollectionHierarchy.name)
    request.put(ContentStoreParams.content_id.name, contentId)
    val response = getResponse(request, LearningRequestRouterPool.getRequestRouter)
    response
  }

  protected def getMimeTypeManger(contentId: String, mimeType: String, node: Node): IMimeTypeManager = {
    TelemetryManager.log("Fetching Mime-Type Factory For Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]")
    val contentType = getContentTypeFrom(node)
    return MimeTypeManagerFactory.getManager(contentType, mimeType)
  }


}
