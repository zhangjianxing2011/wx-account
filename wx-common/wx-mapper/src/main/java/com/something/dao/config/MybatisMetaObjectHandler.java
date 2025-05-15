package com.something.dao.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * @Date 2023/5/18
 * @Version 1.0
 * @Url https://blog.csdn.net/BADAO_LIUMANG_QIZHI/article/details/89450006
 *
 */

@Configuration
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * @TableField(fill = FieldFill.INSERT)
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date date = new Date();
        this.setFieldValByName("createdAt", date, metaObject);
        this.setFieldValByName("updatedAt", date, metaObject);
        this.setFieldValByName("isDel", 0, metaObject);
    }

    /**
     * @TableField(fill = FieldFill.INSERT_UPDATE)
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Date date = new Date();
        this.setFieldValByName("updatedAt", date, metaObject);
    }

}
