package com.musicserver.config;

import com.musicserver.websocket.WebSocketAuthInterceptor;
import com.musicserver.websocket.WebSocketEventListener;
import com.musicserver.websocket.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket配置类
 * 
 * 配置STOMP消息代理和WebSocket端点
 * 支持音乐播放、在线状态、聊天等实时功能
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@ConditionalOnProperty(prefix = "websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * 配置消息代理
     * 
     * @param config 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，处理以"/topic"和"/queue"开头的消息
        config.enableSimpleBroker("/topic", "/queue", "/user")
                .setHeartbeatValue(new long[]{10000, 10000}) // 心跳间隔10秒
                .setTaskScheduler(taskScheduler()); // 使用自定义任务调度器
        
        // 设置应用程序目标前缀，客户端发送消息的目标前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目标前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
        
        log.info("WebSocket消息代理配置完成");
    }

    /**
     * 注册STOMP端点
     * 
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，支持SockJS回退选项
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域，生产环境应该配置具体域名
                .withSockJS()
                .setSessionCookieNeeded(false) // 不需要session cookie
                .setHeartbeatTime(25000) // SockJS心跳时间25秒
                .setDisconnectDelay(5000); // 断开延迟5秒
        
        // 注册原生WebSocket端点（不使用SockJS）
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
        
        log.info("WebSocket端点注册完成");
    }

    /**
     * 配置客户端入站通道拦截器
     * 
     * @param registration 通道注册器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 添加JWT认证拦截器
        registration.interceptors(webSocketAuthInterceptor);
        
        // 设置线程池配置
        registration.taskExecutor()
                .corePoolSize(8) // 核心线程数
                .maxPoolSize(16) // 最大线程数
                .queueCapacity(1000) // 队列容量
                .keepAliveSeconds(60); // 线程保活时间
        
        log.debug("WebSocket客户端入站通道配置完成");
    }

    /**
     * 配置客户端出站通道
     * 
     * @param registration 通道注册器
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 设置出站线程池配置
        registration.taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(16)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
        
        log.debug("WebSocket客户端出站通道配置完成");
    }

    /**
     * 配置WebSocket容器
     * 
     * @return ServletServerContainerFactoryBean
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        
        // 设置最大文本消息大小 (1MB)
        container.setMaxTextMessageBufferSize(1024 * 1024);
        
        // 设置最大二进制消息大小 (1MB)
        container.setMaxBinaryMessageBufferSize(1024 * 1024);
        
        // 设置最大会话空闲超时时间 (30分钟)
        container.setMaxSessionIdleTimeout(30 * 60 * 1000L);
        
        // 设置异步发送超时时间 (5秒)
        container.setAsyncSendTimeout(5000L);
        
        log.info("WebSocket容器配置完成");
        return container;
    }

    /**
     * WebSocket事件监听器
     * 
     * @param onlineUserService 在线用户服务
     * @return WebSocketEventListener
     */
    @Bean
    public WebSocketEventListener webSocketEventListener(OnlineUserService onlineUserService) {
        return new WebSocketEventListener(onlineUserService);
    }

    /**
     * 配置任务调度器
     * 
     * @return TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("websocket-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }
}