package com.musicserver.common.exception;

import com.musicserver.common.ResultCode;

/**
 * 业务异常类
 * 
 * 用于处理业务逻辑中的异常情况
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     * 
     * @param resultCode 结果代码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 构造函数
     * 
     * @param resultCode 结果代码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    /**
     * 构造函数
     * 
     * @param code 错误代码
     * @param message 错误消息
     * @param cause 原因异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     * 
     * @param resultCode 结果代码枚举
     * @param cause 原因异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置错误代码
     * 
     * @param code 错误代码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息
     * 
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}