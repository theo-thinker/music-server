package com.musicserver.ratelimit.annotation;

import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * 
 * 支持多种限流策略和维度，提供企业级的接口限流功能
 * 
 * 使用示例：
 * <pre>
 * {@code
 * @RateLimit(
 *     key = "user_login", 
 *     limit = 5, 
 *     period = 60, 
 *     timeUnit = TimeUnit.SECONDS,
 *     strategy = RateLimitStrategy.SLIDING_WINDOW,
 *     type = RateLimitType.IP
 * )
 * public Result login(LoginRequest request) {
 *     // 登录逻辑
 * }
 * }
 * </pre>
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key，支持SpEL表达式
     * 如果为空，则使用方法签名作为key
     * 
     * @return 限流key
     */
    String key() default "";

    /**
     * 限流次数
     * 在指定时间窗口内允许的最大请求次数
     * 
     * @return 限流次数
     */
    long limit() default 100;

    /**
     * 时间窗口大小
     * 配合timeUnit使用，默认60秒
     * 
     * @return 时间窗口大小
     */
    long period() default 60;

    /**
     * 时间单位
     * 
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 限流策略
     * 支持滑动窗口、令牌桶、漏桶、固定窗口等策略
     * 
     * @return 限流策略
     */
    RateLimitStrategy strategy() default RateLimitStrategy.SLIDING_WINDOW;

    /**
     * 限流类型/维度
     * 支持按IP、用户、接口、自定义等维度限流
     * 
     * @return 限流类型
     */
    RateLimitType type() default RateLimitType.GLOBAL;

    /**
     * 是否启用限流
     * 可用于动态开关限流功能
     * 
     * @return 是否启用
     */
    boolean enabled() default true;

    /**
     * 限流失败时的提示信息
     * 
     * @return 提示信息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 限流失败时的错误码
     * 
     * @return 错误码
     */
    int errorCode() default 429;

    /**
     * 是否异步处理
     * true：异步处理，不阻塞当前请求
     * false：同步处理，可能阻塞当前请求
     * 
     * @return 是否异步处理
     */
    boolean async() default false;

    /**
     * 预热时间（秒）
     * 用于令牌桶策略的预热阶段
     * 
     * @return 预热时间
     */
    long warmupPeriod() default 0;

    /**
     * 令牌桶容量
     * 仅在令牌桶策略下生效，0表示使用默认值
     * 
     * @return 令牌桶容量
     */
    long bucketCapacity() default 0;

    /**
     * 令牌生成速率（令牌/秒）
     * 仅在令牌桶策略下生效，0表示使用默认值
     * 
     * @return 令牌生成速率
     */
    double refillRate() default 0.0;

    /**
     * 漏桶容量
     * 仅在漏桶策略下生效，0表示使用默认值
     * 
     * @return 漏桶容量
     */
    long leakyBucketCapacity() default 0;

    /**
     * 漏桶流出速率（请求/秒）
     * 仅在漏桶策略下生效，0表示使用默认值
     * 
     * @return 漏桶流出速率
     */
    double leakRate() default 0.0;

    /**
     * 滑动窗口分片数量
     * 仅在滑动窗口策略下生效，0表示使用默认值
     * 分片数量越多，精度越高，但内存消耗也越大
     * 
     * @return 滑动窗口分片数量
     */
    int windowSlices() default 0;

    /**
     * 自定义限流键生成器Bean名称
     * 当需要复杂的key生成逻辑时使用
     * 
     * @return Bean名称
     */
    String keyGenerator() default "";

    /**
     * 是否记录限流日志
     * 
     * @return 是否记录日志
     */
    boolean enableLog() default true;

    /**
     * 限流优先级
     * 数值越小优先级越高，用于多个限流规则的执行顺序
     * 
     * @return 优先级
     */
    int order() default 0;

    /**
     * 条件表达式
     * 支持SpEL表达式，只有当条件为true时才进行限流
     * 例如：#request.getRemoteAddr() != '127.0.0.1'
     * 
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 回退方法名
     * 当限流触发时，可以指定一个回退方法执行
     * 该方法必须与原方法在同一个类中，且参数列表相同
     * 
     * @return 回退方法名
     */
    String fallbackMethod() default "";

    /**
     * 是否忽略限流异常
     * true：限流时返回null而不抛异常
     * false：限流时抛出RateLimitException异常
     * 
     * @return 是否忽略异常
     */
    boolean ignoreException() default false;

    /**
     * 限流配置组
     * 用于将相关的限流配置分组管理
     * 
     * @return 配置组名称
     */
    String group() default "default";

    /**
     * 扩展属性
     * 用于传递自定义配置参数
     * 
     * @return 扩展属性键值对
     */
    String[] properties() default {};
}