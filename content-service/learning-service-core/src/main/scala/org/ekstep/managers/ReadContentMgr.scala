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

  def read(request: org.ekstep.commons.Request):String = {

    LearningRequestRouterPool.init()
    SearchRequestRouterPool.init(RequestRouterPool.getActorSystem())

    val params = request.params.getOrElse(Map())

    val identifier: String = "do_1125252239566028801496"//params.getOrElse("identifier", "").asInstanceOf[String]
    val objectType: String = "Content"//params.getOrElse("objectType", "").asInstanceOf[String]
    val fields: List[String] = params.getOrElse("fields", List()).asInstanceOf[List[String]]
    val mode: String = params.getOrElse("mode", "").asInstanceOf[String]

    println("read operaions called " +identifier)

    val requestDto = getRequest("domain", GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition", GraphDACParams.object_type.name, "Content");//buildRequest("domain", objectType)

    val definitionDto = getResponse(requestDto).get(GraphDACParams.definition_node.name).asInstanceOf[DefinitionDTO]

    val resultNode:Node = mode match {
      case EDIT_MODE =>
        editMode(identifier, objectType)
      case _ =>
        nonEditMode(identifier, objectType)
    }

    //TODO: non-edit mode
    val contentMap = ConvertGraphNode.convertGraphNode(resultNode,"domainId", definitionDto, null)

    val externalPropsResp = getExternalProps(identifier,objectType, fields, definitionDto)
    //val resProps = externalPropsResp.get(TaxonomyAPIParams.values.name).asInstanceOf[org.ekstep.common.dto.Response]


    val responseJson = new ObjectMapper().writeValueAsString(contentMap)
    println("responseJson === "+responseJson)

    // TODO: return as Response case
    return responseJson //new Response("Id","12345", "tsvalue",null,"",null)

  }


  private def buildRequest(graphId: String, objectType: String): org.ekstep.common.dto.Request = {

    val request = new org.ekstep.common.dto.Request() {
      getContext.put(GraphHeaderParams.graph_id.toString, graphId)
      setManagerName(GraphEngineManagers.SEARCH_MANAGER)
      setOperation("getDataNode")
      put(GraphDACParams.object_type.name, objectType)
    }
    return request

  }

  private def nonEditMode(identifier: String, objectType: String): Node ={
    val responseNode = getDataNode("domain", identifier)
    val node:Node = responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
    return node

  }

  private def editMode(identifier: String, objectType: String): Node ={
    val imageContentId = identifier+".img"
    val responseNode = getDataNode("domain", imageContentId)
    responseNode.get(GraphDACParams.node.name).asInstanceOf[Node]
  }

  private def getExternalProps(identifier: String,objectType: String, fields: List[String], definitionDTO: DefinitionDTO) = {
    val externalPropsList = getExternalPropList(objectType, definitionDTO)
    val propsList = fields.intersect(externalPropsList)

    if (propsList.nonEmpty) {
      val request: dto.Request = new dto.Request() {
        setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name())
        setOperation("getContentProperties")
        put("content_id", identifier)
        put("properties", propsList.asJava)
      }
      val response = getResponse(request, LearningRequestRouterPool.getRequestRouter())


    } else {
      Map[String, AnyRef]().asJava
    }
  }

  private def getExternalPropList(objectType: String, definitionDTO: DefinitionDTO): List[String] = {
    println("definitionDTO= "+definitionDTO.getProperties())
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
