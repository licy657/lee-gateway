package com.lee.gateway.configuration;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Mono;


import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * @decription:
 * @author: lichangyi
 * @date: 2022/7/21 15:39
 * @version: 1.0
 */

@Slf4j
@Component
@RefreshScope
public class DynamicRouteConfig implements ApplicationEventPublisherAware, Listener {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionLocator routeDefinitionLocator;
    private ApplicationEventPublisher publisher;

    @Autowired
    private CommonProperties commonProperties;

    public DynamicRouteConfig(NacosConfigManager nacosConfigManager,
                              RouteDefinitionWriter routeDefinitionWriter,
                              RouteDefinitionLocator routeDefinitionLocator) {
        this.nacosConfigManager = nacosConfigManager;
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionLocator = routeDefinitionLocator;
    }


    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        log.info("线程：{}---->进行网关更新：\n\r{}", Thread.currentThread().getName(), configInfo);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    @PostConstruct
    public void init() {
        log.info("gateway route init");
        try {
            // 获取nacos配置文件
            NacosConfigProperties properties = nacosConfigManager.getNacosConfigProperties();
            // 获取nacos上的服务
            ConfigService configService = nacosConfigManager.getConfigService();
            // 获取路由uri配置文件
            String dataId = commonProperties.getDataId();
            // 获取路由uri集合
            String configInfo = configService.getConfig(commonProperties.getDataId(), commonProperties.getGroupId(), 5000);

            log.info("获取网关当前配置：\r\n{}", configInfo);
            // 更新路由
            updateRoute(configInfo);
            // 监听nacos下发的动态路由配置
            configService.addListener(dataId, properties.getGroup(), this);
            log.info("添加路由配置监听，dataId:{}", dataId);
        } catch (Exception e) {
            log.error("初始化网关发生错误！", e);
        }
    }

    /**
     *
     * @param configInfo
     */
    private void updateRoute(String configInfo) throws Exception {
        List<RouteDefinition> newRouteDefinitions = transYamConfigData(configInfo);
        // 包括配置文件中的路由
        List<RouteDefinition> oldRouteDefinationList = Optional.ofNullable(routeDefinitionLocator.getRouteDefinitions().collectList().block()).orElse(new ArrayList<>());
        // 清空所有的路由（内存中）
        oldRouteDefinationList.forEach(routeDefinition -> delete(routeDefinition.getId()));
        // 添加新路由
        newRouteDefinitions.forEach(this::add);
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 添加路由
     * @param routeDefinition
     */
    private void add(RouteDefinition routeDefinition) {
        log.info("gateway add route {}", routeDefinition);
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
    }

    /**
     * 删除路由
     *
     * @param id
     */
    private void delete(String id) {
       log.info("gateway delete route id {}", id);
        try {
            this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
        } catch (Exception e) {
            log.error("删除路由出现异常", e);
        }
    }

    private List<RouteDefinition> transYamConfigData(String configInfo) throws Exception {
        if (StringUtils.isEmpty(configInfo)) {
            return new ArrayList<>();
        }
        Yaml yaml = new Yaml();
        List<LinkedHashMap<String, Object>> yamlDataList = Optional.ofNullable((List<LinkedHashMap<String, Object>>) yaml.load(configInfo)).
                orElse(new ArrayList<>());
        log.info("yamlDataList:{}", yamlDataList);
        ObjectMapper mapper = new ObjectMapper();
        List<RouteDefinition> routeDefinitions = mapper.readValue(mapper.writeValueAsString(yamlDataList), new TypeReference<List<RouteDefinition>>() {
        });
        return routeDefinitions;
    }
}
