package com.musicserver.exception;

import com.musicserver.common.ResultCode;

/**
 * Minio存储桶异常
 * <p>
 * 当存储桶操作失败时抛出
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class MinioBucketException extends MinioException {

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public MinioBucketException(String message) {
        super(ResultCode.MINIO_BUCKET_ERROR, message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public MinioBucketException(String message, Throwable cause) {
        super(ResultCode.MINIO_BUCKET_ERROR, message, cause);
    }
}