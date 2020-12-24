package com.graphql.subscriptionwsspringbootstarter.configuration;

import com.graphql.subscriptionwsspringbootstarter.websocket.GraphQLWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketConfigurer {
    @Value("${graphql.websockets.path:/graphql-ws}")
    private String webSocketsHandlerPath;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(grapghQLWebsocketHandler(), webSocketsHandlerPath)
                .setHandshakeHandler(grapghQlWsHandshakeHandler())
                .setAllowedOrigins("*");
    }


    @Bean
    public WebSocketHandler grapghQLWebsocketHandler() {
        return new PerConnectionWebSocketHandler(GraphQLWebSocketHandler.class);
    }


    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }

    @Bean
    public DefaultHandshakeHandler grapghQlWsHandshakeHandler() {
        DefaultHandshakeHandler defaultHandshakeHandler = new DefaultHandshakeHandler();
        defaultHandshakeHandler.setSupportedProtocols("graphql-ws");
        return defaultHandshakeHandler;
    }

}
