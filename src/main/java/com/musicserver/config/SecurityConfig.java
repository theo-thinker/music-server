package com.musicserver.config;

import com.musicserver.security.JwtAuthenticationEntryPoint;
import com.musicserver.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置类
 * <p>
 * 配置系统的安全策略，包括：
 * 1. JWT认证和授权
 * 2. 密码加密策略
 * 3. 跨域资源共享(CORS)
 * 4. 安全过滤器链
 * 5. 异常处理
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT认证过滤器
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * JWT认证异常处理器
     */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * 用户详情服务
     */
    private final UserDetailsService userDetailsService;

    /**
     * 无需认证的公开API路径
     */
    private static final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/captcha",
            "/api/music/search",
            "/api/music/hot",
            "/api/music/recommend",
            "/api/playlist/public",
            "/api/artist/list",
            "/api/album/list",
            "/static/**",
            "/druid/**"
    };

    /**
     * Swagger文档相关路径
     */
    private static final String[] SWAGGER_URLS = {
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/favicon.ico"
    };

    /**
     * 健康检查和监控相关路径
     */
    private static final String[] ACTUATOR_URLS = {
            "/actuator/**",
            "/health",
            "/info",
            "/metrics"
    };

    /**
     * 密码编码器配置
     * 使用BCrypt算法进行密码加密，强度为10
     *
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 认证提供者配置
     * 设置用户详情服务和密码编码器
     *
     * @return DAO认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        // 设置是否隐藏用户不存在异常
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    /**
     * 认证管理器配置
     *
     * @param config 认证配置
     * @return 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS配置源
     * 配置跨域资源共享策略
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
                "X-Trace-Id", "X-Real-IP"
        ));

        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
                "X-Trace-Id", "Content-Disposition"
        ));

        // 允许发送Cookie
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤器链配置
     * 配置HTTP安全策略和过滤器
     *
     * @param http HTTP安全配置对象
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护（因为使用JWT）
                .csrf(AbstractHttpConfigurer::disable)

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理策略为无状态
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置异常处理
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 配置请求授权规则
                .authorizeHttpRequests(authz -> authz
                        // 公开API - 无需认证
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        // Swagger文档 - 开发环境允许访问
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        // 健康检查和监控 - 无需认证
                        .requestMatchers(ACTUATOR_URLS).permitAll()
                        // 音乐流媒体服务 - 需要认证
                        .requestMatchers("/api/music/*/stream").authenticated()
                        // 用户相关API - 需要认证
                        .requestMatchers("/api/user/**").authenticated()
                        // 播放列表私有操作 - 需要认证
                        .requestMatchers("/api/playlist/create", "/api/playlist/*/update", "/api/playlist/*/delete").authenticated()
                        // 音乐上传和管理 - 需要管理员权限
                        .requestMatchers("/api/music/upload", "/api/music/*/update", "/api/music/*/delete").hasRole("ADMIN")
                        // 艺术家和专辑管理 - 需要管理员权限
                        .requestMatchers("/api/artist/create", "/api/artist/*/update", "/api/artist/*/delete").hasRole("ADMIN")
                        .requestMatchers("/api/album/create", "/api/album/*/update", "/api/album/*/delete").hasRole("ADMIN")
                        // 系统管理API - 需要管理员权限
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 设置认证提供者
                .authenticationProvider(authenticationProvider())

                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}