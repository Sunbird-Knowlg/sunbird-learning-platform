package org.sunbird.common.exception;

public class ClientException extends MiddlewareException {

    private static final long serialVersionUID = 4449645476165051068L;

    public ClientException(String errCode, String message) {
        super(errCode, message);
    }

    public ClientException(String errCode, String message, Object... params) {
        super(errCode, message, params);
    }

    public ClientException(String errCode, String message, Throwable root) {
        super(errCode, message, root);
    }

    public ClientException(String errCode, String message, Throwable root, Object... params) {
        super(errCode, message, root, params);
    }

    public ResponseCode getResponseCode() {
        return ResponseCode.CLIENT_ERROR;
    }
}
