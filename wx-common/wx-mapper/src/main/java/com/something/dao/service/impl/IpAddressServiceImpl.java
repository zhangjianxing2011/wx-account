package com.something.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.something.dao.domain.IpAddress;
import com.something.dao.service.IpAddressService;
import com.something.dao.mapper.IpAddressMapper;
import org.springframework.stereotype.Service;

/**
 * @author rock
 * @description 针对表【ip_address】的数据库操作Service实现
 * @createDate 2025-07-30 11:16:54
 */
@Service
public class IpAddressServiceImpl extends ServiceImpl<IpAddressMapper, IpAddress> implements IpAddressService {

    @Override
    public IpAddress getIPDetail(String ip) {
        return baseMapper.queryByIp(ip);
    }
}




