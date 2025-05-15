package com.something.core.constant;

/**
 *  全局统一返回对象
 * @Date 2023/5/18
 * @Version 1.0
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultMsg<T> implements Serializable {

    /**
     * 返回状态(成功:true 失败:false)
     */
    private boolean success;
    /**
     * 状态码(成功:200 失败:500)
     */
    private Integer code;
    /**
     * 异常提示消息
     */
    private String msg;
    /**
     * 返回参数
     */
    private T data;

    public ResultMsg() {
    }

    public ResultMsg(Integer code, String msg) {
        this.success = false;
        this.code = code;
        this.msg = msg;
    }

    public ResultMsg(boolean success, ResponseStatus ResponseStatus) {
        this.success = success;
        this.code = ResponseStatus.value();
        this.msg = ResponseStatus.reason();
    }

    public ResultMsg(ResponseStatus ResponseStatus) {
        this.success = false;
        this.code = ResponseStatus.value();
        this.msg = ResponseStatus.reason();
    }

    public ResultMsg(T data) {
        this.success = true;
        this.code =  ResponseStatus.OK.value();
        this.msg = ResponseStatus.OK.reason();
        this.data = data;
    }

    public ResultMsg(String data) {
        this.success = true;
        this.code = ResponseStatus.OK.value();
        this.msg = ResponseStatus.OK.reason();
        this.data = (T) data;
    }
}


