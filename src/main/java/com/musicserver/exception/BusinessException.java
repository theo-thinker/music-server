package com.musicserver.exception;

import com.musicserver.common.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常类
 * 
 * 用于处理业务逻辑中的异常情况，携带具体的错误码和错误信息
 * 便于统一异常处理和错误信息返回
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Getter
public class BusinessException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    /**
     * 构造函数 - 使用错误码枚举
     * 
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
    
    /**
     * 构造函数 - 自定义错误信息
     * 
     * @param resultCode 错误码枚举
     * @param message 自定义错误信息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }
    
    /**
     * 构造函数 - 自定义错误码和错误信息
     * 
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造函数 - 包含原始异常
     * 
     * @param resultCode 错误码枚举
     * @param cause 原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
    
    /**
     * 构造函数 - 自定义错误信息和原始异常
     * 
     * @param resultCode 错误码枚举
     * @param message 自定义错误信息
     * @param cause 原始异常
     */
    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
        this.message = message;
    }
    
    /**
     * 静态工厂方法 - 创建业务异常
     * 
     * @param resultCode 错误码枚举
     * @return 业务异常实例
     */
    public static BusinessException of(ResultCode resultCode) {
        return new BusinessException(resultCode);
    }
    
    /**
     * 静态工厂方法 - 创建业务异常（自定义消息）
     * 
     * @param resultCode 错误码枚举
     * @param message 自定义错误信息
     * @return 业务异常实例
     */
    public static BusinessException of(ResultCode resultCode, String message) {
        return new BusinessException(resultCode, message);
    }
    
    /**
     * 静态工厂方法 - 创建业务异常（包含原始异常）
     * 
     * @param resultCode 错误码枚举
     * @param cause 原始异常
     * @return 业务异常实例
     */
    public static BusinessException of(ResultCode resultCode, Throwable cause) {
        return new BusinessException(resultCode, cause);
    }
}