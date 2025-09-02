package com.musicserver.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Knife4j接口文档配置类
 * <p>
 * 配置Swagger/OpenAPI文档生成，包括：
 * 1. API基本信息配置
 * 2. JWT认证配置
 * 3. 服务器信息配置
 * 4. 联系人和许可证信息
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Configuration
@EnableKnife4j
public class SwaggerConfig {

    /**
     * JWT安全方案名称
     */
    private static final String SECURITY_SCHEME_NAME = "Bearer Token";

    /**
     * 配置OpenAPI文档信息
     *
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 配置API基本信息
                .info(apiInfo())
                // 配置服务器信息
                .servers(serverInfo())
                // 配置安全认证
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    /**
     * API基本信息配置
     *
     * @return Info对象
     */
    private Info apiInfo() {
        return new Info()
                .title("音乐播放器后端服务API")
                .description("""
                        ## 音乐播放器后端服务接口文档
                        
                        ### 功能特性
                        - 用户注册、登录、权限管理
                        - 音乐上传、管理、播放
                        - 播放列表创建、编辑、分享
                        - 音乐搜索、推荐算法
                        - 播放历史、统计分析
                        
                        ### 技术栈
                        - Spring Boot 3.5.5
                        - Spring Security 6.5.3 + JWT
                        - MyBatis Plus 3.5.12
                        - Redis 8.0.x
                        - MySQL 9.4.0
                        
                        ### 认证方式
                        使用JWT Bearer Token进行认证，请在请求头中添加：
                        ```
                        Authorization: Bearer your-jwt-token
                        ```
                        
                        ### 响应格式
                        所有API响应都遵循统一格式：
                        ```json
                        {
                          "code": 200,
                          "message": "操作成功",
                          "data": {},
                          "timestamp": 1640995200000,
                          "traceId": "trace-id"
                        }
                        ```
                        """)
                .version("v1.0.0")
                .contact(contactInfo())
                .license(licenseInfo())
                .termsOfService("https://www.musicserver.com/terms");
    }

    /**
     * 联系人信息配置
     *
     * @return Contact对象
     */
    private Contact contactInfo() {
        return new Contact()
                .name("音乐服务器开发团队")
                .email("developer@musicserver.com")
                .url("https://www.musicserver.com");
    }

    /**
     * 许可证信息配置
     *
     * @return License对象
     */
    private License licenseInfo() {
        return new License()
                .name("Apache License 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");
    }

    /**
     * 服务器信息配置
     *
     * @return Server列表
     */
    private List<Server> serverInfo() {
        return List.of(
                new Server()
                        .url("http://localhost:8080/api")
                        .description("开发环境服务器"),
                new Server()
                        .url("https://api.musicserver.com")
                        .description("生产环境服务器"),
                new Server()
                        .url("https://test-api.musicserver.com")
                        .description("测试环境服务器")
        );
    }

    /**
     * 安全需求配置
     *
     * @return SecurityRequirement对象
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }

    /**
     * 组件配置，包括安全方案
     *
     * @return Components对象
     */
    private Components components() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme());
    }

    /**
     * JWT安全方案配置
     *
     * @return SecurityScheme对象
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .description("请输入JWT Token，格式：Bearer {token}")
                .in(SecurityScheme.In.HEADER);
    }
}