package com.something.core.constant;

/**
 * @ClassName ResponseStatus
 * @Version 1.0
 */

public enum ResponseStatus {

    OK(200, "OK"),

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    PARAMS_ERROR(406, "Params Error"),
    IP_NOT_ALLOWED(4000151, "IP Limit"),
    CONTENT_IS_EMPTY(4000152, "content is Empty"),


    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    ENCRYPT_ERROR(50001, "Encrypt Error"),
    DECRYPT_ERROR(50002, "Decrypt Error"),
    OBTAIN_IP_FAILED(50003, "Decrypt Error"),
    UPDATE_FAILED(50004, "update failed"),


    NETWORK_AUTHENTICATION_REQUIRED(999999, "Network Authentication Required");//占位用的

    private final int value;
    private final String reason;

    ResponseStatus(int value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    public int value() {
        return this.value;
    }

    public String reason() {
        return this.reason;
    }
}
