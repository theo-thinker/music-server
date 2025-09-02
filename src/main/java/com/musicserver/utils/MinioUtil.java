package com.musicserver.utils;

import com.musicserver.config.MinioProperties;
import com.musicserver.exception.MinioException;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Minio工具类
 * <p>
 * 提供便捷的文件操作方法和工具函数
 * 包含文件验证、路径处理、元数据操作等功能
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    // 常量定义
    private static final Pattern VALID_BUCKET_NAME = Pattern.compile("^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$");
    private static final Pattern VALID_OBJECT_NAME = Pattern.compile("^[^\\x00-\\x1f\\x7f-\\x9f]*$");
    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;
    private static final long TB = GB * 1024L;

    // ========================================
    // 文件验证工具
    // ========================================

    /**
     * 验证文件是否为有效的音乐文件
     *
     * @param file 文件
     * @return 是否有效
     */
    public boolean isValidMusicFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        List<String> allowedTypes = minioProperties.getUpload().getAllowedMusicTypes();
        return allowedTypes.contains(contentType.toLowerCase());
    }

    /**
     * 验证文件是否为有效的图片文件
     *
     * @param file 文件
     * @return 是否有效
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        List<String> allowedTypes = minioProperties.getUpload().getAllowedImageTypes();
        return allowedTypes.contains(contentType.toLowerCase());
    }

    /**
     * 验证文件是否为有效的歌词文件
     *
     * @param file 文件
     * @return 是否有效
     */
    public boolean isValidLyricFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        List<String> allowedTypes = minioProperties.getUpload().getAllowedLyricTypes();
        return allowedTypes.contains(contentType.toLowerCase()) ||
                file.getOriginalFilename() != null &&
                        file.getOriginalFilename().toLowerCase().endsWith(".lrc");
    }

    /**
     * 验证文件大小是否在允许范围内
     *
     * @param file 文件
     * @return 是否在允许范围内
     */
    public boolean isValidFileSize(MultipartFile file) {
        if (file == null) {
            return false;
        }

        long maxSize = minioProperties.getUpload().getMaxFileSize();
        return file.getSize() <= maxSize;
    }

    /**
     * 验证存储桶名称是否有效
     *
     * @param bucketName 存储桶名称
     * @return 是否有效
     */
    public boolean isValidBucketName(String bucketName) {
        if (!StringUtils.hasText(bucketName)) {
            return false;
        }

        // 长度检查
        if (bucketName.length() < 3 || bucketName.length() > 63) {
            return false;
        }

        // 格式检查
        return VALID_BUCKET_NAME.matcher(bucketName).matches();
    }

    /**
     * 验证对象名称是否有效
     *
     * @param objectName 对象名称
     * @return 是否有效
     */
    public boolean isValidObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return false;
        }

        // 长度检查
        if (objectName.length() > 1024) {
            return false;
        }

        // 格式检查
        return VALID_OBJECT_NAME.matcher(objectName).matches();
    }

    // ========================================
    // 文件信息处理工具
    // ========================================

    /**
     * 获取文件的MIME类型
     *
     * @param filename 文件名
     * @return MIME类型
     */
    public String getMimeType(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "application/octet-stream";
        }

        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            // 音频文件
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "flac" -> "audio/flac";
            case "aac" -> "audio/aac";
            case "ogg" -> "audio/ogg";
            case "m4a" -> "audio/mp4";

            // 图片文件
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";

            // 文本文件
            case "txt" -> "text/plain";
            case "lrc" -> "text/plain";
            case "json" -> "application/json";
            case "xml" -> "application/xml";

            // 默认
            default -> "application/octet-stream";
        };
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不包含点号）
     */
    public String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }

        return "";
    }

    /**
     * 获取不包含扩展名的文件名
     *
     * @param filename 完整文件名
     * @return 不包含扩展名的文件名
     */
    public String getFileNameWithoutExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }

        return filename;
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的大小字符串
     */
    public String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        if (size < KB) {
            return size + " B";
        } else if (size < MB) {
            return String.format("%.1f KB", (double) size / KB);
        } else if (size < GB) {
            return String.format("%.1f MB", (double) size / MB);
        } else if (size < TB) {
            return String.format("%.1f GB", (double) size / GB);
        } else {
            return String.format("%.1f TB", (double) size / TB);
        }
    }

    // ========================================
    // 路径处理工具
    // ========================================

    /**
     * 构建对象路径
     *
     * @param fileType 文件类型
     * @param filename 文件名
     * @return 对象路径
     */
    public String buildObjectPath(String fileType, String filename) {
        String pathTemplate = minioProperties.getUpload().getPathTemplate();
        LocalDateTime now = LocalDateTime.now();

        String path = pathTemplate
                .replace("{type}", fileType)
                .replace("{year}", String.valueOf(now.getYear()))
                .replace("{month}", String.format("%02d", now.getMonthValue()))
                .replace("{day}", String.format("%02d", now.getDayOfMonth()));

        return normalizePath(path + "/" + filename);
    }

    /**
     * 标准化路径（移除多余的斜杠）
     *
     * @param path 原始路径
     * @return 标准化后的路径
     */
    public String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }

        // 移除开头的斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // 移除结尾的斜杠
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // 替换多个连续的斜杠为单个斜杠
        path = path.replaceAll("/+", "/");

        return path;
    }

    /**
     * 获取对象的父路径
     *
     * @param objectName 对象名称
     * @return 父路径
     */
    public String getParentPath(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return "";
        }

        int lastSlashIndex = objectName.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            return objectName.substring(0, lastSlashIndex);
        }

        return "";
    }

    /**
     * 从完整路径中提取文件名
     *
     * @param objectName 对象名称
     * @return 文件名
     */
    public String extractFilename(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return "";
        }

        int lastSlashIndex = objectName.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < objectName.length() - 1) {
            return objectName.substring(lastSlashIndex + 1);
        }

        return objectName;
    }

    // ========================================
    // 文件内容处理工具
    // ========================================

    /**
     * 计算文件的MD5值
     *
     * @param inputStream 文件输入流
     * @return MD5值
     */
    public String calculateMD5(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }

            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            log.error("计算MD5失败", e);
            throw new MinioException("计算MD5失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算文件的MD5值
     *
     * @param file 文件
     * @return MD5值
     */
    public String calculateMD5(MultipartFile file) {
        try {
            return calculateMD5(file.getInputStream());
        } catch (Exception e) {
            log.error("计算文件MD5失败: {}", file.getOriginalFilename(), e);
            throw new MinioException("计算文件MD5失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件内容是否为空
     *
     * @param inputStream 文件输入流
     * @return 是否为空
     */
    public boolean isEmptyContent(InputStream inputStream) {
        try {
            return inputStream.available() == 0;
        } catch (Exception e) {
            log.warn("检查文件内容是否为空时发生异常", e);
            return true;
        }
    }

    // ========================================
    // 唯一标识生成工具
    // ========================================

    /**
     * 生成唯一的文件ID
     *
     * @return 文件ID
     */
    public String generateFileId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成基于时间戳的唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    public String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        if (StringUtils.hasText(extension)) {
            return String.format("%s_%s.%s", timestamp, uuid, extension);
        } else {
            return String.format("%s_%s", timestamp, uuid);
        }
    }

    /**
     * 生成安全的文件名（移除特殊字符）
     *
     * @param filename 原始文件名
     * @return 安全的文件名
     */
    public String generateSafeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "unnamed_file";
        }

        // 移除或替换特殊字符
        String safeFilename = filename
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_{2,}", "_");

        // 移除开头和结尾的下划线
        safeFilename = safeFilename.replaceAll("^_+|_+$", "");

        // 如果文件名为空，使用默认名称
        if (safeFilename.isEmpty()) {
            safeFilename = "unnamed_file";
        }

        return safeFilename;
    }

    // ========================================
    // 元数据处理工具
    // ========================================

    /**
     * 构建文件元数据
     *
     * @param file        文件
     * @param fileType    文件类型
     * @param description 描述
     * @return 元数据Map
     */
    public Map<String, String> buildMetadata(MultipartFile file, String fileType, String description) {
        Map<String, String> metadata = new HashMap<>();

        metadata.put("upload-time", LocalDateTime.now().toString());
        metadata.put("original-filename", file.getOriginalFilename());
        metadata.put("file-type", fileType);
        metadata.put("content-type", file.getContentType());
        metadata.put("file-size", String.valueOf(file.getSize()));

        if (StringUtils.hasText(description)) {
            metadata.put("description", description);
        }

        // 添加文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (StringUtils.hasText(extension)) {
            metadata.put("file-extension", extension);
        }

        return metadata;
    }

    // ========================================
    // URL处理工具
    // ========================================

    /**
     * 构建文件的公开访问URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 公开访问URL
     */
    public String buildPublicUrl(String bucketName, String objectName) {
        String endpoint = minioProperties.getFullEndpoint();
        return String.format("%s/%s/%s", endpoint, bucketName, objectName);
    }

    /**
     * 构建文件的缩略图URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 缩略图URL
     */
    public String buildThumbnailUrl(String bucketName, String objectName) {
        String endpoint = minioProperties.getFullEndpoint();
        String thumbnailObject = "thumbnails/thumb_" + extractFilename(objectName);
        return String.format("%s/%s/%s", endpoint, bucketName, thumbnailObject);
    }

    // ========================================
    // 文件操作辅助工具
    // ========================================

    /**
     * 检查对象是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取对象信息
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 对象信息
     */
    public StatObjectResponse getObjectInfo(String bucketName, String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("获取对象信息失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioException("获取对象信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建空的输入流
     *
     * @return 空的输入流
     */
    public InputStream createEmptyInputStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    // ========================================
    // 配置工具
    // ========================================

    /**
     * 获取配置的最大文件大小
     *
     * @return 最大文件大小（字节）
     */
    public long getMaxFileSize() {
        return minioProperties.getUpload().getMaxFileSize();
    }

    /**
     * 获取默认的预签名URL过期时间
     *
     * @return 过期时间（秒）
     */
    public int getDefaultPresignedUrlExpiry() {
        return minioProperties.getUpload().getPresignedUrlExpiry();
    }

    /**
     * 根据文件类型获取对应的存储桶名称
     *
     * @param fileType 文件类型
     * @return 存储桶名称
     */
    public String getBucketNameByFileType(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "music", "audio" -> minioProperties.getBucket().getMusic();
            case "image", "picture" -> minioProperties.getBucket().getImage();
            case "lyric", "lrc" -> minioProperties.getBucket().getLyric();
            case "temp", "temporary" -> minioProperties.getBucket().getTemp();
            default -> minioProperties.getBucket().getName();
        };
    }
}