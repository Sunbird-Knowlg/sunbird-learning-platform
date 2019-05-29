package controllers

import org.ekstep.commons.{APIIds, Request, Response}
import org.ekstep.managers.ReadContentMgr
import play.api.mvc.Action

class ContentController extends BaseController {

  //TODO: inject actor system. call for

  def read(identifier: String, mode: Option[String], fields: Option[List[String]]) = Action {


    val request = Request(APIIds.READ_CONTENT, None, None)

    val contentOperationMgr = new ReadContentMgr()
    val response = contentOperationMgr.read(request)

    Ok(response)

    //sendResponse(response)


  }

}
