package com.musicserver.ratelimit.controller;

import com.musicserver.common.Result;
import com.musicserver.ratelimit.annotation.RateLimit;
import com.musicserver.ratelimit.dto.RateLimitStatistics;
import com.musicserver.ratelimit.enums.RateLimitStrategy;
import com.musicserver.ratelimit.enums.RateLimitType;
import com.musicserver.ratelimit.monitor.RateLimitMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 限流功能演示控制器
 * 
 * 提供各种限流策略的演示接口
 * 用于测试和展示限流功能的效果
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RestController
@RequestMapping("/api/rate-limit-demo")
@RequiredArgsConstructor
@Tag(name = "限流演示", description = "限流功能演示和测试接口")
public class RateLimitDemoController {

    private final RateLimitMonitor rateLimitMonitor;

    /**
     * 滑动窗口限流演示
     * 1分钟内最多10次请求
     */
    @GetMapping("/sliding-window")
    @Operation(summary = "滑动窗口限流演示", description = "1分钟内最多允许10次请求")
    @RateLimit(
        key = "sliding_window_demo",
        limit = 10,
        period = 60,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.SLIDING_WINDOW,
        type = RateLimitType.GLOBAL,
        message = "滑动窗口限流：1分钟内最多10次请求"
    )
    public Result<Map<String, Object>> slidingWindowDemo() {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "滑动窗口");
        response.put("limit", "10次/分钟");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过滑动窗口限流检查");
        
        log.info("滑动窗口限流演示接口被调用");
        return Result.success(response);
    }

    /**
     * 令牌桶限流演示
     * 支持突发流量，桶容量20，每秒生成5个令牌
     */
    @GetMapping("/token-bucket")
    @Operation(summary = "令牌桶限流演示", description = "桶容量20，每秒生成5个令牌，支持突发流量")
    @RateLimit(
        key = "token_bucket_demo",
        limit = 20,
        period = 1,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.TOKEN_BUCKET,
        type = RateLimitType.GLOBAL,
        bucketCapacity = 20,
        refillRate = 5.0,
        message = "令牌桶限流：桶容量20，每秒生成5个令牌"
    )
    public Result<Map<String, Object>> tokenBucketDemo() {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "令牌桶");
        response.put("bucketCapacity", 20);
        response.put("refillRate", "5令牌/秒");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过令牌桶限流检查");
        
        log.info("令牌桶限流演示接口被调用");
        return Result.success(response);
    }

    /**
     * 漏桶限流演示
     * 恒定输出速率，桶容量15，每秒漏出3个请求
     */
    @GetMapping("/leaky-bucket")
    @Operation(summary = "漏桶限流演示", description = "桶容量15，每秒漏出3个请求，恒定输出速率")
    @RateLimit(
        key = "leaky_bucket_demo",
        limit = 15,
        period = 1,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.LEAKY_BUCKET,
        type = RateLimitType.GLOBAL,
        leakyBucketCapacity = 15,
        leakRate = 3.0,
        message = "漏桶限流：桶容量15，每秒漏出3个请求"
    )
    public Result<Map<String, Object>> leakyBucketDemo() {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "漏桶");
        response.put("bucketCapacity", 15);
        response.put("leakRate", "3请求/秒");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过漏桶限流检查");
        
        log.info("漏桶限流演示接口被调用");
        return Result.success(response);
    }

    /**
     * 固定窗口限流演示
     * 每30秒最多8次请求
     */
    @GetMapping("/fixed-window")
    @Operation(summary = "固定窗口限流演示", description = "每30秒最多8次请求")
    @RateLimit(
        key = "fixed_window_demo",
        limit = 8,
        period = 30,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.FIXED_WINDOW,
        type = RateLimitType.GLOBAL,
        message = "固定窗口限流：每30秒最多8次请求"
    )
    public Result<Map<String, Object>> fixedWindowDemo() {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "固定窗口");
        response.put("limit", "8次/30秒");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过固定窗口限流检查");
        
        log.info("固定窗口限流演示接口被调用");
        return Result.success(response);
    }

    /**
     * IP限流演示
     * 每个IP每分钟最多5次请求
     */
    @GetMapping("/ip-limit")
    @Operation(summary = "IP限流演示", description = "每个IP每分钟最多5次请求")
    @RateLimit(
        key = "ip_limit_demo",
        limit = 5,
        period = 60,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.SLIDING_WINDOW,
        type = RateLimitType.IP,
        message = "IP限流：每个IP每分钟最多5次请求"
    )
    public Result<Map<String, Object>> ipLimitDemo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "IP限流");
        response.put("limit", "5次/分钟");
        response.put("clientIp", getClientIp(request));
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过IP限流检查");
        
        log.info("IP限流演示接口被调用，客户端IP: {}", getClientIp(request));
        return Result.success(response);
    }

    /**
     * 用户限流演示（需要认证）
     * 每个用户每分钟最多20次请求
     */
    @GetMapping("/user-limit")
    @Operation(summary = "用户限流演示", description = "每个用户每分钟最多20次请求（需要认证）")
    @RateLimit(
        key = "user_limit_demo",
        limit = 20,
        period = 60,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.TOKEN_BUCKET,
        type = RateLimitType.USER,
        bucketCapacity = 20,
        refillRate = 0.33,  // 每3秒1个令牌
        message = "用户限流：每个用户每分钟最多20次请求"
    )
    public Result<Map<String, Object>> userLimitDemo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "用户限流");
        response.put("limit", "20次/分钟");
        response.put("userId", getUserId(request));
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过用户限流检查");
        
        log.info("用户限流演示接口被调用，用户ID: {}", getUserId(request));
        return Result.success(response);
    }

    /**
     * 热点数据限流演示
     * 热点参数检测和限流
     */
    @GetMapping("/hotspot-limit")
    @Operation(summary = "热点数据限流演示", description = "热点参数检测和限流")
    @RateLimit(
        key = "hotspot_limit_demo",
        limit = 100,  // 普通限流阈值
        period = 60,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.HOTSPOT,
        type = RateLimitType.PARAMETER,
        message = "热点数据限流：热点参数被特殊限流处理"
    )
    public Result<Map<String, Object>> hotspotLimitDemo(
            @Parameter(description = "测试参数，用于热点检测") 
            @RequestParam(defaultValue = "normal") String param) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "热点数据限流");
        response.put("param", param);
        response.put("limit", "普通100次/分钟，热点10次/分钟");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过热点数据限流检查");
        
        log.info("热点数据限流演示接口被调用，参数: {}", param);
        return Result.success(response);
    }

    /**
     * 条件限流演示
     * 只对POST请求进行限流
     */
    @PostMapping("/conditional-limit")
    @Operation(summary = "条件限流演示", description = "只对POST请求进行限流")
    @RateLimit(
        key = "conditional_limit_demo",
        limit = 3,
        period = 30,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.FIXED_WINDOW,
        type = RateLimitType.GLOBAL,
        condition = "#request.getMethod() == 'POST'",
        message = "条件限流：POST请求每30秒最多3次"
    )
    public Result<Map<String, Object>> conditionalLimitDemo(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "条件限流");
        response.put("condition", "仅限制POST请求");
        response.put("limit", "3次/30秒");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "POST请求成功通过条件限流检查");
        
        log.info("条件限流演示接口被调用，数据: {}", data);
        return Result.success(response);
    }

    /**
     * 自定义key限流演示
     * 使用SpEL表达式生成动态key
     */
    @GetMapping("/dynamic-key/{category}")
    @Operation(summary = "动态key限流演示", description = "使用SpEL表达式生成动态key")
    @RateLimit(
        key = "'category_' + #category + '_' + #ip",
        limit = 5,
        period = 60,
        timeUnit = TimeUnit.SECONDS,
        strategy = RateLimitStrategy.SLIDING_WINDOW,
        type = RateLimitType.CUSTOM,
        message = "动态key限流：每个分类每个IP每分钟最多5次"
    )
    public Result<Map<String, Object>> dynamicKeyDemo(
            @Parameter(description = "分类参数")
            @PathVariable String category,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("strategy", "动态key限流");
        response.put("category", category);
        response.put("clientIp", getClientIp(request));
        response.put("key", "category_" + category + "_" + getClientIp(request));
        response.put("limit", "5次/分钟");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "请求成功通过动态key限流检查");
        
        log.info("动态key限流演示接口被调用，分类: {}, IP: {}", category, getClientIp(request));
        return Result.success(response);
    }

    /**
     * 获取限流统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取限流统计信息", description = "查看限流监控统计数据")
    public Result<Map<String, Object>> getStatistics(
            @Parameter(description = "统计类型", example = "hourly")
            @RequestParam(defaultValue = "hourly") String type,
            @Parameter(description = "时间字符串", example = "2025-09-01-14")
            @RequestParam(required = false) String time) {
        
        if (time == null) {
            if ("hourly".equals(type)) {
                time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
            } else {
                time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        }
        
        RateLimitStatistics statistics = rateLimitMonitor.getStatistics(type, time);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("time", time);
        response.put("statistics", statistics);
        response.put("timestamp", LocalDateTime.now());
        
        return Result.success(response);
    }

    /**
     * 获取热点数据统计
     */
    @GetMapping("/hotspot-statistics")
    @Operation(summary = "获取热点数据统计", description = "查看热点数据统计信息")
    public Result<Map<String, Object>> getHotspotStatistics(
            @Parameter(description = "日期", example = "2025-09-01")
            @RequestParam(required = false) String date,
            @Parameter(description = "返回前N个热点", example = "10")
            @RequestParam(defaultValue = "10") int topN) {
        
        if (date == null) {
            date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        Map<String, Object> hotspotStats = rateLimitMonitor.getHotspotStatistics(date, topN);
        
        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("topN", topN);
        response.put("hotspotStatistics", hotspotStats);
        response.put("timestamp", LocalDateTime.now());
        
        return Result.success(response);
    }

    /**
     * 获取告警信息
     */
    @GetMapping("/alerts")
    @Operation(summary = "获取告警信息", description = "查看限流告警数据")
    public Result<Map<String, Object>> getAlerts(
            @Parameter(description = "告警类型", example = "blocked")
            @RequestParam(defaultValue = "blocked") String alertType,
            @Parameter(description = "时间字符串", example = "2025-09-01-14")
            @RequestParam(required = false) String time) {
        
        if (time == null) {
            time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        }
        
        Map<String, Object> alerts = rateLimitMonitor.getAlerts(alertType, time);
        
        Map<String, Object> response = new HashMap<>();
        response.put("alertType", alertType);
        response.put("time", time);
        response.put("alerts", alerts);
        response.put("timestamp", LocalDateTime.now());
        
        return Result.success(response);
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
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * 获取用户ID（模拟）
     */
    private String getUserId(HttpServletRequest request) {
        // 在实际项目中，应该从JWT token或session中获取用户ID
        Object userId = request.getAttribute("userId");
        return userId != null ? userId.toString() : "demo_user_" + getClientIp(request).replace(".", "_");
    }
}