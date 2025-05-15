package com.something.core.exception;

import com.something.core.constant.ResponseStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Date 2023/5/18
 * @Version 1.0
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class MyException extends RuntimeException {
    private Integer code;
    private String message;

    public MyException() {
        super();
    }

    public MyException(ResponseStatus ResponseStatus) {
        this.code = ResponseStatus.value();
        this.message = ResponseStatus.reason();
    }

    public MyException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


}
