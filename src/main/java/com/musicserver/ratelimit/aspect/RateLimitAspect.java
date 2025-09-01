package com.musicserver.ratelimit.aspect;

import com.musicserver.ratelimit.annotation.RateLimit;
import com.musicserver.ratelimit.config.RateLimitProperties;
import com.musicserver.ratelimit.dto.RateLimitResult;
import com.musicserver.ratelimit.exception.RateLimitExceededException;
import com.musicserver.ratelimit.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流AOP切面
 * 
 * 拦截带有@RateLimit注解的方法，执行限流检查
 * 支持SpEL表达式和多种上下文信息提取
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Aspect
@Component
@Order(1) // 高优先级，确保在其他切面之前执行
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final ExpressionParser parser = new SpelExpressionParser();

    public RateLimitAspect(RateLimitService rateLimitService, RateLimitProperties properties) {
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    /**
     * 定义切点：匹配所有带有@RateLimit注解的方法
     */
    @Pointcut("@annotation(com.musicserver.ratelimit.annotation.RateLimit)")
    public void rateLimitPointcut() {
    }

    /**
     * 环绕通知：执行限流检查
     */
    @Around("rateLimitPointcut() && @annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 检查是否启用限流
        if (!properties.getEnabled() || !rateLimit.enabled()) {
            return joinPoint.proceed();
        }

        // 检查条件表达式
        if (!evaluateCondition(rateLimit, joinPoint)) {
            return joinPoint.proceed();
        }

        // 构建限流key
        String rateLimitKey = buildRateLimitKey(rateLimit, joinPoint);
        
        // 构建上下文信息
        Map<String, Object> context = buildContext(joinPoint);
        
        // 执行限流检查
        RateLimitResult result = rateLimitService.tryAcquire(rateLimit, rateLimitKey, context);
        
        // 记录限流结果
        logRateLimitResult(rateLimitKey, result, joinPoint);
        
        // 处理限流结果
        if (!result.isAllowed()) {
            return handleRateLimitExceeded(rateLimit, result, joinPoint);
        }
        
        // 限流检查通过，执行原方法
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            // 如果原方法执行失败，可以考虑回退令牌（取决于策略）
            handleMethodExecutionFailure(rateLimitKey, rateLimit, e);
            throw e;
        }
    }

    /**
     * 构建限流key
     */
    private String buildRateLimitKey(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        String key = rateLimit.key();
        
        // 如果没有指定key，使用方法签名作为默认key
        if (!StringUtils.hasText(key)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            key = signature.getDeclaringTypeName() + "." + signature.getName();
        } else if (key.contains("#")) {
            // 支持SpEL表达式
            key = evaluateSpelExpression(key, joinPoint);
        }
        
        // 如果使用了自定义key生成器
        if (StringUtils.hasText(rateLimit.keyGenerator())) {
            // TODO: 实现自定义key生成器的调用
            log.debug("Custom key generator not implemented yet: {}", rateLimit.keyGenerator());
        }
        
        return key;
    }

    /**
     * 构建上下文信息
     */
    private Map<String, Object> buildContext(ProceedingJoinPoint joinPoint) {
        Map<String, Object> context = new HashMap<>();
        
        try {
            // 获取HTTP请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 添加请求相关信息
                context.put("clientIp", getClientIp(request));
                context.put("apiPath", request.getRequestURI());
                context.put("method", request.getMethod());
                context.put("userAgent", request.getHeader("User-Agent"));
                context.put("referer", request.getHeader("Referer"));
                
                // 添加用户信息（如果存在）
                Object userId = request.getAttribute("userId");
                if (userId != null) {
                    context.put("userId", userId.toString());
                }
                
                Object userRole = request.getAttribute("userRole");
                if (userRole != null) {
                    context.put("userRole", userRole.toString());
                }
                
                // 添加设备信息
                String deviceId = request.getHeader("Device-Id");
                if (StringUtils.hasText(deviceId)) {
                    context.put("deviceId", deviceId);
                }
                
                String appId = request.getHeader("App-Id");
                if (StringUtils.hasText(appId)) {
                    context.put("appId", appId);
                }
            }
            
            // 添加方法相关信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            context.put("className", signature.getDeclaringTypeName());
            context.put("methodName", signature.getName());
            
            // 添加方法参数信息（用于热点参数检测）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                context.put("methodArgs", args);
                
                // 尝试提取热点参数
                String hotspotParam = extractHotspotParameter(args);
                if (StringUtils.hasText(hotspotParam)) {
                    context.put("hotspotParam", hotspotParam);
                }
            }
            
            // 添加时间信息
            context.put("timestamp", System.currentTimeMillis());
            context.put("hour", java.time.LocalDateTime.now().getHour());
            context.put("dayOfWeek", java.time.LocalDateTime.now().getDayOfWeek().getValue());
            
        } catch (Exception e) {
            log.warn("Failed to build context for rate limiting", e);
        }
        
        return context;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }

    /**
     * 提取热点参数
     */
    private String extractHotspotParameter(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 优先使用第一个String类型的参数作为热点参数
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        
        // 如果没有String参数，使用第一个参数的toString
        Object firstArg = args[0];
        return firstArg != null ? firstArg.toString() : null;
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(RateLimit rateLimit, ProceedingJoinPoint joinPoint) {
        String condition = rateLimit.condition();
        if (!StringUtils.hasText(condition)) {
            return true; // 没有条件，默认允许
        }
        
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // 添加方法参数到上下文
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            if (paramNames != null && args != null) {
                for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            
            // 添加请求信息到上下文
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                context.setVariable("request", attributes.getRequest());
            }
            
            Boolean result = parser.parseExpression(condition).getValue(context, Boolean.class);
            return result != null && result;
            
        } catch (Exception e) {
            log.warn("Failed to evaluate condition expression: {}", condition, e);
            return true; // 表达式执行失败时，默认允许通过
        }
    }

    /**
     * 评估SpEL表达式
     */
    private String evaluateSpelExpression(String expression, ProceedingJoinPoint joinPoint) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // 添加方法参数到上下文
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            if (paramNames != null && args != null) {
                for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            
            // 添加请求信息到上下文
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                context.setVariable("request", request);
                context.setVariable("ip", getClientIp(request));
                context.setVariable("uri", request.getRequestURI());
            }
            
            Object result = parser.parseExpression(expression).getValue(context);
            return result != null ? result.toString() : expression;
            
        } catch (Exception e) {
            log.warn("Failed to evaluate SpEL expression: {}", expression, e);
            return expression; // SpEL执行失败时，返回原始表达式
        }
    }

    /**
     * 处理限流超限情况
     */
    private Object handleRateLimitExceeded(RateLimit rateLimit, RateLimitResult result, ProceedingJoinPoint joinPoint) 
            throws Throwable {
        
        // 如果指定了回退方法，尝试执行回退方法
        if (StringUtils.hasText(rateLimit.fallbackMethod())) {
            return executeFallbackMethod(rateLimit.fallbackMethod(), joinPoint);
        }
        
        // 如果配置为忽略异常，返回null
        if (rateLimit.ignoreException()) {
            log.info("Rate limit exceeded but ignoring exception for key: {}", result.getKey());
            return null;
        }
        
        // 抛出限流异常
        throw new RateLimitExceededException(
            result.getMessage() != null ? result.getMessage() : rateLimit.message(),
            result.getErrorCode() != 0 ? result.getErrorCode() : rateLimit.errorCode(),
            result
        );
    }

    /**
     * 执行回退方法
     */
    private Object executeFallbackMethod(String fallbackMethodName, ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object target = joinPoint.getTarget();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method originalMethod = signature.getMethod();
            
            // 查找回退方法（参数列表应该与原方法相同）
            Method fallbackMethod = target.getClass().getDeclaredMethod(
                fallbackMethodName, 
                originalMethod.getParameterTypes()
            );
            
            fallbackMethod.setAccessible(true);
            
            log.info("Executing fallback method: {} for rate limited method: {}", 
                    fallbackMethodName, originalMethod.getName());
            
            return fallbackMethod.invoke(target, joinPoint.getArgs());
            
        } catch (Exception e) {
            log.error("Failed to execute fallback method: {}", fallbackMethodName, e);
            throw e;
        }
    }

    /**
     * 处理方法执行失败的情况
     */
    private void handleMethodExecutionFailure(String key, RateLimit rateLimit, Exception e) {
        // 对于某些策略（如令牌桶），可以考虑回退令牌
        // 这里暂时只记录日志
        log.debug("Method execution failed after rate limit check passed for key: {}, error: {}", 
                 key, e.getMessage());
    }

    /**
     * 记录限流结果日志
     */
    private void logRateLimitResult(String key, RateLimitResult result, ProceedingJoinPoint joinPoint) {
        if (!properties.getEnableLog()) {
            return;
        }
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        
        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for method: {}, key: {}, remaining: {}/{}, resetIn: {}s, hotspot: {}",
                    methodName, key, result.getRemaining(), result.getLimit(), 
                    result.getSecondsToReset(), result.isHotspot());
        } else {
            log.debug("Rate limit passed for method: {}, key: {}, remaining: {}/{}",
                     methodName, key, result.getRemaining(), result.getLimit());
        }
    }
}