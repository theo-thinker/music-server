package com.musicserver.exception;

import com.musicserver.common.Result;
import com.musicserver.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Set;

/**
 * 全局异常处理器
 * 
 * 统一处理系统中的各种异常，将异常转换为标准的JSON响应格式
 * 便于前端统一处理错误信息
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * 
     * @param e 业务异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常：{}, 请求URL: {}", e.getMessage(), request.getRequestURI());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常（@Valid）
     * 
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数校验异常, 请求URL: {}", request.getRequestURI());
        
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getField())
                   .append(": ")
                   .append(fieldError.getDefaultMessage())
                   .append("; ");
        }
        
        return Result.error(ResultCode.UNPROCESSABLE_ENTITY.getCode(), errorMsg.toString());
    }
    
    /**
     * 处理参数绑定异常
     * 
     * @param e 参数绑定异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定异常, 请求URL: {}", request.getRequestURI());
        
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMsg.append(fieldError.getField())
                   .append(": ")
                   .append(fieldError.getDefaultMessage())
                   .append("; ");
        }
        
        return Result.error(ResultCode.BAD_REQUEST.getCode(), errorMsg.toString());
    }
    
    /**
     * 处理约束违反异常（@Validated）
     * 
     * @param e 约束违反异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束违反异常, 请求URL: {}", request.getRequestURI());
        
        StringBuilder errorMsg = new StringBuilder();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            errorMsg.append(violation.getPropertyPath())
                   .append(": ")
                   .append(violation.getMessage())
                   .append("; ");
        }
        
        return Result.error(ResultCode.UNPROCESSABLE_ENTITY.getCode(), errorMsg.toString());
    }
    
    /**
     * 处理缺少请求参数异常
     * 
     * @param e 缺少请求参数异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数异常, 请求URL: {}, 参数: {}", request.getRequestURI(), e.getParameterName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "缺少必需的请求参数: " + e.getParameterName());
    }
    
    /**
     * 处理参数类型不匹配异常
     * 
     * @param e 参数类型不匹配异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配异常, 请求URL: {}, 参数: {}", request.getRequestURI(), e.getName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), 
            String.format("参数类型不匹配: %s，期望类型: %s", e.getName(), 
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知"));
    }
    
    /**
     * 处理文件上传大小超限异常
     * 
     * @param e 文件上传大小超限异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件上传大小超限异常, 请求URL: {}", request.getRequestURI());
        return Result.error(ResultCode.FILE_SIZE_EXCEEDED.getCode(), "上传文件大小超出限制");
    }
    
    /**
     * 处理Spring Security认证异常
     * 
     * @param e 认证异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证异常, 请求URL: {}, 异常信息: {}", request.getRequestURI(), e.getMessage());
        
        if (e instanceof BadCredentialsException) {
            return Result.error(ResultCode.INVALID_USERNAME_OR_PASSWORD.getCode(), "用户名或密码错误");
        } else if (e instanceof DisabledException) {
            return Result.error(ResultCode.USER_DISABLED.getCode(), "用户账户已被禁用");
        } else if (e instanceof LockedException) {
            return Result.error(ResultCode.USER_LOCKED.getCode(), "用户账户已被锁定");
        } else {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "认证失败");
        }
    }
    
    /**
     * 处理Spring Security授权异常
     * 
     * @param e 授权异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException e, HttpServletRequest request) {
        log.warn("授权异常, 请求URL: {}", request.getRequestURI());
        return Result.error(ResultCode.FORBIDDEN.getCode(), "权限不足，访问被拒绝");
    }
    
    /**
     * 处理文件访问拒绝异常
     * 
     * @param e 文件访问拒绝异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(java.nio.file.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleFileAccessDeniedException(java.nio.file.AccessDeniedException e, HttpServletRequest request) {
        log.warn("文件访问拒绝异常, 请求URL: {}", request.getRequestURI());
        return Result.error(ResultCode.FORBIDDEN.getCode(), "文件访问被拒绝");
    }
    
    /**
     * 处理404异常
     * 
     * @param e 404异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404异常, 请求URL: {}", request.getRequestURI());
        return Result.error(ResultCode.NOT_FOUND.getCode(), "请求的资源不存在");
    }
    
    /**
     * 处理数据库重复键异常
     * 
     * @param e 重复键异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("数据库重复键异常, 请求URL: {}", request.getRequestURI());
        return Result.error(ResultCode.DUPLICATE_KEY_ERROR.getCode(), "数据已存在，请检查后重试");
    }
    
    /**
     * 处理数据完整性约束违反异常
     * 
     * @param e 数据完整性约束违反异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("数据完整性约束违反异常, 请求URL: {}", request.getRequestURI());
        
        String message = e.getMessage();
        if (message != null && message.contains("foreign key")) {
            return Result.error(ResultCode.FOREIGN_KEY_ERROR.getCode(), "存在关联数据，无法执行此操作");
        } else if (message != null && message.contains("unique")) {
            return Result.error(ResultCode.DUPLICATE_KEY_ERROR.getCode(), "数据重复，请检查后重试");
        } else {
            return Result.error(ResultCode.DATA_INTEGRITY_ERROR.getCode(), "数据完整性约束违反");
        }
    }
    
    /**
     * 处理运行时异常
     * 
     * @param e 运行时异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常, 请求URL: {}, 异常信息: ", request.getRequestURI(), e);
        return Result.error(ResultCode.INTERNAL_ERROR.getCode(), "系统内部错误，请联系管理员");
    }
    
    /**
     * 处理其他未知异常
     * 
     * @param e 未知异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常, 请求URL: {}, 异常信息: ", request.getRequestURI(), e);
        return Result.error(ResultCode.ERROR.getCode(), "系统异常，请稍后重试");
    }
}