package org.ekstep.orchestrator.dac.model;

public enum RequestTypes {

    GET, POST, PATCH, PUT, DELETE;

    public static boolean isValidRequestType(String str) {
        RequestTypes val = null;
        try {
            val = RequestTypes.valueOf(str);
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }
}
