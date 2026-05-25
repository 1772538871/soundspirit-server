package com.soundspirit.config;

import com.soundspirit.util.JwtUtil;
import com.soundspirit.websocket.VoiceChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket配置
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceChatWebSocketHandler voiceChatHandler;
    private final JwtUtil jwtUtil;

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        var registration = registry.addHandler(voiceChatHandler, "/ws/voice-chat")
                .addInterceptors(new WebSocketAuthInterceptor(jwtUtil));

        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            registration.setAllowedOrigins("*");
        } else {
            registration.setAllowedOrigins(allowedOrigins.split(","));
        }
    }

    @RequiredArgsConstructor
    static class WebSocketAuthInterceptor implements HandshakeInterceptor {

        private final JwtUtil jwtUtil;

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                String token = servletRequest.getServletRequest().getParameter("token");
                if (token != null && jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    attributes.put("userId", userId);
                    return true;
                }
            }
            log.warn("WebSocket认证失败");
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }
}
