package com.something.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "custom")
public class CustomConfigProperties {
    private String authorization;
    private Boolean taskStatus;
    private String staticUser;
    private String staticRole;
    private String picPath;
    private List<String> mapKeys;
    private String ipDetailUrl;

}
