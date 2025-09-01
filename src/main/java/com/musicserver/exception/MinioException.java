package com.musicserver.exception;

import com.musicserver.common.ResultCode;

/**
 * Minio操作异常基类
 * 
 * 所有Minio相关操作的异常基类
 * 提供统一的异常处理机制
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class MinioException extends BusinessException {

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public MinioException(String message) {
        super(ResultCode.MINIO_OPERATION_ERROR, message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原始异常
     */
    public MinioException(String message, Throwable cause) {
        super(ResultCode.MINIO_OPERATION_ERROR, message, cause);
    }

    /**
     * 构造函数
     * 
     * @param resultCode 错误码
     * @param message 异常消息
     */
    public MinioException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    /**
     * 构造函数
     * 
     * @param resultCode 错误码
     * @param message 异常消息
     * @param cause 原始异常
     */
    public MinioException(ResultCode resultCode, String message, Throwable cause) {
        super(resultCode, message, cause);
    }
}