package com.something.dao.mapper;

import com.something.dao.domain.IpAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author rock
* @description 针对表【ip_address】的数据库操作Mapper
* @createDate 2025-07-30 11:16:54
* @Entity com.something.dao.domain.IpAddress
*/
public interface IpAddressMapper extends BaseMapper<IpAddress> {
    IpAddress queryByIp(@Param("ip") String ip);

}




