package com.manifest.gateway.filter;

import com.manifest.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 全局 JWT 鉴权过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 不需要鉴权的白名单路径 */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单直接放行
        if (WHITE_LIST.stream().anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            Claims claims = JwtUtil.parseToken(token.substring(7));
            // 将用户信息写入 Header，传给下游微服务
            ServerHttpRequest mutableReq = exchange.getRequest().mutate()
                    .header("user-id", claims.getSubject())
                    .header("company-code", JwtUtil.getCompanyCode(token.substring(7)))
                    .build();
            return chain.filter(exchange.mutate().request(mutableReq).build());
        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
