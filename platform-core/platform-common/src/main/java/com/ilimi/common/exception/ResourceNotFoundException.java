package com.ilimi.common.exception;

public class ResourceNotFoundException extends MiddlewareException {

    private static final long serialVersionUID = 5170597108192700962L;

    public ResourceNotFoundException(String errCode, String message) {
        super(errCode, message);
    }

    public ResourceNotFoundException(String errCode, String message, Object... params) {
        super(errCode, message, params);
    }

    public ResourceNotFoundException(String errCode, String message, Throwable root) {
        super(errCode, message, root);
    }

    public ResourceNotFoundException(String errCode, String message, Throwable root, Object... params) {
        super(errCode, message, root, params);
    }

    public ResponseCode getResponseCode() {
        return ResponseCode.RESOURCE_NOT_FOUND;
    }
}
