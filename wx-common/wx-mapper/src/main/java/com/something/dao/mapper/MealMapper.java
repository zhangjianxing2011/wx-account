package com.something.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.something.dao.domain.MealEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* @author rock
* @description 针对表【meal】的数据库操作Mapper
* @createDate 2025-05-08 10:39:11
* @Entity domain.com.something.common.mapper.MealEntity
*/
@Mapper
public interface MealMapper extends BaseMapper<MealEntity> {
    MealEntity getMaxTimeNow();
}




