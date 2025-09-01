package com.musicserver.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果封装类
 * 
 * 提供统一的API响应格式，包含状态码、消息和数据
 * 支持链式调用，方便构建响应结果
 * 
 * @param <T> 返回数据的类型
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应状态码
     * 200: 成功
     * 400: 客户端请求错误
     * 401: 未授权
     * 403: 禁止访问
     * 404: 资源未找到
     * 500: 服务器内部错误
     */
    private Integer code;
    
    /**
     * 响应消息描述
     */
    private String message;
    
    /**
     * 响应数据内容
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private Long timestamp;
    
    /**
     * 请求追踪ID，用于日志跟踪
     */
    private String traceId;
    
    /**
     * 私有构造函数，防止外部直接实例化
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 私有构造函数，用于内部创建结果对象
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 返回成功结果（无数据）
     * 
     * @param <T> 数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 返回成功结果（带数据）
     * 
     * @param data 返回的数据
     * @param <T> 数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 返回成功结果（自定义消息）
     * 
     * @param message 自定义成功消息
     * @param <T> 数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, null);
    }
    
    /**
     * 返回成功结果（带数据和消息）
     * 
     * @param data 返回的数据
     * @param message 自定义成功消息
     * @param <T> 数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 返回失败结果
     * 
     * @param <T> 数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }
    
    /**
     * 返回失败结果（自定义消息）
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }
    
    /**
     * 返回失败结果（自定义状态码和消息）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 根据结果码枚举返回结果
     * 
     * @param resultCode 结果码枚举
     * @param <T> 数据类型
     * @return 结果对象
     */
    public static <T> Result<T> result(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    /**
     * 根据结果码枚举返回结果（带数据）
     * 
     * @param resultCode 结果码枚举
     * @param data 返回的数据
     * @param <T> 数据类型
     * @return 结果对象
     */
    public static <T> Result<T> result(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }
    
    /**
     * 设置追踪ID
     * 
     * @param traceId 追踪ID
     * @return 当前结果对象（支持链式调用）
     */
    public Result<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
    
    /**
     * 判断是否成功
     * 
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
    
    /**
     * 判断是否失败
     * 
     * @return true-失败，false-成功
     */
    public boolean isError() {
        return !isSuccess();
    }
}