package com.something.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName ip_address
 */
@TableName(value ="ip_address")
@Data
public class IpAddress {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 
     */
    @TableField(value = "lat")
    private Double lat;

    /**
     * 
     */
    @TableField(value = "lng")
    private Double lng;

    /**
     * 
     */
    @TableField(value = "nation")
    private String nation;

    /**
     * 
     */
    @TableField(value = "province")
    private String province;

    /**
     * 
     */
    @TableField(value = "city")
    private String city;

    /**
     * 
     */
    @TableField(value = "district")
    private String district;

    /**
     * 
     */
    @TableField(value = "adcode")
    private Integer adcode;

    /**
     * 
     */
    @TableField(value = "nation_code")
    private Integer nationCode;

    /**
     * 
     */
    @TableField(value = "created_time")
    private Date createdTime;

}