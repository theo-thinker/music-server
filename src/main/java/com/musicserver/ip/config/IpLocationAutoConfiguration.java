package com.musicserver.ip.config;

import com.musicserver.ip.interceptor.IpLocationInterceptor;
import com.musicserver.ip.service.IpLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * IP定位模块自动配置类
 * 
 * 自动配置IP定位相关的所有组件
 * 包括服务、拦截器、监控等
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(IpLocationProperties.class)
@Import({IpLocationConfig.class})
@ConditionalOnProperty(prefix = "ip-location", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IpLocationAutoConfiguration implements WebMvcConfigurer {

    private final IpLocationProperties properties;
    private final IpLocationService ipLocationService;

    public IpLocationAutoConfiguration(IpLocationProperties properties, 
                                     IpLocationService ipLocationService) {
        this.properties = properties;
        this.ipLocationService = ipLocationService;
        
        log.info("IP Location Auto Configuration initialized");
    }

    /**
     * 注册IP定位拦截器
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.getEnableInterceptor()) {
            log.info("Registering IP Location Interceptor");
            
            IpLocationInterceptor interceptor = new IpLocationInterceptor(ipLocationService, properties);
            
            registry.addInterceptor(interceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                            "/error",
                            "/favicon.ico",
                            "/static/**",
                            "/public/**",
                            "/assets/**",
                            "/webjars/**",
                            "/doc.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/actuator/**"
                    )
                    .order(100); // 设置拦截器优先级
        }
    }
}