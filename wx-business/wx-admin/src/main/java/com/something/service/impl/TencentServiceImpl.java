package com.something.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.something.config.CustomConfigProperties;
import com.something.core.utils.DateUtil;
import com.something.dao.domain.IpAddress;
import com.something.dao.service.IpAddressService;
import com.something.dto.ip.IpDetailVo;
import com.something.dto.TencentResp;
import com.something.service.TencentService;
import com.something.utils.OkHttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TencentServiceImpl implements TencentService {

    private final CustomConfigProperties customConfigProperties;
    private final IpAddressService ipAddressService;
    @Override
    public IpDetailVo getIPDetail(String ip) {
        IpAddress ipAddress = ipAddressService.getIPDetail(ip);
        if (ipAddress != null && DateUtil.isOlderThanDays(ipAddress.getCreatedTime(), 180)) {
            return IpDetailVo.transformIPAddress(ipAddress);
        }

        List<String> keys = customConfigProperties.getMapKeys();
        int index = (int) (Math.random() * keys.size());
        String key = keys.get(index);
        String params = "?key=" + key + "&ip=" + ip;
        String url = customConfigProperties.getIpDetailUrl() + params;
        log.info("url:{}", url);
        String result = OkHttpUtils.get(customConfigProperties.getIpDetailUrl() + params);
        log.info("result:{}", result);
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        TencentResp tencentResp = JSONObject.parseObject(result, TencentResp.class);
        IpDetailVo detailVo = JSONObject.parseObject(tencentResp.getResult().toString(), IpDetailVo.class);
        IpAddress ipAddressQ = IpDetailVo.transformIpDetailVo(detailVo);
        if (ipAddress != null) {
            ipAddressQ.setId(ipAddress.getId());
        }
        ipAddressService.saveOrUpdate(ipAddressQ);
        return detailVo;
    }
}
