package com.musicserver.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Minio对象存储配置类
 * 
 * 负责初始化Minio客户端和相关配置
 * 支持条件化配置和自动装配
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * 创建Minio客户端Bean
     * 
     * 配置连接参数、超时时间等
     * 
     * @return 配置好的MinioClient实例
     * @throws IllegalArgumentException 当配置参数无效时
     */
    @Bean
    @ConditionalOnProperty(prefix = "minio", name = "endpoint")
    public MinioClient minioClient() {
        // 验证配置参数
        validateConfiguration();

        try {
            // 构建Minio客户端
            MinioClient.Builder builder = MinioClient.builder()
                    .endpoint(minioProperties.getFullEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey());

            // 设置HTTP客户端配置
            builder.httpClient(createHttpClient());

            MinioClient client = builder.build();
            
            log.info("Minio客户端初始化成功 - 端点: {}, 安全连接: {}", 
                    minioProperties.getFullEndpoint(), 
                    minioProperties.getSecure());
            
            return client;
            
        } catch (Exception e) {
            log.error("Minio客户端初始化失败", e);
            throw new IllegalStateException("无法初始化Minio客户端: " + e.getMessage(), e);
        }
    }

    /**
     * 创建HTTP客户端配置
     * 
     * @return 配置好的OkHttpClient
     */
    private okhttp3.OkHttpClient createHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(minioProperties.getConnectTimeout()))
                .writeTimeout(Duration.ofMillis(minioProperties.getWriteTimeout()))
                .readTimeout(Duration.ofMillis(minioProperties.getReadTimeout()))
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 验证Minio配置参数
     * 
     * @throws IllegalArgumentException 当配置无效时
     */
    private void validateConfiguration() {
        if (!minioProperties.isValid()) {
            throw new IllegalArgumentException(
                "Minio配置无效：请检查endpoint、accessKey和secretKey是否正确配置"
            );
        }

        // 验证超时时间
        if (minioProperties.getConnectTimeout() <= 0) {
            throw new IllegalArgumentException("连接超时时间必须大于0");
        }
        
        if (minioProperties.getReadTimeout() <= 0) {
            throw new IllegalArgumentException("读取超时时间必须大于0");
        }
        
        if (minioProperties.getWriteTimeout() <= 0) {
            throw new IllegalArgumentException("写入超时时间必须大于0");
        }

        // 验证文件大小限制
        if (minioProperties.getUpload().getMaxFileSize() <= 0) {
            throw new IllegalArgumentException("文件大小限制必须大于0");
        }

        log.debug("Minio配置验证通过");
    }

    /**
     * Minio客户端健康检查Bean
     * 
     * 提供健康检查功能，用于监控Minio服务状态
     * 
     * @param minioClient Minio客户端
     * @return 健康检查指示器
     */
    @Bean
    public MinioHealthIndicator minioHealthIndicator(MinioClient minioClient) {
        return new MinioHealthIndicator(minioClient, minioProperties);
    }

    /**
     * Minio健康检查指示器
     */
    public static class MinioHealthIndicator {
        
        private final MinioClient minioClient;
        private final MinioProperties minioProperties;

        public MinioHealthIndicator(MinioClient minioClient, MinioProperties minioProperties) {
            this.minioClient = minioClient;
            this.minioProperties = minioProperties;
        }

        /**
         * 检查Minio服务健康状态
         * 
         * @return 健康状态信息
         */
        public HealthStatus checkHealth() {
            try {
                // 尝试列举存储桶以测试连接
                minioClient.listBuckets();
                
                return HealthStatus.builder()
                        .status("UP")
                        .endpoint(minioProperties.getFullEndpoint())
                        .message("Minio服务连接正常")
                        .build();
                        
            } catch (Exception e) {
                log.warn("Minio健康检查失败", e);
                
                return HealthStatus.builder()
                        .status("DOWN")
                        .endpoint(minioProperties.getFullEndpoint())
                        .message("Minio服务连接失败: " + e.getMessage())
                        .build();
            }
        }
    }

    /**
     * 健康状态数据类
     */
    public static class HealthStatus {
        private String status;
        private String endpoint;
        private String message;
        private long timestamp;

        private HealthStatus(Builder builder) {
            this.status = builder.status;
            this.endpoint = builder.endpoint;
            this.message = builder.message;
            this.timestamp = System.currentTimeMillis();
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String status;
            private String endpoint;
            private String message;

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public Builder endpoint(String endpoint) {
                this.endpoint = endpoint;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public HealthStatus build() {
                return new HealthStatus(this);
            }
        }

        // Getters
        public String getStatus() { return status; }
        public String getEndpoint() { return endpoint; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}