package com.ilimi.graph.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.dto.Params;
import com.ilimi.graph.common.exception.ResponseCode;

/**
 * 
 * @author rayulu
 * 
 */
public class Response implements Serializable {

    private static final long serialVersionUID = -3773253896160786443L;

    private String id;
    private String version;
    private String ts;
    private Params params;
    private ResponseCode responseCode = ResponseCode.OK;
    private Map<String, BaseValueObject> result = new HashMap<String, BaseValueObject>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
    public Map<String, BaseValueObject> getResult() {
        return result;
    }

    public BaseValueObject get(String key) {
        return result.get(key);
    }

    public void put(String key, BaseValueObject vo) {
        result.put(key, vo);
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public void setResponseCode(ResponseCode code) {
        this.responseCode = code;
    }

    public ResponseCode getResponseCode() {
        return this.responseCode;
    }

}
