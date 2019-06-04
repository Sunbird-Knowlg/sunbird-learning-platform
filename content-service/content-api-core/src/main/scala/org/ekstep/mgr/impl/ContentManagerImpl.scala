package org.ekstep.mgr.impl

import java.util.Date

import org.ekstep.common.exception.{ClientException, ResponseCode, ServerException}
import org.ekstep.common.mgr.{ConvertGraphNode, ConvertToGraphNode}
import org.ekstep.commons.{Constants, ContentErrorCodes, Request, RequestBody, TaxonomyAPIParams, ValidationUtils}
import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.mgr.ConvertGraphNode
import org.ekstep.content.enums.ContentWorkflowPipelineParams
import org.ekstep.content.mimetype.mgr.IMimeTypeManager
import org.ekstep.content.publish.PublishManager
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.common.DateUtils
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.ContentAPIParams
import org.ekstep.learning.contentstore.ContentStoreParams
import org.ekstep.telemetry.logger.TelemetryManager
import org.ekstep.content.util.{JSONUtils, MimeTypeManagerFactory}
import org.ekstep.graph.service.common.DACConfigurationConstants
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil
import org.ekstep.searchindex.util.CompositeSearchConstants

import scala.collection.JavaConverters._


/**
  * This manager is responsible for read operation
  */

class ContentManagerImpl extends BaseContentManager{

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

  /**
    * Create content
    * @param request
    * @return
    */
  def create(request: org.ekstep.commons.Request) : Response ={

    val requestBody = JSONUtils.deserialize[RequestBody](request.body.get)
    val contentMap = requestBody.request.get("content").get.asInstanceOf[Map[String, AnyRef]]
    val definition = getDefinitionNode(TAXONOMY_ID, CONTENT_OBJECT_TYPE)

    restrictProps(definition, contentMap, "status")

    //validations
    val mimeType = contentMap.getOrElse("mimeType","").asInstanceOf[String]
    val code = contentMap.getOrElse("code","").asInstanceOf[String]
    ValidationUtils.isValidProperties(mimeType, code)


    //updating the content map with required values.
    prepareContentMap(contentMap)
    val externalProps = updateExternalPropertiesFrom(contentMap, definition)
    try {
      val node = ConvertToGraphNode.convertToGraphNode(contentMap.asJava, definition, null)
      node.setObjectType(CONTENT_OBJECT_TYPE)
      node.setGraphId(TAXONOMY_ID)
      val response = createDataNode(node)
      if (ValidationUtils.hasError(response)) return response
      else {
        val contentId = response.get(GraphDACParams.node_id.name).asInstanceOf[String]
        if (null != externalProps && !externalProps.isEmpty) {
          val externalPropsResponse = updateContentProperties(contentId, externalProps)
          if (ValidationUtils.hasError(externalPropsResponse)) return externalPropsResponse
        }
        return response
      }
    } catch {
      case e: Exception =>
        return ERROR("ERR_CONTENT_SERVER_ERROR", "Internal error", ResponseCode.SERVER_ERROR)
    }

  }

  /**
    * external properties removed from contenMap and added to external property list
    * @param contentMap
    * @param definitionDTO
    * @return external property list
    */
  private def updateExternalPropertiesFrom(contentMap: Map[String, AnyRef], definitionDTO: DefinitionDTO): Map [String,AnyRef] ={

    val externalProps = Map [String,AnyRef]()
    val externalPropsList = getExternalPropList(definitionDTO)
    if (null != externalPropsList && !externalPropsList.isEmpty) {
      externalPropsList.map(key=>{
        if (null != contentMap.get(key)) externalProps + (key -> contentMap.get(key))
        contentMap - (key)
      })
    }
    return externalProps
  }

  /**
    * Update contentMap to add visibility, framwork, osId, identifier, version
    * @param contentMap
    */
  private def prepareContentMap(contentMap: Map[String,AnyRef]) ={

    val framework = contentMap.getOrElse("framework",{ if (Platform.config.hasPath("platform.framework.default")) Platform.config.getString("platform.framework.default")
    else "NCF"}).asInstanceOf[String]

    val mimeType = contentMap.getOrElse("mimeType","").asInstanceOf[String]

    if (!ValidationUtils.isPluginMimeType(mimeType)) contentMap + "osId" -> "org.ekstep.quiz.app"
    else {
      contentMap + "identifier" -> contentMap.get("code")
    }

    val contentType = contentMap.getOrElse("contentType","").asInstanceOf[String]
    if (StringUtils.isNotBlank(contentType)) {
      val parentVisibilityList = Platform.config.getStringList("content.metadata.visibility.parent")
      if (parentVisibilityList.contains(contentType.toLowerCase)) contentMap + "visibility"-> "Parent"
    }

    updateDefaultValuesByMimeType(contentMap, mimeType)

    if (ValidationUtils.isCollectionMimeType(mimeType) || ValidationUtils.isEcmlMimeType(mimeType))
      contentMap + "version" -> Constants.LATEST_CONTENT_VERSION
    else contentMap + "version" -> Constants.DEFAULT_CONTENT_VERSION

  }


  /**
    * Upload URL
    * @param request
    * @return
    */
  def uploadUrl(request: org.ekstep.commons.Request) : Response ={
    val params = request.params.getOrElse(Map())
    val contentId = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    val fileUrl = params.getOrElse("fileUrl", "").asInstanceOf[String]
    var mimeType = params.getOrElse("mimeType", "").asInstanceOf[String]


    var updateMimeType = false
    try {
      ValidationUtils.has(fileUrl);

      val node = getNodeForOperation(contentId, "upload")
      isNodeUnderProcessing(node, "Upload");
      if (StringUtils.isBlank(mimeType)) {
        mimeType = node.getMetadata().getOrDefault("mimeType", Constants.DEFAULT_MIME_TYPE).toString
      } else {
        node.getMetadata.put("mimeType", mimeType)
        updateDefaultValuesByMimeType(node.getMetadata().asInstanceOf[Map[String, AnyRef]], mimeType)
        updateMimeType = true
      }

      ValidationUtils.validateUrlLicense(mimeType, fileUrl, node)

      TelemetryManager.log("Mime-Type: " + mimeType + " | [Content ID: " + contentId + "]")
      val mimeTypeManager = getMimeTypeManger(contentId, mimeType, node)

      val res = mimeTypeManager.upload(contentId, node, fileUrl)

      if (updateMimeType && !ValidationUtils.hasError(res)) {
        node.getMetadata() + "versionKey" -> res.getResult().get("versionKey")

        val map = Map[String, AnyRef]()
        map + "mimeType" -> mimeType
        map + "versionKey" -> Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY)

        //Calls update functionality here..
        val response = updateResponse(contentId, map)
        if (null != response && ValidationUtils.hasError(response)) return response

      }

      if (ValidationUtils.hasError(res)) return res
      else {
        val nodeId = res.getResult.get("node_id").asInstanceOf[String]
        val returnNodeId = if (StringUtils.endsWith(nodeId, ".img")) nodeId.replace(".img", "") else nodeId
        res.getResult.replace("node_id", nodeId, returnNodeId)
        return res
      }

    } catch {
      case e: ClientException =>
        throw e
      case e: ServerException =>
        return ERROR(e.getErrCode, e.getMessage, ResponseCode.SERVER_ERROR)
      case e: Exception =>
        val message = "Something went wrong while processing uploaded file."
        TelemetryManager.error(message, e)
        return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name, message, ResponseCode.SERVER_ERROR)
    }
  }




  /**
    * Update a content
    * @param request   containing versionKey as latest version of node to update
    * @throws java.lang.Exception
    * @return
    */
  @throws(classOf[Exception])
  def update(request: org.ekstep.commons.Request) : Response ={
    val params = request.params.getOrElse(Map())

    val contentId = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    val requestBody = JSONUtils.deserialize[RequestBody](request.body.get)
    val contentMap = requestBody.request.get("content").get.asInstanceOf[Map[String, AnyRef]]
    updateResponse(contentId, contentMap)

  }

  private def updateResponse (contentIdentifier: String, contentMap: Map[String, AnyRef]): Response = {
    var contentId = contentIdentifier

    if (null == contentMap) return ERROR("ERR_CONTENT_INVALID_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR)

    if (contentMap.contains("dialcodes")) contentMap - "dialcodes"

    val definition = getDefinitionNode(TAXONOMY_ID, CONTENT_OBJECT_TYPE)
    restrictProps(definition, contentMap, "status", "framework", "mimeType", "contentType")

    val originalId = contentId
    var objectType = CONTENT_OBJECT_TYPE
    contentMap + "objectType" -> CONTENT_OBJECT_TYPE
    contentMap + "identifier" -> contentId
    contentMap.get(TaxonomyAPIParams.mimeType.toString)
    val mimeType = contentMap.get(TaxonomyAPIParams.mimeType.toString).getOrElse("").asInstanceOf[String]
    updateDefaultValuesByMimeType(contentMap, mimeType)

    var isImageObjectCreationNeeded = false
    var imageObjectExists = false
    val contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX
    var getNodeResponse = getDataNode(TAXONOMY_ID, contentImageId)
    if (ValidationUtils.hasError(getNodeResponse)) {
      TelemetryManager.log("Content image not found: " + contentImageId)
      isImageObjectCreationNeeded = true
      getNodeResponse = getDataNode(TAXONOMY_ID, contentId)
      TelemetryManager.log("Content node response: " + getNodeResponse)
    }
    else imageObjectExists = true
    val hasError = ValidationUtils.hasError(getNodeResponse)
    if (hasError) {
      TelemetryManager.log("Content not found: " + contentId)
      return getNodeResponse
    }

    if (contentMap.contains(ContentAPIParams.body.name)) contentMap + (ContentAPIParams.artifactUrl.name -> null)

    val externalProps: Map[String, AnyRef] = Map()
    val externalPropsList = getExternalPropListX(definition)
    if (null != externalPropsList) {
      externalPropsList.map(key=>{
        if ( null != contentMap.get(key)) externalProps + (key -> contentMap.get(key))
        if (StringUtils.equalsIgnoreCase(ContentAPIParams.screenshots.name, key) && null != contentMap.get(key)) contentMap + (key -> null)
        else contentMap - (key)
      })
    }

    val graphNode = getNodeResponse.get(GraphDACParams.node.name).asInstanceOf[Node]
    TelemetryManager.log("Graph node found: " + graphNode.getIdentifier)
    val metadata = graphNode.getMetadata
    val status = metadata.get("status").asInstanceOf[String]
    val inputStatus = contentMap.getOrElse("status", "").asInstanceOf[String]
    if (null != inputStatus) if (reviewStatus.contains(inputStatus) && !reviewStatus.contains(status))
      contentMap + ("lastSubmittedOn"-> DateUtils.format(new Date()))

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
        checkError = ValidationUtils.hasError(createResponse)
        if (!checkError) {
          TelemetryManager.log("Updating external props for: " + contentImageId)
          val bodyResponse = getContentProperties(contentId, externalPropsList)
          checkError = ValidationUtils.hasError(bodyResponse)
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
    if (ValidationUtils.hasError(createResponse)) return createResponse

    createResponse.put(GraphDACParams.node_id.name, originalId)

    if (null != externalProps ) {
      val externalPropsResponse: org.ekstep.common.dto.Response = updateContentProperties(contentId, externalProps)
      if (ValidationUtils.hasError(externalPropsResponse)) {
        return externalPropsResponse
      }
    }
    return createResponse

  }

  /**
    * Review a content- changes the status to 'Review'
    *
    * @param request
    * @return
    */
  def review(request: org.ekstep.commons.Request) : Response ={

    val contentId = request.params.getOrElse(Map()).getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]

    ValidationUtils.isValidContentId(contentId)

    val node = getNodeForOperation(contentId, "review")

    isNodeUnderProcessing(node, "Review")

    val body = getContentBody(node.getIdentifier)
    node.getMetadata.put(ContentAPIParams.body.name, body)

    node.getMetadata.put(TaxonomyAPIParams.lastSubmittedOn.toString, DateUtils.formatCurrentDate)

    var mimeType = getMimeTypeFrom(node)
    if (StringUtils.isBlank(mimeType)) mimeType = "assets"

    TelemetryManager.log("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]")
    val artifactUrl = getArtifactUrlFrom(node)
    val license = node.getMetadata.get("license").asInstanceOf[String]

    if (ValidationUtils.isYoutubeMimeType(mimeType) && null != artifactUrl && StringUtils.isBlank(license)) ValidationUtils.checkYoutubeLicense(artifactUrl, node)
    TelemetryManager.log("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]")

    val contentType = getContentTypeFrom(node)

    val mimeTypeManager: IMimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType)

    val response = mimeTypeManager.review(contentId, node, false)

    TelemetryManager.log("Returning 'Response' Object: ", response.getResult)

    response
  }

  /**
    * Deletes(Soft delete) a content with given content id
    *
    * @param request
    * @return
    */
  def retire(request: org.ekstep.commons.Request) : Response ={

    val contentId = request.params.getOrElse(Map()).getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]

    ValidationUtils.isValidContentId(contentId)

    val params = Map[String, AnyRef]()
    params + "status" -> "Retired"
    params + "lastStatusChangedOn" -> DateUtils.formatCurrentDate

    val response = getDataNode(TAXONOMY_ID, contentId)
    if (ValidationUtils.hasError(response))
      return response

    val node = response.get(GraphDACParams.node.name).asInstanceOf[Node]
    val mimeType = node.getMetadata.get(ContentAPIParams.mimeType.name).asInstanceOf[String]
    val status = node.getMetadata.get(ContentAPIParams.status.name).asInstanceOf[String]

    if (StringUtils.equalsIgnoreCase(ContentAPIParams.Retired.name, status))
      throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.toString, "Content with Identifier [" + contentId + "] is already Retired.")

    val imageNodeResponse = getDataNode(TAXONOMY_ID, contentId+DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)
    val isImageNodeExist = if (!ValidationUtils.hasError(imageNodeResponse)) true else false

    val identifiers = if (isImageNodeExist) List[String](contentId, contentId+DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)
    else List[String](contentId)

    val responseUpdated = updateDataNodes(params, identifiers, TAXONOMY_ID)
    if (ValidationUtils.hasError(responseUpdated)) return responseUpdated
    else {
     return responseForBulkDelete(contentId, mimeType, status)
    }
  }

  protected def responseForBulkDelete(contentId: String, mimeType: String, status: String): Response ={

    if (ValidationUtils.isCollectionMimeType(mimeType) && ValidationUtils.isLive(status)) { // Delete Units from ES
      val hierarchyResponse = getCollectionHierarchy(contentId)
      if (ValidationUtils.hasError(hierarchyResponse)) {
        throw new ClientException("", "Unable to fetch Hierarchy for Root Node: [" + contentId + "]")
      }
      val rootHierarchy = hierarchyResponse.getResult.get("hierarchy").asInstanceOf[Map[String, AnyRef]]
      val rootChildren = rootHierarchy.get("children").asInstanceOf[List[Map[String, AnyRef]]]
      val childrenIdentifiers = getChildrenIdentifiers(rootChildren)
      try {
        ElasticSearchUtil.bulkDeleteDocumentById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, childrenIdentifiers.asJava)
      } catch {
        case e: Exception =>
          throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.toString, "Something Went Wrong While Removing Children's from ES.")
      }
      deleteHierarchy(List(contentId))
      RedisStoreUtil.delete(Constants.COLLECTION_CACHE_KEY_PREFIX + contentId)
    }
    ValidationUtils.isValidContentId(contentId)
    val responseNode = getDataNode(TAXONOMY_ID, contentId)
    val node = responseNode.get("node").asInstanceOf[Node]
    if (!ValidationUtils.isCollectionMimeType(mimeType)) {
      RedisStoreUtil.delete(contentId)
    }

    val res = getSuccessResponse
    res.put(ContentAPIParams.node_id.name, node.getIdentifier)
    res.put(ContentAPIParams.versionKey.name, node.getMetadata.get("versionKey"))
    return res
  }

  protected def getChildrenIdentifiers(childrens: List[Map[String, AnyRef]]):List[String] = {
    val identifiers = scala.collection.mutable.MutableList[String]()

    childrens.map(child=>{
      val cVisibility = child.get(ContentAPIParams.visibility.name()).asInstanceOf[String]
      val identifier = child.get(ContentAPIParams.identifier.name()).asInstanceOf[String]
      if(StringUtils.equalsIgnoreCase("Parent",cVisibility)) identifiers += identifier

      getChildrenIdentifiers(child.get(ContentAPIParams.children.name).asInstanceOf[List[Map[String, AnyRef]]])
    })

    identifiers.toList
  }

  /**
    * Accepts the content of 'Flagged' status
    * Applicable to the content with 'Flagged' status
    * @param request
    * @returnversion
    */
  def acceptFlag(request: org.ekstep.commons.Request) : Response ={

    val contentId = request.params.getOrElse(Map()).getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]

    ValidationUtils.isValidContentId(contentId)

    val response = getDataNode(TAXONOMY_ID, contentId)
    if (ValidationUtils.hasError(response))
      throw new ClientException(TaxonomyErrorCodes.ERR_TAXONOMY_INVALID_CONTENT.name, "Error! While Fetching the Content for Operation | [Content Id: " + contentId + "]")

    val node = response.get(GraphDACParams.node.name).asInstanceOf[Node]
    ValidationUtils.isValidFlaggedContent(node)

    val definition = getDefinitionNode(TAXONOMY_ID, CONTENT_OBJECT_TYPE)
    val externalPropsList = getExternalPropList(definition)

    val imageContentId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX
    val imageResponse = getDataNode(TAXONOMY_ID, imageContentId)
    val isImageNodeExist = if(!ValidationUtils.hasError(imageResponse)) true else false


    val versionKey = if (!isImageNodeExist) {
      val createNode = response.get(GraphDACParams.node.name).asInstanceOf[Node]
      createNode.setIdentifier(imageContentId)
      createNode.setObjectType(CONTENT_IMAGE_OBJECT_TYPE)
      createNode.getMetadata.put(ContentAPIParams.status.name, "FlagDraft")
      createNode.setGraphId(TAXONOMY_ID)

      val createResponse = createDataNode(createNode)
      if (!ValidationUtils.hasError(createResponse)) {
        TelemetryManager.log("Updating external props for: " + imageContentId)
        val bodyResponse = getContentProperties(contentId, externalPropsList)
        if (!ValidationUtils.hasError(bodyResponse)) {
          val extValues = bodyResponse.get(ContentStoreParams.values.name).asInstanceOf[Map[String,AnyRef]]
          if (null != extValues && !extValues.isEmpty) updateContentProperties(imageContentId, extValues)
        }
        createResponse.get("versionKey").asInstanceOf[String]
      }
      else return createResponse
    } else {
      TelemetryManager.log("Updating Image node: " + imageContentId)
      val imageNode = imageResponse.get(GraphDACParams.node.name).asInstanceOf[Node]
      imageNode.setGraphId(TAXONOMY_ID)
      imageNode.getMetadata.put(ContentAPIParams.status.name, "FlagDraft")
      val updateResponse = updateDataNode(imageNode)
      if (ValidationUtils.hasError(updateResponse)) return  updateResponse

      updateResponse.get("versionKey").asInstanceOf[String]
    }

    TelemetryManager.log("Updating Original node: " + contentId)
    val nodeResponse = getDataNode(TAXONOMY_ID, contentId)
    val originalNode = nodeResponse.get(GraphDACParams.node.name).asInstanceOf[Node]
    originalNode.getMetadata.put(ContentAPIParams.status.name, "Retired")
    val retireResponse = updateDataNode(originalNode)
    if (!ValidationUtils.hasError(retireResponse)) {
      if (StringUtils.equalsIgnoreCase(originalNode.getMetadata.get("mimeType").asInstanceOf[String], "application/vnd.ekstep.content-collection"))
        deleteHierarchy(List[String](contentId))

      getSuccessResponse.getResult.put("node_id", contentId)
      getSuccessResponse.getResult.put("versionKey", versionKey)
      return getSuccessResponse
    } else return retireResponse

  }

  def publishByType(request: org.ekstep.commons.Request, publishType: String) : Response ={
    val params = request.params.getOrElse(Map())

    val contentId = params.getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
    ValidationUtils.isValidContentId(contentId)

    val requestBody = JSONUtils.deserialize[RequestBody](request.body.get)
    val contentMap = requestBody.request.get("content").get.asInstanceOf[Map[String, AnyRef]]

    contentMap + "publish_type" ->  publishType

    val node: Node = getNodeForOperation(contentId, "publish")
    isNodeUnderProcessing(node, "Publish")

    if (null != contentMap && !(ValidationUtils.isValidList(contentMap.getOrElse("publishChecklist","")))) {
      contentMap + "publishChecklist" -> null
    }
    val publisher = contentMap.getOrElse("lastPublishedBy","").asInstanceOf[String]
    node.getMetadata.putAll(contentMap.asJava)
    node.getMetadata.put("rejectReasons", null)
    node.getMetadata.put("rejectComment", null)
    if (StringUtils.isNotBlank(publisher)) {
      TelemetryManager.log("LastPublishedBy: " + publisher)
      node.getMetadata.put(GraphDACParams.lastUpdatedBy.name, publisher)
    }
    else {
      node.getMetadata.put("lastPublishedBy", null)
      node.getMetadata.put(GraphDACParams.lastUpdatedBy.name, null)
    }

    val response = try {
       new PublishManager().publish(contentId, node)
    } catch {
      case e: ClientException =>
        throw e
      case e: ServerException =>
        throw e
      case e: Exception =>
        throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.toString, "Error occured during content publish")
    }

    TelemetryManager.log("Returning 'Response' Object.")
    if (StringUtils.endsWith(response.getResult.get("node_id").toString, ".img")) {
      val identifier: String = response.getResult.get("node_id").asInstanceOf[String]
      val new_identifier: String = identifier.replace(".img", "")
      TelemetryManager.log("replacing image id with content id in response" + identifier + new_identifier)
      response.getResult.replace("node_id", identifier, new_identifier)
    }
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
