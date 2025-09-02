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
 * <p>
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
            // 优化DevTools配置，避免SilentExitException
            configureDevTools(args);

            // 创建Spring应用
            SpringApplication app = new SpringApplication(MusicServerApplication.class);

            // 设置自定义Banner（可选，如果想要更高级的自定义）
            // app.setBanner(new BannerConfig());

            // 启动Spring Boot应用
            ConfigurableApplicationContext context = app.run(args);

            // 打印启动信息
            printStartupInfo(context);

        } catch (Exception e) {
            log.error("应用启动失败", e);
            System.exit(1);
        }
    }

    /**
     * 配置DevTools，避免启动冲突
     *
     * @param args 命令行参数
     */
    private static void configureDevTools(String[] args) {
        // 只在生产环境下禁用DevTools
        if (isProductionEnvironment(args)) {
            System.setProperty("spring.devtools.restart.enabled", "false");
            System.setProperty("spring.devtools.livereload.enabled", "false");
            log.info("生产环境：已禁用DevTools功能");
        } else {
            // 开发环境下优化DevTools配置
            System.setProperty("spring.devtools.restart.poll-interval", "3000");
            System.setProperty("spring.devtools.restart.quiet-period", "1000");
            // 避免SilentExitException的配置
            System.setProperty("spring.devtools.restart.log-condition-evaluation-delta", "false");
            log.debug("开发环境：DevTools热重载已启用");
        }
    }

    /**
     * 判断是否为生产环境
     *
     * @param args 命令行参数
     * @return 是否为生产环境
     */
    private static boolean isProductionEnvironment(String[] args) {
        // 检查命令行参数
        for (String arg : args) {
            if (arg.contains("spring.profiles.active=prod") ||
                    arg.contains("spring.profiles.active=production")) {
                return true;
            }
        }

        // 检查系统属性
        String activeProfiles = System.getProperty("spring.profiles.active");
        if (activeProfiles != null &&
                (activeProfiles.contains("prod") || activeProfiles.contains("production"))) {
            return true;
        }

        // 检查环境变量
        String springProfilesActive = System.getenv("SPRING_PROFILES_ACTIVE");
        return springProfilesActive != null &&
                (springProfilesActive.contains("prod") || springProfilesActive.contains("production"));
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