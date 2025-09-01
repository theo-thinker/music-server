package com.musicserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 音乐播放器后端服务主启动类
 * 
 * 使用Spring Boot框架启动应用程序
 * 集成了以下功能模块：
 * - Spring Security安全框架
 * - MyBatis Plus数据库框架
 * - Redis缓存
 * - JWT认证
 * - Swagger API文档
 * - 异步处理
 * - 定时任务
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class
})
@EnableCaching
@EnableAsync
@EnableScheduling
public class MusicServerApplication {

    /**
     * 应用程序主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 设置系统属性
            System.setProperty("spring.devtools.restart.enabled", "false");
            
            // 启动Spring Boot应用
            ConfigurableApplicationContext context = SpringApplication.run(MusicServerApplication.class, args);
            
            // 打印启动信息
            printStartupInfo(context);
            
        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }

    /**
     * 打印应用启动信息
     * 
     * @param context Spring应用上下文
     */
    private static void printStartupInfo(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();
        
        try {
            String protocol = "http";
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https";
            }
            
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            
            log.info("""
                    
                    ----------------------------------------------------------
                    🎵 音乐播放器后端服务启动成功！ 🎵
                    ----------------------------------------------------------
                    应用名称:    {}
                    运行环境:    {}
                    服务地址:    {}://localhost:{}{}
                    外部地址:    {}://{}:{}{}
                    API文档:     {}://localhost:{}{}/doc.html
                    数据库监控:  {}://localhost:{}{}/druid
                    ----------------------------------------------------------
                    """,
                    env.getProperty("spring.application.name", "Music Server"),
                    env.getActiveProfiles().length == 0 ? "default" : String.join(",", env.getActiveProfiles()),
                    protocol, serverPort, contextPath,
                    protocol, hostAddress, serverPort, contextPath,
                    protocol, serverPort, contextPath,
                    protocol, serverPort, contextPath
            );
            
        } catch (UnknownHostException e) {
            log.warn("无法获取主机地址: {}", e.getMessage());
            log.info("🎵 音乐播放器后端服务启动成功！ 🎵");
        }
    }
}