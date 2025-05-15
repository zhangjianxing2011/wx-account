package com.something.core.exception;


import com.something.core.constant.ResponseStatus;

public class SystemException extends RuntimeException {
    private Integer code;
    private String message;

    public SystemException() {
        super();
    }

    public SystemException(ResponseStatus ResponseStatus) {
        this.code = ResponseStatus.value();
        this.message = ResponseStatus.reason();
    }

    public SystemException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
