package com.something.controller;

import com.something.dto.ip.IpDetailVo;
import com.something.service.TencentService;
import com.something.utils.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CommonToolController {

    private final TencentService tencentService;

    @GetMapping("/common/getIpAddress")
    public String getIpAddress(HttpServletRequest request) {
        return IpUtils.getIpAddress(request);
    }


    @GetMapping("/common/ipInfo")
    public IpDetailVo getIpInfo(@RequestParam(value = "ip") String ip) {
        return tencentService.getIPDetail(ip);
    }


}
