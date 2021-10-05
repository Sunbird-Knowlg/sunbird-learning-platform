package org.sunbird.common.exception;

public enum ResponseCode {

    OK(200), CLIENT_ERROR(400), SERVER_ERROR(500), RESOURCE_NOT_FOUND(404), PARTIAL_SUCCESS(207);

    private int code;

    private ResponseCode(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ResponseCode getResponseCode(Integer code) {
        if (null != code) {
            try {
                ResponseCode[] arr = ResponseCode.values();
                if (null != arr) {
                    for (ResponseCode rc : arr) {
                        if (rc.code() == code)
                            return rc;
                    }
                }
            } catch (Exception e) {
            }
        }
        return ResponseCode.SERVER_ERROR;
    }
}
