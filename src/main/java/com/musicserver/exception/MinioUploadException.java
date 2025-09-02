package com.musicserver.exception;

import com.musicserver.common.ResultCode;

/**
 * Minio文件上传异常
 * <p>
 * 当文件上传失败时抛出
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class MinioUploadException extends MinioException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public MinioUploadException(String message) {
        super(ResultCode.FILE_UPLOAD_FAILED, message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public MinioUploadException(String message, Throwable cause) {
        super(ResultCode.FILE_UPLOAD_FAILED, message, cause);
    }
}