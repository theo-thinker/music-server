package com.musicserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义Banner配置类
 * <p>
 * 提供动态Banner显示功能
 * 支持彩色输出和环境信息展示
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
public class BannerConfig implements Banner {

    private static final String BANNER_TEXT = """
            
                 __    __   __    __   ______   __   ______      ______   ______   ______   __     __   ______   ______
                /\\ "-./  \\ /\\ \\  /\\ \\ /\\  ___\\ /\\ \\ /\\  ___\\    /\\  ___\\ /\\  ___\\ /\\  == \\ /\\ \\   /\\ \\ /\\  ___\\ /\\  == \\
                \\ \\ \\-./\\ \\\\ \\ \\_\\ \\ \\\\ \\___  \\\\ \\ \\\\ \\ \\____   \\ \\___  \\\\ \\  __\\ \\ \\  __< \\ \\ \\  /  / \\ \\  __\\ \\ \\  __<
                 \\ \\_\\ \\ \\_\\\\ \\_____\\ \\\\/_____\\\\ \\_\\\\ \\_____\\   \\/\\_____\\\\ \\_____\\\\ \\_\\ \\_\\\\ \\_\\/\\_/   \\ \\_____\\\\ \\_\\ \\_\\
                  \\/_/  \\/_/ \\/_____/  \\/_____/ \\/_/ \\/_____/    \\/_____/ \\/_____/ \\/_/ /_/ \\/_/\\/_/     \\/_____/ \\/_/ /_/
            
            """;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        // 打印主标题
        printColoredText(out, BANNER_TEXT, AnsiColor.CYAN, AnsiStyle.BOLD);

        // 打印项目信息
        printProjectInfo(out, environment);

        // 打印技术栈信息
        printTechStack(out);

        // 打印启动信息
        printStartupInfo(out, environment);

        // 打印分隔线
        printSeparator(out);
    }

    /**
     * 打印项目信息
     */
    private void printProjectInfo(PrintStream out, Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "music-server");
        String version = environment.getProperty("application.version", "1.0.0");
        String description = environment.getProperty("application.description", "企业级音乐播放器后端服务");

        out.println(AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW, AnsiStyle.BOLD,
                "    ♪♫♪ " + description + " ♫♪♫"));
        out.println();
        out.println(AnsiOutput.toString(AnsiColor.GREEN,
                "    :: Application Name ::               (" + applicationName + ")"));
        out.println(AnsiOutput.toString(AnsiColor.GREEN,
                "    :: Version ::                        (v" + version + ")"));
        out.println(AnsiOutput.toString(AnsiColor.GREEN,
                "    :: Spring Boot ::                    (v" + SpringBootVersion.getVersion() + ")"));
    }

    /**
     * 打印技术栈信息
     */
    private void printTechStack(PrintStream out) {
        out.println();
        out.println(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, AnsiStyle.BOLD,
                "    🎵 核心技术栈:"));
        out.println(AnsiOutput.toString(AnsiColor.BLUE,
                "    ● Java 21 + Spring Boot 3.5.5    ● MySQL 9.4.0 + MyBatis Plus"));
        out.println(AnsiOutput.toString(AnsiColor.BLUE,
                "    ● Spring Security + JWT           ● Redis + Caffeine 缓存"));
        out.println(AnsiOutput.toString(AnsiColor.BLUE,
                "    ● MinIO 对象存储                  ● WebSocket 实时通信"));
        out.println(AnsiOutput.toString(AnsiColor.BLUE,
                "    ● Knife4j API 文档                ● Gradle Kotlin DSL"));
    }

    /**
     * 打印启动信息
     */
    private void printStartupInfo(PrintStream out, Environment environment) {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "/api");
        String activeProfile = environment.getProperty("spring.profiles.active", "default");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = dateFormat.format(new Date());

        out.println();
        out.println(AnsiOutput.toString(AnsiColor.BRIGHT_MAGENTA, AnsiStyle.BOLD,
                "    💫 启动信息:"));
        out.println(AnsiOutput.toString(AnsiColor.MAGENTA,
                "    📅 启动时间: " + currentTime));
        out.println(AnsiOutput.toString(AnsiColor.MAGENTA,
                "    🌍 活动环境: " + activeProfile));
        out.println(AnsiOutput.toString(AnsiColor.MAGENTA,
                "    📍 服务端口: " + port));
        out.println(AnsiOutput.toString(AnsiColor.MAGENTA,
                "    🌐 服务地址: http://localhost:" + port + contextPath));
        out.println(AnsiOutput.toString(AnsiColor.MAGENTA,
                "    📖 API文档: http://localhost:" + port + contextPath + "/doc.html"));
        out.println(AnsiOutput.toString(AnsiColor.MAGENTA,
                "    🔍 系统监控: http://localhost:" + port + contextPath + "/actuator"));
    }

    /**
     * 打印分隔线
     */
    private void printSeparator(PrintStream out) {
        out.println();
        out.println(AnsiOutput.toString(AnsiColor.BRIGHT_CYAN,
                "    ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        out.println();
        out.println(AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, AnsiStyle.BOLD,
                "    ♫ ♪ ♫ ♪ Ready to serve the music world! ♪ ♫ ♪ ♫"));
        out.println();
    }

    /**
     * 打印彩色文本
     */
    private void printColoredText(PrintStream out, String text, AnsiColor color, AnsiStyle style) {
        out.println(AnsiOutput.toString(color, style, text));
    }
}