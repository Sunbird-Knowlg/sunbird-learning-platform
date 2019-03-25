package org.ekstep.jobs.samza.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DaggitAPIRequest {
    public static final String REQUEST_ID = "unique API ID";
    public static final String VERSION = "1.0";
    private final String identifier;

    public DaggitAPIRequest(String identifier) {
        this.identifier = identifier;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> m = new HashMap<String, Object>();
        m.put("id", REQUEST_ID);
        m.put("ts", new Date().toString());
        m.put("ver", VERSION);
        m.put("request", getRequest());
        m.put("params", new HashMap<String, String>());

        return m;
    }

    private HashMap<String,Object> getRequest() {
        HashMap<String, Object> request = new HashMap<String, Object>();
        HashMap<String, Object> input = new HashMap<String, Object>();
        HashMap<String, Object> identifierMap = new HashMap<String, Object>();
        ArrayList<Map> content = new ArrayList<Map>();
        identifierMap.put("identifier", identifier);
        content.add(identifierMap);
        input.put("content", content);
        request.put("input", input);
        return request;
    }
}
