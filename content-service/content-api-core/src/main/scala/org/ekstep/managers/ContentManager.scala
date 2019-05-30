package org.ekstep.managers

import akka.actor.{ActorSystem, Props}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.BooleanUtils
import org.ekstep.common.dto
import org.ekstep.common.mgr.BaseManager
import org.ekstep.common.router.RequestRouterPool
import org.ekstep.graph.engine.router.GraphEngineManagers
import org.ekstep.graph.model.node.DefinitionDTO
import org.ekstep.learning.actor.ContentStoreActor
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.search.router.SearchRequestRouterPool
import org.ekstep.graph.dac.enums.GraphDACParams
import org.ekstep.learning.common.enums.{ContentErrorCodes, LearningActorNames}
import org.ekstep.commons.Constants
import org.ekstep.graph.common.enums.GraphHeaderParams

import scala.collection.JavaConverters._

/**
  * This is responsible for talking to Graph engine
  * This holds the basic required operations, can be used by all managers
  */

abstract class ContentManager extends BaseManager {


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
    getResponse(requestDto).get(GraphDACParams.definition_node.name).asInstanceOf[DefinitionDTO]

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


  /**
    * Build Request object
    *
    * @return RequestDTO
    */
  private def buildRequest(): org.ekstep.common.dto.Request = {

    val request = new org.ekstep.common.dto.Request() {
      getContext.put(GraphHeaderParams.graph_id.toString, Constants.TAXONOMY_ID)
      setManagerName(GraphEngineManagers.SEARCH_MANAGER)
      setOperation("getDataNode")
      put(GraphDACParams.object_type.name, Constants.CONTENT_OBJECT_TYPE)
    }
    return request
  }


}
