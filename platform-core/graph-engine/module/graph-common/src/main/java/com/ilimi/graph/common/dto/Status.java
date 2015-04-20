package com.ilimi.graph.common.dto;

/**
 * 
 * @author santhosh
 * 
 */
public class Status extends BaseValueObject {

    private static final long serialVersionUID = 6772142067149203497L;
    private String code;
    private String status;
    private String message;

    public enum StatusType {

        SUCCESS, WARNING, ERROR;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Status [" + (code != null ? "statusCode=" + code + ", " : "") + (status != null ? "statusType=" + status + ", " : "")
                + (message != null ? "statusMessage=" + message + ", " : "") + "]";
    }
}
