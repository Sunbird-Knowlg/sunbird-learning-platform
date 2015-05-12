package com.ilimi.util;

public class AuditLogUtil {

    public static String createObjectId(String graphId, String objectId) {
        String auditObjectId = "";
        if(objectId == null) {
            auditObjectId = "g-"+graphId;
        } else {
            auditObjectId = "n-"+graphId+"::"+objectId;
        }
        return auditObjectId;
    }
}
