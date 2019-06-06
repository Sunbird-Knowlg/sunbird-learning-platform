package org.ekstep.managers


import org.apache.commons.lang3.StringUtils
import org.ekstep.common.dto.Response
import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.exception.{ClientException, ResponseCode, ServerException}
import org.ekstep.common.mgr.ConvertToGraphNode
import org.ekstep.commons.{Constants, ContentErrorCodes, Request, RequestBody, TaxonomyAPIParams, ValidationUtils}
import org.ekstep.content.mimetype.mgr.IMimeTypeManager
import org.ekstep.content.publish.PublishManager
import org.ekstep.content.util.{JSONUtils, MimeTypeManagerFactory}
import org.ekstep.graph.common.DateUtils
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.ContentAPIParams
import org.ekstep.graph.dac.model.Node
import org.ekstep.learning.contentstore.ContentStoreParams
import org.ekstep.telemetry.logger.TelemetryManager

import scala.collection.JavaConverters._

object ContentManager extends BaseContentManagerImpl {

    def create(request: Request): Response = {
        val requestBody = JSONUtils.deserialize[RequestBody](request.body.get)
        var contentMap = requestBody.request.getOrElse("content", throw new ClientException("ERR_CONTENT_INVALID_OBJECT", "Invalid Request")).asInstanceOf[Map[String, AnyRef]]

        val mimeType: String = contentMap.getOrElse("mimeType", throw new ClientException("ERR_CONTENT_INVALID_CONTENT_MIMETYPE_TYPE", "Mime Type cannot be empty")).asInstanceOf[String]
        val contentType: String = contentMap.getOrElse("contentType", throw new ClientException("ERR_CONTENT_INVALID_CONTENT_TYPE", "Content Type cannot be empty")).asInstanceOf[String]
        val code: String = contentMap.getOrElse("code", throw new ClientException("ERR_CONTENT_INVALID_CODE", "Content code cannot be empty")).asInstanceOf[String]

        val definition: DefinitionDTO = getDefinition(TAXONOMY_ID, CONTENT_OBJECT_TYPE)
        restrictProps(definition, contentMap, "status")
        val framework: String = contentMap.getOrElse("framework", DEFAULT_FRAMEWORK).asInstanceOf[String]
        contentMap = contentMap + ("framework" -> framework)

        if (parentVisibilityList.contains(contentType))
            contentMap = contentMap + ("visibility" -> "Parent")

        if (PLUGIN_MIMETYPE.equalsIgnoreCase(mimeType)) {
            contentMap = contentMap + ("identifier" -> code)
        } else {
            contentMap = contentMap + ("osId" -> "org.ekstep.quiz.app")
        }
        if (COLLECTION_MIME_TYPE.equalsIgnoreCase(mimeType) || ECML_MIMETYPE.equalsIgnoreCase(mimeType))
            contentMap += ("version" -> LATEST_CONTENT_VERSION.asInstanceOf[AnyRef])
        else contentMap += ("version" -> DEFAULT_CONTENT_VERSION.asInstanceOf[AnyRef])

        val externalPropList: List[String] = getExternalPropList(definition)
        val externalPropMap: Map[String, AnyRef] = externalPropList.map(prop => (prop, contentMap.getOrElse(prop, ""))).toMap
        contentMap = contentMap.filterKeys(key => !externalPropList.contains(key))

        try {
            val node = ConvertToGraphNode.convertToGraphNode(contentMap.asJava, definition, null)
            node.setObjectType(CONTENT_OBJECT_TYPE)
            node.setGraphId(TAXONOMY_ID)
            val response = createDataNode(node)
            if (checkError(response))
                response
            else {
                if (!externalPropMap.isEmpty) {
                    val identifier: String = response.getResult.asScala.getOrElse(GraphDACParams.node_id.name, "").asInstanceOf[String]
                    val externalUpdateResponse: Response = updateContentProperties(identifier, externalPropMap)
                    if (checkError(externalUpdateResponse))
                        externalUpdateResponse
                    else
                        response
                } else
                    response
            }
        } catch {
            case e: Exception =>
                e.printStackTrace()
                throw e
        }
    }

    def review(request: org.ekstep.commons.Request) : Response ={
        val contentId = request.params.getOrElse(Map()).getOrElse(Constants.IDENTIFIER,"").asInstanceOf[String]

        val node = getNodeForOperation(contentId, "review")
        isNodeUnderProcessing(node, "Review")
        val body = getContentBody(node.getIdentifier)

        node.getMetadata.asScala += ContentAPIParams.body.name -> body
        node.getMetadata.asScala += TaxonomyAPIParams.lastSubmittedOn.toString -> DateUtils.formatCurrentDate

        val mimeType = node.getMetadata.getOrDefault(ContentAPIParams.mimeType.name,"assets").asInstanceOf[String]

        val artifactUrl = node.getMetadata.getOrDefault(ContentAPIParams.artifactUrl.name,"").asInstanceOf[String]
        val license = node.getMetadata.getOrDefault("license","").asInstanceOf[String]

        if (YOUTUBE_MIMETYPE.equalsIgnoreCase(mimeType) && StringUtils.isBlank(artifactUrl) && StringUtils.isBlank(license)) ValidationUtils.checkYoutubeLicense(artifactUrl, node)

        val contentType = node.getMetadata.getOrDefault("contentType","").asInstanceOf[String]
        val mimeTypeManager: IMimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType)
        val response = mimeTypeManager.review(contentId, node, false)

        response
    }

    def publishByType(request: org.ekstep.commons.Request, publishType: String) : Response ={
        val params = request.params.getOrElse(Map())
        val contentId = params.getOrElse(Constants.IDENTIFIER,"").asInstanceOf[String]
        val requestBody = JSONUtils.deserialize[RequestBody](request.body.get)
        val contentMap = requestBody.request.getOrElse("content", throw new ClientException("ERR_CONTENT_INVALID_OBJECT", "Invalid Request")).asInstanceOf[Map[String, AnyRef]]
        //adding the publish type(public or unlisted) as given
        contentMap + "publish_type" ->  publishType

        val node: Node = getNodeForOperation(contentId, "publish")
        isNodeUnderProcessing(node, "Publish")

        if (!contentMap.contains("publishChecklist") || (contentMap.contains("publishChecklist") && contentMap.get("publishChecklist").isEmpty)) {
            contentMap + "publishChecklist" -> null
        }

        node.getMetadata.asScala ++ (contentMap)
        node.getMetadata.asScala += "rejectReasons" -> null
        node.getMetadata.asScala += "rejectComment"-> null
        val publisher = contentMap.getOrElse("lastPublishedBy",null)
        node.getMetadata.asScala += GraphDACParams.lastUpdatedBy.name -> publisher

        val response = try {
            new PublishManager().publish(contentId, node)
        } catch {
            case e: ClientException =>
                TelemetryManager.error("Error occured during content publish: ",e)
                throw e
            case e: ServerException =>
                TelemetryManager.error("Error occured during content publish: ",e)
                throw e
            case e: Exception =>
                TelemetryManager.error("Error occured during content publish ",e)
                throw new ServerException(ContentErrorCodes.ERR_CONTENT_PUBLISH.toString, "Error occured during content publish")
        }

        contentCleanUp(contentMap)
        return response
    }

    def retire(request: org.ekstep.commons.Request) : Response ={

        val contentId = request.params.getOrElse(Map()).getOrElse(Constants.IDENTIFIER, "").asInstanceOf[String]
        val response = getDataNode(TAXONOMY_ID, contentId)
        if (checkError(response))
            return response

        val node = response.get(GraphDACParams.node.name).asInstanceOf[Node]
        val mimeType = node.getMetadata.getOrDefault(ContentAPIParams.mimeType.name,"").asInstanceOf[String]
        val status = node.getMetadata.getOrDefault(ContentAPIParams.status.name,"").asInstanceOf[String]

        if (StringUtils.equalsIgnoreCase(ContentAPIParams.Retired.name, status))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_RETIRE.toString, "Content with Identifier [" + contentId + "] is already Retired.")

        val imageNodeResponse = getDataNode(TAXONOMY_ID, contentId+DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)
        val isImageNodeExist = if (!checkError(imageNodeResponse)) true else false
        val identifiers = if (isImageNodeExist) List[String](contentId, contentId+DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX) else List[String](contentId)

        val params = Map[String, AnyRef]()
        params + "status" -> "Retired"
        params + ("lastStatusChangedOn" -> DateUtils.formatCurrentDate)

        val responseUpdated = updateDataNodes(params, identifiers, TAXONOMY_ID)
        if (checkError(responseUpdated)) return responseUpdated

        deletionsFor(contentId, mimeType, status)

        val responseNode = getDataNode(TAXONOMY_ID, contentId)
        val updatedNode = responseNode.get("node").asInstanceOf[Node]
        val res = getSuccessResponse()
        res.put(ContentAPIParams.node_id.name, updatedNode.getIdentifier)
        res.put(ContentAPIParams.versionKey.name, updatedNode.getMetadata.get("versionKey"))
        return res

    }


}
