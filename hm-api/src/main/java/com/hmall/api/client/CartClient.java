package com.hmall.api.client;

import com.hmall.api.client.fallback.CartClientFallBackFactory;
import java.util.Collection;

import com.hmall.api.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    value = "cart-service",
    configuration = DefaultFeignConfig.class,
    fallbackFactory = CartClientFallBackFactory.class)
public interface CartClient {
  @DeleteMapping("/carts")
  void deleteCartItemByIds(@RequestParam("ids") Collection<Long> ids);
}
