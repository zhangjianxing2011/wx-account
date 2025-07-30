package com.something.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TencentResp<T> implements Serializable {
    private Integer status;
    private String message;
    private String requestId;
    private T result;
}
