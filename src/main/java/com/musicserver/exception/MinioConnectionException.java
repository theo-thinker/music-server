package com.musicserver.exception;

import com.musicserver.common.ResultCode;

/**
 * Minio连接异常
 * 
 * 当无法连接到Minio服务器时抛出
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class MinioConnectionException extends MinioException {

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public MinioConnectionException(String message) {
        super(ResultCode.MINIO_CONNECTION_ERROR, message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原始异常
     */
    public MinioConnectionException(String message, Throwable cause) {
        super(ResultCode.MINIO_CONNECTION_ERROR, message, cause);
    }
}