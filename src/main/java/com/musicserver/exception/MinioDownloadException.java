package com.musicserver.exception;

import com.musicserver.common.ResultCode;

/**
 * Minio文件下载异常
 * <p>
 * 当文件下载失败时抛出
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class MinioDownloadException extends MinioException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public MinioDownloadException(String message) {
        super(ResultCode.FILE_DOWNLOAD_ERROR, message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public MinioDownloadException(String message, Throwable cause) {
        super(ResultCode.FILE_DOWNLOAD_ERROR, message, cause);
    }
}