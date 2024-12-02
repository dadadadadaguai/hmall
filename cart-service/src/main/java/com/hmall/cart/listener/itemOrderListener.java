package com.hmall.cart.listener;

import com.hmall.cart.service.impl.CartServiceImpl;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class itemOrderListener {
  private final CartServiceImpl cartService;

  @RabbitListener(
      bindings =
          @QueueBinding(
              value = @Queue(name = "cart.clear.queue", durable = "true"),
              exchange = @Exchange(name = "trade.topic", type = "topic"),
              key = "order.create"))
  public void listenItemOrder(List<Long> ids) {
    cartService.removeByItemIds(ids);
  }
}
