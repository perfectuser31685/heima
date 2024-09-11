package com.heima.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import com.heima.gateway.config.AuthProperties;
import com.heima.gateway.utils.JwtTool;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter,Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取Request
        ServerHttpRequest request = exchange.getRequest();
        //判断是否要登录拦截，无需拦截的直接放行
        List<String> excludePaths = authProperties.getExcludePaths();
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }

        //获取token,默认保存到名为authorization的请求头中
        request.getHeaders().get("authorization");
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if (!CollUtils.isEmpty(headers)) {
            token = headers.get(0);
        }
        //检验token
        Long userId = null;
        try{
            userId = jwtTool.parseToken(token).get("userId",Long.class);
            System.out.println(userId);
        }catch (UnauthorizedException e){
            //抛出异常，就要获取响应体，并响应
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //用户信息放入请求头传递信息
        String userInfo = userId.toString();
        exchange.mutate().request(builder -> builder.header("user-info",userInfo));
        //放行
        return chain.filter(exchange);
    }

    private boolean isExclude(String antPath) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if(antPathMatcher.match(pathPattern, antPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
