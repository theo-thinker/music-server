package com.musicserver.controller;

import com.musicserver.common.Result;
import com.musicserver.dto.request.FileUploadRequest;
import com.musicserver.dto.response.FileInfoResponse;
import com.musicserver.dto.response.PageResponse;
import com.musicserver.service.MinioService;
import com.musicserver.vo.FileDetailVO;
import com.musicserver.vo.FileListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 * 
 * 提供文件上传、下载、删除等REST API接口
 * 支持多种文件类型和操作方式
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作接口")
public class FileController {

    private final MinioService minioService;

    // ========================================
    // 文件上传接口
    // ========================================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "支持多种文件类型的上传")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "请求参数错误"),
            @ApiResponse(responseCode = "413", description = "文件大小超出限制"),
            @ApiResponse(responseCode = "415", description = "不支持的文件类型")
    })
    public Result<FileInfoResponse> uploadFile(@Valid @ModelAttribute FileUploadRequest request) {
        log.info("上传文件请求: fileType={}, filename={}, size={}", 
                request.getFileType(), 
                request.getFile().getOriginalFilename(), 
                request.getFile().getSize());

        FileInfoResponse response = minioService.uploadFile(request);
        
        log.info("文件上传成功: fileId={}, originalFilename={}", 
                response.getFileId(), response.getOriginalFilename());

        return Result.success(response);
    }

    @PostMapping("/upload/simple")
    @Operation(summary = "简单文件上传", description = "简化的文件上传接口")
    public Result<FileInfoResponse> uploadFileSimple(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型", required = true, example = "music")
            @RequestParam("fileType") String fileType) {
        
        log.info("简单上传文件: fileType={}, filename={}", fileType, file.getOriginalFilename());

        FileInfoResponse response = minioService.uploadFile(file, fileType);
        return Result.success(response);
    }

    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传文件", description = "批量上传多个文件")
    public Result<List<FileInfoResponse>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "文件类型", required = true, example = "music")
            @RequestParam("fileType") String fileType) {
        
        log.info("批量上传文件: fileType={}, count={}", fileType, files.size());

        List<FileInfoResponse> responses = minioService.uploadFiles(files, fileType);
        return Result.success(responses);
    }

    // ========================================
    // 文件下载接口
    // ========================================

    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    public void downloadFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            HttpServletResponse response) {
        
        log.info("下载文件请求: fileId={}", fileId);

        try {
            // 获取文件信息
            FileInfoResponse fileInfo = minioService.getFileInfo(fileId);
            
            // 设置响应头
            response.setContentType(fileInfo.getMimeType());
            response.setHeader("Content-Disposition", 
                    "attachment; filename=\"" + fileInfo.getOriginalFilename() + "\"");
            response.setContentLengthLong(fileInfo.getFileSize());

            // 获取文件流并写入响应
            try (InputStream inputStream = minioService.downloadFile(fileId)) {
                inputStream.transferTo(response.getOutputStream());
                response.flushBuffer();
            }

            log.info("文件下载成功: fileId={}, filename={}", fileId, fileInfo.getOriginalFilename());

        } catch (Exception e) {
            log.error("文件下载失败: fileId={}", fileId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/download/url/{fileId}")
    @Operation(summary = "获取下载链接", description = "获取文件的预签名下载URL")
    public Result<String> getDownloadUrl(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            @Parameter(description = "链接有效期（秒）", example = "3600")
            @RequestParam(defaultValue = "3600") int expiry) {
        
        log.info("获取下载链接: fileId={}, expiry={}", fileId, expiry);

        String downloadUrl = minioService.getPresignedDownloadUrl(fileId, expiry);
        return Result.success(downloadUrl);
    }

    @GetMapping("/preview/{fileId}")
    @Operation(summary = "预览文件", description = "获取文件的公开访问URL用于预览")
    public Result<String> getPreviewUrl(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.info("获取预览链接: fileId={}", fileId);

        String previewUrl = minioService.getPublicUrl(fileId);
        return Result.success(previewUrl);
    }

    // ========================================
    // 文件管理接口
    // ========================================

    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    @PreAuthorize("hasRole('ADMIN') or @filePermissionChecker.canDelete(#fileId)")
    public Result<Void> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.info("删除文件请求: fileId={}", fileId);

        minioService.deleteFile(fileId);
        
        log.info("文件删除成功: fileId={}", fileId);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteFiles(
            @Parameter(description = "文件ID列表", required = true)
            @RequestBody List<String> fileIds) {
        
        log.info("批量删除文件请求: count={}", fileIds.size());

        minioService.deleteFiles(fileIds);
        
        log.info("批量文件删除成功: count={}", fileIds.size());
        return Result.success();
    }

    @PostMapping("/copy")
    @Operation(summary = "复制文件", description = "复制文件到新位置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<FileInfoResponse> copyFile(
            @Parameter(description = "源文件ID", required = true)
            @RequestParam String sourceFileId,
            @Parameter(description = "目标文件ID", required = true)
            @RequestParam String targetFileId) {
        
        log.info("复制文件请求: sourceFileId={}, targetFileId={}", sourceFileId, targetFileId);

        FileInfoResponse response = minioService.copyFile(sourceFileId, targetFileId);
        
        log.info("文件复制成功: sourceFileId={}, targetFileId={}", sourceFileId, targetFileId);
        return Result.success(response);
    }

    @PostMapping("/move")
    @Operation(summary = "移动文件", description = "移动文件到新位置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<FileInfoResponse> moveFile(
            @Parameter(description = "源文件ID", required = true)
            @RequestParam String sourceFileId,
            @Parameter(description = "目标文件ID", required = true)
            @RequestParam String targetFileId) {
        
        log.info("移动文件请求: sourceFileId={}, targetFileId={}", sourceFileId, targetFileId);

        FileInfoResponse response = minioService.moveFile(sourceFileId, targetFileId);
        
        log.info("文件移动成功: sourceFileId={}, targetFileId={}", sourceFileId, targetFileId);
        return Result.success(response);
    }

    @PutMapping("/{fileId}/rename")
    @Operation(summary = "重命名文件", description = "重命名文件")
    @PreAuthorize("hasRole('ADMIN') or @filePermissionChecker.canEdit(#fileId)")
    public Result<FileInfoResponse> renameFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId,
            @Parameter(description = "新文件名", required = true)
            @RequestParam String newName) {
        
        log.info("重命名文件请求: fileId={}, newName={}", fileId, newName);

        FileInfoResponse response = minioService.renameFile(fileId, newName);
        
        log.info("文件重命名成功: fileId={}, newName={}", fileId, newName);
        return Result.success(response);
    }

    // ========================================
    // 文件信息查询接口
    // ========================================

    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件基本信息")
    public Result<FileInfoResponse> getFileInfo(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.debug("获取文件信息: fileId={}", fileId);

        FileInfoResponse response = minioService.getFileInfo(fileId);
        return Result.success(response);
    }

    @GetMapping("/{fileId}/detail")
    @Operation(summary = "获取文件详细信息", description = "获取文件的详细信息")
    public Result<FileDetailVO> getFileDetail(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.debug("获取文件详细信息: fileId={}", fileId);

        FileDetailVO response = minioService.getFileDetail(fileId);
        return Result.success(response);
    }

    @GetMapping("/{fileId}/stats")
    @Operation(summary = "获取文件统计信息", description = "获取文件的统计信息")
    public Result<Map<String, Object>> getFileStats(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.debug("获取文件统计信息: fileId={}", fileId);

        Map<String, Object> stats = minioService.getFileStats(fileId);
        return Result.success(stats);
    }

    @GetMapping("/{fileId}/metadata")
    @Operation(summary = "获取文件元数据", description = "获取文件的元数据信息")
    public Result<Map<String, String>> getFileMetadata(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.debug("获取文件元数据: fileId={}", fileId);

        Map<String, String> metadata = minioService.getFileMetadata(fileId);
        return Result.success(metadata);
    }

    @GetMapping("/{fileId}/exists")
    @Operation(summary = "检查文件是否存在", description = "检查指定文件是否存在")
    public Result<Boolean> fileExists(
            @Parameter(description = "文件ID", required = true)
            @PathVariable String fileId) {
        
        log.debug("检查文件是否存在: fileId={}", fileId);

        boolean exists = minioService.fileExists(fileId);
        return Result.success(exists);
    }

    // ========================================
    // 文件列表查询接口
    // ========================================

    @GetMapping("/list")
    @Operation(summary = "文件列表", description = "获取文件列表")
    public Result<PageResponse<FileListVO>> listFiles(
            @Parameter(description = "存储桶名称")
            @RequestParam(required = false) String bucketName,
            @Parameter(description = "文件前缀")
            @RequestParam(required = false) String prefix,
            @Parameter(description = "最大数量", example = "20")
            @RequestParam(defaultValue = "20") int maxKeys) {
        
        log.debug("获取文件列表: bucketName={}, prefix={}, maxKeys={}", bucketName, prefix, maxKeys);

        if (bucketName == null) {
            bucketName = "music-server"; // 默认存储桶
        }

        List<FileListVO> files = minioService.listFiles(bucketName, prefix, maxKeys);
        
        // 构建分页响应
        PageResponse<FileListVO> pageResponse = new PageResponse<>();
        pageResponse.setRecords(files);
        pageResponse.setTotal((long) files.size());
        pageResponse.setSize(maxKeys);
        pageResponse.setCurrent(1);
        pageResponse.setPages(1L);

        return Result.success(pageResponse);
    }

    @GetMapping("/list/type/{fileType}")
    @Operation(summary = "按类型获取文件列表", description = "根据文件类型获取文件列表")
    public Result<PageResponse<FileListVO>> listFilesByType(
            @Parameter(description = "文件类型", required = true, example = "music")
            @PathVariable String fileType,
            @Parameter(description = "最大数量", example = "20")
            @RequestParam(defaultValue = "20") int maxKeys) {
        
        log.debug("按类型获取文件列表: fileType={}, maxKeys={}", fileType, maxKeys);

        List<FileListVO> files = minioService.listFilesByType(fileType, maxKeys);
        
        // 构建分页响应
        PageResponse<FileListVO> pageResponse = new PageResponse<>();
        pageResponse.setRecords(files);
        pageResponse.setTotal((long) files.size());
        pageResponse.setSize(maxKeys);
        pageResponse.setCurrent(1);
        pageResponse.setPages(1L);

        return Result.success(pageResponse);
    }

    @GetMapping("/list/user/{userId}")
    @Operation(summary = "按用户获取文件列表", description = "根据用户ID获取文件列表")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public Result<PageResponse<FileListVO>> listFilesByUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "文件类型")
            @RequestParam(required = false) String fileType,
            @Parameter(description = "最大数量", example = "20")
            @RequestParam(defaultValue = "20") int maxKeys) {
        
        log.debug("按用户获取文件列表: userId={}, fileType={}, maxKeys={}", userId, fileType, maxKeys);

        List<FileListVO> files = minioService.listFilesByUser(userId, fileType, maxKeys);
        
        // 构建分页响应
        PageResponse<FileListVO> pageResponse = new PageResponse<>();
        pageResponse.setRecords(files);
        pageResponse.setTotal((long) files.size());
        pageResponse.setSize(maxKeys);
        pageResponse.setCurrent(1);
        pageResponse.setPages(1L);

        return Result.success(pageResponse);
    }

    // ========================================
    // 存储桶管理接口
    // ========================================

    @GetMapping("/buckets")
    @Operation(summary = "获取存储桶列表", description = "获取所有存储桶列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<String>> listBuckets() {
        log.debug("获取存储桶列表");

        List<String> buckets = minioService.listBuckets();
        return Result.success(buckets);
    }

    @PostMapping("/buckets/{bucketName}")
    @Operation(summary = "创建存储桶", description = "创建新的存储桶")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createBucket(
            @Parameter(description = "存储桶名称", required = true)
            @PathVariable String bucketName) {
        
        log.info("创建存储桶: bucketName={}", bucketName);

        minioService.createBucket(bucketName);
        
        log.info("存储桶创建成功: bucketName={}", bucketName);
        return Result.success();
    }

    @DeleteMapping("/buckets/{bucketName}")
    @Operation(summary = "删除存储桶", description = "删除指定存储桶")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBucket(
            @Parameter(description = "存储桶名称", required = true)
            @PathVariable String bucketName) {
        
        log.info("删除存储桶: bucketName={}", bucketName);

        minioService.deleteBucket(bucketName);
        
        log.info("存储桶删除成功: bucketName={}", bucketName);
        return Result.success();
    }

    @GetMapping("/buckets/{bucketName}/exists")
    @Operation(summary = "检查存储桶是否存在", description = "检查指定存储桶是否存在")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> bucketExists(
            @Parameter(description = "存储桶名称", required = true)
            @PathVariable String bucketName) {
        
        log.debug("检查存储桶是否存在: bucketName={}", bucketName);

        boolean exists = minioService.bucketExists(bucketName);
        return Result.success(exists);
    }
}