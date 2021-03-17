package org.sunbird.common.exception;

public class ServerException extends MiddlewareException {

    private static final long serialVersionUID = 1650554603214529226L;

    public ServerException(String errCode, String message) {
        super(errCode, message);
    }

    public ServerException(String errCode, String message, Object... params) {
        super(errCode, message, params);
    }

    public ServerException(String errCode, String message, Throwable root) {
        super(errCode, message, root);
    }

    public ServerException(String errCode, String message, Throwable root, Object... params) {
        super(errCode, message, root, params);
    }

    public ResponseCode getResponseCode() {
        return ResponseCode.SERVER_ERROR;
    }
}
