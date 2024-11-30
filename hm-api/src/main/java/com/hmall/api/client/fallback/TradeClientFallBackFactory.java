package com.hmall.api.client.fallback;

import com.hmall.api.client.TradeClient;
import com.hmall.common.exception.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class TradeClientFallBackFactory implements FallbackFactory<TradeClient> {

  @Override
  public TradeClient create(Throwable cause) {
    return new TradeClient() {
      @Override
      public void markOrderPaySuccess(Long orderId) {
        log.error("标记订单支付成功失败", cause);
        throw new BizIllegalException(cause);
      }
    };
  }
}
