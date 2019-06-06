package org.ekstep.managers

import org.apache.commons.lang3.{BooleanUtils, StringUtils}
import org.ekstep.common.Platform
import org.ekstep.common.dto.{Request, Response}
import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.exception.{ClientException, ServerException}
import org.ekstep.common.mgr.BaseManager
import org.ekstep.common.router.RequestRouterPool
import org.ekstep.commons.{Constants, ContentErrorCodes, TaxonomyAPIParams}
import org.ekstep.graph.cache.util.RedisStoreUtil
import org.ekstep.graph.dac.enums.{GraphDACParams, SystemNodeTypes}
import org.ekstep.graph.dac.model.Node
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.common.enums.{ContentAPIParams, LearningActorNames}
import org.ekstep.learning.contentstore.{ContentStoreOperations, ContentStoreParams}
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil
import org.ekstep.searchindex.util.CompositeSearchConstants
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
    val YOUTUBE_MIMETYPE = "video/x-youtube"
    val DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX: String = ".img"

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

    protected def isNodeUnderProcessing(node: Node, operation: String): Unit = {
        val statusList = List[String](TaxonomyAPIParams.Processing.toString)
        var isProcessing = statusList.contains(node.getMetadata.get(TaxonomyAPIParams.status).asInstanceOf[String])
        if(isProcessing)
            throw new ClientException(TaxonomyErrorCodes.ERR_NODE_ACCESS_DENIED.name(), "Operation Denied! | [Cannot Apply '" + operation + "' Operation on the Content in '" + node.getMetadata.get(TaxonomyAPIParams.status).asInstanceOf[String] + "' Status.] ")
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

    protected def deletionsFor(contentId: String, mimeType: String, status: String) ={
        if (COLLECTION_MIME_TYPE.equalsIgnoreCase(mimeType) && "Live".equalsIgnoreCase(status)) { // Delete Units from ES
            val hierarchyResponse = getCollectionHierarchy(contentId)
            if (checkError(hierarchyResponse)) {
                throw new ServerException("ERR_ROOT_NODE_HIERARCHY", "Unable to fetch Hierarchy for Root Node: " + contentId + " :: " + hierarchyResponse.getParams.getErrmsg)
            }
            val rootHierarchy = hierarchyResponse.getResult.getOrDefault("hierarchy", Map()).asInstanceOf[Map[String, AnyRef]]
            val rootChildren = rootHierarchy.getOrElse("children", List()).asInstanceOf[List[Map[String, AnyRef]]]
            val childrenIdentifiers = getChildrenIdentifiers(rootChildren)
            try {
                ElasticSearchUtil.bulkDeleteDocumentById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, childrenIdentifiers.asJava)
            } catch {
                case e: Exception =>
                    TelemetryManager.error("Error occured during bulk delete in ES ", e)
                    throw new ServerException(ContentErrorCodes.ERR_CONTENT_RETIRE.toString, e.getMessage)
            }
            deleteHierarchy(List(contentId))
            RedisStoreUtil.delete(Constants.COLLECTION_CACHE_KEY_PREFIX + contentId)
        }
        if (!COLLECTION_MIME_TYPE.equalsIgnoreCase(mimeType)) {
            RedisStoreUtil.delete(contentId)
        }
    }

    private def getChildrenIdentifiers(childrens: List[Map[String, AnyRef]]):List[String] = {
        childrens.filter(child => child.getOrElse(ContentAPIParams.visibility.name(),"").toString.equalsIgnoreCase("Parent"))
          .map(child=>{
                child.getOrElse(ContentAPIParams.identifier.name(),"").asInstanceOf[String]
            }).toList
    }


}
