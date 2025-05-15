package com.something.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.something.dao.domain.SignEntity;
import com.something.dao.mapper.SignMapper;
import com.something.dao.service.ISignService;
import org.springframework.stereotype.Service;

/**
 * @author rock
 * @description 针对表【sign】的数据库操作Service实现
 * @createDate 2025-05-08 10:39:11
 */
@Service
public class SignServiceImpl extends ServiceImpl<SignMapper, SignEntity> implements ISignService {


    @Override
    public SignEntity getMaxTimeNow() {
        return baseMapper.getMaxTimeNow();
    }
}




