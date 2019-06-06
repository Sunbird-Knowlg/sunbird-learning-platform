package org.ekstep.managers


import org.apache.commons.lang3.StringUtils
import org.ekstep.common.Platform
import org.ekstep.common.dto.Response
import org.ekstep.common.exception.ClientException
import org.ekstep.commons.ContentMetadata.DialCodeEnum
import org.ekstep.commons.Request
import org.ekstep.graph.dac.model.Node
import org.ekstep.learning.common.enums.ContentAPIParams

import scala.collection.mutable

/**
  * This Class holds implementation of DIAL Operations (link, reserve, release) with Content.
  *
  * @author Kumar Gauraw
  *
  */
object ContentDialCodeManagerImpl extends BaseContentManagerImpl {

    val DIALCODE_SEARCH_URI = if(Platform.config.hasPath("dialcode.api.search.url")) Platform.config.getString("dialcode.api.search.url")
    else "http://localhost:8080/learning-service/v3/dialcode/search"




    /**
      * Link DIAL Code to Neo4j Object
      *
      * @param request
      * @return
      */
    def linkDialCode(request: Request): Response = {
        try {
            //code will go here.
            println("ContentDialCodeManagerImpl ==>> linkDialCode :: start")
            return OK()
        } catch {
            case e: Exception =>
                throw e;
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
            //code will go here.
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










    private def getRequestList(reqObj: AnyRef): List[Map[String, AnyRef]] = {
        if (reqObj.isInstanceOf[List])
            reqObj.asInstanceOf[List[Map[String, AnyRef]]]
        else
            List(reqObj.asInstanceOf[Map[String, AnyRef]])
    }

    def validateReqStructure(dialcodes: List[String], contents: List[String]): Unit = {
        if (dialcodes.isEmpty || contents.isEmpty)
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.toString, "Please provide required properties in request.")
        val maxLimit: Int = {
            if (Platform.config.hasPath("dialcode.link.content.max")) Platform.config.getInt("dialcode.link.content.max") else 10
        }
        if (dialcodes.size >= maxLimit || contents.size >= maxLimit)
            throw new ClientException(DialCodeEnum.ERR_DIALCODE_LINK_REQUEST.toString, "Max limit for link content to dialcode in a request is " + maxLimit)
    }


    @throws[Exception]
    private def validateDialCodes(channelId: String, dialcodes: List[String]): Unit = {
        /*if (!dialcodes.isEmpty) {
            var resultList = null

            var invalidDialCodeList = List[String]()
            invalidDialCodeList ++ dialcodes
            val dialcodeCount = dialcodes.size

            val requestMap = new mutable.HashMap[String, AnyRef]
            val searchMap = new mutable.HashMap[String, AnyRef]
            val data = new mutable.HashMap[String, AnyRef]
            data.put(ContentAPIParams.identifier.name, dialcodes)
            searchMap.put("search", data)
            requestMap.put("request", searchMap)

            val headerParam = new HashMap[String, String]
            headerParam.put("X-Channel-Id", channelId)
            val searchResponse = HttpRestUtil.makePostRequest(DIALCODE_SEARCH_URI, requestMap, headerParam)
            if (searchResponse.getResponseCode eq ResponseCode.OK) {
                val result = searchResponse.getResult
                val count = result.get(DialCodeEnum.count.name).asInstanceOf[Integer]
                if (dialcodeCount ne count) {
                    resultList = result.get(DialCodeEnum.dialcodes.name).asInstanceOf[List[AnyRef]]
                    import scala.collection.JavaConversions._
                    for (obj <- resultList) {
                        val map = obj.asInstanceOf[Map[String, AnyRef]]
                        val identifier = map.get(ContentAPIParams.identifier.name).asInstanceOf[String]
                        invalidDialCodeList.remove(identifier)
                    }
                    throw new ResourceNotFoundException(DialCodeEnum.ERR_DIALCODE_LINK.name, "DIAL Code not found with id(s):" + invalidDialCodeList)
                }
            }
            else throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name, "Something Went Wrong While Processing Your Request. Please Try Again After Sometime!")
        }*/
    }

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
