package com.something.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.something.dao.domain.MealEntity;

/**
* @author rock
* @description 针对表【meal】的数据库操作Service
* @createDate 2025-05-08 10:39:11
*/
public interface IMealService extends IService<MealEntity> {
    MealEntity getMaxTimeNow();
}
