package org.ekstep.actor.read

import java.util
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.Patterns
import akka.util.Timeout
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang.StringUtils
import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.api.{APIIds, Request}
import org.ekstep.common._
import org.ekstep.common.exception.ResponseCode
import org.ekstep.learning.actor.ContentStoreActor
import org.ekstep.learning.common.enums.LearningActorNames
import org.ekstep.searchindex.dto.SearchDTO
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil
import org.ekstep.searchindex.processor.SearchProcessor
import org.ekstep.searchindex.util.{CompositeSearchConstants, ObjectDefinitionCache}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


object ReadAPIActor extends BaseAPIActor {

  val processor = new SearchProcessor()
  val ERR_INVALID_REQUEST: String = "ERR_INVALID_REQUEST"

  val system = ActorSystem.create("learningActor")

  val learningActor = system.actorOf(Props[ContentStoreActor], name = "learningActor")


  override def preStart() = {
    ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, Platform.config.getString
    ("search.es_conn_info"))
  }

  val mapper = new ObjectMapper

  override def onReceive(request: Request): Unit = {
    val result = request.apiId match {
      case APIIds.READ_CONTENT =>
        readContent(request)
      case APIIds.READ_FRAMEWORK =>
        readFramework(request);
      case APIIds.READ_CHANNEL =>
        readChannel(request);
      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }
  }


  def readContent(request: Request) = {
    val params = request.params.getOrElse(Map())

    val identifier: String = params.getOrElse("identifier", "").asInstanceOf[String]
    val objectType: String = params.getOrElse("objectType", "").asInstanceOf[String]
    val fields: List[String] = params.getOrElse("fields", List()).asInstanceOf[List[String]]
    val mode: String = params.getOrElse("mode", "").asInstanceOf[String]
    val externalPropsList = getExternalPropList(objectType) //List[String]("screenshots", "body")

    val externalPropsResp = getExternalProps(identifier, fields.intersect(externalPropsList))

    if (mode.equalsIgnoreCase("edit")) {
      val searchResponse = search(List(identifier, (identifier + ".img")), List(objectType, (objectType + "Image")), request.apiId, Option(fields))
      val response = searchResponse.map(searchList => {
        if (!searchList.isEmpty) {
          var content: util.Map[String, AnyRef] = null
          if (searchList.size() == 2) {
            for (contentData <- searchList.asScala.toList) {
              if (contentData.asInstanceOf[util.Map[String, AnyRef]].containsValue(identifier + ".img")) {
                content = contentData.asInstanceOf[util.Map[String, AnyRef]]
              }
            }
          } else {
            content = searchList.get(0).asInstanceOf[util.HashMap[String, AnyRef]]
          }
          if (content.isEmpty) {
            errorResponseSerialized(request.apiId, ERR_INVALID_REQUEST, "Content not found", ResponseCode
              .RESOURCE_NOT_FOUND)
          } else {
            content.put("identifier", identifier)
            content.put("objectType", objectType)
            content.putAll(externalPropsResp)
            content = getRelationsMetadata(content, objectType)
            val result = new org.ekstep.common.dto.Response() {
              put(objectType.toLowerCase(), content)
            }
            OK(request.apiId, result)
          }
        } else {
          errorResponseSerialized(request.apiId, ERR_INVALID_REQUEST, "Content not found", ResponseCode
            .RESOURCE_NOT_FOUND)

        }
      })(getContext().dispatcher)

      Patterns.pipe(response, getContext().dispatcher).to(sender())
    } else {
      val searchResponse = search(List(identifier), List(objectType, (objectType + "Image")), request.apiId, Option(fields))

      val response = searchResponse.map(searchList => {
        if (!searchList.isEmpty) {
          var content: util.Map[String, AnyRef] = searchList.get(0).asInstanceOf[util.HashMap[String, AnyRef]]
          content.putAll(externalPropsResp)
          content = getRelationsMetadata(content, objectType)
          val result = new org.ekstep.common.dto.Response() {
            put(objectType.toLowerCase(), content)
          }
          OK(request.apiId, result)
        } else {
          errorResponseSerialized(request.apiId, ERR_INVALID_REQUEST, "Content not found", ResponseCode
            .RESOURCE_NOT_FOUND)
        }
      })(getContext().dispatcher)

      Patterns.pipe(response, getContext().dispatcher).to(sender())
    }


  }

  private def getExternalProps(identifier: String, propsList: List[String]) = {
    if (propsList.nonEmpty) {
      val request: dto.Request = new dto.Request() {
        setManagerName(LearningActorNames.CONTENT_STORE_ACTOR.name())
        setOperation("getContentProperties")
        put("content_id", identifier)
        put("properties", propsList.asJava)
      }

      val future = Patterns.ask(learningActor, request, 20000);
      val result = Await.result(future, Duration.create("30 second"));
      val response = result.asInstanceOf[org.ekstep.common.dto.Response];

      if (response.getParams.getStatus.equalsIgnoreCase("successful")) {
        response.get("values").asInstanceOf[util.Map[String, AnyRef]]
      } else {
        Map[String, AnyRef]().asJava
      }
    } else {
      Map[String, AnyRef]().asJava
    }
  }

  private def search(identifier: List[String], objectType: List[String], apiId: String, fields: Option[List[String]]): Future[util
  .List[AnyRef]]
  = {
    val searchDTO = new SearchDTO() {
      val properties = new util.ArrayList[java.util.HashMap[String, Any]]() {
        add(new util.HashMap[String, Any]() {
          put("operation", "EQ")
          put("propertyName", "identifier")
          put("values", identifier.asJava)
        })
        add(new util.HashMap[String, Any]() {
          put("operation", "EQ")
          put("propertyName", "objectType")
          put("values", objectType.asJava)
        })
        add(new util.HashMap[String, Any]() {
          put("operation", "EQ")
          put("propertyName", "status")
          put("values", new util.ArrayList[String]())
        })
      }
      setProperties(properties.asInstanceOf[util.List[java.util.Map[_, _]]])
      var fieldList = new util.ArrayList[String]()
      fields.getOrElse(List()).foreach(s => fieldList.add(s))
      setFields(fieldList)
      setOperation("AND")
    }

    val searchResponse = processor.processSearchQuery(searchDTO, false, CompositeSearchConstants
      .COMPOSITE_SEARCH_INDEX, false)

    searchResponse
  }


  def readFramework(request: Request) = {
    val identifier: String = request.params.getOrElse(Map()).getOrElse("identifier", "").asInstanceOf[String]
    val objectType: String = request.params.getOrElse(Map()).getOrElse("objectType", "").asInstanceOf[String]
    val returnCategories: List[String] = request.params.getOrElse(Map()).getOrElse("categories", "")
      .asInstanceOf[String].split(",").toList

    if (identifier.isEmpty || objectType.isEmpty) {

    }

    val searchResponse = search(List(identifier), List(objectType), request.apiId, None)
    val response = searchResponse.map(searchResult => {
      if (!CollectionUtils.isEmpty(searchResult)) {
        val responseMap = searchResult.get(0).asInstanceOf[util.Map[String, AnyRef]].asScala
        if (StringUtils.isNotEmpty(responseMap.getOrElse("fw_hierarchy", "").asInstanceOf[String])) {
          mapper.registerModule(DefaultScalaModule)
          val hierarchy: Map[String, Any] = mapper.readValue(responseMap.getOrElse("fw_hierarchy", "").asInstanceOf[String], classOf[Map[String, String]])
          val categories: List[Map[String, Any]] = hierarchy.getOrElse("categories", List()).asInstanceOf[List[Map[String,
            Any]]]

          if (returnCategories.nonEmpty) {
            val resWithCategories = responseMap += "categories" -> categories.filter(p =>
              returnCategories.contains(p("code")))

            val actCategories = resWithCategories.getOrElse("categories", List()).asInstanceOf[List[Map[String, Any]]]
              .map { category =>
                val terms = removeAssociations(category.getOrElse("terms", List()).asInstanceOf[List[Map[String, Any]]], returnCategories)
                category + ("terms" -> terms);
              }

            responseMap += "categories" -> actCategories

          }
          else {
            responseMap += "categories" -> categories
          }
        }
        responseMap -= ("fw_hierarchy")
        val result = new org.ekstep.common.dto.Response() {
          put(objectType.toLowerCase(), responseMap)
        }

        OK(request.apiId, result)
      } else {
        errorResponseSerialized(request.apiId, ERR_INVALID_REQUEST, "Framework not found", ResponseCode
          .RESOURCE_NOT_FOUND)
      }
    })(getContext().dispatcher)

    Patterns.pipe(response, getContext().dispatcher).to(sender())
  }


  def removeAssociations(terms: List[Map[String, Any]], returnCategories: List[String]): List[Map[String, Any]] = {
    if (terms.nonEmpty) {
      terms.map { term =>
        val associations = term.getOrElse("associations", List()).asInstanceOf[List[Map[String, Any]]]
          .filter(p => returnCategories.contains(p("category")))
        val updatedTerm = if (associations.isEmpty)
          term - "associations"
        else
          term + ("associations" -> associations)

        val children = removeAssociations(term.getOrElse("children", List()).asInstanceOf[List[Map[String, Any]]], returnCategories)
        if (children.nonEmpty)
          updatedTerm + ("children" -> children)
        else
          updatedTerm
      }
    } else {
      terms
    }
  }

  def readChannel(request: Request) = {
    val identifier: String = request.params.getOrElse(Map()).getOrElse("identifier", "").asInstanceOf[String]
    val objectType: String = request.params.getOrElse(Map()).getOrElse("objectType", "").asInstanceOf[String]

    if (identifier.isEmpty) {
      errorResponseSerialized(request.apiId, ERR_INVALID_REQUEST, "Identifier is empty", ResponseCode.CLIENT_ERROR)
    }

    val searchResponse = search(List(identifier), List(objectType), request.apiId, None)

    val response = searchResponse.map(searchResult => {
      if (searchResult.isEmpty) {
        errorResponseSerialized(request.apiId, ERR_INVALID_REQUEST, "Channel not found", ResponseCode.RESOURCE_NOT_FOUND)
      } else {
        val responseMap = searchResult.get(0).asInstanceOf[util.Map[String, Any]].asScala
        val frameworks = responseMap.getOrElse("frameworks", new util.ArrayList[String]()).asInstanceOf[util.List[String]].asScala.toList

        if (!frameworks.isEmpty) {
          val framework = Await.result(search(frameworks, List("Framework"), request.apiId, Some(List("name", "description", "objectType", "code"))), Duration.create(30, TimeUnit.SECONDS))
          responseMap += "frameworks" -> framework.asScala
        } else {
          responseMap += "suggested_frameworks" -> getAllFrameworkList()
        }
        val result = new org.ekstep.common.dto.Response() {
          put(objectType.toLowerCase(), responseMap)
        }
        OK(request.apiId, result)
      }
    })(getContext().dispatcher)

    Patterns.pipe(response, getContext().dispatcher).to(sender())
  }


  private def getAllFrameworkList(): util.List[AnyRef] = {
    val searchDto = new SearchDTO() {
      val properties = new util.ArrayList[java.util.HashMap[String, Any]]() {
        add(new util.HashMap[String, Any]() {
          put("operation", "EQ")
          put("propertyName", "objectType")
          put("values", "Framework")
        })
        add(new util.HashMap[String, Any]() {
          put("operation", "EQ")
          put("propertyName", "status")
          put("values", "Live")
        })
      }
      setFuzzySearch(false)
      setProperties(properties.asInstanceOf[util.List[java.util.Map[_, _]]])
      setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND)
      setFields(new util.ArrayList[String]() {
        add("name")
        add("code")
        add("description")
      })
    }
    Await.result(processor.processSearchQuery(searchDto, false, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, false), Timeout(Duration.create(30, TimeUnit.SECONDS)).duration)

  }

  private def getExternalPropList(objectType: String): List[String] = {
    val definitionNode = ObjectDefinitionCache.getDefinitionNode(objectType, "domain")
    val list: List[String] = List[String]()
    for (key <- definitionNode.keySet().asScala) {
      if (definitionNode.get(key).asInstanceOf[util.Map[String, AnyRef]].get("dataType").asInstanceOf[String].equalsIgnoreCase("external"))
        list + definitionNode.get(key).asInstanceOf[util.Map[String, AnyRef]].get("propertyName").asInstanceOf[String]
    }
    list
  }


  private def getRelationsMetadata(content: util.Map[String, AnyRef], objectType: String): util.Map[String, AnyRef] = {
    val relationMap = ObjectDefinitionCache.getRelationDefinition(objectType, "domain")
    val relations = relationMap.values().asScala.toList
    val idMap = content.asScala.filter(entry => relations.contains(entry._1)).toMap
    val idList = idMap.values.toList.map(_.asInstanceOf[util.List[String]].asScala.toList).flatten
    val searchResponse = Await.result(search(idList.asInstanceOf[List[String]], List[String](), "", Some(List[String]("name", "description", "index"))), Duration.create(30, TimeUnit.SECONDS))
    if(!searchResponse.isEmpty){
      val relationResponse = searchResponse.asScala.map(item => item.asInstanceOf[util.Map[String, AnyRef]].get("identifier") -> item).toMap
      val result = idMap.map(entry => entry._1 -> entry._2.asInstanceOf[util.List[String]].asScala.map(item => relationResponse.get(item).get).toList).toMap
      content.putAll(result.asJava)
    }
    return content
  }
}



