package com.fb.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Broker tin nhắn Redis Pub/Sub
 */
@Slf4j
@Component
public class RedisMessageBroker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer container;

    public RedisMessageBroker(RedisTemplate<String, Object> redisTemplate,
                              RedisMessageListenerContainer container) {
        this.redisTemplate = redisTemplate;
        this.container = container;
    }

    /**
     * Gửi tin nhắn đến channel
     */
    public void publish(String channel, Object message) {
        log.debug("Gửi tin nhắn đến channel {}: {}", channel, message);
        redisTemplate.convertAndSend(channel, message);
    }

    /**
     * Đăng ký lắng nghe channel
     */
    public void subscribe(String channel, MessageListener listener) {
        container.addMessageListener(listener, new ChannelTopic(channel));
    }

    /**
     * Hủy đăng ký lắng nghe
     */
    public void unsubscribe(String channel, MessageListener listener) {
        container.removeMessageListener(listener, new ChannelTopic(channel));
    }
}
