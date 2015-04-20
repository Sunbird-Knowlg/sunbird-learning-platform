package com.ilimi.graph.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ilimi.graph.common.dto.BaseValueObject;

/**
 * This contains data (value objects) to be passed to middleware command
 * 
 * @author rayulu
 * 
 */
public class Request implements Serializable {

    private static final long serialVersionUID = -2362783406031347676L;

    protected Map<String, Object> context;

    private Map<String, BaseValueObject> request = new HashMap<String, BaseValueObject>();

    private String managerName;
    private String operation;

    {
        // Set the context here.
        Map<String, Object> currContext = ExecutionContext.getCurrent().getContextValues();
        context = currContext == null ? new HashMap<String, Object>() : new HashMap<String, Object>(currContext);
        if (ExecutionContext.getCurrent().getGlobalContext().containsKey(HeaderParam.CURRENT_INVOCATION_PATH.getParamName())) {
            context.put(HeaderParam.REQUEST_PATH.getParamName(),
                    ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CURRENT_INVOCATION_PATH.getParamName()));
        }
    }

    public Request() {
    }

    public Request(Request request) {
        this.context.putAll(request.getContext());
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    /**
     * @return the requestValueObjects
     */
    public Map<String, BaseValueObject> getRequest() {
        return request;
    }

    public void setRequest(Map<String, BaseValueObject> request) {
        this.request = request;
    }

    public BaseValueObject get(String key) {
        return request.get(key);
    }

    public void put(String key, BaseValueObject vo) {
        request.put(key, vo);
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void copyRequestValueObjects(Map<String, BaseValueObject> map) {
        if (null != map && map.size() > 0) {
            this.request.putAll(map);
        }
    }

    @Override
    public String toString() {
        return "Request [" + (context != null ? "context=" + context + ", " : "")
                + (request != null ? "requestValueObjects=" + request : "") + "]";
    }

}
