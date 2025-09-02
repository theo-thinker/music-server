package com.musicserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

/**
 * Banner选择器配置类
 * <p>
 * 根据不同的环境自动选择合适的Banner
 * 支持开发环境、测试环境、生产环境的个性化Banner
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
public class BannerSelectorConfig {

    /**
     * 根据环境选择Banner
     *
     * @param environment 环境对象
     * @return Banner实例
     */
    @Bean
    public Banner environmentBanner(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        String bannerFile = "banner.txt"; // 默认banner文件

        // 根据活动的profile选择不同的banner
        if (activeProfiles.length > 0) {
            String activeProfile = activeProfiles[0].toLowerCase();

            switch (activeProfile) {
                case "dev", "development" -> {
                    bannerFile = "banner-dev.txt";
                    log.debug("使用开发环境Banner: {}", bannerFile);
                }
                case "prod", "production" -> {
                    bannerFile = "banner-prod.txt";
                    log.debug("使用生产环境Banner: {}", bannerFile);
                }
                case "test", "testing" -> {
                    bannerFile = "banner.txt"; // 测试环境使用默认banner
                    log.debug("使用测试环境Banner: {}", bannerFile);
                }
                default -> {
                    log.debug("使用默认Banner: {}", bannerFile);
                }
            }
        }

        // 检查banner文件是否存在，如果不存在使用默认banner
        ClassPathResource bannerResource = new ClassPathResource(bannerFile);
        if (!bannerResource.exists()) {
            log.warn("Banner文件不存在: {}，使用默认banner.txt", bannerFile);
            bannerResource = new ClassPathResource("banner.txt");
        }

        // 如果默认banner也不存在，使用Spring Boot默认banner
        if (!bannerResource.exists()) {
            log.warn("默认Banner文件也不存在，使用Spring Boot默认Banner");
            return (environment1, sourceClass, out) -> {
                // 返回空实现，使用Spring Boot默认banner
            };
        }

        // 创建并返回ResourceBanner
        return new ResourceBanner(bannerResource);
    }
}