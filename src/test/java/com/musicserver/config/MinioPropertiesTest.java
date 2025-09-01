package com.musicserver.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Minio配置属性单元测试
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
class MinioPropertiesTest {

    private MinioProperties minioProperties;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties();
        
        // 设置基本配置
        minioProperties.setEndpoint("localhost:9000");
        minioProperties.setPort(9000);
        minioProperties.setSecure(false);
        minioProperties.setAccessKey("minioadmin");
        minioProperties.setSecretKey("minioadmin");
        minioProperties.setEnabled(true);
        
        // 设置超时配置
        minioProperties.setConnectTimeout(30000L);
        minioProperties.setReadTimeout(30000L);
        minioProperties.setWriteTimeout(30000L);
        
        // 初始化存储桶配置
        MinioProperties.Bucket bucketConfig = new MinioProperties.Bucket();
        bucketConfig.setName("music-server");
        bucketConfig.setMusic("music-files");
        bucketConfig.setImage("image-files");
        bucketConfig.setLyric("lyric-files");
        bucketConfig.setTemp("temp-files");
        minioProperties.setBucket(bucketConfig);
        
        // 初始化上传配置
        MinioProperties.Upload uploadConfig = new MinioProperties.Upload();
        uploadConfig.setMaxFileSize(100 * 1024 * 1024L); // 100MB
        uploadConfig.setFilenameStrategy("uuid");
        uploadConfig.setKeepOriginalExtension(true);
        uploadConfig.setPathTemplate("{type}/{year}/{month}/{day}");
        uploadConfig.setPresignedUrlExpiry(7200);
        minioProperties.setUpload(uploadConfig);
    }

    @Test
    void testBasicProperties() {
        assertThat(minioProperties.getEndpoint()).isEqualTo("localhost:9000");
        assertThat(minioProperties.getPort()).isEqualTo(9000);
        assertThat(minioProperties.getSecure()).isFalse();
        assertThat(minioProperties.getAccessKey()).isEqualTo("minioadmin");
        assertThat(minioProperties.getSecretKey()).isEqualTo("minioadmin");
        assertThat(minioProperties.getEnabled()).isTrue();
    }

    @Test
    void testTimeoutProperties() {
        assertThat(minioProperties.getConnectTimeout()).isEqualTo(30000L);
        assertThat(minioProperties.getReadTimeout()).isEqualTo(30000L);
        assertThat(minioProperties.getWriteTimeout()).isEqualTo(30000L);
    }

    @Test
    void testBucketConfiguration() {
        MinioProperties.Bucket bucketConfig = minioProperties.getBucket();
        
        assertThat(bucketConfig).isNotNull();
        assertThat(bucketConfig.getName()).isEqualTo("music-server");
        assertThat(bucketConfig.getMusic()).isEqualTo("music-files");
        assertThat(bucketConfig.getImage()).isEqualTo("image-files");
        assertThat(bucketConfig.getLyric()).isEqualTo("lyric-files");
        assertThat(bucketConfig.getTemp()).isEqualTo("temp-files");
    }

    @Test
    void testUploadConfiguration() {
        MinioProperties.Upload uploadConfig = minioProperties.getUpload();
        
        assertThat(uploadConfig).isNotNull();
        assertThat(uploadConfig.getMaxFileSize()).isEqualTo(100 * 1024 * 1024L);
        assertThat(uploadConfig.getFilenameStrategy()).isEqualTo("uuid");
        assertThat(uploadConfig.getKeepOriginalExtension()).isTrue();
        assertThat(uploadConfig.getPathTemplate()).isEqualTo("{type}/{year}/{month}/{day}");
        assertThat(uploadConfig.getPresignedUrlExpiry()).isEqualTo(7200);
    }

    @Test
    void testGetFullEndpoint() {
        String fullEndpoint = minioProperties.getFullEndpoint();
        assertThat(fullEndpoint).isEqualTo("http://localhost:9000");
        
        // 测试HTTPS端点
        minioProperties.setSecure(true);
        fullEndpoint = minioProperties.getFullEndpoint();
        assertThat(fullEndpoint).isEqualTo("https://localhost:9000");
        
        // 测试已经包含协议的endpoint
        minioProperties.setEndpoint("http://minio.example.com:9000");
        fullEndpoint = minioProperties.getFullEndpoint();
        assertThat(fullEndpoint).isEqualTo("http://minio.example.com:9000");
    }

    @Test
    void testValidConfiguration() {
        assertTrue(minioProperties.isValid());
        
        // 测试无效配置
        minioProperties.setEndpoint(null);
        assertFalse(minioProperties.isValid());
        
        minioProperties.setEndpoint("localhost");
        minioProperties.setAccessKey(null);
        assertFalse(minioProperties.isValid());
        
        minioProperties.setAccessKey("minioadmin");
        minioProperties.setSecretKey("");
        assertFalse(minioProperties.isValid());
    }

    @Test
    void testDefaultValues() {
        MinioProperties defaultProperties = new MinioProperties();
        
        // 测试默认值
        assertThat(defaultProperties.getPort()).isEqualTo(9000);
        assertThat(defaultProperties.getSecure()).isFalse();
        assertThat(defaultProperties.getEnabled()).isTrue();
        assertThat(defaultProperties.getConnectTimeout()).isEqualTo(30000L);
        assertThat(defaultProperties.getReadTimeout()).isEqualTo(30000L);
        assertThat(defaultProperties.getWriteTimeout()).isEqualTo(30000L);
    }

    @Test
    void testBucketConfigDefaults() {
        MinioProperties.Bucket bucketConfig = new MinioProperties.Bucket();
        
        assertThat(bucketConfig.getName()).isEqualTo("music-server");
        assertThat(bucketConfig.getMusic()).isEqualTo("music-files");
        assertThat(bucketConfig.getImage()).isEqualTo("image-files");
        assertThat(bucketConfig.getLyric()).isEqualTo("lyric-files");
        assertThat(bucketConfig.getTemp()).isEqualTo("temp-files");
    }

    @Test
    void testUploadConfigDefaults() {
        MinioProperties.Upload uploadConfig = new MinioProperties.Upload();
        
        assertThat(uploadConfig.getMaxFileSize()).isEqualTo(100 * 1024 * 1024L);
        assertThat(uploadConfig.getFilenameStrategy()).isEqualTo("uuid");
        assertThat(uploadConfig.getKeepOriginalExtension()).isTrue();
        assertThat(uploadConfig.getPathTemplate()).isEqualTo("{type}/{year}/{month}/{day}");
        assertThat(uploadConfig.getPresignedUrlExpiry()).isEqualTo(3600);
        
        // 测试默认允许的文件类型
        assertThat(uploadConfig.getAllowedMusicTypes()).isNotEmpty();
        assertThat(uploadConfig.getAllowedImageTypes()).isNotEmpty();
        assertThat(uploadConfig.getAllowedLyricTypes()).isNotEmpty();
    }

    @Test
    void testAllowedFileTypes() {
        MinioProperties.Upload uploadConfig = minioProperties.getUpload();
        
        // 测试音乐文件类型
        assertThat(uploadConfig.getAllowedMusicTypes())
                .contains("audio/mpeg", "audio/wav", "audio/flac", "audio/aac");
        
        // 测试图片文件类型
        assertThat(uploadConfig.getAllowedImageTypes())
                .contains("image/jpeg", "image/png", "image/gif", "image/webp");
        
        // 测试歌词文件类型
        assertThat(uploadConfig.getAllowedLyricTypes())
                .contains("text/plain", "text/lrc");
    }

    @Test
    void testToString() {
        String propertiesString = minioProperties.toString();
        
        assertThat(propertiesString).isNotNull();
        assertThat(propertiesString).contains("endpoint=localhost");
        assertThat(propertiesString).contains("port=9000");
        assertThat(propertiesString).contains("secure=false");
        assertThat(propertiesString).contains("enabled=true");
    }

    @Test
    void testEqualsAndHashCode() {
        MinioProperties properties1 = new MinioProperties();
        properties1.setEndpoint("localhost");
        properties1.setPort(9000);
        properties1.setAccessKey("admin");
        properties1.setSecretKey("password");
        
        MinioProperties properties2 = new MinioProperties();
        properties2.setEndpoint("localhost");
        properties2.setPort(9000);
        properties2.setAccessKey("admin");
        properties2.setSecretKey("password");
        
        assertThat(properties1).isEqualTo(properties2);
        assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
        
        // 测试不同配置
        properties2.setPort(9001);
        assertThat(properties1).isNotEqualTo(properties2);
    }
}