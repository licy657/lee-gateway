package com.lee.gateway.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @decription:
 * @author: lichangyi
 * @date: 2022/7/20 15:58
 * @version: 1.0
 */

@Component
@Data
@ConfigurationProperties(prefix = "gateway.common")
public class CommonProperties {

    private String dataId;

    private String groupId;

    private long timeout = 5000;
}
