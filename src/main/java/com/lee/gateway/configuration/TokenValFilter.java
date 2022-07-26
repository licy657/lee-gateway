package com.lee.gateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @decription:
 * @author: lichangyi
 * @date: 2022/7/25 18:58
 * @version: 1.0
 */

@Slf4j
@Component
public class TokenValFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        log.info("请求的url：{}", uri);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
