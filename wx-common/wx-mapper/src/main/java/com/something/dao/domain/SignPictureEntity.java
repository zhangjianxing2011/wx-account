package com.something.dao.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 签到照片表
 *
 * @TableName sign_picture
 */
@TableName(value = "sign_picture")
@Data
public class SignPictureEntity implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long signId;

    /**
     * 签到或签退照片
     */
    private String signPicture;

    /**
     * 签到或签退原始照片
     */
    private String signPictureOrigin;

    /**
     * 0未爬取，1已爬取，2爬取异常
     */
    private Integer spiderStatus;

    /**
     * 1为签到，2为签退；默认为2
     */
    private Integer type;

    /**
     *
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     *
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    /**
     *
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
        sb.append(", signPicture=").append(signPicture);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", isDel=").append(isDel);
        sb.append(", type=").append(type);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}