package org.ekstep.managers


import org.ekstep.common.dto.Response
import org.ekstep.common.exception.ClientException
import org.ekstep.common.mgr.ConvertToGraphNode
import org.ekstep.commons.{Request, RequestBody}
import org.ekstep.content.util.JSONUtils
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.graph.model.node.DefinitionDTO

import scala.collection.JavaConverters._

class ContentManager extends BaseContentManagerImpl {

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
                throw e
        }
    }

}
