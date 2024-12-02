package com.hmall.common.config;

import com.hmall.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class AmqpConfig {
  @Bean
  public MessageConverter messageConverter() {
    // 1.定义消息转换器
    Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
    // 2.配置自动创建消息id，用于识别不同消息，也可以在业务中基于ID判断是否是重复消息
    jackson2JsonMessageConverter.setCreateMessageIds(true);
    return new AuthMessageConverter(jackson2JsonMessageConverter);
  }

  static class AuthMessageConverter implements MessageConverter {
    private final MessageConverter messageConverter;

    public AuthMessageConverter(MessageConverter messageConverter) {
      this.messageConverter = messageConverter;
    }

    // 发送消息
    @Override
    public Message toMessage(Object o, MessageProperties messageProperties)
        throws MessageConversionException {
      Long userId = UserContext.getUser();
      if (userId == null) {
        log.info("发送消息未获取到用户信息");
      }
      messageProperties.setHeader("user-info", userId);
      log.info("消息发送时转化成功");
      return messageConverter.toMessage(o, messageProperties);
    }

    // 接受消息
    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
      Long userId = message.getMessageProperties().getHeader("user-info");
      if (userId == null) {
        log.info("未获取到用户信息，可能为重复消息，或消息发送方未设置用户信息");
      }
      UserContext.setUser(userId);
      log.info("消息接受时转化成功");
      return messageConverter.fromMessage(message);
    }
  }
}
