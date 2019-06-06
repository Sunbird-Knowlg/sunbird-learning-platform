package org.ekstep.managers

import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.exception.ClientException
import org.ekstep.common.mgr.BaseManager
import org.ekstep.commons.{ContentErrorCodes, TaxonomyAPIParams}
import org.ekstep.graph.dac.enums.{GraphDACParams, SystemNodeTypes}
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.LearningActorNames
import org.ekstep.learning.contentstore.{ContentStoreOperations, ContentStoreParams}
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.telemetry.logger.TelemetryManager

import scala.collection.JavaConverters._

class BaseContentManagerImpl extends BaseManager {

    val DEFAULT_FRAMEWORK: String = {if (Platform.config.hasPath("platform.framework.default"))Platform.config.getString("platform.framework.default") else "NCF"}
    val TAXONOMY_ID: String = "domain"
    val CONTENT_OBJECT_TYPE = "Content"
    val CONTENT_IMAGE_OBJECT_TYPE = "ContentImage"
    val parentVisibilityList:List[String] = Platform.config.getStringList("content.metadata.visibility.parent").asScala.toList
    val PLUGIN_MIMETYPE = "application/vnd.ekstep.plugin-archive"
    val ECML_MIMETYPE = "application/vnd.ekstep.ecml-archive"
    val COLLECTION_MIME_TYPE = "application/vnd.ekstep.content-collection"
    val DEFAULT_CONTENT_VERSION: Int = 1
    val LATEST_CONTENT_VERSION:Int = 2
    val publishedStatus:List[String] = List("Live", "Unlisted", "Flagged")

    /**
      * Get definition
      * @param graphId
      * @param objectType
      * @return
      */
    protected def getDefinition(graphId: String, objectType: String): DefinitionDTO = {
        val request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.object_type.name, objectType)
        val response = getResponse(request)
        if (!checkError(response)) {
            val definition = response.get(GraphDACParams.definition_node.name).asInstanceOf[DefinitionDTO]
            return definition
        }
        null
    }

    /**
      * Fetch external Prop List
      * @param definitionDTO
      * @return
      */
    def getExternalPropList(definitionDTO: DefinitionDTO): List[String] = {
        if (null != definitionDTO) {
            definitionDTO.getProperties.asScala.toList.filter(prop => prop.getDataType.equalsIgnoreCase("external")).map(prop => {prop.getPropertyName.trim}).toList
        } else{
            List[String]()
        }
    }

    /**
      * Throws client error for restricted property update
      * @param definition
      * @param map
      * @param props
      */
    protected def restrictProps(definition: DefinitionDTO, map: Map[String, AnyRef], props: String*): Unit = {
        props.foreach(prop => {
            if(!(definition.getMetadata.asScala.getOrElse("allowupdate_" + prop, false)).asInstanceOf[Boolean] && map.contains(prop))
                throw new ClientException(ContentErrorCodes.ERR_CONTENT_UPDATE.toString, "Error! " + prop + " can't be set for the content.")
        })
    }

    /**
      * Create DataNode
      * @param node
      * @return
      */
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

    /**
      * Updated Cassandra with externalProperties
      * @param contentId
      * @param properties
      * @return
      */
    protected def updateContentProperties(contentId: String, properties: Map[String, AnyRef])= {
        val request = new org.ekstep.common.dto.Request()
        request.setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name)
        request.setOperation(ContentStoreOperations.updateContentProperties.name)
        request.put(ContentStoreParams.content_id.name, contentId)
        request.put(ContentStoreParams.properties.name, properties)
        val response = getResponse(request, LearningRequestRouterPool.getRequestRouter)
        response
    }

    protected def getNodeForOperation(identifier: String, operation: String) : Node = {
        val imageNodeResponse: Response = getDataNode(TAXONOMY_ID, identifier + ".img")
        if(checkError(imageNodeResponse)){
            val contentResponse = getDataNode(TAXONOMY_ID, identifier)
            if(checkError(contentResponse))
                throw new ClientException("ERROR_GET_NODE", "Error while fetching node for " + operation + " for content id : " + identifier +" " + contentResponse.getParams.getErrmsg)

            val node = contentResponse.getResult.get(GraphDACParams.node.name).asInstanceOf[Node]
            val status:String = node.getMetadata.get("status").asInstanceOf[String]
            if(!status.isEmpty && publishedStatus.contains(status)){
                createImageNode(identifier, node)
            }else
                node
        }else
            imageNodeResponse.getResult.get(GraphDACParams.node.name).asInstanceOf[Node]
    }

    protected def createImageNode(identifier: String, node: Node) = {
        val imageNode = new Node(TAXONOMY_ID, SystemNodeTypes.DATA_NODE.name, CONTENT_IMAGE_OBJECT_TYPE)
        imageNode.setGraphId(TAXONOMY_ID)
        imageNode.setIdentifier(identifier + ".img")
        imageNode.setMetadata(node.getMetadata)
        imageNode.setInRelations(node.getInRelations)
        imageNode.setOutRelations(node.getOutRelations)
        imageNode.getMetadata.put(TaxonomyAPIParams.status.toString, TaxonomyAPIParams.Draft.toString)
        val response = createDataNode(imageNode)
        if (checkError(response)) throw new ClientException("ERR_IMAGE_NODE_CREATION", response.getParams.getErrmsg + " Content Id: " + node.getIdentifier)
        val resp = getDataNode(TAXONOMY_ID, identifier + ".img")
        resp.get(GraphDACParams.node.name).asInstanceOf[Node]
    }
}
