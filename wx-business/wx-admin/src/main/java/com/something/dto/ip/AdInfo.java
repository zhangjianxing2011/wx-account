package com.something.dto.ip;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdInfo {
    private String nation;//中国
    private String province;//台湾
    private String city;//新竹市
    private String district;//湖口乡
    private Integer adcode;//区号
    @JSONField(name = "nation_code")
    private Integer nationCode;//码
}
