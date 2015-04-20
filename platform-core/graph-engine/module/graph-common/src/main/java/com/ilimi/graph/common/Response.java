package com.ilimi.graph.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.exception.ResponseCode;

/**
 * 
 * @author rayulu
 * 
 */
public class Response implements Serializable {

    private static final long serialVersionUID = -3773253896160786443L;

    private Map<String, BaseValueObject> result = new HashMap<String, BaseValueObject>();
    private Status status;
    private ResponseCode responseCode = ResponseCode.OK;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setResponseCode(ResponseCode code) {
        this.responseCode = code;
    }

    public ResponseCode getResponseCode() {
        return this.responseCode;
    }

}
