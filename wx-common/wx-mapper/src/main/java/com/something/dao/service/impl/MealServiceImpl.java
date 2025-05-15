package com.something.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.something.dao.domain.MealEntity;
import com.something.dao.mapper.MealMapper;
import com.something.dao.service.IMealService;
import org.springframework.stereotype.Service;

/**
* @author rock
* @description 针对表【meal】的数据库操作Service实现
* @createDate 2025-05-08 10:39:11
*/
@Service
public class MealServiceImpl extends ServiceImpl<MealMapper, MealEntity> implements IMealService {

    @Override
    public MealEntity getMaxTimeNow() {
        return baseMapper.getMaxTimeNow();
    }
}




