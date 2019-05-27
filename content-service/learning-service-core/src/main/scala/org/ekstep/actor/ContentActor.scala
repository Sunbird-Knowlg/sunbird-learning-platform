package org.ekstep.actor

import org.ekstep.actor.core.BaseAPIActor
import org.ekstep.commons.{APIIds, Request}
import org.ekstep.managers.ReadContentMgr

object ContentActor extends BaseAPIActor{

  val readContentMgr = new ReadContentMgr()

  override def onReceive(request: Request) = {

    request.apiId match {
      case APIIds.READ_CONTENT =>
        readContentMgr.read(request)


      //case APIIds.CREATE_CONTENT =>

      //case APIIds.UPDATE_CONTENT =>


      case _ =>
        invalidAPIResponseSerialized(request.apiId);
    }

  }


}
