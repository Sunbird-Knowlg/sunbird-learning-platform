package com.ilimi.orchestrator.dac.model;

import org.apache.commons.lang3.StringUtils;

public enum RequestRouters {

    GRAPH_REQUEST_ROUTER, LANGUAGE_REQUEST_ROUTER;
    
    public static boolean isValidRequestRouter(String str) {
        if (StringUtils.isBlank(str))
            return false;
        RequestRouters val = null;
        try {
            val = RequestRouters.valueOf(str.toUpperCase().trim());
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }
}
