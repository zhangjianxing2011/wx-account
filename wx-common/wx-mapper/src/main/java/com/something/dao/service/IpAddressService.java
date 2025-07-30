package com.something.dao.service;

import com.something.dao.domain.IpAddress;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author rock
* @description 针对表【ip_address】的数据库操作Service
* @createDate 2025-07-30 11:16:54
*/
public interface IpAddressService extends IService<IpAddress> {

    IpAddress getIPDetail(String ip);

}
