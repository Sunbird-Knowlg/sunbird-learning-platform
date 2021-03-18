package org.sunbird.common.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.exception.ResponseCode;

/**
 * 
 * @author rayulu
 * 
 */
public class Response implements Serializable {

    private static final long serialVersionUID = -3773253896160786443L;

    private String id;
    private String ver;
    private String ts;
    private ResponseParams params;
    private ResponseCode responseCode = ResponseCode.OK;
    private Map<String, Object> result = new HashMap<String, Object>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    /**
     * @return the responseValueObjects
     */
    public Map<String, Object> getResult() {
        return result;
    }

    public Object get(String key) {
        return result.get(key);
    }

    public void put(String key, Object vo) {
        result.put(key, vo);
    }

    public ResponseParams getParams() {
        return params;
    }

    public void setParams(ResponseParams params) {
        this.params = params;
    }

    public void setResponseCode(ResponseCode code) {
        this.responseCode = code;
    }

    public ResponseCode getResponseCode() {
        return this.responseCode;
    }

}
