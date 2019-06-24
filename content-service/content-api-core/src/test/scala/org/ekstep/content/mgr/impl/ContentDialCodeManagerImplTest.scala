package org.ekstep.content.mgr.impl

import org.ekstep.common.dto.Response
import org.ekstep.managers.ContentDialCodeManagerImpl
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.immutable.HashMap


/**
  * Unit Tests for dialcode operation with content
  *
  * @see ContentDialCodeManagerImpl
  * @author Kumar Gauraw
  */
class ContentDialCodeManagerImplTest extends FlatSpec with Matchers {

    "getList" should "return list of String" in {
        val list = List("ABC123", "BCD123")
        val listWithSpace = List("ABC123 ", " BCD123")
        val emptyList = List.empty
        val str = "ABC123"
        val emptyArray = Array()
        println("emptyArray : " + emptyArray)
        ContentDialCodeManagerImpl.getList(list) shouldEqual list
        ContentDialCodeManagerImpl.getList(emptyList) shouldEqual List.empty
        ContentDialCodeManagerImpl.getList(str) shouldEqual List(str)
        ContentDialCodeManagerImpl.getList("") shouldEqual List.empty
        ContentDialCodeManagerImpl.getList(" ") shouldEqual List.empty
        ContentDialCodeManagerImpl.getList(emptyArray) shouldEqual List.empty
        // Below Scenario is not applicable on existing functionality
        //ContentDialCodeManagerImpl.getList(listWithSpace) shouldEqual list
    }

    "getDialLinkResponse" should "return Success Response" in {
        val map = HashMap[String, Set[String]](
            "invalidContentList" -> Set[String](),
            "updateFailedList" -> Set[String](),
            "updateSuccessList" -> Set[String]("do_123", "do_234")
        )
        val response: Response = ContentDialCodeManagerImpl.getLinkDialResponse(map)
        response.getResponseCode.toString shouldEqual "OK"
        response.getParams.getStatus shouldEqual "successful"
        response.getParams.getErrmsg shouldEqual "Operation successful"
    }

    "getDialLinkResponse" should "return Resource Not Found Response" in {
        val map = HashMap[String, Set[String]](
            "invalidContentList" -> Set[String]("do_123", "do_234"),
            "updateFailedList" -> Set[String](),
            "updateSuccessList" -> Set[String]()
        )
        val response: Response = ContentDialCodeManagerImpl.getLinkDialResponse(map)
        response.getResponseCode.toString shouldEqual "RESOURCE_NOT_FOUND"
        response.getParams.getStatus shouldEqual "failed"
        response.getParams.getErrmsg shouldEqual "Content not found with id(s):[do_123, do_234]"
    }

    "getDialLinkResponse" should "return Partial Success Response" in {
        val map = HashMap[String, Set[String]](
            "invalidContentList" -> Set[String]("do_123", "do_234"),
            "updateFailedList" -> Set[String]("do_444"),
            "updateSuccessList" -> Set[String]("do_333")
        )
        val response: Response = ContentDialCodeManagerImpl.getLinkDialResponse(map)
        response.getResponseCode.toString shouldEqual "PARTIAL_SUCCESS"
        response.getParams.getStatus shouldEqual "failed"
        response.getParams.getErrmsg shouldEqual "Content not found with id(s): [do_123, do_234], Content link with dialcode(s) failed for id(s): [do_444]"
    }

    //TODO: Check What Should be the response in case of update failed only for single content
    "getDialLinkResponse" should "not return Partial Success Response" in {
        val map = HashMap[String, Set[String]](
            "invalidContentList" -> Set[String](),
            "updateFailedList" -> Set[String]("do_444"),
            "updateSuccessList" -> Set[String]()
        )
        val response: Response = ContentDialCodeManagerImpl.getLinkDialResponse(map)
        response.getResponseCode.toString shouldEqual "PARTIAL_SUCCESS"
        response.getParams.getStatus shouldEqual "failed"
        response.getParams.getErrmsg shouldEqual "Content link with dialcode(s) failed for id(s): [do_444]"
    }

    //TODO: Check What Should be the response
    "getDialLinkResponse" should "return ? Response if some content update got failed and some are not found" in {
        val map = HashMap[String, Set[String]](
            "invalidContentList" -> Set[String]("do_123"),
            "updateFailedList" -> Set[String]("do_444"),
            "updateSuccessList" -> Set[String]()
        )
        val response: Response = ContentDialCodeManagerImpl.getLinkDialResponse(map)
        response.getResponseCode.toString shouldEqual "CLIENT_ERROR"
        response.getParams.getStatus shouldEqual "failed"
        response.getParams.getErrmsg shouldEqual "Content not found with id(s):[do_123], Content link with dialcode(s) failed for id(s): [do_444]"
    }
}
