package com.lee.gateway.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
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
        Object attribute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

        long start = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long end = System.currentTimeMillis();
            log.info("实际调用地址为：{}，调用耗时为：{}ms", attribute, (end - start));
        }));
    }

    @Override
    public int getOrder() {
        // 优先级设置最低，先让TokenValFilter被调用
        return Ordered.LOWEST_PRECEDENCE;
    }
}
