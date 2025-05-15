package com.something.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.something.dao.domain.SignEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* @author rock
* @description 针对表【sign】的数据库操作Mapper
* @createDate 2025-05-08 10:39:11
* @Entity domain.com.something.common.mapper.SignEntity
*/
@Mapper
public interface SignMapper extends BaseMapper<SignEntity> {

    SignEntity getMaxTimeNow();
}




