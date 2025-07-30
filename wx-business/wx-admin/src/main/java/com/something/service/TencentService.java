package com.something.service;

import com.something.dto.ip.IpDetailVo;

public interface TencentService {

    /**
     *  获取IP详情
     *
     * @param ip
     * @return
     */
    IpDetailVo getIPDetail(String ip);
}
