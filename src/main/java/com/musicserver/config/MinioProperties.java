package com.musicserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Minio配置属性类
 * 
 * 管理Minio对象存储服务的连接配置参数
 * 支持多环境配置和自动配置属性绑定
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * Minio服务器端点URL
     * 例如：http://localhost:9000 或 https://minio.example.com
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKey;

    /**
     * 秘密访问密钥
     */
    private String secretKey;

    /**
     * 是否启用HTTPS连接
     * 默认为false，使用HTTP连接
     */
    private Boolean secure = false;

    /**
     * 连接超时时间（毫秒）
     * 默认10秒
     */
    private Long connectTimeout = 10000L;

    /**
     * 写入超时时间（毫秒）
     * 默认60秒
     */
    private Long writeTimeout = 60000L;

    /**
     * 读取超时时间（毫秒）
     * 默认10秒
     */
    private Long readTimeout = 10000L;

    /**
     * 默认存储桶配置
     */
    private Bucket bucket = new Bucket();

    /**
     * 文件上传配置
     */
    private Upload upload = new Upload();

    /**
     * 存储桶配置内部类
     */
    @Data
    public static class Bucket {
        
        /**
         * 默认存储桶名称
         */
        private String name = "music-server";

        /**
         * 音乐文件存储桶
         */
        private String music = "music-files";

        /**
         * 图片文件存储桶
         */
        private String image = "image-files";

        /**
         * 歌词文件存储桶
         */
        private String lyric = "lyric-files";

        /**
         * 临时文件存储桶
         */
        private String temp = "temp-files";

        /**
         * 是否在启动时自动创建存储桶
         */
        private Boolean autoCreate = true;

        /**
         * 默认存储桶权限
         * 可选值：private、public-read、public-read-write
         */
        private String defaultPolicy = "private";
    }

    /**
     * 文件上传配置内部类
     */
    @Data
    public static class Upload {
        
        /**
         * 单个文件最大大小（字节）
         * 默认100MB
         */
        private Long maxFileSize = 100 * 1024 * 1024L;

        /**
         * 允许的音乐文件类型
         */
        private List<String> allowedMusicTypes = List.of(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/flac", 
            "audio/aac", "audio/ogg", "audio/m4a"
        );

        /**
         * 允许的图片文件类型
         */
        private List<String> allowedImageTypes = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", 
            "image/webp", "image/bmp"
        );

        /**
         * 允许的歌词文件类型
         */
        private List<String> allowedLyricTypes = List.of(
            "text/plain", "application/x-subrip", "text/lrc"
        );

        /**
         * 文件名生成策略
         * uuid: 使用UUID生成唯一文件名
         * timestamp: 使用时间戳生成文件名
         * original: 保持原始文件名（需要处理重名）
         */
        private String filenameStrategy = "uuid";

        /**
         * 是否保留原始文件扩展名
         */
        private Boolean keepOriginalExtension = true;

        /**
         * 文件上传路径模板
         * 支持占位符：{year}, {month}, {day}, {type}
         */
        private String pathTemplate = "{type}/{year}/{month}/{day}";

        /**
         * 预签名URL有效期（秒）
         * 默认1小时
         */
        private Integer presignedUrlExpiry = 3600;
    }

    /**
     * 获取完整的端点URL
     * 
     * @return 带协议的完整URL
     */
    public String getFullEndpoint() {
        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        
        // 如果已经包含协议，直接返回
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        
        // 根据secure标志添加协议
        String protocol = Boolean.TRUE.equals(secure) ? "https://" : "http://";
        return protocol + endpoint;
    }

    /**
     * 验证必需的配置项是否已设置
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        return endpoint != null && !endpoint.trim().isEmpty() &&
               accessKey != null && !accessKey.trim().isEmpty() &&
               secretKey != null && !secretKey.trim().isEmpty();
    }

    /**
     * 获取所有存储桶名称列表
     * 
     * @return 存储桶名称列表
     */
    public List<String> getAllBucketNames() {
        return List.of(
            bucket.getName(),
            bucket.getMusic(),
            bucket.getImage(),
            bucket.getLyric(),
            bucket.getTemp()
        );
    }
}