package com.something.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName meal
 */
@TableName(value = "meal")
@Data
public class MealEntity implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * sign_id
     */
    private Long signId;

    /**
     * 今天的日期(2025-05-09)
     */
    private String timeNow;

    /**
     * 早餐
     */
    private String breakfast;

    /**
     * 早点
     */
    private String fruit;

    /**
     * 午餐
     */
    private String lunch;

    /**
     * 下午茶
     */
    private String lunchMiddle;

    /**
     * 备注
     */
    private String remark;

    /**
     * 原始数据
     */
    private String orginData;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    /**
     * 已删除，0为否，1为是
     */
    private Integer isDel;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", signId=").append(signId);
        sb.append(", timeNow=").append(timeNow);
        sb.append(", breakfast=").append(breakfast);
        sb.append(", fruit=").append(fruit);
        sb.append(", lunch=").append(lunch);
        sb.append(", lunchMiddle=").append(lunchMiddle);
        sb.append(", remark=").append(remark);
        sb.append(", orginData=").append(orginData);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updateAt=").append(updatedAt);
        sb.append(", isDel=").append(isDel);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}