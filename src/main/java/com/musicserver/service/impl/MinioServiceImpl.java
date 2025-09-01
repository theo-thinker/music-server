package com.musicserver.service.impl;

import com.musicserver.config.MinioProperties;
import com.musicserver.dto.request.FileUploadRequest;
import com.musicserver.dto.response.FileInfoResponse;
import com.musicserver.exception.*;
import com.musicserver.service.MinioService;
import com.musicserver.vo.FileDetailVO;
import com.musicserver.vo.FileListVO;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Minio对象存储服务实现类
 * 
 * 实现文件上传、下载、删除等操作的具体逻辑
 * 支持多种文件类型和存储策略
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    // ========================================
    // 存储桶管理
    // ========================================

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查存储桶是否存在失败: {}", bucketName, e);
            throw new MinioBucketException("检查存储桶是否存在失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                
                log.info("存储桶创建成功: {}", bucketName);
                
                // 如果是公开访问的存储桶，设置默认策略
                if (isPublicBucket(bucketName)) {
                    setPublicReadPolicy(bucketName);
                }
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", bucketName, e);
            throw new MinioBucketException("创建存储桶失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            if (bucketExists(bucketName)) {
                minioClient.removeBucket(RemoveBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                
                log.info("存储桶删除成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("删除存储桶失败: {}", bucketName, e);
            throw new MinioBucketException("删除存储桶失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            return buckets.stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取存储桶列表失败", e);
            throw new MinioBucketException("获取存储桶列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void setBucketPolicy(String bucketName, String policy) {
        try {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build());
            
            log.info("设置存储桶策略成功: {}", bucketName);
        } catch (Exception e) {
            log.error("设置存储桶策略失败: {}", bucketName, e);
            throw new MinioBucketException("设置存储桶策略失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getBucketPolicy(String bucketName) {
        try {
            return minioClient.getBucketPolicy(GetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("获取存储桶策略失败: {}", bucketName, e);
            throw new MinioBucketException("获取存储桶策略失败: " + e.getMessage(), e);
        }
    }

    // ========================================
    // 文件上传
    // ========================================

    @Override
    public FileInfoResponse uploadFile(FileUploadRequest request) {
        validateUploadRequest(request);
        
        MultipartFile file = request.getFile();
        String fileType = request.getFileType();
        
        // 验证文件类型
        if (!validateFileType(file, fileType)) {
            throw new MinioUploadException("不支持的文件类型: " + file.getContentType());
        }
        
        // 验证文件大小
        if (file.getSize() > minioProperties.getUpload().getMaxFileSize()) {
            throw new MinioUploadException("文件大小超出限制: " + formatFileSize(file.getSize()));
        }
        
        String bucketName = getBucketNameByFileType(fileType);
        String objectName = generateObjectName(file.getOriginalFilename(), fileType);
        
        if (StringUtils.hasText(request.getCustomFilename())) {
            objectName = buildCustomObjectName(request.getCustomFilename(), fileType);
        }
        
        // 检查文件是否已存在
        if (!Boolean.TRUE.equals(request.getOverwrite()) && fileExists(bucketName, objectName)) {
            throw new MinioUploadException("文件已存在: " + objectName);
        }
        
        try {
            // 确保存储桶存在
            ensureBucketExists(bucketName);
            
            // 构建用户元数据
            Map<String, String> userMetadata = buildUserMetadata(request);
            
            // 上传文件
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(userMetadata)
                    .build());
            
            log.info("文件上传成功: bucket={}, object={}, size={}", 
                    bucketName, objectName, file.getSize());
            
            // 构建响应
            return buildFileInfoResponse(bucketName, objectName, file, response, request);
            
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileInfoResponse uploadFile(MultipartFile file, String fileType) {
        FileUploadRequest request = new FileUploadRequest();
        request.setFile(file);
        request.setFileType(fileType);
        return uploadFile(request);
    }

    @Override
    public FileInfoResponse uploadFile(InputStream inputStream, String objectName, 
                                      String contentType, long size) {
        String bucketName = minioProperties.getBucket().getName();
        return uploadFile(bucketName, objectName, inputStream, contentType, size);
    }

    @Override
    public FileInfoResponse uploadFile(String bucketName, String objectName, 
                                      InputStream inputStream, String contentType, long size) {
        try {
            // 确保存储桶存在
            ensureBucketExists(bucketName);
            
            // 上传文件
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("文件流上传成功: bucket={}, object={}, size={}", 
                    bucketName, objectName, size);
            
            // 构建响应
            return FileInfoResponse.builder()
                    .fileId(generateFileId())
                    .originalFilename(extractFilenameFromObjectName(objectName))
                    .storedFilename(objectName)
                    .fileType(determineFileTypeFromObjectName(objectName))
                    .mimeType(contentType)
                    .fileSize(size)
                    .formattedSize(formatFileSize(size))
                    .bucketName(bucketName)
                    .accessUrl(getPublicUrl(bucketName, objectName))
                    .etag(response.etag())
                    .uploadTime(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();
            
        } catch (Exception e) {
            log.error("文件流上传失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioUploadException("文件流上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FileInfoResponse> uploadFiles(List<MultipartFile> files, String fileType) {
        List<FileInfoResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                FileInfoResponse response = uploadFile(file, fileType);
                responses.add(response);
            } catch (Exception e) {
                log.error("批量上传文件失败: {}", file.getOriginalFilename(), e);
                // 可以选择继续上传其他文件或者全部失败
                throw new MinioUploadException(
                    "批量上传文件失败，文件: " + file.getOriginalFilename() + ", 错误: " + e.getMessage(), e);
            }
        }
        
        return responses;
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 验证上传请求
     */
    private void validateUploadRequest(FileUploadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("上传请求不能为空");
        }
        
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (!StringUtils.hasText(request.getFileType())) {
            throw new IllegalArgumentException("文件类型不能为空");
        }
    }

    /**
     * 构建用户元数据
     */
    private Map<String, String> buildUserMetadata(FileUploadRequest request) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("upload-time", LocalDateTime.now().toString());
        metadata.put("file-type", request.getFileType());
        
        if (request.getBusinessId() != null) {
            metadata.put("business-id", request.getBusinessId().toString());
        }
        
        if (StringUtils.hasText(request.getDescription())) {
            metadata.put("description", request.getDescription());
        }
        
        if (request.getPublicAccess() != null) {
            metadata.put("public-access", request.getPublicAccess().toString());
        }
        
        return metadata;
    }

    /**
     * 构建文件信息响应
     */
    private FileInfoResponse buildFileInfoResponse(String bucketName, String objectName, 
                                                  MultipartFile file, ObjectWriteResponse response, 
                                                  FileUploadRequest request) {
        return FileInfoResponse.builder()
                .fileId(generateFileId())
                .originalFilename(file.getOriginalFilename())
                .storedFilename(objectName)
                .fileType(request.getFileType())
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .formattedSize(formatFileSize(file.getSize()))
                .bucketName(bucketName)
                .accessUrl(getPublicUrl(bucketName, objectName))
                .presignedUrl(getPresignedDownloadUrl(bucketName, objectName, 
                        minioProperties.getUpload().getPresignedUrlExpiry()))
                .presignedUrlExpiry(LocalDateTime.now().plusSeconds(
                        minioProperties.getUpload().getPresignedUrlExpiry()))
                .description(request.getDescription())
                .publicAccess(request.getPublicAccess())
                .businessId(request.getBusinessId())
                .uploadTime(LocalDateTime.now())
                .status("ACTIVE")
                .etag(response.etag())
                .build();
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists(String bucketName) {
        if (!bucketExists(bucketName)) {
            createBucket(bucketName);
        }
    }

    /**
     * 判断是否为公开存储桶
     */
    private boolean isPublicBucket(String bucketName) {
        // 根据存储桶名称判断是否需要公开访问
        return bucketName.equals(minioProperties.getBucket().getImage()) ||
               bucketName.equals(minioProperties.getBucket().getMusic());
    }

    /**
     * 设置公开读取策略
     */
    private void setPublicReadPolicy(String bucketName) {
        String policy = String.format("""
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": "*",
                        "Action": "s3:GetObject",
                        "Resource": "arn:aws:s3:::%s/*"
                    }
                ]
            }
            """, bucketName);
        
        setBucketPolicy(bucketName, policy);
    }

    /**
     * 构建自定义对象名称
     */
    private String buildCustomObjectName(String customFilename, String fileType) {
        String pathTemplate = minioProperties.getUpload().getPathTemplate();
        LocalDateTime now = LocalDateTime.now();
        
        String path = pathTemplate
                .replace("{type}", fileType)
                .replace("{year}", String.valueOf(now.getYear()))
                .replace("{month}", String.format("%02d", now.getMonthValue()))
                .replace("{day}", String.format("%02d", now.getDayOfMonth()));
        
        return path + "/" + customFilename;
    }

    /**
     * 从对象名称中提取文件名
     */
    private String extractFilenameFromObjectName(String objectName) {
        if (objectName.contains("/")) {
            return objectName.substring(objectName.lastIndexOf("/") + 1);
        }
        return objectName;
    }

    /**
     * 从对象名称确定文件类型
     */
    private String determineFileTypeFromObjectName(String objectName) {
        if (objectName.startsWith("music/")) {
            return "music";
        } else if (objectName.startsWith("image/")) {
            return "image";
        } else if (objectName.startsWith("lyric/")) {
            return "lyric";
        } else {
            return "other";
        }
    }

    // 继续实现其他方法...
    // 由于文件过长，我将在下一个文件中继续实现剩余的方法

    // ========================================
    // 文件下载
    // ========================================

    @Override
    public InputStream downloadFile(String fileId) {
        // 这里需要根据fileId查找对应的bucketName和objectName
        // 暂时使用默认存储桶，实际项目中应该从数据库查询
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId; // 简化处理
        return downloadFile(bucketName, objectName);
    }

    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioDownloadException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedDownloadUrl(String fileId, int expiry) {
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId;
        return getPresignedDownloadUrl(bucketName, objectName, expiry);
    }

    @Override
    public String getPresignedDownloadUrl(String bucketName, String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiry)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名下载URL失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioException("获取预签名下载URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPublicUrl(String fileId) {
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId;
        return getPublicUrl(bucketName, objectName);
    }

    @Override
    public String getPublicUrl(String bucketName, String objectName) {
        return String.format("%s/%s/%s", 
                minioProperties.getFullEndpoint(), bucketName, objectName);
    }

    // ========================================
    // 文件管理
    // ========================================

    @Override
    public boolean fileExists(String fileId) {
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId;
        return fileExists(bucketName, objectName);
    }

    @Override
    public boolean fileExists(String bucketName, String objectName) {
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

    @Override
    public void deleteFile(String fileId) {
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId;
        deleteFile(bucketName, objectName);
    }

    @Override
    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            log.info("文件删除成功: bucket={}, object={}", bucketName, objectName);
        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFiles(List<String> fileIds) {
        String bucketName = minioProperties.getBucket().getName();
        deleteFiles(bucketName, fileIds);
    }

    @Override
    public void deleteFiles(String bucketName, List<String> objectNames) {
        try {
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());
            
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build());
            
            // 检查删除结果
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                if (error != null) {
                    log.error("批量删除文件失败: object={}, error={}", 
                            error.objectName(), error.message());
                }
            }
            
            log.info("批量文件删除完成: bucket={}, count={}", bucketName, objectNames.size());
        } catch (Exception e) {
            log.error("批量文件删除失败: bucket={}", bucketName, e);
            throw new MinioException("批量文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileInfoResponse copyFile(String sourceFileId, String targetFileId) {
        String sourceBucket = minioProperties.getBucket().getName();
        String targetBucket = minioProperties.getBucket().getName();
        return copyFile(sourceBucket, sourceFileId, targetBucket, targetFileId);
    }

    @Override
    public FileInfoResponse copyFile(String sourceBucket, String sourceObject, 
                                    String targetBucket, String targetObject) {
        try {
            // 复制文件
            ObjectWriteResponse response = minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(targetBucket)
                    .object(targetObject)
                    .source(CopySource.builder()
                            .bucket(sourceBucket)
                            .object(sourceObject)
                            .build())
                    .build());
            
            log.info("文件复制成功: {}:{} -> {}:{}", 
                    sourceBucket, sourceObject, targetBucket, targetObject);
            
            // 获取源文件信息来构建响应
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(targetBucket)
                    .object(targetObject)
                    .build());
            
            return FileInfoResponse.builder()
                    .fileId(generateFileId())
                    .originalFilename(extractFilenameFromObjectName(targetObject))
                    .storedFilename(targetObject)
                    .fileType(determineFileTypeFromObjectName(targetObject))
                    .mimeType(stat.contentType())
                    .fileSize(stat.size())
                    .formattedSize(formatFileSize(stat.size()))
                    .bucketName(targetBucket)
                    .accessUrl(getPublicUrl(targetBucket, targetObject))
                    .etag(response.etag())
                    .uploadTime(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();
            
        } catch (Exception e) {
            log.error("文件复制失败: {}:{} -> {}:{}", 
                    sourceBucket, sourceObject, targetBucket, targetObject, e);
            throw new MinioException("文件复制失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileInfoResponse moveFile(String sourceFileId, String targetFileId) {
        // 先复制文件
        FileInfoResponse response = copyFile(sourceFileId, targetFileId);
        
        // 然后删除源文件
        try {
            deleteFile(sourceFileId);
            log.info("文件移动成功: {} -> {}", sourceFileId, targetFileId);
            return response;
        } catch (Exception e) {
            // 如果删除失败，尝试删除已复制的文件
            try {
                deleteFile(targetFileId);
            } catch (Exception deleteException) {
                log.error("清理复制文件失败: {}", targetFileId, deleteException);
            }
            throw new MinioException("文件移动失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileInfoResponse renameFile(String fileId, String newName) {
        String bucketName = minioProperties.getBucket().getName();
        String oldObjectName = fileId;
        String newObjectName = buildNewObjectName(oldObjectName, newName);
        
        return copyFile(bucketName, oldObjectName, bucketName, newObjectName);
    }

    // ========================================
    // 文件信息查询
    // ========================================

    @Override
    public FileInfoResponse getFileInfo(String fileId) {
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId;
        
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            return FileInfoResponse.builder()
                    .fileId(fileId)
                    .originalFilename(extractFilenameFromObjectName(objectName))
                    .storedFilename(objectName)
                    .fileType(determineFileTypeFromObjectName(objectName))
                    .mimeType(stat.contentType())
                    .fileSize(stat.size())
                    .formattedSize(formatFileSize(stat.size()))
                    .bucketName(bucketName)
                    .accessUrl(getPublicUrl(bucketName, objectName))
                    .etag(stat.etag())
                    .uploadTime(LocalDateTime.now()) // 这里应该从元数据中获取
                    .status("ACTIVE")
                    .build();
            
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", fileId, e);
            throw new MinioException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileDetailVO getFileDetail(String fileId) {
        // 实现文件详细信息获取
        // 这里需要结合数据库查询来获取完整信息
        FileInfoResponse fileInfo = getFileInfo(fileId);
        
        return FileDetailVO.builder()
                .fileId(fileInfo.getFileId())
                .originalFilename(fileInfo.getOriginalFilename())
                .storedFilename(fileInfo.getStoredFilename())
                .fileType(fileInfo.getFileType())
                .mimeType(fileInfo.getMimeType())
                .fileSize(fileInfo.getFileSize())
                .formattedSize(fileInfo.getFormattedSize())
                .bucketName(fileInfo.getBucketName())
                .accessUrl(fileInfo.getAccessUrl())
                .description(fileInfo.getDescription())
                .publicAccess(fileInfo.getPublicAccess())
                .businessId(fileInfo.getBusinessId())
                .uploadTime(fileInfo.getUploadTime())
                .status(fileInfo.getStatus())
                .build();
    }

    @Override
    public Map<String, Object> getFileStats(String fileId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            FileInfoResponse fileInfo = getFileInfo(fileId);
            stats.put("fileSize", fileInfo.getFileSize());
            stats.put("formattedSize", fileInfo.getFormattedSize());
            stats.put("fileType", fileInfo.getFileType());
            stats.put("mimeType", fileInfo.getMimeType());
            stats.put("uploadTime", fileInfo.getUploadTime());
            stats.put("accessCount", 0L); // 这里应该从数据库获取
            stats.put("lastAccessTime", null); // 这里应该从数据库获取
            
        } catch (Exception e) {
            log.error("获取文件统计信息失败: {}", fileId, e);
            throw new MinioException("获取文件统计信息失败: " + e.getMessage(), e);
        }
        
        return stats;
    }

    @Override
    public Map<String, String> getFileMetadata(String fileId) {
        String bucketName = minioProperties.getBucket().getName();
        String objectName = fileId;
        return getFileMetadata(bucketName, objectName);
    }

    @Override
    public Map<String, String> getFileMetadata(String bucketName, String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            
            return new HashMap<>(stat.userMetadata());
            
        } catch (Exception e) {
            log.error("获取文件元数据失败: bucket={}, object={}", bucketName, objectName, e);
            throw new MinioException("获取文件元数据失败: " + e.getMessage(), e);
        }
    }

    // ========================================
    // 文件列表查询
    // ========================================

    @Override
    public List<FileListVO> listFiles(String bucketName, String prefix, int maxKeys) {
        List<FileListVO> fileList = new ArrayList<>();
        
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .maxKeys(maxKeys)
                    .build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                
                FileListVO fileVO = FileListVO.builder()
                        .fileId(generateFileId())
                        .originalFilename(extractFilenameFromObjectName(item.objectName()))
                        .fileType(determineFileTypeFromObjectName(item.objectName()))
                        .formattedSize(formatFileSize(item.size()))
                        .accessUrl(getPublicUrl(bucketName, item.objectName()))
                        .uploadTime(item.lastModified().toLocalDateTime())
                        .status("ACTIVE")
                        .build();
                
                fileList.add(fileVO);
            }
            
        } catch (Exception e) {
            log.error("列举文件失败: bucket={}, prefix={}", bucketName, prefix, e);
            throw new MinioException("列举文件失败: " + e.getMessage(), e);
        }
        
        return fileList;
    }

    @Override
    public List<FileListVO> listFilesByType(String fileType, int maxKeys) {
        String bucketName = getBucketNameByFileType(fileType);
        return listFiles(bucketName, fileType + "/", maxKeys);
    }

    @Override
    public List<FileListVO> listFilesByUser(Long userId, String fileType, int maxKeys) {
        // 这里需要结合数据库查询用户的文件
        // 暂时返回空列表
        return new ArrayList<>();
    }

    // ========================================
    // 工具方法
    // ========================================

    @Override
    public String getBucketNameByFileType(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "music", "audio" -> minioProperties.getBucket().getMusic();
            case "image", "picture" -> minioProperties.getBucket().getImage();
            case "lyric", "lrc" -> minioProperties.getBucket().getLyric();
            case "temp", "temporary" -> minioProperties.getBucket().getTemp();
            default -> minioProperties.getBucket().getName();
        };
    }

    @Override
    public String generateObjectName(String originalFilename, String fileType) {
        String extension = getFileExtension(originalFilename);
        String pathTemplate = minioProperties.getUpload().getPathTemplate();
        LocalDateTime now = LocalDateTime.now();
        
        String path = pathTemplate
                .replace("{type}", fileType)
                .replace("{year}", String.valueOf(now.getYear()))
                .replace("{month}", String.format("%02d", now.getMonthValue()))
                .replace("{day}", String.format("%02d", now.getDayOfMonth()));
        
        String filename;
        if ("uuid".equals(minioProperties.getUpload().getFilenameStrategy())) {
            filename = UUID.randomUUID().toString();
        } else if ("timestamp".equals(minioProperties.getUpload().getFilenameStrategy())) {
            filename = String.valueOf(System.currentTimeMillis());
        } else {
            // original strategy
            filename = originalFilename.substring(0, 
                    originalFilename.lastIndexOf('.') > 0 ? 
                    originalFilename.lastIndexOf('.') : originalFilename.length());
        }
        
        if (Boolean.TRUE.equals(minioProperties.getUpload().getKeepOriginalExtension()) 
                && StringUtils.hasText(extension)) {
            filename += "." + extension;
        }
        
        return path + "/" + filename;
    }

    @Override
    public boolean validateFileType(MultipartFile file, String fileType) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        List<String> allowedTypes = switch (fileType.toLowerCase()) {
            case "music", "audio" -> minioProperties.getUpload().getAllowedMusicTypes();
            case "image", "picture" -> minioProperties.getUpload().getAllowedImageTypes();
            case "lyric", "lrc" -> minioProperties.getUpload().getAllowedLyricTypes();
            default -> List.of(); // 不允许其他类型
        };
        
        return allowedTypes.contains(contentType.toLowerCase());
    }

    @Override
    public String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", 
                size / Math.pow(1024, digitGroups), 
                units[digitGroups]);
    }

    @Override
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }

    @Override
    public String generateFileId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 构建新的对象名称（用于重命名）
     */
    private String buildNewObjectName(String oldObjectName, String newName) {
        String directory = "";
        if (oldObjectName.contains("/")) {
            directory = oldObjectName.substring(0, oldObjectName.lastIndexOf("/") + 1);
        }
        
        String extension = getFileExtension(oldObjectName);
        String newFilename = newName;
        
        if (StringUtils.hasText(extension) && !newName.endsWith("." + extension)) {
            newFilename += "." + extension;
        }
        
        return directory + newFilename;
    }
}