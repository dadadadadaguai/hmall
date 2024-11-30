package com.hmall.api.client.fallback;

import com.hmall.api.client.CartClient;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;

@Slf4j
public class CartClientFallBackFactory implements FallbackFactory<CartClient> {

  @Override
  public CartClient create(Throwable cause) {
    return new CartClient() {
      @Override
      public void deleteCartItemByIds(Collection<Long> ids) {
        log.error("删除购物车失败", cause);
        throw new BizIllegalException(cause);
      }
    };
  }
}
