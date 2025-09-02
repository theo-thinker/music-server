package com.musicserver.service;

import com.musicserver.dto.request.FileUploadRequest;
import com.musicserver.dto.response.FileInfoResponse;
import com.musicserver.vo.FileDetailVO;
import com.musicserver.vo.FileListVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Minio对象存储服务接口
 * <p>
 * 提供文件上传、下载、删除等操作的标准接口
 * 支持多种文件类型和存储策略
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public interface MinioService {

    // ========================================
    // 存储桶管理
    // ========================================

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    void createBucket(String bucketName);

    /**
     * 删除存储桶
     *
     * @param bucketName 存储桶名称
     */
    void deleteBucket(String bucketName);

    /**
     * 获取存储桶列表
     *
     * @return 存储桶名称列表
     */
    List<String> listBuckets();

    /**
     * 设置存储桶策略
     *
     * @param bucketName 存储桶名称
     * @param policy     策略内容
     */
    void setBucketPolicy(String bucketName, String policy);

    /**
     * 获取存储桶策略
     *
     * @param bucketName 存储桶名称
     * @return 策略内容
     */
    String getBucketPolicy(String bucketName);

    // ========================================
    // 文件上传
    // ========================================

    /**
     * 上传文件
     *
     * @param request 文件上传请求
     * @return 文件信息响应
     */
    FileInfoResponse uploadFile(FileUploadRequest request);

    /**
     * 上传文件（简化版本）
     *
     * @param file     文件
     * @param fileType 文件类型
     * @return 文件信息响应
     */
    FileInfoResponse uploadFile(MultipartFile file, String fileType);

    /**
     * 上传文件流
     *
     * @param inputStream 文件输入流
     * @param objectName  对象名称
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 文件信息响应
     */
    FileInfoResponse uploadFile(InputStream inputStream, String objectName,
                                String contentType, long size);

    /**
     * 上传文件到指定存储桶
     *
     * @param bucketName  存储桶名称
     * @param objectName  对象名称
     * @param inputStream 文件输入流
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 文件信息响应
     */
    FileInfoResponse uploadFile(String bucketName, String objectName,
                                InputStream inputStream, String contentType, long size);

    /**
     * 批量上传文件
     *
     * @param files    文件列表
     * @param fileType 文件类型
     * @return 文件信息响应列表
     */
    List<FileInfoResponse> uploadFiles(List<MultipartFile> files, String fileType);

    // ========================================
    // 文件下载
    // ========================================

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream downloadFile(String fileId);

    /**
     * 下载文件（指定存储桶和对象名）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 获取文件预签名下载URL
     *
     * @param fileId 文件ID
     * @param expiry 过期时间（秒）
     * @return 预签名URL
     */
    String getPresignedDownloadUrl(String fileId, int expiry);

    /**
     * 获取文件预签名下载URL（指定存储桶和对象名）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 预签名URL
     */
    String getPresignedDownloadUrl(String bucketName, String objectName, int expiry);

    /**
     * 获取文件公开访问URL
     *
     * @param fileId 文件ID
     * @return 访问URL
     */
    String getPublicUrl(String fileId);

    /**
     * 获取文件公开访问URL（指定存储桶和对象名）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 访问URL
     */
    String getPublicUrl(String bucketName, String objectName);

    // ========================================
    // 文件管理
    // ========================================

    /**
     * 检查文件是否存在
     *
     * @param fileId 文件ID
     * @return 是否存在
     */
    boolean fileExists(String fileId);

    /**
     * 检查文件是否存在（指定存储桶和对象名）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String bucketName, String objectName);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     */
    void deleteFile(String fileId);

    /**
     * 删除文件（指定存储桶和对象名）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    void deleteFile(String bucketName, String objectName);

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     */
    void deleteFiles(List<String> fileIds);

    /**
     * 批量删除文件（指定存储桶）
     *
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称列表
     */
    void deleteFiles(String bucketName, List<String> objectNames);

    /**
     * 复制文件
     *
     * @param sourceFileId 源文件ID
     * @param targetFileId 目标文件ID
     * @return 复制后的文件信息
     */
    FileInfoResponse copyFile(String sourceFileId, String targetFileId);

    /**
     * 复制文件（指定存储桶）
     *
     * @param sourceBucket 源存储桶
     * @param sourceObject 源对象名
     * @param targetBucket 目标存储桶
     * @param targetObject 目标对象名
     * @return 复制后的文件信息
     */
    FileInfoResponse copyFile(String sourceBucket, String sourceObject,
                              String targetBucket, String targetObject);

    /**
     * 移动文件
     *
     * @param sourceFileId 源文件ID
     * @param targetFileId 目标文件ID
     * @return 移动后的文件信息
     */
    FileInfoResponse moveFile(String sourceFileId, String targetFileId);

    /**
     * 重命名文件
     *
     * @param fileId  文件ID
     * @param newName 新文件名
     * @return 重命名后的文件信息
     */
    FileInfoResponse renameFile(String fileId, String newName);

    // ========================================
    // 文件信息查询
    // ========================================

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件信息响应
     */
    FileInfoResponse getFileInfo(String fileId);

    /**
     * 获取文件详细信息
     *
     * @param fileId 文件ID
     * @return 文件详细信息
     */
    FileDetailVO getFileDetail(String fileId);

    /**
     * 获取文件统计信息
     *
     * @param fileId 文件ID
     * @return 文件统计信息
     */
    Map<String, Object> getFileStats(String fileId);

    /**
     * 获取文件元数据
     *
     * @param fileId 文件ID
     * @return 文件元数据
     */
    Map<String, String> getFileMetadata(String fileId);

    /**
     * 获取文件元数据（指定存储桶和对象名）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件元数据
     */
    Map<String, String> getFileMetadata(String bucketName, String objectName);

    // ========================================
    // 文件列表查询
    // ========================================

    /**
     * 列举文件
     *
     * @param bucketName 存储桶名称
     * @param prefix     前缀
     * @param maxKeys    最大数量
     * @return 文件列表
     */
    List<FileListVO> listFiles(String bucketName, String prefix, int maxKeys);

    /**
     * 根据文件类型列举文件
     *
     * @param fileType 文件类型
     * @param maxKeys  最大数量
     * @return 文件列表
     */
    List<FileListVO> listFilesByType(String fileType, int maxKeys);

    /**
     * 根据用户ID列举文件
     *
     * @param userId   用户ID
     * @param fileType 文件类型（可选）
     * @param maxKeys  最大数量
     * @return 文件列表
     */
    List<FileListVO> listFilesByUser(Long userId, String fileType, int maxKeys);

    // ========================================
    // 工具方法
    // ========================================

    /**
     * 根据文件类型获取存储桶名称
     *
     * @param fileType 文件类型
     * @return 存储桶名称
     */
    String getBucketNameByFileType(String fileType);

    /**
     * 生成对象名称
     *
     * @param originalFilename 原始文件名
     * @param fileType         文件类型
     * @return 对象名称
     */
    String generateObjectName(String originalFilename, String fileType);

    /**
     * 验证文件类型
     *
     * @param file     文件
     * @param fileType 期望的文件类型
     * @return 是否验证通过
     */
    boolean validateFileType(MultipartFile file, String fileType);

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的大小
     */
    String formatFileSize(long size);

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    String getFileExtension(String filename);

    /**
     * 生成唯一文件ID
     *
     * @return 文件ID
     */
    String generateFileId();
}