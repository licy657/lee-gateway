package com.lee.gateway;

import com.lee.gateway.configuration.CommonProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GatewayApplicationTests {

    @Autowired
    private CommonProperties commonProperties;

    @Test
    void contextLoads() {
        String dataId = commonProperties.getDataId();
        String groupId = commonProperties.getGroupId();
        System.out.println(dataId);
        System.out.println(groupId);
    }

}
