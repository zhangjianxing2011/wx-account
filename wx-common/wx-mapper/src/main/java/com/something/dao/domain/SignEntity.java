package com.something.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName sign
 */
@TableName(value = "sign")
@Data
public class SignEntity implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 今天的日期
     */
    private String timeNow;

    /**
     * 签到时间
     */
    private String signInTime;

    /**
     * 签退时间
     */
    private String signOutTime;

    /**
     * 签到照片
     */
    private String signInPic;

    /**
     * 签退照片
     */
    private String signOutPic;


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
        sb.append(", timeNow=").append(timeNow);
        sb.append(", signInTime=").append(signInTime);
        sb.append(", signOutTime=").append(signOutTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updateAt=").append(updatedAt);
        sb.append(", isDel=").append(isDel);
        sb.append(", signInPic=").append(signInPic);
        sb.append(", signOutPic=").append(signOutPic);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}