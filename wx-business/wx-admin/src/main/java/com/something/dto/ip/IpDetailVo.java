package com.something.dto.ip;

import com.alibaba.fastjson2.annotation.JSONField;
import com.something.dao.domain.IpAddress;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IpDetailVo {
    private String ip;
    private Location location;
    @JSONField(name = "ad_info")
    private AdInfo adInfo;
    public static IpDetailVo transformIPAddress(IpAddress ipAddress) {
        IpDetailVo ipDetailVo = new IpDetailVo();
        ipDetailVo.setIp(ipAddress.getIp());
        ipDetailVo.setLocation(new Location(ipAddress.getLat(), ipAddress.getLng()));
        ipDetailVo.setAdInfo(new AdInfo().setProvince(ipAddress.getProvince()).setCity(ipAddress.getCity()).setDistrict(ipAddress.getDistrict()).setAdcode(ipAddress.getAdcode()).setNationCode(ipAddress.getNationCode()));
        return ipDetailVo;
    }

    public static IpAddress transformIpDetailVo(IpDetailVo ipDetailVo) {
        IpAddress ipAddress = new IpAddress();

        ipAddress.setIp(ipDetailVo.getIp());

        ipAddress.setLat(ipDetailVo.getLocation().getLat());
        ipAddress.setLng(ipDetailVo.getLocation().getLng());

        ipAddress.setNation(ipDetailVo.getAdInfo().getNation());
        ipAddress.setProvince(ipDetailVo.getAdInfo().getProvince());
        ipAddress.setCity(ipDetailVo.getAdInfo().getCity());
        ipAddress.setDistrict(ipDetailVo.getAdInfo().getDistrict());
        ipAddress.setAdcode(ipDetailVo.getAdInfo().getAdcode());
        ipAddress.setNationCode(ipDetailVo.getAdInfo().getNationCode());
        return ipAddress;
    }

}
