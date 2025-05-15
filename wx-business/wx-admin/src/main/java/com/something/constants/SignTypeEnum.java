package com.something.constants;

import lombok.Getter;

@Getter
public enum SignTypeEnum {
    /**
     *  1为签到，2为签退；默认为2
     */

    SIGNING(1, "签到"),
    SINGOUT(2, "签退"),
    UNKNOWN(3, "UNKNOWN");

    private final Integer code;
    private final String desc;

    SignTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


}
