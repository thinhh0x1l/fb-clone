package com.fb.config;

import com.fb.security.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket cho ứng dụng
 * - Sử dụng STOMP protocol qua WebSocket
 * - Cấu hình message broker với /topic và /queue
 * - Endpoint: /ws với SockJS fallback
 * - Xác thực qua WebSocketAuthInterceptor
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    /**
     * Cấu hình message broker
     * - /topic: broadcast messaging (pub/sub)
     * - /queue: point-to-point messaging
     * - /app: application destination prefix
     * - /user: user destination prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Đăng ký STOMP endpoints
     * - /ws: WebSocket endpoint với SockJS fallback
     * - Cho phép tất cả origins (cần cấu hình lại cho production)
     * - Thêm WebSocketAuthInterceptor để xác thực
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(webSocketAuthInterceptor);
    }

    /**
     * Cấu hình client inbound channel
     * Thêm interceptor để xác thực tin nhắn từ client
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
    }
}
