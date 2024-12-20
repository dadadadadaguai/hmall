package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.api.domain.dto.ItemDTO;
import com.hmall.api.domain.dto.OrderDetailDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现类
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

  private final IOrderDetailService detailService;
  private final CartClient cartClient;
  private final ItemClient itemClient;
  private final RabbitTemplate rabbitTemplate;

  @Override
  @GlobalTransactional
  public Long createOrder(OrderFormDTO orderFormDTO) {
    // 1.订单数据
    Order order = new Order();
    // 1.1.查询商品
    List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
    // 1.2.获取商品id和数量的Map
    Map<Long, Integer> itemNumMap =
        detailDTOS.stream()
            .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
    Set<Long> itemIds = itemNumMap.keySet();
    // 1.3.查询商品
    List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
    if (items == null || items.size() < itemIds.size()) {
      throw new BadRequestException("商品不存在");
    }
    // 1.4.基于商品价格、购买数量计算商品总价：totalFee
    int total = 0;
    Long userId = UserContext.getUser();
    for (ItemDTO item : items) {
      total += item.getPrice() * itemNumMap.get(item.getId());
    }
    order.setTotalFee(total);
    // 1.5.其它属性
    order.setPaymentType(orderFormDTO.getPaymentType());
    order.setUserId(userId);
    order.setStatus(1);
    // 1.6.将Order写入数据库order表中
    save(order);

    // 2.保存订单详情
    List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
    detailService.saveBatch(details);

    // 3.清理购物车商品，采用队列实现异步
    //    cartClient.deleteCartItemByIds(itemIds);
    try {
      rabbitTemplate.convertAndSend("trade.topic", "order.create", itemIds);
      log.info("id={}用户进行下单", userId);
      //          new MessagePostProcessor() {
      //            @Override
      //            public Message postProcessMessage(Message message) throws AmqpException {
      //              log.info("id={}用户进行下单，删除购物车对应商品成功", userId);
      //              message.getMessageProperties().setHeader("user-info", userId);
      //              return message;
      //            }
      //          });
    } catch (AmqpException e) {
      log.info("下单失败", e);
    }

    // 4.扣减库存
    try {
      itemClient.deductStock(detailDTOS);
    } catch (Exception e) {
      log.info("扣减库存失败", e);
    }
    return order.getId();
  }

  @Override
  @Transactional
  public void markOrderPaySuccess(Long orderId) {
    Order order = new Order();
    order.setId(orderId);
    order.setStatus(2);
    order.setPayTime(LocalDateTime.now());
    updateById(order);
  }

  private List<OrderDetail> buildDetails(
      Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
    List<OrderDetail> details = new ArrayList<>(items.size());
    for (ItemDTO item : items) {
      OrderDetail detail = new OrderDetail();
      detail.setName(item.getName());
      detail.setSpec(item.getSpec());
      detail.setPrice(item.getPrice());
      detail.setNum(numMap.get(item.getId()));
      detail.setItemId(item.getId());
      detail.setImage(item.getImage());
      detail.setOrderId(orderId);
      details.add(detail);
    }
    return details;
  }
}
