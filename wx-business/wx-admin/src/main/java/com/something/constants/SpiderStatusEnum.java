package com.something.constants;

import lombok.Getter;

@Getter
public enum SpiderStatusEnum {
    /**
     * 0未爬取，1已爬取，2爬取异常
     */


    TODO(0, "未爬取"),
    DONE(1, "已爬取"),
    UNKNOWN(2, "UNKNOWN");

    private final Integer code;
    private final String desc;

    SpiderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


}
