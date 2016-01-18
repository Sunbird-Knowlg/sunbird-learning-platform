package com.ilimi.orchestrator.dac.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class RequestPath implements Serializable {

    private static final long serialVersionUID = 1370901881625105226L;
    private String type;
    private String url;
    @Field("path_params")
    private List<String> pathParams;
    @Field("request_params")
    private List<String> requestParams;
    @Field("body_params")
    private List<String> bodyParams;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(List<String> pathParams) {
        this.pathParams = pathParams;
    }

    public List<String> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<String> requestParams) {
        this.requestParams = requestParams;
    }

    public List<String> getBodyParams() {
        return bodyParams;
    }

    public void setBodyParams(List<String> bodyParams) {
        this.bodyParams = bodyParams;
    }

}
