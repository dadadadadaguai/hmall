package com.hmall.gateway.filters;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.AntPathMatcher;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
  private final JwtTool jwtTool;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();
  private final AuthProperties authProperties;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    if (isExclude(request.getPath().toString())) {
      // 放行
      return chain.filter(exchange);
    }
    List<String> tokens = request.getHeaders().get("Authorization");
    String token = null;
    if (!CollUtil.isEmpty(tokens)) {
      token = tokens.get(0);
    }
    Long userId = null;
    try {
      userId = jwtTool.parseToken(token);
    } catch (Exception e) {
      // 如果无效，拦截
      ServerHttpResponse response = exchange.getResponse();
      response.setRawStatusCode(401);
      return response.setComplete();
    }
    String userInfo = userId.toString();
    // 如果有效，放行
    ServerWebExchange swe =
        exchange.mutate().request(builder -> builder.header("user-info", userInfo)).build();
    return chain.filter(swe);
  }

  private boolean isExclude(String path) {
    for (String excludePath : authProperties.getExcludePaths()) {
      if (antPathMatcher.match(excludePath, path)) {
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
