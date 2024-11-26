package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.domain.dto.ItemDTO;
import com.hmall.api.domain.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class ItemClientFallBackFactory implements FallbackFactory<ItemClient> {
  @Override
  public ItemClient create(Throwable cause) {
    return new ItemClient() {
      @Override
      public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        log.error("查询商品失败", cause);
        return CollUtils.emptyList();
      }

      @Override
      public void deductStock(List<OrderDetailDTO> items) {
        log.error("扣除库存失败", cause);
        throw new RuntimeException(cause);
      }
    };
  }
}
