package org.ekstep.commons


/**
  * Utility Object for DIAL Operations with Content
  *
  * @author Kumar Gauraw
  */
object DialCodeUtil {

    private def getRequestList(reqObj: AnyRef): List[Map[String, AnyRef]] = {
        if (reqObj.isInstanceOf[List])
            reqObj.asInstanceOf[List[Map[String, AnyRef]]]
        else
            List(reqObj.asInstanceOf[Map[String, AnyRef]])
    }


}
