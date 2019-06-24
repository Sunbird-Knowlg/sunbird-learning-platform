package org.ekstep.managers

import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.{HeaderParam, Response}
import org.ekstep.common.enums.TaxonomyErrorCodes
import org.ekstep.common.exception.{ClientException, ResourceNotFoundException, ResponseCode, ServerException}
import org.ekstep.commons.ContentMetadata.DialCodeEnum
import org.ekstep.commons.{Request, RequestBody}
import org.ekstep.content.util.{HttpRestUtil, JSONUtils}
import org.ekstep.graph.dac.model.Node
import org.ekstep.learning.common.enums.ContentAPIParams

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * This Class holds implementation of DIAL Operations (link, reserve, release) with Content.
  *
  * @author Kumar Gauraw
  *
  */
object ContentDialCodeManagerImpl extends BaseContentManagerImpl {

    val DIALCODE_SEARCH_URI = if (Platform.config.hasPath("dialcode.api.search.url")) Platform.config.getString("dialcode.api.search.url")
    else "http://localhost:8080/learning-service/v3/dialcode/search"

    /**
      * Link DIAL Code to Neo4j Object
      *
      * @param request
      * @return
      */
    def linkDialCode(request: Request): Response = {
        try {
            val channelId: String = request.context.get(HeaderParam.CHANNEL_ID.name).asInstanceOf[String]
            val requestData: List[Map[String, AnyRef]] = getRequestData(request)
            val requestMap: Map[String, AnyRef] = validateAndGetRequestMap(channelId, requestData)
            getLinkDialResponse(updateDialCodes(requestMap))
        } catch {
            case e: Exception => throw e;
        }
    }

    /**
      * Link DIAL Code to Collection Objects
      *
      * @param request
      * @return
      */
    def collectionLinkDialCode(request: Request): Response = {
        try {
            println("ContentDialCodeManagerImpl ==>> collectionLinkDialCode :: start")

            return OK()
        } catch {
            case e: Exception =>
                throw e;
        }
    }

    /**
      * Reserve DIAL Codes for Textbook
      *
      * @param request
      * @return
      */
    def reserveDialCode(request: Request): Response = {
        try {
            //code will go here.
            println("ContentDialCodeManagerImpl ==>> reserveDialCode :: start")
            return OK()
        } catch {
            case e: Exception =>
                throw e;
        }
    }

    /**
      * Release DIAL Codes for Textbook
      *
      * @param request
      * @return
      */
    def releaseDialCode(request: Request): Response = {
        try {
            //code will go here.
            println("ContentDialCodeManagerImpl ==>> releaseDialCode :: start")
            return OK()
        } catch {
            case e: Exception =>
                throw e;
        }
    }

    /**
      *
      * @param request
      * @return
      */
    def getRequestData(request: Request): List[Map[String, AnyRef]] = {
        val reqBody = JSONUtils.deserialize[RequestBody](request.body.get)
        val reqObj = reqBody.request.getOrElse("content",
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.toString, "Invalid Request! Please provide valid Request."))
        reqObj match {
            case reqObj: List[Map[String, AnyRef]] => reqObj
            case reqObj: Map[String, AnyRef] => List(reqObj)
            case _ => throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.toString, "Invalid Request! Please provide valid Request.")
        }
    }

    /**
      *
      * @param channelId
      * @param requestList
      * @return
      */
    def validateAndGetRequestMap(channelId: String, requestList: List[Map[String, AnyRef]]): Map[String, AnyRef] = {
        var dialList: List[String] = List.empty
        var reqMap = HashMap[String, AnyRef]()
        for (req <- requestList) {
            val contents: List[String] = getList(req.get(DialCodeEnum.identifier.toString).get)
            val dialcodes: List[String] = getList(req.get(DialCodeEnum.dialcode.toString).get)
            validateReqStructure(dialcodes, contents)
            contents.foreach(id => reqMap += (id -> dialcodes))
            dialList = dialList ++ dialcodes
        }
        val isValReq: Boolean = {
            if (Platform.config.hasPath("learning.content.link_dialcode_validation")) Platform.config.getBoolean("learning.content.link_dialcode_validation") else true
        }
        if (isValReq)
            validateDialCodes(channelId, dialList)
        reqMap
    }

    /**
      *
      * @param obj
      * @return
      */
    def getList(obj: AnyRef): List[String] = {
        (obj match {
            case obj: List[String] => obj
            case obj: String => List(obj)
            case _ => List.empty
        }).filter((x: String) => StringUtils.isNotBlank(x) && !StringUtils.equals(" ", x))
    }

    /**
      *
      * @param dialcodes
      * @param contents
      */
    def validateReqStructure(dialcodes: List[String], contents: List[String]): Unit = {
        if (null == dialcodes || null == contents || contents.isEmpty)
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.toString, "Please provide required properties in request.")
        val maxLimit: Int = {
            if (Platform.config.hasPath("dialcode.link.content.max")) Platform.config.getInt("dialcode.link.content.max") else 10
        }
        if (dialcodes.size >= maxLimit || contents.size >= maxLimit)
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.toString, "Max limit for link content to dialcode in a request is " + maxLimit)
    }

    /**
      *
      * @param channelId
      * @param dialcodes
      * @return
      */
    def validateDialCodes(channelId: String, dialcodes: List[String]): Boolean = {
        if (!dialcodes.isEmpty) {
            val reqMap = HashMap[String, AnyRef](
                "request" -> HashMap[String, AnyRef](
                    "search" -> HashMap[String, AnyRef](
                        "identifier" -> dialcodes
                    )
                )
            )

            val headerParam = HashMap[String, String]("X-Channel-Id" -> channelId)
            val searchResponse = HttpRestUtil.post(DIALCODE_SEARCH_URI, reqMap, headerParam)
            if (searchResponse.responseCode == "OK") {
                val result = searchResponse.result
                if (dialcodes.size == result.get(DialCodeEnum.count.toString).asInstanceOf[Integer]) {
                    return true
                } else {
                    val dials = result.get(DialCodeEnum.dialcodes.toString).asInstanceOf[List[Map[String, AnyRef]]].map(_.getOrElse("identifier", "")).asInstanceOf[List[String]]
                    throw new ResourceNotFoundException(DialCodeEnum.ERR_DIALCODE_LINK.toString, "DIAL Code not found with id(s):" + dialcodes.diff(dials).asJava)
                }
            }
            else {
                throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name, "Something Went Wrong While Processing Your Request. Please Try Again After Sometime!")
            }
        }
        true
    }

    /**
      *
      * @param requestMap
      * @return
      */
    def updateDialCodes(requestMap: Map[String, AnyRef]): Map[String, Set[String]] = {
        val passportKey: String = Platform.config.getString("graph.passport.key.base")
        var result = mutable.HashMap[String, mutable.Set[String]](
            "invalidContentList" -> mutable.Set[String](),
            "updateFailedList" -> mutable.Set[String](),
            "updateSuccessList" -> mutable.Set[String]()
        )
        requestMap.keys.foreach(id => {
            val dials = requestMap.getOrElse(id, List()).asInstanceOf[List[String]]
            try {
                val node: Node = getNodeForOperation(id, "update")
                if (dials.isEmpty) node.getMetadata.put("dialcodes", null) else node.getMetadata.put("dialcodes", dials.asJava)
                node.getMetadata.put("versionKey", passportKey)
                val updateResp = updateDataNode(node)
                if (!checkError(updateResp)) result.get("updateSuccessList").get.add(id) else result.get("updateFailedList").get.add(id)
            } catch {
                case e: ClientException => if (e.getErrCode == "ERROR_GET_NODE") result.get("invalidContentList").get.add(id) else result.get("updateFailedList").get.add(id)
                case ex: Exception => throw ex
            }
        })
        result.map(kv => (kv._1, kv._2.toSet)).toMap
    }

    /**
      *
      * @param resultMap
      * @return
      */
    def getLinkDialResponse(resultMap: Map[String, Set[String]]): Response = {
        val invalidContentList: Set[String] = resultMap.get("invalidContentList").get
        val updateFailedList = resultMap.get("updateFailedList").get
        val updateSuccessList = resultMap.get("updateSuccessList").get
        val response: Response = {
            if (invalidContentList.isEmpty && updateFailedList.isEmpty) {
                OK()
            } else if (!invalidContentList.isEmpty && updateFailedList.isEmpty && updateSuccessList.size == 0) {
                ERROR(DialCodeEnum.ERR_DIALCODE_LINK.toString, "Content not found with id(s):" + invalidContentList.asJava, ResponseCode.RESOURCE_NOT_FOUND)
            } else if (!invalidContentList.isEmpty && !updateFailedList.isEmpty && updateSuccessList.size == 0) {
                //TODO: check on Response Code for this scenario.
                ERROR(DialCodeEnum.ERR_DIALCODE_LINK.toString, "Content not found with id(s):" + invalidContentList.asJava + ", Content link with dialcode(s) failed for id(s): " + updateFailedList.asJava, ResponseCode.CLIENT_ERROR)
            } else {
                val resp = new Response
                resp.setResponseCode(ResponseCode.PARTIAL_SUCCESS)
                var messages = ListBuffer[String]()
                if (!invalidContentList.isEmpty) messages += ("Content not found with id(s): " + invalidContentList.asJava)
                if (!updateFailedList.isEmpty) messages += ("Content link with dialcode(s) failed for id(s): " + updateFailedList.asJava)
                resp.setParams(getErrorStatus(DialCodeEnum.ERR_DIALCODE_LINK.toString, messages.mkString(", ")))
                return resp
            }
        }
        response
    }

    /**
      *
      * @param rootIdentifier
      * @return
      */
    def validateRootNode(rootIdentifier: String): Boolean = {
        val nodeResponse = getDataNode(TAXONOMY_ID, rootIdentifier)
        if (checkError(nodeResponse))
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK.toString, "Unable to fetch Content with Identifier : [" + rootIdentifier + "]")
        val node = nodeResponse.get("node").asInstanceOf[Node]
        val mimeType = node.getMetadata.get(ContentAPIParams.mimeType.name).asInstanceOf[String]
        val visibility = node.getMetadata.get(ContentAPIParams.visibility.name).asInstanceOf[String]
        if (!StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType) &&
                !StringUtils.equalsIgnoreCase("default", visibility))
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK.toString, "Invalid Root Node Identifier : [" + rootIdentifier + "]")
        true
    }


}
