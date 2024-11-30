package com.hmall.api.client.fallback;

import com.hmall.api.client.UserClient;
import com.hmall.common.exception.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class UserClientFallBackFactory implements FallbackFactory<UserClient> {

  @Override
  public UserClient create(Throwable cause) {
    return new UserClient() {
      @Override
      public void deductMoney(String pw, Integer amount) {
        log.error("扣除用户余额失败", cause);
        throw new BizIllegalException(cause);
      }
    };
  }
}
