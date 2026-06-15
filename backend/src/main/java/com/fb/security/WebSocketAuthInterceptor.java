package com.fb.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

/**
 * Bộ chặn xác thực WebSocket
 * Xác thực JWT token trong quá trình handshake WebSocket
 * và tạo WebSocketPrincipal chứa thông tin người dùng
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Xác thực trước khi thực hiện handshake WebSocket
     * Trích xuất JWT token từ header hoặc query parameter
     * @return true nếu xác thực thành công
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getUserIdFromToken(token).toString();

                WebSocketPrincipal principal = new WebSocketPrincipal(userId, email);
                attributes.put("principal", principal);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                return true;
            }
        } catch (Exception e) {
            log.error("Xác thực WebSocket thất bại", e);
        }
        return false;
    }

    /**
     * Callback sau khi handshake hoàn thành (không sử dụng)
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Không xử lý
    }

    /**
     * Trích xuất JWT token từ request WebSocket
     * Hỗ trợ cả Authorization header và query parameter 'token'
     */
    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String bearerToken = servletRequest.getServletRequest().getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
            String tokenParam = servletRequest.getServletRequest().getParameter("token");
            if (StringUtils.hasText(tokenParam)) {
                return tokenParam;
            }
        }
        return null;
    }

    /**
     * Principal cho WebSocket chứa thông tin người dùng
     */
    public static class WebSocketPrincipal implements Principal {
        private final Long userId;
        private final String email;

        public WebSocketPrincipal(Long userId, String email) {
            this.userId = userId;
            this.email = email;
        }

        @Override
        public String getName() {
            return email;
        }

        public Long getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }
    }
}
